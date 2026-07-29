package com.amigoscode.spring_project1.cart;


import com.amigoscode.spring_project1.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Integer> {
    Optional<Cart> findByUserId(int userId);

    int user(User user);
}
