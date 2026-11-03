package fu.de190423.de190423_roomservice.repository;


import fu.de190423.de190423_roomservice.entity.Room;
import fu.de190423.de190423_roomservice.entity.RoomStatus;
import fu.de190423.de190423_roomservice.entity.RoomType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoomRepository extends JpaRepository<Room, Long> {
    boolean existsByRoomNumber(String roomNumber);
    boolean existsByRoomNumberAndRoomIdNot(String roomNumber, Long roomId);

    // Null-safe filter: when a param is null it is ignored
    @Query("""
            SELECT u FROM Room u
            WHERE (:roomType IS NULL OR u.roomType = :roomType)
              AND (:status IS NULL OR u.status = :status)
            """)
    Page<Room> search(@Param("roomType") RoomType roomType,
                      @Param("status") RoomStatus status,
                      Pageable pageable);
}
