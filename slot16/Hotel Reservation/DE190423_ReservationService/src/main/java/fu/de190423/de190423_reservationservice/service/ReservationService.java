package fu.de190423.de190423_reservationservice.service;

import fu.de190423.de190423_reservationservice.dto.ApiResponse;
import fu.de190423.de190423_reservationservice.dto.ReservationDTO;
import org.springframework.data.domain.Pageable;

public interface ReservationService {
    ApiResponse createReservation(ReservationDTO dto);
    ApiResponse updateReservation(Long reservationId, ReservationDTO dto);
    ApiResponse getReservationById(Long reservationId);
    ApiResponse cancelReservation(Long reservationId);
    ApiResponse getReservationList(String guestName, String status, Pageable pageable);
}

