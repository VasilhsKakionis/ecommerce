package com.ecommerce.analytics.controller;

import com.ecommerce.analytics.model.OrderMetrics;
import com.ecommerce.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService service;

    @GetMapping("/metrics")
    public ResponseEntity<OrderMetrics> getMetrics() {
        OrderMetrics metrics = service.getMetrics();
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/summary")
    public ResponseEntity<String> getSummary() {
        OrderMetrics metrics = service.getMetrics();
        String summary = String.format("Total Orders: %d, Processed: %d, Shipped: %d, Revenue: %.2f",
                metrics.getTotalOrders(),
                metrics.getProcessedOrders(),
                metrics.getShippedOrders(),
                metrics.getTotalRevenue());
        return ResponseEntity.ok(summary);
    }
}