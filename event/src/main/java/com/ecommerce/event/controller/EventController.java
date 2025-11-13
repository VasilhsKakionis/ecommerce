package com.ecommerce.event.controller;

import com.ecommerce.event.model.OrderEvent;
import com.ecommerce.event.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<OrderEvent>> getEventsByOrderId(@PathVariable Long orderId) {
        List<OrderEvent> events = eventService.getEventsByOrderId(orderId);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/type/{eventType}")
    public ResponseEntity<List<OrderEvent>> getEventsByType(@PathVariable String eventType) {
        List<OrderEvent> events = eventService.getEventsByType(eventType);
        return ResponseEntity.ok(events);
    }
}