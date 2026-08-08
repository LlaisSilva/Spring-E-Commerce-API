package com.amigoscode.spring_project1.auth;

import com.amigoscode.spring_project1.cart.Cart;
import com.amigoscode.spring_project1.cart.CartRepository;
import com.amigoscode.spring_project1.config.JwtService;
import com.amigoscode.spring_project1.user.Role;
import com.amigoscode.spring_project1.user.User;
import com.amigoscode.spring_project1.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {
    @Mock
    private  UserRepository userRepository;
    @Mock
    private  PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    @InjectMocks
    private AuthenticationService  authenticationService;
    @Mock
    private CartRepository cartRepository;

    private User user;
    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1)
                .username("joao")
                .email("joao@email.com")
                .password("hashed-password")
                .role(Role.USER)
                .build();
    }


    // Register
    @Test
    void shouldRegisterNewUserAndCreateCartWhenEmailIsAvailable(){
        RegisterRequest request = new RegisterRequest(
                "João",
                "joao@email.com",
                "1234567"
                );
        when(userRepository.existsByEmail("joao@email.com")).thenReturn(false);
        when(passwordEncoder.encode("1234567")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("fake-jwt-token");

        AuthenticationResponse response = authenticationService.register(request);
        assertThat(response.token()).isEqualTo("fake-jwt-token");
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void shouldThrowExceptionWhenEmailIsAlreadyRegistered(){
        RegisterRequest request = new RegisterRequest(
                "João",
                "joao@email.com",
                "1234567"
        );
        when(userRepository.existsByEmail("joao@email.com")).thenReturn(true);
        assertThatThrownBy(()-> authenticationService.register(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("E-mail ja cadastrado!");
        verify(userRepository, never()).save(any());

    }

    // Authenticate

    @Test
    void shouldReturnTokenWhenCredentialsAreValid(){
        AuthenticationRequest request = new AuthenticationRequest(
                "joao@email.com",
                "1234567"
        );
        when(userRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken((any(UserDetails.class)))).thenReturn("fake-jwt-token");

        AuthenticationResponse response = authenticationService.authenticate(request);
        assertThat(response.token()).isEqualTo("fake-jwt-token");
        verify(authenticationManager).authenticate(any());
    }
    // ForgotPassword

    @Test
    void shouldGenerateRestTokenWhenEmailExists(){
        ForgotPasswordRequest request = new ForgotPasswordRequest("joao@email.com");

        when(userRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(user));

        String token = authenticationService.forgotPassword(request);

        assertThat(token).isNotBlank();
        assertThat(user.getResetToken()).isEqualTo(token);
        verify(userRepository).save(user);
    }

    @Test
    void ShouldThrowExceptionWhenEmailDoesNotExistsOnForgotPassword(){
        ForgotPasswordRequest request  = new ForgotPasswordRequest(
                "desconhecido@gmail.com"
        );
        when(userRepository.findByEmail("desconhecido@gmail.com")).thenReturn(Optional.empty());

        assertThatThrownBy(()-> authenticationService.forgotPassword(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("não encontrado");
    }
    // ResetPassword

    @Test
    void shouldResetPasswordWhenTokenIsValidAndNotExpired() {
        user.setResetToken("valid-token");
        user.setResetTokenExpiration(LocalDateTime.now().plusMinutes(10));

        ResetPasswordRequest request = new ResetPasswordRequest("valid-token", "novaSenha123");

        when(userRepository.findByResetToken("valid-token")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("novaSenha123")).thenReturn("new-hashed-password");

        authenticationService.resetPassword(request);

        assertThat(user.getPassword()).isEqualTo("new-hashed-password");
        assertThat(user.getResetToken()).isNull();
        verify(userRepository).save(user);
    }
    @Test
    void shouldThrowExceptionWhenResetTokenIsInvalid(){
        ResetPasswordRequest request = new ResetPasswordRequest("token-invalido","novaSenha");
        when(userRepository.findByResetToken("token-invalido")).thenReturn(Optional.empty());
        assertThatThrownBy(()-> authenticationService.resetPassword(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("inválido");
    }

    @Test
    void shouldThrowExceptionWhenResetTokenIsExpired(){
        user.setResetToken("expired-token");
        user.setResetTokenExpiration(LocalDateTime.now().minusMinutes(1));

        ResetPasswordRequest request = new ResetPasswordRequest("expired-token","novaSenha");
        when(userRepository.findByResetToken("expired-token")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authenticationService.resetPassword(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("expirado");


        verify(passwordEncoder, never()).encode(anyString());
    }
}
