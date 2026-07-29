package com.amigoscode.spring_project1.cart;

import com.amigoscode.spring_project1.cartItem.CartItemResponse;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(List<CartItemResponse> items, BigDecimal total) {
}
