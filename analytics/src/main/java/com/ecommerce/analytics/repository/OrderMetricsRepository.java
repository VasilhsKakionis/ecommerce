package com.ecommerce.analytics.repository;

import com.ecommerce.analytics.model.OrderMetrics;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderMetricsRepository extends MongoRepository<OrderMetrics, String> {
}