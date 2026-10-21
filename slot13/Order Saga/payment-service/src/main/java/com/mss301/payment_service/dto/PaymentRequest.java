package com.mss301.payment_service.dto;

public record PaymentRequest(Long orderId, String customerId, double amount) {
}
