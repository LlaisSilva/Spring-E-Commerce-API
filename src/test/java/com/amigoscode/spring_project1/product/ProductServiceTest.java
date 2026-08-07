package com.amigoscode.spring_project1.product;

import com.amigoscode.spring_project1.category.Category;
import com.amigoscode.spring_project1.category.CategoryRepository;
import com.amigoscode.spring_project1.category.CategoryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;
    @Mock
    private ProductMapper productMapper;

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
    // Register
    @Test
    void shouldRegisterNewProductWhenCategoryExists(){
        ProductRequest request = new ProductRequest(
                "Mouse",
                "Mouse sem fio",
                1,
                new BigDecimal("300"),
                10,
                "url"
        );
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenAnswer( invocation -> invocation.getArgument(0));
        Product result = productService.register(request);

        assertThat(result.getName()).isEqualTo("Mouse");
        assertThat(result.getCategory()).isEqualTo(category);
        verify(productRepository).save(any(Product.class));
    }
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

    // GetAllProducts
    @Test
    void shouldReturnAllProducts(){
        when(productRepository.findAll()).thenReturn(List.of(product));
        when(productMapper.toResponse(product)).thenReturn(
                new ProductResponse(
                        "Notebook",
                        null,
                        new CategoryResponse("Eletrônicos"),
                        new BigDecimal("4500.00"),
                        10,
                        null)
        );
        List<ProductResponse> result = productService.getAllProducts();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Notebook");
    }

    // GetByCategory

    @Test
     void shouldReturnProductsByCategory(){
        when(productRepository.findByCategory_Id(1)).thenReturn(List.of(product));
        when(productMapper.toResponse(product)).thenReturn(
                new ProductResponse(
                        "Notebook",
                        null,
                        new CategoryResponse("Eletrônicos"),
                        new BigDecimal("4500.00"),
                        10,
                        null)
        );
        List<ProductResponse> result = productService.getByCategory(1);
        assertThat(result).hasSize(1);

    }
    // GetProductById
    @Test
    void shouldReturnProductWhenIdExists(){
        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(productMapper.toResponse(product)).thenReturn(
                new ProductResponse(
                        "Notebook",
                        null,
                        new CategoryResponse("Eletrônicos"),
                        new BigDecimal("4500.00"),
                        10,
                        null)
        );

        ProductResponse result = productService.getProductById(1);
        assertThat(result.name()).isEqualTo("Notebook");

    }

    @Test
    void shouldThrowExceptionWhenProductByIdDoesNotExist(){
        when(productRepository.findById(99)).thenReturn(Optional.empty());
        assertThatThrownBy(()-> productService.getProductById(99))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Id de produto não foi achado");
    }


    // Update
    @Test
    void shouldUpdateProductWhenProductAndCategoryExist(){
        ProductRequest request = new ProductRequest(
                "Mouse Gamer",
                "Mouse RBG",
                1,
                new BigDecimal("80.80"),
                20,
                "url2"
        );
        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        Product result = productService.update(1,request);
        assertThat(result.getName()).isEqualTo("Mouse Gamer");
        assertThat(result.getStock()).isEqualTo(20);
    }
    @Test
    void shouldThrowExceptionWhenUpdatingWithNonExistentCategory(){
        ProductRequest request= new ProductRequest(
                "Mouse",
                "Mouse sem fio",
                99,
                new BigDecimal("300.00"),
                10,
                "url"
        );

        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(categoryRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(()-> productService.update(1,request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Categoria não encontrada");
        verify(productRepository, never()).save(any());
    }

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

    // UpdateStock


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

    // Update Price

    @Test
    void shouldUpdatePriceWhenProductExists(){
        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        Product result = productService.updatePrice(1, new UpdatePriceRequest(new BigDecimal("999.00")));

        assertThat(result.getPrice()).isEqualByComparingTo("999.00");
        verify(productRepository).save(product);
    }
    @Test
    void shouldThrowExceptionWhenUpdatingPriceOfNonExistentProduct(){
        when(productRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updatePrice(99, new UpdatePriceRequest(new BigDecimal("999.00"))))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("não encontrado");
    }

    // DeleteProduct

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

    // Search
    @Test
    void shouldSearchProductsWithoutAnyFilters() {
        Page<Product> page = new PageImpl<>(List.of(product));
        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(productMapper.toResponse(product)).thenReturn(
                new ProductResponse("Notebook", null, new CategoryResponse("Eletrônicos"), new BigDecimal("4500.00"), 10, null)
        );

        Page<ProductResponse> result = productService.search(
                null, null, null, null, 0, 10, "name", "asc"
        );

        assertThat(result.getContent()).hasSize(1);
    }
    @Test
    void shouldSearchProductsWithAllFiltersApplied() {
        Page<Product> page = new PageImpl<>(List.of(product));
        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(productMapper.toResponse(product)).thenReturn(
                new ProductResponse("Notebook", null, new CategoryResponse("Eletrônicos"), new BigDecimal("4500.00"), 10, null)
        );

        Page<ProductResponse> result = productService.search(
                "note", 1, new BigDecimal("100.00"), new BigDecimal("5000.00"), 0, 10, "price", "desc"
        );

        assertThat(result.getContent()).hasSize(1);
    }




}