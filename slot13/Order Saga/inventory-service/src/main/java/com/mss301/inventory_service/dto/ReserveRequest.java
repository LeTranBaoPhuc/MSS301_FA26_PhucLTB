package com.mss301.inventory_service.dto;

public record ReserveRequest(Long orderId, String productId, int quantity) {
}
