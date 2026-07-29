package com.amigoscode.spring_project1.order;

import com.amigoscode.spring_project1.orderItem.OrderItemResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(List<OrderItemResponse> items, BigDecimal total, OrderStatus status, LocalDateTime createdAt) {
}
