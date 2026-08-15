package com.amigoscode.spring_project1.cart;

import com.amigoscode.spring_project1.cartItem.CartItem;
import com.amigoscode.spring_project1.exception.ResourceNotFoundException;
import com.amigoscode.spring_project1.product.Product;
import com.amigoscode.spring_project1.product.ProductRepository;
import com.amigoscode.spring_project1.user.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;

    private final ProductRepository productRepository;
    private final CartMapper cartMapper;

    public CartResponse getCart(CustomUserDetails userDetails){

        Cart cart = findCartByUserId(userDetails);

        return cartMapper.toResponse(cart);
    }

    public CartResponse addProduct(CustomUserDetails userDetails, int productId, int quantity){

        Cart cart = findCartByUserId(userDetails);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));

        CartItem existingItem = cart.getItems()
                .stream()
                .filter(item -> item.getProduct().getId() == productId)
                .findFirst()
                .orElse(null);

        if(existingItem != null){
            existingItem.setQuantity(
                    existingItem.getQuantity() + quantity
            );
        }else{
            CartItem cartItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(quantity)
                    .build();
            cart.getItems().add(cartItem);
        }

        cartRepository.save(cart);

        return cartMapper.toResponse(cart);
    }

    public CartResponse removeProduct(CustomUserDetails userDetails, int productId){

        Cart cart = findCartByUserId(userDetails);

        cart.getItems()
                .removeIf(item -> item.getProduct().getId() == productId);

        cartRepository.save(cart);

        return cartMapper.toResponse(cart);
    }

    public CartResponse updateQuantity(CustomUserDetails userDetails, int productId, int quantity){

        Cart cart = findCartByUserId(userDetails);

        CartItem item = cart.getItems()
                .stream()
                .filter(cartItem -> cartItem.getProduct().getId() == productId)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Produto não está no carrinho"));

        item.setQuantity(quantity);

        cartRepository.save(cart);

        return cartMapper.toResponse(cart);
    }

    public CartResponse clearCart(CustomUserDetails userDetails){
        Cart cart = findCartByUserId(userDetails);
        cart.getItems().clear();
        cartRepository.save(cart);

        return cartMapper.toResponse(cart);
    }


    private Cart findCartByUserId(CustomUserDetails userDetails){

        return cartRepository.findByUserId(userDetails.getUser().getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Carrinho não encontrado")
                );
    }

}
