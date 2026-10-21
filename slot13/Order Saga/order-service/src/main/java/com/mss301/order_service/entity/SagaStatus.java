package com.mss301.order_service.entity;

// Đúng 6 trạng thái ở Slide 27
public enum SagaStatus {
    PENDING,
    EXECUTING,
    COMPLETED,
    FAILED,
    COMPENSATING,
    COMPENSATED
}
