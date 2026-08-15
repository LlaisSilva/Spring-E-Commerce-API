package com.amigoscode.spring_project1.user;

import com.amigoscode.spring_project1.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    public List<UserResponse> getAllUsers(){

        return userRepository.findAll()
                .stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail()
                )).toList();
    }

    public UserResponse getUserById(int id) {
        User user =  userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Usuário não encontrado!"));
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail()
        );
    }


    public void deleteUserById(int id){
        if( !userRepository.existsById(id)){
            throw new ResourceNotFoundException("Id de Usuário Invalido!");
        }

        userRepository.deleteById(id);
    }



}
