package com.mss301.order_service.controller;

import com.mss301.order_service.dto.CreateOrderRequest;
import com.mss301.order_service.entity.Order;
import com.mss301.order_service.entity.OrderStatus;
import com.mss301.order_service.entity.SagaLog;
import com.mss301.order_service.entity.SagaStatus;
import com.mss301.order_service.repository.OrderRepository;
import com.mss301.order_service.repository.SagaLogRepository;
import com.mss301.order_service.saga.OrderSagaOrchestrator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderSagaOrchestrator orchestrator;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private SagaLogRepository sagaLogRepository;

    @InjectMocks
    private OrderController orderController;

    @Test
    void testPlaceOrder() {
        Order expectedOrder = new Order();
        expectedOrder.setId(1L);
        expectedOrder.setStatus(OrderStatus.CONFIRMED);
        expectedOrder.setSagaStatus(SagaStatus.COMPLETED);

        when(orchestrator.placeOrder(any(CreateOrderRequest.class))).thenReturn(expectedOrder);

        CreateOrderRequest req = new CreateOrderRequest("C1", "P1", 2, 2000.0);
        Order actual = orderController.placeOrder(req);

        assertNotNull(actual);
        assertEquals(1L, actual.getId());
        assertEquals(OrderStatus.CONFIRMED, actual.getStatus());
        verify(orchestrator).placeOrder(req);
    }

    @Test
    void testFindAll() {
        Order o1 = new Order();
        o1.setId(1L);
        when(orderRepository.findAll()).thenReturn(List.of(o1));

        List<Order> orders = orderController.findAll();
        assertEquals(1, orders.size());
        assertEquals(1L, orders.get(0).getId());
        verify(orderRepository).findAll();
    }

    @Test
    void testSagaLogs() {
        SagaLog log1 = new SagaLog(1L, "CREATE_ORDER", "SUCCESS", "Created");
        when(sagaLogRepository.findByOrderIdOrderByIdAsc(1L)).thenReturn(List.of(log1));

        List<SagaLog> logs = orderController.sagaLogs(1L);
        assertEquals(1, logs.size());
        assertEquals("CREATE_ORDER", logs.get(0).getStep());
        verify(sagaLogRepository).findByOrderIdOrderByIdAsc(1L);
    }
}
