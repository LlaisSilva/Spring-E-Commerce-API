package com.amigoscode.spring_project1.product;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
@Tag(
        name="Products",
        description = "Product Management Endpoints"
)
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService service;


    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Product> register(@RequestBody ProductRequest request){
        return ResponseEntity.ok(service.register(request));
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts(){
        return ResponseEntity.ok(service.getAllProducts());
    }
    //arrumar aqui
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductResponse>> getByCategory(@PathVariable int categoryId){
        return ResponseEntity.ok(service.getByCategory(categoryId));
    }
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable int id){
        return ResponseEntity.ok(service.getProductById(id));
    }

    @PutMapping("/update/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Product> update(
            @PathVariable int id,
            @RequestBody ProductRequest request) {

        return ResponseEntity.ok(service.update(id, request));
    }
    @PatchMapping("/stock/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Product> updateStock(
            @PathVariable int id,
            @RequestBody UpdateStockRequest request) {

        return ResponseEntity.ok(service.updateStock( id,request));
    }
    @PatchMapping("/price/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Product> updatePrice(
            @PathVariable int id,
            @RequestBody UpdatePriceRequest request) {

        return ResponseEntity.ok(service.updatePrice(id,request));
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> deleteProduct(@PathVariable int id){
        service.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProductResponse>> searchProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ){
        return ResponseEntity.ok(service.search(
                name, categoryId, minPrice, maxPrice, page, size, sortBy, direction
        ));
    }
}
