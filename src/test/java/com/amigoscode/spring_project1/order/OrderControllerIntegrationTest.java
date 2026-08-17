package com.amigoscode.spring_project1.order;

import com.amigoscode.spring_project1.cart.Cart;
import com.amigoscode.spring_project1.cart.CartRepository;
import com.amigoscode.spring_project1.cartItem.CartItem;
import com.amigoscode.spring_project1.category.Category;
import com.amigoscode.spring_project1.category.CategoryRepository;
import com.amigoscode.spring_project1.config.TestcontainersConfiguration;
import com.amigoscode.spring_project1.orderItem.OrderItem;
import com.amigoscode.spring_project1.product.Product;
import com.amigoscode.spring_project1.product.ProductRepository;
import com.amigoscode.spring_project1.user.CustomUserDetails;
import com.amigoscode.spring_project1.user.Role;
import com.amigoscode.spring_project1.user.User;
import com.amigoscode.spring_project1.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
public class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private OrderRepository orderRepository;

    private CustomUserDetails userDetails;
    private User user;
    private Product product;

    @BeforeEach
    void setup() {
        orderRepository.deleteAll();
        cartRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        user = userRepository.save(
                User.builder()
                        .username("joao")
                        .email("joao@email.com")
                        .password("hashed-password")
                        .role(Role.USER)
                        .build()
        );
        userDetails = new CustomUserDetails(user);

        Category category = categoryRepository.save(
                Category.builder().name("Eletrônicos").build()
        );
        product = productRepository.save(
                Product.builder()
                        .name("Notebook")
                        .description("Notebook 16GB RAM")
                        .price(new BigDecimal("100.00"))
                        .stock(10)
                        .category(category)
                        .imageUrl("url")
                        .build()
        );

        cartRepository.save(
                Cart.builder()
                        .user(user)
                        .items(new ArrayList<>())
                        .build()
        );
    }
    private RequestPostProcessor asCurrentUser() {
        return authentication(new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        ));
    }

    private Order givenAnOrderExistsForUser(User owner, int quantity) {
        Order order = Order.builder()
                .user(owner)
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .total(product.getPrice().multiply(BigDecimal.valueOf(quantity)))
                .items(new ArrayList<>())
                .build();

        OrderItem item = OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(quantity)
                .price(product.getPrice())
                .build();

        order.setItems(List.of(item));
        return orderRepository.save(order);
    }
    @Test
    void shouldRejectRequest_whenUserIsNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().is4xxClientError());
    }


    // Checkout

    @Test
    void shouldCheckout_whenCartHasSufficientStock() throws Exception{
        Cart cart = cartRepository.findByUserId(user.getId()).orElseThrow();
        cart.setItems(new ArrayList<>(List.of(
                CartItem.builder().cart(cart).product(product).quantity(2).build()
        )));
        cartRepository.save(cart);
        mockMvc.perform(post("/api/v1/orders/checkout")
                        .with(asCurrentUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.total").value(200.00));
        Product updateProduct= productRepository.findById(product.getId()).orElseThrow();
        assertThat(updateProduct.getStock()).isEqualTo(8);
    }

    @Test
    void shouldReturnBadRequest_whenCartIsEmpty()throws Exception{

        mockMvc.perform(post("/api/v1/orders/checkout")
                        .with(asCurrentUser()))
                .andExpect(status().isBadRequest());

    }

    @Test
    void shouldReturnBadRequest_whenStockIsInsufficient() throws Exception{
        Cart cart = cartRepository.findByUserId(user.getId()).orElseThrow();
        cart.setItems(new ArrayList<>(List.of(
                CartItem.builder().cart(cart).product(product).quantity(999).build()
        )));
        cartRepository.save(cart);
        mockMvc.perform(post("/api/v1/orders/checkout")
                        .with(asCurrentUser()))
                .andExpect(status().isBadRequest());
    }
     // GetMyOrders

    @Test
    void shouldReturnMyOrders_whenUserIsAuthenticated() throws Exception{
        givenAnOrderExistsForUser(user, 1);
        mockMvc.perform(get("/api/v1/orders")
                        .with(asCurrentUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$",hasSize(1)));


    }

    // GetOrderById
    @Test
    void shouldReturnOrder_whenOrderBelongsToUser() throws Exception{
        Order order = givenAnOrderExistsForUser(user,1);
        mockMvc.perform(get("/api/v1/orders/"+order.getId())
                        .with(asCurrentUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));

    }

    @Test
    void shouldReturnForbidden_whenOrderBelongsToAnotherUser()throws Exception{
        User anotherUser = userRepository.save(
                User.builder()
                        .username("maria")
                        .email("maria@email.com")
                        .password("hashed-password")
                        .role(Role.USER)
                        .build()
        );

        Order orderFromAnotherUser = givenAnOrderExistsForUser(anotherUser,1);
        mockMvc.perform(get("/api/v1/orders/"+orderFromAnotherUser.getId())
                        .with(asCurrentUser()))
                .andExpect(status().isForbidden());
    }

    // GetAllOrders (admin)


    @Test
    void shouldRejectRequest_whenNonAdminAccessesAllOrders()throws Exception{
        mockMvc.perform(get("/api/v1/orders/admin")
                        .with(user("regularUser").authorities(()->"USER")))
                .andExpect(status().isForbidden());
    }
    @Test
    void shouldReturnAllOrder_whenUserIsAdmin() throws Exception{
        givenAnOrderExistsForUser(user,1);
        mockMvc.perform(get("/api/v1/orders/admin")
                        .with(user("admin").authorities(()->"ADMIN")))
                .andExpect(jsonPath("$",hasSize(1)));

    }
    // UpdateStatus

    @Test
    void shouldRejectRequest_whenNonAdminUpdatesStatus()throws Exception{
        Order order = givenAnOrderExistsForUser(user,1);
        mockMvc.perform(patch("/api/v1/orders/"+order.getId()+"/status")
                        .with(user("regularUser").authorities(()->"USER"))
                        .contentType("application/json")
                        .content("{\"status\": \"SHIPPED\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldUpdateStatus_whenUserIsAdmin()throws Exception{
        Order order = givenAnOrderExistsForUser(user,1);
        mockMvc.perform(patch("/api/v1/orders/"+order.getId()+"/status")
                        .with(user("admin").authorities(()->"ADMIN"))
                        .contentType("application/json")
                        .content("{\"status\": \"SHIPPED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SHIPPED"));
    }

    // CancelOrder
    @Test
    void shouldCancelOrderAndRestoreStock_whenOrderBelongsToUser()throws Exception {
        Order order = givenAnOrderExistsForUser(user, 3);
        mockMvc.perform(patch("/api/v1/orders/" + order.getId() + "/cancel")
                        .with(asCurrentUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
        Product updatedProduct = productRepository.findById(product.getId()).orElseThrow();
        assertThat(updatedProduct.getStock()).isEqualTo(13);
    }

    @Test
    void shouldReturnConflict_whenCancellingAlreadyCancelledOrder()throws Exception{
        Order order = givenAnOrderExistsForUser(user, 1);

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        mockMvc.perform(
                        patch("/api/v1/orders/" + order.getId() + "/cancel")
                                .with(asCurrentUser())
                )
                .andExpect(status().isConflict());
    }



}





