package com.mss301.order_service.dto;

public record CreateOrderRequest(String customerId, String productId, int quantity, double amount) {
}
