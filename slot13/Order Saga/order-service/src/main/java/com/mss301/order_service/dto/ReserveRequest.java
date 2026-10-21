package com.mss301.order_service.dto;

public record ReserveRequest(Long orderId, String productId, int quantity) {
}
