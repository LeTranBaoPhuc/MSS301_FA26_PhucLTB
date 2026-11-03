package fu.de190423.de190423_roomservice.controller;

import fu.de190423.de190423_roomservice.dto.ApiResponse;
import fu.de190423.de190423_roomservice.dto.RoomDTO;
import fu.de190423.de190423_roomservice.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rooms")
public class DE190423RoomController {

    private final RoomService roomService;

    public DE190423RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    // 1. Create Room -> 201/1, 406/2, 226/3, 500/0
    @PostMapping
    public ResponseEntity<ApiResponse> create(@Valid @RequestBody RoomDTO roomDTO, BindingResult br) {
        if (br.hasErrors()) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE) // 406
                    .body(ApiResponse.error(2, firstError(br)));
        }
        return map(roomService.createRoom(roomDTO), HttpStatus.CREATED); // 201 on success
    }

    // 2. Update Room -> 200/1, 406/2, 226/3, 404/4, 500/0
    @PutMapping("/{roomId}")
    public ResponseEntity<ApiResponse> update(@PathVariable Long roomId, @Valid @RequestBody RoomDTO roomDTO, BindingResult br) {
        if (br.hasErrors()) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE) // 406
                    .body(ApiResponse.error(2, firstError(br)));
        }
        return map(roomService.updateRoom(roomId, roomDTO), HttpStatus.OK); // 200 on success
    }

    // 3. Get Room detail -> 200/1, 404/4, 500/0
    @GetMapping("/{roomId}")
    public ResponseEntity<ApiResponse> getDetail(@PathVariable Long roomId) {
        return map(roomService.getRoomById(roomId), HttpStatus.OK);
    }

    // 4. Set to MAINTENANCE (delete) -> 200/1, 404/4, 500/0
    @DeleteMapping("/{roomId}")
    public ResponseEntity<ApiResponse> deactivate(@PathVariable Long roomId) {
        return map(roomService.deactivateRoom(roomId), HttpStatus.OK);
    }

    // 5. Get Room List -> 200/1, 406/2, 500/0
    @GetMapping
    public ResponseEntity<ApiResponse> list(@RequestParam(required = false) String roomType,
                                            @RequestParam(required = false) String status,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        if (size < 1 || size > 100) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE) // 406
                    .body(ApiResponse.error(2, "size must be between 1 and 100"));
        }
        Pageable pageable = PageRequest.of(page, size);
        return map(roomService.getRoomList(roomType, status, pageable), HttpStatus.OK);
    }

    // -- helpers --
    private String firstError(BindingResult br) {
        return br.getAllErrors().isEmpty() ? "Validation failed" : br.getAllErrors().get(0).getDefaultMessage();
    }

    // map service status -> HTTP code
    private ResponseEntity<ApiResponse> map(ApiResponse apiResponse, HttpStatus okStatus) {
        int status = apiResponse.getStatus();
        HttpStatus httpStatus;
        switch (status) {
            case 1: // success
                httpStatus = okStatus;
                break;
            case 2: // validation error
                httpStatus = HttpStatus.NOT_ACCEPTABLE; // 406
                break;
            case 3: // duplicated room number
                httpStatus = HttpStatus.IM_USED; // 226
                break;
            case 4: // room not found
                httpStatus = HttpStatus.NOT_FOUND; // 404
                break;
            default: // unknown error
                httpStatus = HttpStatus.INTERNAL_SERVER_ERROR; // 500
                break;
        }
        return ResponseEntity.status(httpStatus).body(apiResponse);
    }
}
