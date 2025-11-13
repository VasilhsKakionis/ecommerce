package com.ecommerce.analytics.integration;

import com.ecommerce.analytics.model.OrderMetrics;
import com.ecommerce.analytics.repository.OrderMetricsRepository;
import com.ecommerce.analytics.service.AnalyticsService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AnalyticsServiceIntegrationTest {

    @Autowired
    private AnalyticsService service;

    @Autowired
    private OrderMetricsRepository repository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Container
    static MongoDBContainer mongo = new MongoDBContainer(DockerImageName.parse("mongo:6.0"));

    @Container
    static KafkaContainer kafka =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Test
    @Order(1)
    void testConsumeAndAggregateMetrics() throws InterruptedException {
        kafkaTemplate.send("order-events", "OrderCreated:7001:250.0");

        Thread.sleep(2000);

        OrderMetrics metrics = service.getMetrics();

        assertNotNull(metrics);
        assertTrue(metrics.getTotalOrders() > 0);
        assertTrue(metrics.getTotalRevenue() >= 250.0);
    }
}