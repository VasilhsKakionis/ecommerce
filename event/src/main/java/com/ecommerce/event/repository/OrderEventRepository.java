package com.ecommerce.event.repository;

import com.ecommerce.event.model.OrderEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderEventRepository extends JpaRepository<OrderEvent, Long> {

    List<OrderEvent> findByOrderIdOrderByCreatedAtDesc(Long orderId);

    List<OrderEvent> findByEventType(String eventType);
}