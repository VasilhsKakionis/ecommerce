package com.ecommerce.order.mapper;

import com.ecommerce.order.dto.OrderDTO;
import com.ecommerce.order.dto.OrderLineDTO;
import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.OrderLine;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    OrderDTO toDTO(Order order);
    Order toEntity(OrderDTO orderDTO);

    OrderLineDTO toDTO(OrderLine orderLine);
    OrderLine toEntity(OrderLineDTO orderLineDTO);

    List<OrderDTO> toDTOList(List<Order> orders);
    List<Order> toEntityList(List<OrderDTO> orderDTOs);
    List<OrderLineDTO> toDTOListOrderLine(List<OrderLine> orderLines);
    List<OrderLine> toEntityListOrderLine(List<OrderLineDTO> orderLineDTOs);
}