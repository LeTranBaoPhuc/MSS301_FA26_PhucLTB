package com.fudn.customer_service.controller;

import com.fudn.customer_service.dto.ChangePasswordRequest;
import com.fudn.customer_service.dto.CustomerResponse;
import com.fudn.customer_service.dto.ProfileUpdateRequest;
import com.fudn.customer_service.dto.RegisterRequest;
import com.fudn.customer_service.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    // TODO 2.6
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse register(@Valid @RequestBody RegisterRequest request) {
        return customerService.register(request);
    }

    @GetMapping("/me")
    public CustomerResponse getProfile(@RequestHeader("X-User-Id") Long userId) {
        return customerService.getProfile(userId);
    }

    @PutMapping("/me")
    public CustomerResponse updateProfile(@RequestHeader("X-User-Id") Long userId,
                                          @Valid @RequestBody ProfileUpdateRequest request) {
        return customerService.updateProfile(userId, request);
    }

    @PutMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@RequestHeader("X-User-Id") Long userId,
                               @Valid @RequestBody ChangePasswordRequest request) {
        customerService.changePassword(userId, request);
    }
}
