package com.amigoscode.spring_project1.product;

import com.amigoscode.spring_project1.category.Category;

import java.math.BigDecimal;


public record ProductRequest(
        String name,
        String description,
        int categoryId,
        BigDecimal price,
        int stock,
        String imageUrl ) {
}
