package com.amigoscode.spring_project1.product;

import com.amigoscode.spring_project1.category.Category;
import com.amigoscode.spring_project1.category.CategoryResponse;

import java.math.BigDecimal;

public record ProductResponse(
        String name,
        String description,
        CategoryResponse category,
        BigDecimal price,
        int stock,
        String imageUrl
        ) {
}
