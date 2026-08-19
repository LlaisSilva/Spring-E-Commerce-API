package com.amigoscode.spring_project1.auth;

import com.amigoscode.spring_project1.config.TestcontainersConfiguration;
import com.amigoscode.spring_project1.user.Role;
import com.amigoscode.spring_project1.user.User;
import com.amigoscode.spring_project1.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.time.LocalDateTime;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
public class AuthenticationControllerIntegrationTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private User user;

    @BeforeEach
    void setUp(){
        userRepository.deleteAll();
        user = userRepository.save(
                User.builder()
                        .username("joao")
                        .email("joao@email.com")
                        .password(passwordEncoder.encode("hashed-password"))
                        .role(Role.USER)
                        .createdAt(LocalDateTime.now())
                        .build()

        );


    }

    // Register

    @Test
    void shouldRegisterAndReturnToken_whenEmailIsNew()throws Exception{
        String body = "{\"username\":\"maria\",\"email\":\"maria@email.com\",\"password\":\"hashed-password\"}";
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
        assertThat(userRepository.existsByEmail("maria@email.com")).isTrue();
    }
    @Test
    void shouldRejectRegister_whenEmailAlreadyExists()throws Exception{
        String body = "{\"username\":\"joao\",\"email\":\"joao@email.com\",\"password\":\"hashed-password\"}";
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isConflict());

    }

    // Authenticate
    @Test
    void should





}
