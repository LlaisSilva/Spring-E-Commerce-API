package com.amigoscode.spring_project1.order;


import com.amigoscode.spring_project1.category.CategoryResponse;
import com.amigoscode.spring_project1.orderItem.OrderItem;
import com.amigoscode.spring_project1.orderItem.OrderItemResponse;
import com.amigoscode.spring_project1.product.Product;
import com.amigoscode.spring_project1.product.ProductResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderMapper {

    public OrderResponse toResponse(Order order){

        List<OrderItemResponse> items = order.getItems()
                .stream()
                .map(this::toItemResponse)
                .toList();


        return new OrderResponse(
                items,
                order.getTotal(),
                order.getStatus(),
                order.getCreatedAt()
        );
    }


    private OrderItemResponse toItemResponse(OrderItem item){

        Product product = item.getProduct();

        ProductResponse productResponse = new ProductResponse(
                product.getName(),
                product.getDescription(),
                new CategoryResponse(
                        product.getCategory().getName()
                ),
                product.getPrice(),
                product.getStock(),
                product.getImageUrl()
        );


        return new OrderItemResponse(
                productResponse,
                item.getPrice(),
                item.getQuantity()
        );
    }
}