package com.ecommerce.order.service;

import com.ecommerce.order.dto.OrderDTO;
import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.OrderStatus;
import com.ecommerce.order.model.OrderHistory;
import com.ecommerce.order.model.Inventory;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.order.repository.InventoryRepository;
import com.ecommerce.order.repository.OrderHistoryRepository;
import com.ecommerce.order.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryRepository inventoryRepository;
    private final OrderHistoryRepository historyRepository;
    private final OrderMapper orderMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    private final String ORDER_TOPIC = "order-events";

    @Transactional
    public OrderDTO createOrder(OrderDTO orderDTO) {

        for (var line : orderDTO.getOrderLines()) {
            Inventory inventory = inventoryRepository.findById(line.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + line.getProductId()));
            if (inventory.getAvailableStock() < line.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + line.getProductId());
            }

            inventory.setAvailableStock(inventory.getAvailableStock() - line.getQuantity());
            inventoryRepository.save(inventory);
        }


        Order order = orderMapper.toEntity(orderDTO);
        order.setStatus(OrderStatus.UNPROCESSED);
        order.setOrderDate(LocalDateTime.now());
        order.getOrderLines().forEach(line -> line.setOrder(order));

        order.setTotalAmount(order.getOrderLines().stream()
                .map(line -> line.getUnitPrice().multiply(BigDecimal.valueOf(line.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        Order savedOrder = orderRepository.save(order);

        saveHistory(savedOrder, "Order created");

        kafkaTemplate.send(ORDER_TOPIC, "OrderCreated:" + savedOrder.getOrderId());

        return orderMapper.toDTO(savedOrder);
    }

    public OrderDTO getOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        return orderMapper.toDTO(order);
    }

    @Transactional
    public OrderDTO updateOrder(Long orderId, OrderDTO orderDTO) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        if (order.getStatus() == OrderStatus.SHIPPED) {
            throw new RuntimeException("Cannot update shipped orders");
        }

        order.getOrderLines().clear();
        var newLines = orderMapper.toEntityListOrderLine(orderDTO.getOrderLines());
        newLines.forEach(line -> line.setOrder(order));
        order.getOrderLines().addAll(newLines);

        order.setTotalAmount(newLines.stream()
                .map(line -> line.getUnitPrice().multiply(java.math.BigDecimal.valueOf(line.getQuantity())))
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add));

        Order updatedOrder = orderRepository.save(order);

        saveHistory(updatedOrder, "Order updated");

        kafkaTemplate.send(ORDER_TOPIC, "OrderUpdated:" + updatedOrder.getOrderId());

        return orderMapper.toDTO(updatedOrder);
    }

    @Transactional
    public void deleteOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        order.setStatus(OrderStatus.SHIPPED); // Mark as completed
        orderRepository.save(order);
        saveHistory(order, "Order deleted");
    }

    public Page<OrderDTO> listOrders(Pageable pageable) {
        Page<Order> orders = orderRepository.findAll(pageable);
        return orders.map(orderMapper::toDTO);
    }

    @Transactional
    public OrderDTO updateOrderStatus(Long orderId, OrderStatus status) {
        int updated = orderRepository.updateStatus(orderId, status);
        if (updated == 0) throw new RuntimeException("Failed to update order status");
        Order order = orderRepository.findById(orderId).orElseThrow();
        saveHistory(order, "Status updated to " + status);
        kafkaTemplate.send(ORDER_TOPIC, "OrderStatusChanged:" + orderId + ":" + status);
        return orderMapper.toDTO(order);
    }

    public List<OrderHistory> getOrderHistory(Long orderId) {
        return historyRepository.findByOrderOrderIdOrderByUpdatedAtDesc(orderId);
    }

    private void saveHistory(Order order, String note) {
        OrderHistory history = OrderHistory.builder()
                .order(order)
                .status(order.getStatus())
                .updatedAt(LocalDateTime.now())
                .note(note)
                .build();
        historyRepository.save(history);
    }
}