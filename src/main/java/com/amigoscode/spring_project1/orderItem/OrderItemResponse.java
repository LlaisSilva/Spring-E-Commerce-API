package com.amigoscode.spring_project1.orderItem;

import com.amigoscode.spring_project1.product.ProductResponse;

import java.math.BigDecimal;

public record OrderItemResponse(ProductResponse product , BigDecimal price, int quantity) {
}
