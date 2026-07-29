package com.amigoscode.spring_project1.order;

import com.amigoscode.spring_project1.product.UpdateStockRequest;
import com.amigoscode.spring_project1.user.CustomUserDetails;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name="Orders",
        description="Order Management Endpoints"
)

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")

public class OrderController {
    private final OrderService service;

    @PostMapping("/checkout")
    public ResponseEntity<OrderResponse> checkout(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        return ResponseEntity.ok(
                service.checkout(userDetails)
        );
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getMyOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        return ResponseEntity.ok(
                service.getMyOrders(userDetails)
        );
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable int orderId
    ){
        return ResponseEntity.ok(
                service.getOrderById(
                        userDetails,
                        orderId
                )
        );
    }

    @GetMapping("/admin")
    public ResponseEntity<List<OrderResponse>> getAllOrders(){
        return ResponseEntity.ok(
                service.getAllOrders()
        );
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable int orderId,
            @RequestBody UpdateOrderStatusRequest request
            ){
        return ResponseEntity.ok(
                service.updateStatus(
                        orderId,
                        request.status()
                )
        );
    }

    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable int orderId
    ){
        return ResponseEntity.ok(
                service.cancelOrder(
                        userDetails,
                        orderId
                )
        );
    }

}
