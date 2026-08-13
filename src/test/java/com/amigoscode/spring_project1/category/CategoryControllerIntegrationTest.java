package com.amigoscode.spring_project1.category;

import com.amigoscode.spring_project1.config.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
public class CategoryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void setup(){
        categoryRepository.deleteAll();
        categoryRepository.save(Category.builder().name("Eletrônicos").build());
    }

    // Register
    @Test
    void shouldRejectCategoryCreationWithoutAuthentication() throws Exception{
        mockMvc.perform(post("/api/v1/categories")
                        .contentType("application/json")
                        .content("{\"name\":\"Livros\"}"))
                .andExpect(status().is4xxClientError());

    }

    @Test
    void shouldCreateCategoryWhenUserIsAdmin() throws Exception{
        mockMvc.perform(post("/api/v1/categories")
                        .with(user("admin").authorities(()-> "ADMIN"))
                        .contentType("application/json")
                        .content("{\"name\":\"Livros\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Livros"));

    }
    // GetAllCategories
    @Test
    void shouldListCategoriesWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Eletrônicos"));
    }
    // GetCategoryById

    //@Test


    // Delete

    @Test
    void shouldRejectCategoryDeletionWhenUserIsNotAdmin() throws Exception {
        Category category = categoryRepository.findAll().get(0);

        mockMvc.perform(delete("/api/v1/categories/" + category.getId())
                        .with(user("regularUser").authorities(() -> "USER")))
                .andExpect(status().isForbidden());
    }



}


