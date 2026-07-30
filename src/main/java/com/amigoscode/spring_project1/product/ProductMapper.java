package com.amigoscode.spring_project1.product;

import com.amigoscode.spring_project1.category.CategoryMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = CategoryMapper.class)
public interface ProductMapper {
    ProductResponse toResponse(Product product);
}
