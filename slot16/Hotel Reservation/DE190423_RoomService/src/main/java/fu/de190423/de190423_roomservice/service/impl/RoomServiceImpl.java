package fu.de190423.de190423_roomservice.service.impl;

import fu.de190423.de190423_roomservice.dto.ApiResponse;
import fu.de190423.de190423_roomservice.dto.PageDTO;
import fu.de190423.de190423_roomservice.dto.RoomDTO;
import fu.de190423.de190423_roomservice.entity.RoomStatus;
import fu.de190423.de190423_roomservice.entity.RoomType;
import fu.de190423.de190423_roomservice.service.RoomService;
import fu.de190423.de190423_roomservice.entity.Room;
import fu.de190423.de190423_roomservice.repository.RoomRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RoomServiceImpl implements RoomService{

    private final RoomRepository roomRepository;

    public RoomServiceImpl(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Override
    @Transactional
    public ApiResponse createRoom(RoomDTO dto) {

        if (roomRepository.existsByRoomNumber((dto.getRoomNumber()))) {
            return ApiResponse.error(3, "Room number is duplicated");
        }

        Room room = new Room();
        room.setRoomNumber(dto.getRoomNumber());
        room.setRoomType(RoomType.valueOf(dto.getRoomType()));
        room.setPricePerNight(dto.getPricePerNight());
        room.setCapacity(dto.getCapacity());
        room.setFloor(dto.getFloor());


        // Default status is ACTIVE when not provided
        room.setStatus(dto.getStatus() == null ? RoomStatus.AVAILABLE : RoomStatus.valueOf(dto.getStatus()));

        Room saved = roomRepository.save(room);
        return ApiResponse.ok("Room created successfully", toDTO(saved));
    }

    @Override
    @Transactional
    public ApiResponse updateRoom(Long roomId, RoomDTO dto) {
        Room room = roomRepository.findById(roomId).orElse(null);
        if (room == null) {
            return ApiResponse.error(4, "Room is not found");
        }

        if (dto.getRoomNumber() != null
                && roomRepository.existsByRoomNumberAndRoomIdNot(dto.getRoomNumber(), roomId)) {
            return ApiResponse.error(3, "Room number is duplicated");
        }

        // Partial update: only non-null fields
        if (dto.getRoomNumber() != null)      room.setRoomNumber(dto.getRoomNumber());
        if (dto.getRoomType() != null)          room.setRoomType(RoomType.valueOf(dto.getRoomType()));
        if (dto.getPricePerNight() != null)   room.setPricePerNight(dto.getPricePerNight());
        if (dto.getCapacity() != null) room.setCapacity(dto.getCapacity());
        if (dto.getFloor() != null)         room.setFloor(dto.getFloor());
        if (dto.getStatus() != null)        room.setStatus(RoomStatus.valueOf(dto.getStatus()));

        Room saved = roomRepository.save(room);
        return ApiResponse.ok("Room updated successfully", toDTO(saved));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse getRoomById(Long roomId) {
        Room room = roomRepository.findById(roomId).orElse(null);
        if (room == null) {
            return ApiResponse.error(4, "Room is not found");
        }
        return ApiResponse.ok("Successful", toDTO(room));
    }

    @Override
    @Transactional
    public ApiResponse deactivateRoom(Long roomId) {
        Room room = roomRepository.findById(roomId).orElse(null);
        if (room == null) {
            return ApiResponse.error(4, "Room is not found");
        }

        room.setStatus(RoomStatus.MAINTENANCE);
        roomRepository.save(room);
        return ApiResponse.ok("Room is set to MAINTENANCE successfully", null);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse getRoomList(String roomType, String status, Pageable pageable) {
        Page<Room> page = roomRepository.search(parseRoomType(roomType), parseStatus(status), pageable);
        List<RoomDTO> content = page.getContent().stream().map(this::toDTO).toList();
        PageDTO pageDTO = new PageDTO(
                page.getSize(), page.getNumber(), page.getTotalPages(),
                page.getTotalElements(), page.isFirst(), page.isLast(), content);
        return ApiResponse.ok("Successful", pageDTO);
    }

    // -- helpers --
    private RoomDTO toDTO(Room u) {
        RoomDTO d = new RoomDTO();
        d.setRoomId(u.getRoomId());
        d.setRoomNumber(u.getRoomNumber());
        d.setRoomType(u.getRoomType().name());
        d.setPricePerNight(u.getPricePerNight());
        d.setCapacity(u.getCapacity());
        d.setFloor(u.getFloor());
        d.setStatus(u.getStatus().name());
        return d;
    }

    // Parse filter string -> enum; tra null neu rong/khong hop le (bo qua filter)
    private RoomType parseRoomType(String s) {
        if (s == null || s.isBlank()) return null;
        try { return RoomType.valueOf(s); } catch (IllegalArgumentException e) { return null; }
    }

    private RoomStatus parseStatus(String s) {
        if (s == null || s.isBlank()) return null;
        try { return RoomStatus.valueOf(s); } catch (IllegalArgumentException e) { return null; }
    }
}
