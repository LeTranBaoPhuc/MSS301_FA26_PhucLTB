package fu.de190423.de190423_reservationservice.client;

import fu.de190423.de190423_reservationservice.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "room-service", url = "${room.service.url:http://localhost:8081}")
public interface RoomClient {

    @GetMapping("/api/rooms/{roomId}")
    ApiResponse getRoomById(@PathVariable("roomId") Long roomId);
}

