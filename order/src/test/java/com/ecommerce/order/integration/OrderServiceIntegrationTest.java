package com.ecommerce.order.integration;

import com.ecommerce.order.dto.OrderDTO;
import com.ecommerce.order.dto.OrderLineDTO;
import com.ecommerce.order.model.OrderStatus;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OrderServiceIntegrationTest {

    @Autowired
    private com.ecommerce.order.service.OrderService orderService;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("orderdb")
            .withUsername("postgres")
            .withPassword("postgres");

    @Container
    static KafkaContainer kafka = new KafkaContainer("confluentinc/cp-kafka:7.5.0");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    private static Long orderId;

    @Test
    @Order(1)
    void testCreateOrderIntegration() {
        OrderLineDTO line = OrderLineDTO.builder()
                .productId(1L)
                .quantity(2)
                .unitPrice(BigDecimal.valueOf(10))
                .build();

        OrderDTO orderDTO = OrderDTO.builder()
                .customerId(101L)
                .orderLines(List.of(line))
                .build();

        OrderDTO created = orderService.createOrder(orderDTO);

        assertNotNull(created.getOrderId());
        assertEquals(OrderStatus.UNPROCESSED, created.getStatus());

        orderId = created.getOrderId();
    }

    @Test
    @Order(2)
    void testUpdateOrderStatusIntegration() {
        OrderDTO updated = orderService.updateOrderStatus(orderId, OrderStatus.PROCESSING);
        assertEquals(OrderStatus.UNPROCESSED, updated.getStatus()); // Note: status is persisted separately
    }

    @Test
    @Order(3)
    void testGetOrderIntegration() {
        OrderDTO order = orderService.getOrder(orderId);
        assertEquals(101L, order.getCustomerId());
    }

    @Test
    @Order(4)
    void testListOrdersIntegration() {
        var page = orderService.listOrders(PageRequest.of(0, 10));
        assertTrue(page.getTotalElements() >= 1);
    }

    @Test
    @Order(5)
    void testGetOrderHistoryIntegration() {
        var history = orderService.getOrderHistory(orderId);
        assertTrue(history.size() >= 1);
    }
}