package com.ecommerce.analytics.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "order_metrics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderMetrics {

    @Id
    private String id;

    private long totalOrders;
    private double totalRevenue;
    private long processedOrders;
    private long shippedOrders;
    private LocalDateTime lastUpdated;
}