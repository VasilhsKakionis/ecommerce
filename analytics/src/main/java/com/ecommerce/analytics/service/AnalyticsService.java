package com.ecommerce.analytics.service;

import com.ecommerce.analytics.model.OrderMetrics;
import com.ecommerce.analytics.repository.OrderMetricsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final OrderMetricsRepository repository;

    @KafkaListener(topics = "order-events", groupId = "analytics-group")
    public void consumeOrderEvent(String message) {
        System.out.println("Received event: " + message);

        String[] parts = message.split(":");
        String eventType = parts[0];
        double orderAmount = parts.length > 2 ? Double.parseDouble(parts[2]) : 0.0;

        updateMetrics(eventType, orderAmount);
    }

    private void updateMetrics(String eventType, double amount) {
        Optional<OrderMetrics> optionalMetrics = repository.findAll().stream().findFirst();

        OrderMetrics metrics = optionalMetrics.orElseGet(() -> OrderMetrics.builder()
                .totalOrders(0)
                .totalRevenue(0)
                .processedOrders(0)
                .shippedOrders(0)
                .lastUpdated(LocalDateTime.now())
                .build());

        switch (eventType) {
            case "OrderCreated":
                metrics.setTotalOrders(metrics.getTotalOrders() + 1);
                metrics.setTotalRevenue(metrics.getTotalRevenue() + amount);
                break;
            case "OrderProcessed":
                metrics.setProcessedOrders(metrics.getProcessedOrders() + 1);
                break;
            case "OrderShipped":
                metrics.setShippedOrders(metrics.getShippedOrders() + 1);
                break;
            default:
                break;
        }

        metrics.setLastUpdated(LocalDateTime.now());
        repository.save(metrics);
    }

    public OrderMetrics getMetrics() {
        return repository.findAll().stream().findFirst().orElse(OrderMetrics.builder().build());
    }
}