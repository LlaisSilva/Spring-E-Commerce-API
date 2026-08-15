package com.amigoscode.spring_project1.cart;


import com.amigoscode.spring_project1.category.Category;
import com.amigoscode.spring_project1.category.CategoryRepository;
import com.amigoscode.spring_project1.config.TestcontainersConfiguration;
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
import java.util.ArrayList;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
public class CartControllerIntegrationTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private CustomUserDetails userDetails;
    private Product product;


    @BeforeEach
    void setup(){
        cartRepository.deleteAll();
        userRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();

        User user = userRepository.save(
                User.builder()
                        .username("joao")
                        .email("joao@email.com")
                        .password("hashed-password")
                        .role(Role.USER)
                        .build()
        );
        userDetails = new CustomUserDetails(user);

        cartRepository.save(
                Cart.builder()
                        .user(user)
                        .items(new ArrayList<>())
                        .build()
        );

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


    }





    private RequestPostProcessor asCurrentUser(){
        return authentication(new UsernamePasswordAuthenticationToken(
                userDetails,null,userDetails.getAuthorities()
        ));
    }

    // Register

    @Test
    void shouldRejectRequest_whenUserIsNotAuthenticated() throws Exception{
        mockMvc.perform(get("/api/v1/carts"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void shouldReturnEmptyCart_whenUserIsAuthenticated() throws Exception{
        mockMvc.perform(get("/api/v1/carts").with(asCurrentUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items",hasSize(0)))
                .andExpect(jsonPath("$.total").value(0));
    }

     // AddProduct

    @Test
    void shouldAddProduct_whenProductIsNewToCart() throws Exception{
        mockMvc.perform(post("/api/v1/carts/products/" + product.getId())
                    .with(asCurrentUser())
                    .contentType("application/json")
                    .content("{\"quantity\":2}"))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items",hasSize(1)))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.total").value(200.00));

    }

    @Test
     void shouldIncreaseQuantity_whenProductAlreadyExistsInCart() throws Exception{
        mockMvc.perform(post("/api/v1/carts/products/"+product.getId())
                    .with(asCurrentUser())
                    .contentType("application/json")
                    .content("{\"quantity\":2}"));

        mockMvc.perform(post("/api/v1/carts/products/"+product.getId())
                    .with(asCurrentUser())
                    .contentType("application/json")
                    .content("{\"quantity\":3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items",hasSize(1)))
                .andExpect(jsonPath("$.items[0].quantity").value(5));

    }

    @Test
    void shouldReturnNotFound_whenAddingNonExistentProductToCart() throws Exception{
        int nonExistentProductId= 999;
        mockMvc.perform(post("/api/v1/carts/products/"+nonExistentProductId)
                    .with(asCurrentUser())
                    .contentType("application/json")
                    .content("{\"quantity\":3}"))
                    .andExpect(status().isNotFound());

        }
    // UpdateQuantity
    @Test
    void shouldReturnNotFound_whenUpdatingQuantityOfNonExistentProduct()throws Exception{
        int nonExistentProductId = 999;
        mockMvc.perform(post("/api/v1/carts/products/"+nonExistentProductId)
                        .with(asCurrentUser())
                        .contentType("application/json")
                        .content("{\"quantity\":2}"))
                .andExpect(status().isNotFound());
    }
    @Test
    void shouldUpdateQuantity_whenProductIdExists()throws Exception{
        mockMvc.perform(post("/api/v1/carts/products/"+product.getId())
                        .with(asCurrentUser())
                        .contentType("application/json")
                        .content("{\"quantity\":2}"))
                .andExpect(status().isOk());

    }

    @Test
    void shouldReturnBadRequest_whenQuantityIsInvalid() throws Exception {
        mockMvc.perform(patch("/api/v1/carts/products/" + product.getId())
                        .with(asCurrentUser())
                        .contentType("application/json")
                        .content("{\"quantity\":0}"))
                .andExpect(status().isBadRequest());
    }

    //  Delete

    @Test
    void shouldRemoveProduct_whenProductExistsInCart() throws Exception{
        mockMvc.perform(post("/api/v1/carts/products/"+product.getId())
                    .with(asCurrentUser())
                    .contentType("application/json")
                    .content("{\"quantity\":2}"));
        mockMvc.perform(delete("/api/v1/carts/products/"+product.getId())
                    .with(asCurrentUser()) )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items",hasSize(0)));


    }

    // Clear

    @Test
    void shouldClearCart_whenCartHasItems() throws Exception{
        mockMvc.perform(post("/api/v1/carts/products/"+product.getId())
                    .with(asCurrentUser())
                    .contentType("application/json")
                    .content("{\"quantity\":2}"));
        mockMvc.perform(delete("/api/v1/carts").with(asCurrentUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items",hasSize(0)))
                .andExpect(jsonPath("$.total").value(0));

    }



}
