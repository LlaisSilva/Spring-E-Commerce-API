package com.amigoscode.spring_project1.cart;

import com.amigoscode.spring_project1.cartItem.CartItem;
import com.amigoscode.spring_project1.cartItem.CartItemResponse;
import com.amigoscode.spring_project1.product.ProductMapper;
import org.mapstruct.Mapper;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring", uses = ProductMapper.class)
public interface CartMapper {

    CartItemResponse toItemResponse(CartItem item);

    default CartResponse toResponse(Cart cart) {

        List<CartItemResponse> items = cart.getItems()
                .stream()
                .map(this::toItemResponse)
                .toList();

        BigDecimal total = items.stream()
                .map(item ->
                        item.product()
                                .price()
                                .multiply(
                                        BigDecimal.valueOf(item.quantity())
                                )
                )
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(items, total);
    }
}
