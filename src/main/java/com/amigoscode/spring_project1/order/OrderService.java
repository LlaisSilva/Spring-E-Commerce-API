package com.amigoscode.spring_project1.order;

import com.amigoscode.spring_project1.cart.Cart;
import com.amigoscode.spring_project1.cart.CartRepository;
import com.amigoscode.spring_project1.cartItem.CartItem;
import com.amigoscode.spring_project1.exception.*;
import com.amigoscode.spring_project1.orderItem.OrderItem;
import com.amigoscode.spring_project1.product.Product;
import com.amigoscode.spring_project1.product.ProductRepository;
import com.amigoscode.spring_project1.user.CustomUserDetails;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final ProductRepository productRepository;

    @Transactional
    public OrderResponse checkout(CustomUserDetails userDetails){
        Cart cart = findCartByUser(userDetails);

        if(cart.getItems().isEmpty()){
            throw new EmptyCartException("Carrinho vazio");
        }

        for(CartItem cartItem: cart.getItems()){
            Product product = cartItem.getProduct();
            if(product.getStock()< cartItem.getQuantity())
                throw new InsufficientStockException("Estoque insuficiente para o produto: "+product.getName());

        }
        Order order = Order.builder()
                .user(cart.getUser())
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        List<OrderItem> orderItems = cart.getItems()
                .stream()
                .map(cartItem -> OrderItem.builder()
                        .order(order)
                        .product(cartItem.getProduct())
                        .quantity(cartItem.getQuantity())
                        .price(cartItem.getProduct().getPrice())
                        .build()
                ).toList();

        order.setItems(orderItems);
        BigDecimal total = orderItems.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotal(total);
        for(CartItem cartItem: cart.getItems()){
            Product product = cartItem.getProduct();
            product.setStock(product.getStock()- cartItem.getQuantity());
            productRepository.save(product);
        }
        orderRepository.save(order);
        cart.getItems().clear();
        cartRepository.save(cart);

        return orderMapper.toResponse(order);


    }

    public List<OrderResponse> getMyOrders(CustomUserDetails userDetails){
        int userId = userDetails.getUser().getId();
        List<Order> orders = orderRepository.findByUserId(userId);

        return orders.stream()
                .map(orderMapper::toResponse)
                .toList();
    }


    public OrderResponse getOrderById(CustomUserDetails userDetails, int orderId){
        Order order = findOrderById(orderId);
        validateOrderOwner(order,userDetails);

        return orderMapper.toResponse(order);

    }


    public List<OrderResponse> getAllOrders(){
        return orderRepository.findAll()
                .stream()
                .map(orderMapper::toResponse)
                .toList();

    }



    public OrderResponse updateStatus(int orderId, OrderStatus status){
        Order order = findOrderById(orderId);
        order.setStatus(status);
        orderRepository.save(order);

        return orderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse cancelOrder(CustomUserDetails userDetails, int orderId){
        Order order = findOrderById(orderId);
        validateOrderOwner(order,userDetails);
        if(order.getStatus() == OrderStatus.CANCELLED){
            throw new ResourceConflictException("Pedido já está cancelado");

        }

        for(OrderItem item: order.getItems()){
            Product product = item.getProduct();
            product.setStock(product.getStock()+ item.getQuantity());
            productRepository.save(product);

        }
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        return orderMapper.toResponse(order);
    }


    private Cart findCartByUser(CustomUserDetails userDetails){
        return cartRepository.findByUserId(userDetails.getUser().getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Carrinho não encontrado")
                );
    }


    private Order findOrderById(int orderId){
        return orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Pedido não encontrado")
                );
    }


    private void validateOrderOwner(Order order, CustomUserDetails userDetails){
        if(order.getUser().getId() != userDetails.getUser().getId()){
            throw new ForbiddenAccessException("Pedido não pertence ao usuário");


        }
    }


}