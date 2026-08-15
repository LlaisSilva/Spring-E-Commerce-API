package com.amigoscode.spring_project1.user;

import com.amigoscode.spring_project1.config.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

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
public class UserControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;


    private User user;


    @BeforeEach
    void setUp(){
        userRepository.deleteAll();
        user = userRepository.save(
                User.builder()
                        .username("joao")
                        .email("joao@email.com")
                        .password("hashed-password")
                        .role(Role.USER)
                        .build()
        );
    }

    // GetAllUsers

    @Test
    void shouldRejectRequest_whenUserIsNotAdmin() throws Exception{
        mockMvc.perform(get("/api/v1/user/")
                        .with(user("regularUser").authorities(()-> "USER")))
                .andExpect(status().isForbidden());

    }

    @Test
    void shouldReturnAllUsers_whenUserIsAdmin() throws Exception{
        mockMvc.perform(get("/api/v1/user/get-all")
                        .with(user("Admin").authorities(()->"ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$",hasSize(1)))
                .andExpect(jsonPath("$[0].username").value("joao"));
    }

    // GetUserById

    @Test
    void shouldRejectRequest_whenUserTriesToGetUserById() throws Exception {

        mockMvc.perform(get("/api/v1/user/get-id/" + user.getId())
                        .with(user("regularUser").authorities(() -> "USER")))
                .andExpect(status().isForbidden());
    }
    @Test
    void shouldReturnUser_whenIdExists() throws Exception {
        mockMvc.perform(get("/api/v1/user/get-id/" + user.getId())
                        .with(user("admin").authorities(() -> "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("joao@email.com"));
    }

    @Test
    void shouldReturnNotFound_whenGettingNonExistentUser() throws Exception{
        int nonExistentUserId= 999;
        mockMvc.perform(get("/api/v1/user/get-id/"+nonExistentUserId)
                        .with(user("admin").authorities(()-> "ADMIN")))
                .andExpect(status().isNotFound());
    }

    // Delete


    @Test
    void shouldRejectRequest_whenUserTriesToDeleteUser() throws Exception {
        mockMvc.perform(delete("/api/v1/user/delete/" + user.getId())
                        .with(user("regularUser").authorities(() -> "USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldDeleteUser_whenIdExists() throws Exception {
        mockMvc.perform(delete("/api/v1/user/delete/" + user.getId())
                        .with(user("admin").authorities(() -> "ADMIN")))
                .andExpect(status().isNoContent());

        assertThat(userRepository.existsById(user.getId())).isFalse();
    }
    @Test

    void shouldReturnNotFound_whenDeletingNonExistentUser() throws Exception {
        int nonExistentId = 9999;

        mockMvc.perform(delete("/api/v1/user/delete/" + nonExistentId)
                        .with(user("admin").authorities(() -> "ADMIN")))
                .andExpect(status().isNotFound());
    }



}
