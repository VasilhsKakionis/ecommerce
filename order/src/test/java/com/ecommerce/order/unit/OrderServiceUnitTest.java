package com.ecommerce.order.unit;

import com.ecommerce.order.dto.OrderDTO;
import com.ecommerce.order.dto.OrderLineDTO;
import com.ecommerce.order.mapper.OrderMapper;
import com.ecommerce.order.model.*;
import com.ecommerce.order.repository.InventoryRepository;
import com.ecommerce.order.repository.OrderHistoryRepository;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.order.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class OrderServiceUnitTest {

    @Mock private OrderRepository orderRepository;
    @Mock private InventoryRepository inventoryRepository;
    @Mock private OrderHistoryRepository historyRepository;
    @Mock private KafkaTemplate<String, String> kafkaTemplate;
    @Mock private OrderMapper orderMapper;

    @InjectMocks private OrderService orderService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateOrderSuccess() {

        OrderLineDTO lineDTO = OrderLineDTO.builder()
                .productId(1L)
                .quantity(2)
                .unitPrice(BigDecimal.valueOf(10))
                .build();

        OrderDTO orderDTO = OrderDTO.builder()
                .customerId(100L)
                .orderLines(List.of(lineDTO))
                .build();

        Inventory inventory = Inventory.builder().productId(1L).availableStock(10).build();
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(inventory));

        Order order = Order.builder().orderId(1L).status(OrderStatus.UNPROCESSED).orderLines(new ArrayList<>()).build();
        when(orderMapper.toEntity(orderDTO)).thenReturn(order);
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toDTO(order)).thenReturn(orderDTO);

        OrderDTO result = orderService.createOrder(orderDTO);

        assertNotNull(result);
        verify(inventoryRepository).save(any(Inventory.class));
        verify(kafkaTemplate).send(anyString(), anyString());
        verify(historyRepository).save(any(OrderHistory.class));
    }

    @Test
    void testCreateOrderInsufficientStock() {
        OrderLineDTO lineDTO = OrderLineDTO.builder().productId(1L).quantity(5).unitPrice(BigDecimal.TEN).build();
        OrderDTO orderDTO = OrderDTO.builder().customerId(100L).orderLines(List.of(lineDTO)).build();

        Inventory inventory = Inventory.builder().productId(1L).availableStock(3).build();
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(inventory));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderService.createOrder(orderDTO);
        });

        assertTrue(exception.getMessage().contains("Insufficient stock"));
    }

    @Test
    void testGetOrderById() {
        Order order = Order.builder().orderId(1L).status(OrderStatus.UNPROCESSED).build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        OrderDTO dto = OrderDTO.builder().orderId(1L).build();
        when(orderMapper.toDTO(order)).thenReturn(dto);

        OrderDTO result = orderService.getOrder(1L);

        assertEquals(1L, result.getOrderId());
    }

    @Test
    void testUpdateOrderStatus() {
        Order order = Order.builder().orderId(1L).status(OrderStatus.UNPROCESSED).build();
        when(orderRepository.updateStatus(1L, OrderStatus.PROCESSING)).thenReturn(1);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderDTO dto = OrderDTO.builder().orderId(1L).build();
        when(orderMapper.toDTO(order)).thenReturn(dto);

        OrderDTO result = orderService.updateOrderStatus(1L, OrderStatus.PROCESSING);

        assertEquals(1L, result.getOrderId());
        verify(historyRepository).save(any(OrderHistory.class));
        verify(kafkaTemplate).send(anyString(), anyString());
    }

    @Test
    void testListOrders() {
        Order order = Order.builder().orderId(1L).build();
        Page<Order> page = new PageImpl<>(List.of(order));
        when(orderRepository.findAll(PageRequest.of(0,10))).thenReturn(page);
        OrderDTO dto = OrderDTO.builder().orderId(1L).build();
        when(orderMapper.toDTO(order)).thenReturn(dto);

        Page<OrderDTO> result = orderService.listOrders(PageRequest.of(0,10));

        assertEquals(1, result.getTotalElements());
    }
}