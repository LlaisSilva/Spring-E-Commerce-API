package com.amigoscode.spring_project1.product;

import com.amigoscode.spring_project1.category.Category;
import com.amigoscode.spring_project1.category.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private Category category;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id(1)
                .name("Eletrônicos")
                .build();

        product = Product.builder()
                .id(1)
                .name("Notebook")
                .stock(10)
                .price(new BigDecimal("4500.00"))
                .category(category)
                .build();
    }

    // updateStock


    @Test
    void shouldUpdateStockWhenValueIsValid() {
        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        Product result = productService.updateStock(1, new UpdateStockRequest(25));

        assertThat(result.getStock()).isEqualTo(25);
        verify(productRepository).save(product);
    }

    @Test
    void shouldThrowExceptionWhenStockIsNegative() {
        when(productRepository.findById(1)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> productService.updateStock(1, new UpdateStockRequest(-5)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("negativo");

        verify(productRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenProductDoesNotExistWhenUpdatingStock() {
        when(productRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateStock(99, new UpdateStockRequest(10)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("não encontrado");
    }

// deleteProduct

    @Test
    void shouldDeleteProductWhenIdExists() {
        when(productRepository.existsById(1)).thenReturn(true);

        productService.deleteProduct(1);

        verify(productRepository).deleteById(1);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentProduct() {
        when(productRepository.existsById(99)).thenReturn(false);

        assertThatThrownBy(() -> productService.deleteProduct(99))
                .isInstanceOf(RuntimeException.class);

        verify(productRepository, never()).deleteById(anyInt());
    }

// register

    @Test
    void shouldThrowExceptionWhenRegisteringWithNonExistentCategory() {
        ProductRequest request = new ProductRequest(
                "Mouse", "Mouse sem fio", 99, new BigDecimal("50.00"), 10, "url"
        );

        when(categoryRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.register(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Categoria não encontrada");

        verify(productRepository, never()).save(any());
    }

// update

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentProduct() {
        ProductRequest request = new ProductRequest(
                "Mouse", "Mouse sem fio", 1, new BigDecimal("50.00"), 10, "url"
        );

        when(productRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.update(99, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Produto não encontrado");
    }
}