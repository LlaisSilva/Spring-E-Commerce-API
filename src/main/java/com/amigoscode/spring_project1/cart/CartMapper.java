package com.amigoscode.spring_project1.cart;

import com.amigoscode.spring_project1.cartItem.CartItemResponse;
import com.amigoscode.spring_project1.category.CategoryResponse;
import com.amigoscode.spring_project1.product.ProductResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class CartMapper {


    public CartResponse toResponse(Cart cart){

        List<CartItemResponse> items = cart.getItems()
                .stream()
                .map(item -> new CartItemResponse(
                        new ProductResponse(
                                item.getProduct().getName(),
                                item.getProduct().getDescription(),
                                new CategoryResponse(
                                        item.getProduct().getCategory().getName()
                                ),
                                item.getProduct().getPrice(),
                                item.getProduct().getStock(),
                                item.getProduct().getImageUrl()
                        ),
                        item.getQuantity()
                ))
                .toList();


        BigDecimal total = items.stream()
                .map(item ->
                        item.product().price().multiply(BigDecimal.valueOf( item.quantity()))
                )
                .reduce(BigDecimal.ZERO, BigDecimal::add);


        return new CartResponse(
                items,
                total
        );
    }
}