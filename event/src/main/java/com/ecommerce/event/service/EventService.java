package com.ecommerce.event.service;

import com.ecommerce.event.model.OrderEvent;
import com.ecommerce.event.repository.OrderEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final OrderEventRepository orderEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String PROCESSED_TOPIC = "order-processed-events";

    @KafkaListener(topics = "order-events", groupId = "event-group")
    public void consumeOrderEvent(String message) {
        System.out.println("Consumed event: " + message);

        String[] parts = message.split(":");
        String eventType = parts[0];
        Long orderId = Long.parseLong(parts[1]);

        OrderEvent event = OrderEvent.builder()
                .orderId(orderId)
                .eventType(eventType)
                .payload(message)
                .createdAt(LocalDateTime.now())
                .build();

        orderEventRepository.save(event);

        if ("OrderCreated".equals(eventType)) {
            kafkaTemplate.send(PROCESSED_TOPIC, "OrderProcessed:" + orderId);
        }
    }

    public List<OrderEvent> getEventsByOrderId(Long orderId) {
        return orderEventRepository.findByOrderIdOrderByCreatedAtDesc(orderId);
    }

    public List<OrderEvent> getEventsByType(String eventType) {
        return orderEventRepository.findByEventType(eventType);
    }
}