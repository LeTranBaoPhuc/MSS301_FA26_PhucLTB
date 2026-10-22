package com.mss301.order_service.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class ClientsTest {

    private RestClient inventoryRestClient;
    private RestClient paymentRestClient;
    private MockRestServiceServer mockInventoryServer;
    private MockRestServiceServer mockPaymentServer;

    private InventoryClient inventoryClient;
    private PaymentClient paymentClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder invBuilder = RestClient.builder().baseUrl("http://localhost:8082");
        mockInventoryServer = MockRestServiceServer.bindTo(invBuilder).build();
        inventoryRestClient = invBuilder.build();
        inventoryClient = new InventoryClient(inventoryRestClient);

        RestClient.Builder payBuilder = RestClient.builder().baseUrl("http://localhost:8083");
        mockPaymentServer = MockRestServiceServer.bindTo(payBuilder).build();
        paymentRestClient = payBuilder.build();
        paymentClient = new PaymentClient(paymentRestClient);
    }

    @Test
    void testInventoryReserveSuccess() {
        mockInventoryServer.expect(requestTo("http://localhost:8082/api/inventory/reserve"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andRespond(withSuccess());

        inventoryClient.reserve(1L, "P1", 2);
        mockInventoryServer.verify();
    }

    @Test
    void testInventoryReleaseSuccess() {
        mockInventoryServer.expect(requestTo("http://localhost:8082/api/inventory/release/1"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess());

        inventoryClient.release(1L);
        mockInventoryServer.verify();
    }

    @Test
    void testPaymentPaySuccess() {
        mockPaymentServer.expect(requestTo("http://localhost:8083/api/payments/pay"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andRespond(withSuccess());

        paymentClient.pay(1L, "C1", 2000.0);
        mockPaymentServer.verify();
    }

    @Test
    void testPaymentRefundSuccess() {
        mockPaymentServer.expect(requestTo("http://localhost:8083/api/payments/refund/1"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess());

        paymentClient.refund(1L);
        mockPaymentServer.verify();
    }

    @Test
    void testPaymentPayFailureThrowsException() {
        mockPaymentServer.expect(requestTo("http://localhost:8083/api/payments/pay"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.PAYMENT_REQUIRED));

        assertThrows(RestClientResponseException.class, () -> paymentClient.pay(1L, "C2", 2000.0));
        mockPaymentServer.verify();
    }
}
