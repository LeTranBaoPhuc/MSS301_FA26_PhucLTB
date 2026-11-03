package fu.de190423.de190423_reservationservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import fu.de190423.de190423_reservationservice.client.RoomClient;
import fu.de190423.de190423_reservationservice.dto.*;
import fu.de190423.de190423_reservationservice.entity.Reservation;
import fu.de190423.de190423_reservationservice.entity.ReservationStatus;
import fu.de190423.de190423_reservationservice.repository.ReservationRepository;
import fu.de190423.de190423_reservationservice.service.ReservationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final RoomClient roomClient;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    public ReservationServiceImpl(ReservationRepository reservationRepository,
                                  RoomClient roomClient) {
        this.reservationRepository = reservationRepository;
        this.roomClient = roomClient;
    }

    @Override
    @Transactional
    public ApiResponse createReservation(ReservationDTO dto) {
        // 1. Validate dates
        if (dto.getCheckInDate() == null || dto.getCheckOutDate() == null) {
            return ApiResponse.error(2, "checkInDate and checkOutDate are required");
        }
        if (!dto.getCheckOutDate().isAfter(dto.getCheckInDate())) {
            return ApiResponse.error(2, "checkOutDate must be after checkInDate");
        }

        // 2. Fetch Room from RoomService via OpenFeign
        RoomDTO room = fetchRoom(dto.getRoomId());
        if (room == null) {
            return ApiResponse.error(4, "Room ID is not found");
        }

        // 3. Room status must be AVAILABLE
        if (!"AVAILABLE".equalsIgnoreCase(room.getStatus())) {
            return ApiResponse.error(5, "Room is not AVAILABLE for reservation");
        }

        // 4. Number of guests must not exceed room capacity
        if (dto.getNumberOfGuests() != null && room.getCapacity() != null
                && dto.getNumberOfGuests() > room.getCapacity()) {
            return ApiResponse.error(2, "Number of guests exceeds room capacity");
        }

        // 5. Compute totalAmount = pricePerNight * nights
        long nights = ChronoUnit.DAYS.between(dto.getCheckInDate(), dto.getCheckOutDate());
        if (nights < 1) nights = 1;
        BigDecimal totalAmount = room.getPricePerNight().multiply(BigDecimal.valueOf(nights));

        // 6. Build and save Reservation entity
        Reservation reservation = new Reservation();
        reservation.setGuestName(dto.getGuestName());
        reservation.setGuestEmail(dto.getGuestEmail());
        reservation.setGuestPhone(dto.getGuestPhone());
        reservation.setCheckInDate(dto.getCheckInDate());
        reservation.setCheckOutDate(dto.getCheckOutDate());
        reservation.setNumberOfGuests(dto.getNumberOfGuests());
        reservation.setTotalAmount(totalAmount);
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setRoomId(dto.getRoomId());

        Reservation saved = reservationRepository.save(reservation);
        return ApiResponse.ok("Reservation created successfully", toDTO(saved));
    }

    @Override
    @Transactional
    public ApiResponse updateReservation(Long reservationId, ReservationDTO dto) {
        Reservation reservation = reservationRepository.findById(reservationId).orElse(null);
        if (reservation == null) {
            return ApiResponse.error(4, "Reservation ID is not found");
        }

        // Partial updates
        if (dto.getGuestName() != null) reservation.setGuestName(dto.getGuestName());
        if (dto.getGuestEmail() != null) reservation.setGuestEmail(dto.getGuestEmail());
        if (dto.getGuestPhone() != null) reservation.setGuestPhone(dto.getGuestPhone());
        if (dto.getStatus() != null) {
            try {
                reservation.setStatus(ReservationStatus.valueOf(dto.getStatus()));
            } catch (IllegalArgumentException ignored) {}
        }

        // Date update and recalculation
        LocalDate checkIn = dto.getCheckInDate() != null ? dto.getCheckInDate() : reservation.getCheckInDate();
        LocalDate checkOut = dto.getCheckOutDate() != null ? dto.getCheckOutDate() : reservation.getCheckOutDate();
        if (dto.getCheckInDate() != null || dto.getCheckOutDate() != null) {
            if (!checkOut.isAfter(checkIn)) {
                return ApiResponse.error(2, "checkOutDate must be after checkInDate");
            }
            reservation.setCheckInDate(checkIn);
            reservation.setCheckOutDate(checkOut);

            RoomDTO room = fetchRoom(reservation.getRoomId());
            if (room != null && room.getPricePerNight() != null) {
                long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
                if (nights < 1) nights = 1;
                reservation.setTotalAmount(room.getPricePerNight().multiply(BigDecimal.valueOf(nights)));
            }
        }

        if (dto.getNumberOfGuests() != null) {
            RoomDTO room = fetchRoom(reservation.getRoomId());
            if (room != null && room.getCapacity() != null && dto.getNumberOfGuests() > room.getCapacity()) {
                return ApiResponse.error(2, "Number of guests exceeds room capacity");
            }
            reservation.setNumberOfGuests(dto.getNumberOfGuests());
        }

        Reservation saved = reservationRepository.save(reservation);
        return ApiResponse.ok("Reservation updated successfully", toDTO(saved));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse getReservationById(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId).orElse(null);
        if (reservation == null) {
            return ApiResponse.error(4, "Reservation is not found");
        }

        RoomDTO room = fetchRoom(reservation.getRoomId());
        return ApiResponse.ok("Successful", toDetailDTO(reservation, room));
    }

    @Override
    @Transactional
    public ApiResponse cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId).orElse(null);
        if (reservation == null) {
            return ApiResponse.error(4, "Reservation is not found");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
        return ApiResponse.ok("Reservation cancelled successfully", null);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse getReservationList(String guestName, String status, Pageable pageable) {
        ReservationStatus resStatus = parseStatus(status);
        Page<Reservation> page = reservationRepository.search(guestName, resStatus, pageable);

        List<ReservationDetailDTO> content = page.getContent().stream()
                .map(r -> toDetailDTO(r, fetchRoom(r.getRoomId())))
                .toList();

        PageDTO pageDTO = new PageDTO(
                page.getSize(), page.getNumber(), page.getTotalPages(),
                page.getTotalElements(), page.isFirst(), page.isLast(), content);

        return ApiResponse.ok("Successful", pageDTO);
    }

    // -- helpers --
    private RoomDTO fetchRoom(Long roomId) {
        if (roomId == null) return null;
        try {
            ApiResponse resp = roomClient.getRoomById(roomId);
            if (resp == null || resp.getStatus() != 1 || resp.getData() == null) {
                return null;
            }
            return objectMapper.convertValue(resp.getData(), RoomDTO.class);
        } catch (FeignException e) {
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private ReservationDTO toDTO(Reservation r) {
        ReservationDTO d = new ReservationDTO();
        d.setReservationId(r.getReservationId());
        d.setGuestName(r.getGuestName());
        d.setGuestEmail(r.getGuestEmail());
        d.setGuestPhone(r.getGuestPhone());
        d.setCheckInDate(r.getCheckInDate());
        d.setCheckOutDate(r.getCheckOutDate());
        d.setNumberOfGuests(r.getNumberOfGuests());
        d.setTotalAmount(r.getTotalAmount());
        d.setStatus(r.getStatus() != null ? r.getStatus().name() : null);
        d.setRoomId(r.getRoomId());
        return d;
    }

    private ReservationDetailDTO toDetailDTO(Reservation r, RoomDTO room) {
        ReservationDetailDTO d = new ReservationDetailDTO();
        d.setReservationId(r.getReservationId());
        d.setGuestName(r.getGuestName());
        d.setGuestEmail(r.getGuestEmail());
        d.setGuestPhone(r.getGuestPhone());
        d.setCheckInDate(r.getCheckInDate());
        d.setCheckOutDate(r.getCheckOutDate());
        d.setNumberOfGuests(r.getNumberOfGuests());
        d.setTotalAmount(r.getTotalAmount());
        d.setStatus(r.getStatus() != null ? r.getStatus().name() : null);
        d.setRoom(room);
        return d;
    }

    private ReservationStatus parseStatus(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return ReservationStatus.valueOf(s.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
