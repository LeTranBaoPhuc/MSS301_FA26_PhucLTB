package com.fudn.booking_service.controller;

import com.fudn.booking_service.dto.BookingResponse;
import com.fudn.booking_service.dto.CreateBookingRequest;
import com.fudn.booking_service.dto.ReportResponse;
import com.fudn.booking_service.dto.SeatMapResponse;
import com.fudn.booking_service.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private static final String USER_ID = "X-User-Id";
    private static final String USER_ROLE = "X-User-Role";

    private final BookingService bookingService;

    // Public
    // TODO 7.7
    @GetMapping("/showtimes/{showtimeId}/seats")
    public SeatMapResponse getSeatMap(@PathVariable String showtimeId) {
        return bookingService.getSeatMap(showtimeId);
    }

    // CUSTOMER
    // TODO 7.7
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse create(@RequestHeader(USER_ID) Long userId,
                                  @Valid @RequestBody CreateBookingRequest request) {
        return bookingService.create(userId, request);
    }

    // ======================= F8 =======================

    // CUSTOMER
    @GetMapping("/my")
    public List<BookingResponse> getMyBookings(@RequestHeader(USER_ID) Long userId) {
        return bookingService.getMyBookings(userId);
    }

    // ADMIN
    @GetMapping
    public List<BookingResponse> getAll() {
        return bookingService.getAll();
    }

    // TODO 8.4
    @GetMapping("/{bookingId}")
    public BookingResponse getById(@PathVariable Long bookingId,
                                   @RequestHeader(USER_ID) Long userId,
                                   @RequestHeader(USER_ROLE) String role) {
        return bookingService.getById(bookingId, userId, role);
    }

    // TODO 8.4
    @PostMapping("/{bookingId}/cancel")
    public BookingResponse cancel(@PathVariable Long bookingId,
                                  @RequestHeader(USER_ID) Long userId,
                                  @RequestHeader(USER_ROLE) String role) {
        return bookingService.cancel(bookingId, userId, role);
    }

    // ======================= F9 =======================

    // TODO 9.3
    @GetMapping("/report")
    public ReportResponse report(@RequestParam LocalDate startDate,
                                 @RequestParam LocalDate endDate) {
        return bookingService.report(startDate, endDate);
    }
}
