package com.mss301.order_service.saga;

import com.mss301.order_service.client.InventoryClient;
import com.mss301.order_service.client.PaymentClient;
import com.mss301.order_service.dto.CreateOrderRequest;
import com.mss301.order_service.entity.*;
import com.mss301.order_service.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderSagaOrchestratorTest {

    @Mock
    private OrderService orderService;

    @Mock
    private InventoryClient inventoryClient;

    @Mock
    private PaymentClient paymentClient;

    @InjectMocks
    private OrderSagaOrchestrator orchestrator;

    private Order testOrder;
    private CreateOrderRequest request;

    @BeforeEach
    void setUp() {
        testOrder = new Order();
        testOrder.setId(100L);
        testOrder.setCustomerId("C1");
        testOrder.setProductId("P1");
        testOrder.setQuantity(2);
        testOrder.setAmount(2000.0);
        testOrder.setStatus(OrderStatus.PENDING);
        testOrder.setSagaStatus(SagaStatus.PENDING);

        request = new CreateOrderRequest("C1", "P1", 2, 2000.0);
    }

    @Test
    void testHappyPath() {
        when(orderService.createPendingOrder(request)).thenReturn(testOrder);
        when(orderService.findById(100L)).thenAnswer(inv -> {
            testOrder.setStatus(OrderStatus.CONFIRMED);
            testOrder.setSagaStatus(SagaStatus.COMPLETED);
            return testOrder;
        });

        Order result = orchestrator.placeOrder(request);

        assertNotNull(result);
        assertEquals(OrderStatus.CONFIRMED, result.getStatus());
        assertEquals(SagaStatus.COMPLETED, result.getSagaStatus());

        verify(orderService).createPendingOrder(request);
        verify(orderService).updateSagaStatus(100L, SagaStatus.EXECUTING);
        verify(inventoryClient).reserve(100L, "P1", 2);
        verify(paymentClient).pay(100L, "C1", 2000.0);
        verify(orderService).confirmOrder(100L);

        // Compensation should never be called in happy path
        verify(inventoryClient, never()).release(anyLong());
        verify(paymentClient, never()).refund(anyLong());
        verify(orderService, never()).cancelOrder(anyLong());
    }

    @Test
    void testFailureAtInventoryReserve() {
        when(orderService.createPendingOrder(request)).thenReturn(testOrder);
        doThrow(new HttpClientErrorException(HttpStatus.CONFLICT, "Hết hàng"))
                .when(inventoryClient).reserve(100L, "P1", 2);

        when(orderService.findById(100L)).thenAnswer(inv -> {
            testOrder.setStatus(OrderStatus.CANCELLED);
            testOrder.setSagaStatus(SagaStatus.COMPENSATED);
            return testOrder;
        });

        Order result = orchestrator.placeOrder(request);

        assertNotNull(result);
        assertEquals(OrderStatus.CANCELLED, result.getStatus());
        assertEquals(SagaStatus.COMPENSATED, result.getSagaStatus());

        verify(orderService).markFailed(eq(100L), anyString());
        verify(orderService).updateSagaStatus(100L, SagaStatus.COMPENSATING);
        verify(orderService).cancelOrder(100L);

        // Inventory release and Payment refund shouldn't be called because neither succeeded
        verify(inventoryClient, never()).release(anyLong());
        verify(paymentClient, never()).pay(anyLong(), anyString(), anyDouble());
        verify(paymentClient, never()).refund(anyLong());
    }

    @Test
    void testFailureAtPaymentProcess() {
        when(orderService.createPendingOrder(request)).thenReturn(testOrder);
        // Reserve succeeds
        doNothing().when(inventoryClient).reserve(100L, "P1", 2);
        // Payment fails (e.g. 402 PAYMENT_REQUIRED)
        String jsonError = "{\"message\":\"Số dư không đủ\"}";
        HttpClientErrorException payError = HttpClientErrorException.create(
                HttpStatus.PAYMENT_REQUIRED,
                "Payment Required",
                HttpHeaders.EMPTY,
                jsonError.getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
        );
        doThrow(payError).when(paymentClient).pay(100L, "C1", 2000.0);

        when(orderService.findById(100L)).thenAnswer(inv -> {
            testOrder.setStatus(OrderStatus.CANCELLED);
            testOrder.setSagaStatus(SagaStatus.COMPENSATED);
            return testOrder;
        });

        Order result = orchestrator.placeOrder(request);

        assertNotNull(result);
        assertEquals(OrderStatus.CANCELLED, result.getStatus());
        assertEquals(SagaStatus.COMPENSATED, result.getSagaStatus());

        verify(orderService).markFailed(eq(100L), contains("402"));
        verify(orderService).updateSagaStatus(100L, SagaStatus.COMPENSATING);
        // Reverse order compensation: release inventory first, then cancelOrder
        verify(inventoryClient).release(100L);
        verify(orderService).cancelOrder(100L);
        verify(paymentClient, never()).refund(anyLong());
    }
}
