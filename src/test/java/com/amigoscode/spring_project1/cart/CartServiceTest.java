package com.amigoscode.spring_project1.cart;

import com.amigoscode.spring_project1.cartItem.CartItem;
import com.amigoscode.spring_project1.product.Product;
import com.amigoscode.spring_project1.product.ProductRepository;
import com.amigoscode.spring_project1.user.CustomUserDetails;
import com.amigoscode.spring_project1.user.Role;
import com.amigoscode.spring_project1.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CartMapper cartMapper;

    @InjectMocks
    private CartService cartService;

    private CustomUserDetails userDetails;
    private Product product;
    private Cart cart;

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .id(1)
                .email("Laís@email.com")
                .role(Role.USER)
                .build();

        userDetails = new CustomUserDetails(user);

        product = Product.builder()
                .id(1)
                .name("Notebook")
                .price(new BigDecimal("100.00"))
                .stock(10)
                .build();

        cart = Cart.builder()
                .id(1)
                .user(user)
                .items(new ArrayList<>())
                .build();
    }

    //  AddProduct

    @Test
    void shouldAddNewProductToCart() {
        when(cartRepository.findByUserId(1))
                .thenReturn(Optional.of(cart));

        when(productRepository.findById(1))
                .thenReturn(Optional.of(product));

        cartService.addProduct(userDetails, 1, 2);

        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(2);

        verify(cartRepository).save(cart);
    }

    @Test
    void shouldIncreaseQuantityWhenProductAlreadyExistsInCart() {
        CartItem existingItem = CartItem.builder()
                .product(product)
                .quantity(3)
                .build();

        cart.setItems(new ArrayList<>(List.of(existingItem)));

        when(cartRepository.findByUserId(1))
                .thenReturn(Optional.of(cart));

        when(productRepository.findById(1))
                .thenReturn(Optional.of(product));

        cartService.addProduct(userDetails, 1, 2);

        // Already had 3, added 2 more -> should become 5 without creating a new item
        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(5);
    }

    @Test
    void shouldThrowExceptionWhenAddingNonExistentProduct() {
        when(cartRepository.findByUserId(1))
                .thenReturn(Optional.of(cart));

        when(productRepository.findById(99))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addProduct(userDetails, 99, 1))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Produto não encontrado");
    }

    // UpdateQuantity

    @Test
    void shouldUpdateQuantityOfExistingCartItem() {
        CartItem item = CartItem.builder()
                .product(product)
                .quantity(1)
                .build();

        cart.setItems(new ArrayList<>(List.of(item)));

        when(cartRepository.findByUserId(1))
                .thenReturn(Optional.of(cart));

        cartService.updateQuantity(userDetails, 1, 10);

        assertThat(item.getQuantity()).isEqualTo(10);

        verify(cartRepository).save(cart);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingQuantityOfProductNotInCart() {
        when(cartRepository.findByUserId(1))
                .thenReturn(Optional.of(cart)); // Empty cart

        assertThatThrownBy(() -> cartService.updateQuantity(userDetails, 1, 5))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("não está no carrinho");
    }
}

