package com.mss301.order_service.service;

import com.mss301.order_service.dto.CreateOrderRequest;
import com.mss301.order_service.entity.*;
import com.mss301.order_service.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private SagaLogRepository sagaLogRepository;

    @InjectMocks
    private OrderService orderService;

    private Order testOrder;

    @BeforeEach
    void setUp() {
        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setCustomerId("C1");
        testOrder.setProductId("P1");
        testOrder.setQuantity(2);
        testOrder.setAmount(2000.0);
        testOrder.setStatus(OrderStatus.PENDING);
        testOrder.setSagaStatus(SagaStatus.PENDING);
    }

    @Test
    void testCreatePendingOrder() {
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateOrderRequest req = new CreateOrderRequest("C1", "P1", 2, 2000.0);
        Order created = orderService.createPendingOrder(req);

        assertNotNull(created);
        assertEquals("C1", created.getCustomerId());
        assertEquals(OrderStatus.PENDING, created.getStatus());
        assertEquals(SagaStatus.PENDING, created.getSagaStatus());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void testUpdateSagaStatus() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        orderService.updateSagaStatus(1L, SagaStatus.EXECUTING);
        assertEquals(SagaStatus.EXECUTING, testOrder.getSagaStatus());
    }

    @Test
    void testMarkFailed() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        orderService.markFailed(1L, "Payment failed");
        assertEquals(SagaStatus.FAILED, testOrder.getSagaStatus());
        assertEquals("Payment failed", testOrder.getFailureReason());
    }

    @Test
    void testConfirmOrder() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        orderService.confirmOrder(1L);
        assertEquals(OrderStatus.CONFIRMED, testOrder.getStatus());
        assertEquals(SagaStatus.COMPLETED, testOrder.getSagaStatus());
    }

    @Test
    void testCancelOrder() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        orderService.cancelOrder(1L);
        assertEquals(OrderStatus.CANCELLED, testOrder.getStatus());
        assertEquals(SagaStatus.COMPENSATED, testOrder.getSagaStatus());
    }

    @Test
    void testLog() {
        when(sagaLogRepository.save(any(SagaLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        orderService.log(1L, "RESERVE", "SUCCESS", "Reserved 2 items");
        verify(sagaLogRepository).save(any(SagaLog.class));
    }
}
