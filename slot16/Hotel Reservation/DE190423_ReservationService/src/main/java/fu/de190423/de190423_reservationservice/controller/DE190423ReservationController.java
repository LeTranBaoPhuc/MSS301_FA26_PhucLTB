package fu.de190423.de190423_reservationservice.controller;

import fu.de190423.de190423_reservationservice.dto.ApiResponse;
import fu.de190423.de190423_reservationservice.dto.ReservationDTO;
import fu.de190423.de190423_reservationservice.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations")
public class DE190423ReservationController {

    private final ReservationService reservationService;

    public DE190423ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    // 1. Create Reservation -> 201/1, 406/2, 404/4, 400/5, 500/0
    @PostMapping
    public ResponseEntity<ApiResponse> create(@Valid @RequestBody ReservationDTO dto, BindingResult br) {
        if (br.hasErrors()) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                    .body(ApiResponse.error(2, firstError(br)));
        }
        return map(reservationService.createReservation(dto), HttpStatus.CREATED);
    }

    // 2. Update Reservation -> 200/1, 406/2, 404/4, 500/0
    @PutMapping("/{reservationId}")
    public ResponseEntity<ApiResponse> update(@PathVariable Long reservationId,
                                              @RequestBody ReservationDTO dto) {
        return map(reservationService.updateReservation(reservationId, dto), HttpStatus.OK);
    }

    // 3. Get Detail -> 200/1, 404/4, 500/0
    @GetMapping("/{reservationId}")
    public ResponseEntity<ApiResponse> getDetail(@PathVariable Long reservationId) {
        return map(reservationService.getReservationById(reservationId), HttpStatus.OK);
    }

    // 4. Cancel Reservation -> 200/1, 404/4, 500/0
    @DeleteMapping("/{reservationId}")
    public ResponseEntity<ApiResponse> cancel(@PathVariable Long reservationId) {
        return map(reservationService.cancelReservation(reservationId), HttpStatus.OK);
    }

    // 5. Get Reservation List -> 200/1, 406/2, 500/0
    @GetMapping
    public ResponseEntity<ApiResponse> list(@RequestParam(required = false) String guestName,
                                            @RequestParam(required = false) String status,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        if (size < 1 || size > 100) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                    .body(ApiResponse.error(2, "size must be between 1 and 100"));
        }
        Pageable pageable = PageRequest.of(page, size);
        return map(reservationService.getReservationList(guestName, status, pageable), HttpStatus.OK);
    }

    // -- helpers --
    private String firstError(BindingResult br) {
        return br.getAllErrors().isEmpty() ? "Data validation failed" : br.getAllErrors().get(0).getDefaultMessage();
    }

    private ResponseEntity<ApiResponse> map(ApiResponse apiResponse, HttpStatus okStatus) {
        int status = apiResponse.getStatus();
        HttpStatus httpStatus;
        switch (status) {
            case 1:
                httpStatus = okStatus;
                break;
            case 2:
                httpStatus = HttpStatus.NOT_ACCEPTABLE; // 406
                break;
            case 4:
                httpStatus = HttpStatus.NOT_FOUND; // 404
                break;
            case 5:
                httpStatus = HttpStatus.BAD_REQUEST; // 400
                break;
            default:
                httpStatus = HttpStatus.INTERNAL_SERVER_ERROR; // 500
                break;
        }
        return ResponseEntity.status(httpStatus).body(apiResponse);
    }
}

