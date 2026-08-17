package com.amigoscode.spring_project1.product;


import com.amigoscode.spring_project1.category.Category;
import com.amigoscode.spring_project1.category.CategoryRepository;
import com.amigoscode.spring_project1.config.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
public class ProductControllerIntegrationTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category category;
    private Product product;

    @BeforeEach
    void setup(){
        productRepository.deleteAll();
        categoryRepository.deleteAll();

        category = categoryRepository.save(
                Category.builder().name("Eletrônicos").build()
        );

        product = productRepository.save(
                Product.builder()
                        .name("Notebook")
                        .description("Notebook 16GB RAM")
                        .price(new BigDecimal("4500.00"))
                        .stock(10)
                        .category(category)
                        .imageUrl("url")
                        .build()
        );

    }

    // Register

    @Test
    void shouldRejectRequest_whenNonAdminCreatesRequest() throws Exception{
        mockMvc.perform(post("/api/v1/products")
                        .with(user("regularUser").authorities(()->"USER"))
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldCreateProduct_whenUserIsAdmin()throws Exception{
        String body = String.format(
                "{\"name\":\"Mouse\",\"description\":\"Mouse sem fio\",\"categoryId\":%d,\"price\":50.00,\"stock\":20,\"imageUrl\":\"url\"}",
                category.getId()
        );
        mockMvc.perform(post("/api/v1/products")
                        .with(user("admin").authorities(()->"ADMIN"))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Mouse"));

    }




    // GetAllProducts
    @Test
    void shouldListProducts_whenNoAuthenticationProvided()throws Exception{
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Notebook"))
                .andExpect(jsonPath("$",hasSize(1)));
    }

    // GetProductById

    @Test
    void shouldReturnNotFound_whenGettingNonExistentProduct()throws Exception{
        int nonExistentProductId = 999;
        mockMvc.perform(get("/api/v1/products/"+nonExistentProductId))
                .andExpect(status().isNotFound());
    }
    @Test
    void shouldReturnProduct_whenIdExists()throws Exception{
        mockMvc.perform(get("/api/v1/products/"+product.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Notebook"));
    }

    // GetProductByCategoryId
    @Test
    void shouldReturnProductInCategory_whenCategoryExists()throws Exception{
        mockMvc.perform(get("/api/v1/products/category/"+category.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Notebook"))
                .andExpect(jsonPath("$",hasSize(1)));

    }

    @Test
    void shouldEmptyList_whenCategoryDoesNotExists() throws Exception{
        int nonExistentCategoryId = 999;
        mockMvc.perform(get("/api/v1/products/category/"+nonExistentCategoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$",hasSize(0)));

    }
    // Update
    @Test
    void shouldUpdateProduct_whenUserIsAdmin() throws Exception {
        String body = String.format(
                "{\"name\":\"Notebook Pro\",\"description\":\"Notebook 32GB RAM\",\"categoryId\":%d,\"price\":6000.00,\"stock\":5,\"imageUrl\":\"nova-url\"}",
                category.getId()
        );
        mockMvc.perform(put("/api/v1/products/update/" + product.getId())
                        .with(user("admin").authorities(() -> "ADMIN"))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Notebook Pro"))
                .andExpect(jsonPath("$.stock").value(5));

    }

    // UpdatePrice

    @Test
    void shouldUpdatePrice_whenUserIsAdmin() throws Exception {
        mockMvc.perform(patch("/api/v1/products/price/" + product.getId())
                        .with(user("admin").authorities(() -> "ADMIN"))
                        .contentType("application/json")
                        .content("{\"price\": 999.90}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(999.90));
    }

    // UpdateStock

    @Test
    void shouldUpdateStock_whenUserIsAdmin()throws Exception{
        mockMvc.perform(patch("/api/v1/products/stock/"+product.getId())
                        .with(user("admin").authorities(()->"ADMIN"))
                        .contentType("application/json")
                        .content("{\"stock\": 50}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(50));

    }

    @Test
    void shouldReturnBadRequest_whenUpdatingStockToNegativeValue() throws Exception {
        mockMvc.perform(patch("/api/v1/products/stock/" + product.getId())
                        .with(user("admin").authorities(() -> "ADMIN"))
                        .contentType("application/json")
                        .content("{\"stock\": -5}"))
                .andExpect(status().isBadRequest());
    }

    // Delete

    @Test
    void shouldDeleteProduct_whenUserIsAdmin() throws Exception{
        mockMvc.perform(delete("/api/v1/products/"+product.getId())
                        .with(user("admin").authorities(()->"ADMIN")))
                .andExpectAll(status().isNoContent());
        assertThat(productRepository.existsById(product.getId())).isFalse();
    }

    // Search
    @Test
    void shouldFilterProductsByPriceRange_whenSearching() throws Exception {
        productRepository.save(
                Product.builder()
                        .name("Mouse")
                        .description("Mouse sem fio")
                        .price(new BigDecimal("50.00"))
                        .stock(30)
                        .category(category)
                        .imageUrl("url")
                        .build()
        );

        // só o Notebook (4500.00) deveria bater com essa faixa de preço
        mockMvc.perform(get("/api/v1/products/search")
                        .param("minPrice", "1000")
                        .param("maxPrice", "5000")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value("Notebook"));
    }




}
