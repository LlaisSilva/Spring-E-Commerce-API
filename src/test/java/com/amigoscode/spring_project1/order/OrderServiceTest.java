package com.amigoscode.spring_project1.order;

import com.amigoscode.spring_project1.cart.Cart;
import com.amigoscode.spring_project1.cart.CartRepository;
import com.amigoscode.spring_project1.cartItem.CartItem;
import com.amigoscode.spring_project1.orderItem.OrderItem;
import com.amigoscode.spring_project1.product.Product;
import com.amigoscode.spring_project1.product.ProductRepository;
import com.amigoscode.spring_project1.user.CustomUserDetails;
import com.amigoscode.spring_project1.user.Role;
import com.amigoscode.spring_project1.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private CartRepository cartRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private OrderMapper orderMapper;
    @InjectMocks
    private OrderService orderService;

    private User user;
    private CustomUserDetails userDetails;
    private Product product;
    private Cart cart;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1)
                .email("Laís@email.com")
                .role(Role.USER)
                .build();
        userDetails = new CustomUserDetails(user);

        product = Product.builder()
                .id(1)
                .name("Notebook")
                .stock(10)
                .price(new BigDecimal("100.00"))
                .build();

        CartItem cartItem = CartItem.builder()
                .id(1)
                .product(product)
                .quantity(3)
                .build();

        cart = Cart.builder()
                .id(1)
                .user(user)
                .items(new ArrayList<>(List.of(cartItem)))
                .build();
    }

    // Checkout

    @Test
    void shouldCheckoutWhenStockIsSufficient() {
        when(cartRepository.findByUserId(1)).thenReturn(Optional.of(cart));

        orderService.checkout(userDetails);

        // Stock had 10, 3 were purchased  -> 7 should remain in stock
        assertThat(product.getStock()).isEqualTo(7);
        verify(productRepository).save(product);
        verify(orderRepository).save(any(Order.class));
        // The cart must be cleared after checkout
        assertThat(cart.getItems()).isEmpty();
    }

    @Test
    void shouldThrowExceptionWhenStockIsInsufficientDuringCheckout() {
        CartItem cartItemGrande = CartItem.builder()
                .id(2)
                .product(product)
                .quantity(999) // Greater than the available stock (10)
                .build();
        cart.setItems(new ArrayList<>(List.of(cartItemGrande)));

        when(cartRepository.findByUserId(1)).thenReturn(Optional.of(cart));

        assertThatThrownBy(() -> orderService.checkout(userDetails))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Estoque insuficiente");

        // Nothing should be saved if the validation fails
        verify(orderRepository, never()).save(any());
        verify(productRepository, never()).save(any());
    }


    @Test
    void shouldThrowExceptionWhenUserHasNoCartDuringCheckout() {
        when(cartRepository.findByUserId(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.checkout(userDetails))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Carrinho não encontrado");
    }

    @Test
    void shouldThrowExceptionWhenCartIsEmpty() {
        cart.setItems(new ArrayList<>());
        when(cartRepository.findByUserId(1)).thenReturn(Optional.of(cart));

        assertThatThrownBy(() -> orderService.checkout(userDetails))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Carrinho vazio");

        verify(orderRepository, never()).save(any());
    }


    // GetMyOrders

    @Test
    void shouldReturnOrdersBelongingToTheUser(){
        Order order = Order.builder()
                .id(10)
                .user(user)
                .status(OrderStatus.PENDING)
                .items(List.of())
                .build();
        when(orderRepository.findByUserId(1)).thenReturn(List.of(order));
        List<OrderResponse> result = orderService.getMyOrders(userDetails);

        assertThat(result).hasSize(1);
        verify(orderMapper).toResponse(order);

    }
    // GetAllOrders

    @Test
    void shouldReturnAllOrdersRegardlessOfUser(){
        Order order = Order.builder()
                .id(10)
                .user(user)
                .status(OrderStatus.PENDING)
                .items(List.of())
                .build();
        when(orderRepository.findAll()).thenReturn(List.of(order));
        List<OrderResponse> result = orderService.getAllOrders();
        assertThat(result).hasSize(1);
        verify(orderMapper).toResponse(order);
    }

    //updateStatus
    @Test
    void shouldUpdateOrderStatus(){
        Order order = Order.builder()
                .id(10)
                .user(user)
                .status(OrderStatus.PENDING)
                .items(List.of())
                .build();
        when(orderRepository.findById(10)).thenReturn(Optional.of(order));
        orderService.updateStatus(10,OrderStatus.SHIPPED);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);
        verify(orderRepository).save(order);
    }
    //  CancelOrder

    @Test
    void shouldCancelOrderAndReturnStock() {
        OrderItem orderItem = OrderItem.builder()
                .product(product)
                .quantity(4)
                .price(new BigDecimal("100.00"))
                .build();

        Order order = Order.builder()
                .id(10)
                .user(user)
                .status(OrderStatus.PENDING)
                .items(List.of(orderItem))
                .build();

        when(orderRepository.findById(10)).thenReturn(Optional.of(order));

        orderService.cancelOrder(userDetails, 10);

        // Stock was 10, returned 4 -> should become 14
        assertThat(product.getStock()).isEqualTo(14);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(orderRepository).save(order);
    }

    @Test
    void shouldThrowExceptionWhenCancellingAlreadyCancelledOrder() {
        Order order = Order.builder()
                .id(10)
                .user(user)
                .status(OrderStatus.CANCELLED)
                .items(List.of())
                .build();

        when(orderRepository.findById(10)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder(userDetails, 10))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("já está cancelado");

        // Nothing should be saved because the validation blocked the operation
        verify(orderRepository, never()).save(any());
        verify(productRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenCancellingOrderBelongingToAnotherUser() {
        User otherUser = User.builder().id(999).email("outro@email.com").role(Role.USER).build();

        Order order = Order.builder()
                .id(10)
                .user(otherUser) // The order belongs to another user
                .status(OrderStatus.PENDING)
                .items(List.of())
                .build();

        when(orderRepository.findById(10)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder(userDetails, 10))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("não pertence");
    }

    //GetOrderById

    @Test
    void shouldSearchOrderWhenBelongsToUser() {
        Order order = Order.builder()
                .id(10)
                .user(user)
                .status(OrderStatus.PENDING)
                .items(List.of())
                .build();

        when(orderRepository.findById(10)).thenReturn(Optional.of(order));

        orderService.getOrderById(userDetails, 10);

        // If it didn't throw, the ownership check passed; confirm that the mapper was called.
        verify(orderMapper).toResponse(order);
    }

    @Test
    void shouldThrowExceptionWhenFetchingAnotherUserOrder() {
        User otherUser = User.builder().id(999).email("outro@email.com").role(Role.USER).build();

        Order order = Order.builder()
                .id(10)
                .user(otherUser)
                .status(OrderStatus.PENDING)
                .items(List.of())
                .build();

        when(orderRepository.findById(10)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.getOrderById(userDetails, 10))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("não pertence");
    }

    @Test
    void shouldThrowExceptionWhenFetchingNonExistentOrder() {
        when(orderRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById(userDetails, 999))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Pedido não encontrado");
    }

}