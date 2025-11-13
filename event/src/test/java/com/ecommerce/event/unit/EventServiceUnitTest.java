package com.ecommerce.event.unit;

import com.ecommerce.event.model.OrderEvent;
import com.ecommerce.event.repository.OrderEventRepository;
import com.ecommerce.event.service.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EventServiceUnitTest {

    @Mock private OrderEventRepository repository;
    @Mock private KafkaTemplate<String, String> kafkaTemplate;
    @InjectMocks private EventService eventService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testConsumeOrderEvent_OrderCreated() {
        String message = "OrderCreated:1001";

        eventService.consumeOrderEvent(message);

        ArgumentCaptor<OrderEvent> captor = ArgumentCaptor.forClass(OrderEvent.class);
        verify(repository, times(1)).save(captor.capture());
        verify(kafkaTemplate, times(1)).send(eq("order-processed-events"), anyString());

        OrderEvent saved = captor.getValue();
        assertEquals(1001L, saved.getOrderId());
        assertEquals("OrderCreated", saved.getEventType());
    }

    @Test
    void testGetEventsByOrderId() {
        OrderEvent event = OrderEvent.builder()
                .id(1L)
                .orderId(1001L)
                .eventType("OrderCreated")
                .payload("OrderCreated:1001")
                .createdAt(LocalDateTime.now())
                .build();

        when(repository.findByOrderIdOrderByCreatedAtDesc(1001L)).thenReturn(List.of(event));

        List<OrderEvent> events = eventService.getEventsByOrderId(1001L);

        assertEquals(1, events.size());
        assertEquals("OrderCreated", events.get(0).getEventType());
    }

    @Test
    void testGetEventsByType() {
        OrderEvent event = OrderEvent.builder()
                .id(1L)
                .orderId(1001L)
                .eventType("OrderCreated")
                .payload("OrderCreated:1001")
                .createdAt(LocalDateTime.now())
                .build();

        when(repository.findByEventType("OrderCreated")).thenReturn(List.of(event));

        List<OrderEvent> events = eventService.getEventsByType("OrderCreated");

        assertEquals(1, events.size());
        assertEquals(1001L, events.get(0).getOrderId());
    }
}