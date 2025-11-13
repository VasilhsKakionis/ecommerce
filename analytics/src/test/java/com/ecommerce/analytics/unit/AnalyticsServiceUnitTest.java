package com.ecommerce.analytics.unit;

import com.ecommerce.analytics.model.OrderMetrics;
import com.ecommerce.analytics.repository.OrderMetricsRepository;
import com.ecommerce.analytics.service.AnalyticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AnalyticsServiceUnitTest {

    @Mock
    private OrderMetricsRepository repository;

    @InjectMocks
    private AnalyticsService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testUpdateMetricsOrderCreated() {
        OrderMetrics metrics = OrderMetrics.builder()
                .totalOrders(5)
                .totalRevenue(1000.0)
                .processedOrders(2)
                .shippedOrders(1)
                .build();

        when(repository.findAll()).thenReturn(Optional.of(metrics).stream().toList());
        when(repository.save(any(OrderMetrics.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.consumeOrderEvent("OrderCreated:101:200.0");

        verify(repository, atLeastOnce()).save(any(OrderMetrics.class));
    }

    @Test
    void testGetMetricsWhenEmpty() {
        when(repository.findAll()).thenReturn(java.util.Collections.emptyList());

        OrderMetrics metrics = service.getMetrics();

        assertNotNull(metrics);
        assertEquals(0, metrics.getTotalOrders());
    }
}