package com.amigoscode.spring_project1.cartItem;

import com.amigoscode.spring_project1.product.ProductResponse;

public record CartItemResponse(ProductResponse product, int quantity) {
}
