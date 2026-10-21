package com.mss301.order_service.dto;

public record PaymentRequest(Long orderId, String customerId, double amount) {
}
