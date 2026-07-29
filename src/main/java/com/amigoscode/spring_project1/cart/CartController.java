package com.amigoscode.spring_project1.cart;

import com.amigoscode.spring_project1.user.CustomUserDetails;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
@Tag(
        name="Carts",
        description="Cart Management Endpoints"
)

@RestController
@RequestMapping("/api/v1/carts")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class CartController {

    private final CartService service;


    @GetMapping
    public ResponseEntity<CartResponse> getCart(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        return ResponseEntity.ok(
                service.getCart(userDetails)
        );
    }

    @PostMapping("/products/{productId}")
    public ResponseEntity<CartResponse> addProduct(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable int productId,
            @RequestBody QuantityRequest request
    ) {

        return ResponseEntity.ok(
                service.addProduct(
                        userDetails,
                        productId,
                        request.quantity()
                )
        );
    }

    @DeleteMapping("/products/{productId}")
    public ResponseEntity<CartResponse> removeProduct(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable int productId
    ) {

        return ResponseEntity.ok(
                service.removeProduct(
                        userDetails,
                        productId
                )
        );
    }

    @PatchMapping("/products/{productId}")
    public ResponseEntity<CartResponse> updateQuantity(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable int productId,
            @RequestBody QuantityRequest request
    ) {

        return ResponseEntity.ok(
                service.updateQuantity(
                        userDetails,
                        productId,
                        request.quantity()
                )
        );
    }
    @DeleteMapping
    public ResponseEntity<CartResponse> clearCart(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        return ResponseEntity.ok(
                service.clearCart(userDetails)
        );
    }
}