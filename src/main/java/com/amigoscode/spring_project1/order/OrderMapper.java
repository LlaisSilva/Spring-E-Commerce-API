package com.amigoscode.spring_project1.order;

import com.amigoscode.spring_project1.orderItem.OrderItem;
import com.amigoscode.spring_project1.orderItem.OrderItemResponse;
import com.amigoscode.spring_project1.product.ProductMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = ProductMapper.class)
public interface OrderMapper {
    OrderResponse toResponse(Order order);
    OrderItemResponse toItemResponse(OrderItem item);
}
