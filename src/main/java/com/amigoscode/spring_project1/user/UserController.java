package com.amigoscode.spring_project1.user;

import com.amigoscode.spring_project1.auth.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService service ;


    @GetMapping("/get-all")
    public ResponseEntity<List<UserResponse>> getAllUser(){
        return ResponseEntity.ok(service.getAllUsers());
    }
    @GetMapping("/get-id/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable int id ){
        return ResponseEntity.ok(service.getUserById(id));
    }
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable int id){
        service.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }

}
