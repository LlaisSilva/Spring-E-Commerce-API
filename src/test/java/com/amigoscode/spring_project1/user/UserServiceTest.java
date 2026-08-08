package com.amigoscode.spring_project1.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.anyInt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1)
                .username("joao")
                .email("joao@email.com")
                .role(Role.USER)
                .build();
    }

    // GetAllUsers
    @Test
    void shouldReturnAllUsers(){
        when(userRepository.findAll()).thenReturn(List.of(user));
        List<UserResponse> result = userService.getAllUsers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).username()).isEqualTo("joao");
        assertThat(result.get(0).email()).isEqualTo("joao@email.com");

    }

    // FindById
    @Test
    void shouldReturnUserWhenIdExists(){
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        UserResponse result = userService.getUserById(1);

        assertThat(result.username()).isEqualTo("joao");

    }
    @Test
    void shouldThrowExceptionWhenUserIdDoesNotExist(){
        when(userRepository.findById(99)).thenReturn(Optional.empty());
        assertThatThrownBy(()-> userService.getUserById(99))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("não encontrado");

    }
    // DeleteUserById

    @Test
    void shouldDeleteUserWhenIdExists(){
        when(userRepository.existsById(1)).thenReturn(true);
        userService.deleteUserById(1);
        verify(userRepository).deleteById(1);
    }
    @Test
    void shouldThrowExceptionWhenDeletingNonExistentUser(){
        when(userRepository.existsById(99)).thenReturn(false);
        assertThatThrownBy(()-> userService.deleteUserById(99))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalido");

        verify(userRepository, never()).deleteById(anyInt());
    }



}
