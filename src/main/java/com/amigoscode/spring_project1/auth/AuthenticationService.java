package com.amigoscode.spring_project1.auth;

import com.amigoscode.spring_project1.cart.Cart;
import com.amigoscode.spring_project1.cart.CartRepository;
import com.amigoscode.spring_project1.config.JwtService;
import com.amigoscode.spring_project1.user.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtService jwtService;
        private final AuthenticationManager authenticationManager;
        private final CartRepository cartRepository;

    public AuthenticationResponse register (RegisterRequest registerRequest) {
        if(userRepository.existsByEmail(registerRequest.email()))
            throw new RuntimeException("E-mail ja cadastrado!");

        User user = User.builder()
                .username(registerRequest.username())
                .email(registerRequest.email())
                .password(passwordEncoder.encode(registerRequest.password()))
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);

        Cart cart = new Cart();
        cart.setUser(savedUser);
        cartRepository.save(cart);
        UserDetails userDetails = new CustomUserDetails(savedUser);
        String jwtToken = jwtService.generateToken(userDetails);
        return  AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request){
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.email(),
                request.password()
        ));
        var user = userRepository.findByEmail(request.email())
                .orElseThrow();
        UserDetails userDetails = new CustomUserDetails(user);
        String jwtToken = jwtService.generateToken(userDetails);
        return  AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    public  String  forgotPassword(ForgotPasswordRequest request){
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(()-> new RuntimeException("Usuário não encontrado"));

        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiration(LocalDateTime.now().plusMinutes(15));

        userRepository.save(user);
        return token;
    }
    public void resetPassword(ResetPasswordRequest request){
        User user = userRepository.findByResetToken(request.token())
                .orElseThrow(()-> new RuntimeException("Token inválido"));
        if(user.getResetTokenExpiration()==null  || user.getResetTokenExpiration().isBefore(LocalDateTime.now())){
            throw new RuntimeException("Token expirado");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));

        user.setResetToken(null);
        user.setResetTokenExpiration(null);
        userRepository.save(user);
    }
}
