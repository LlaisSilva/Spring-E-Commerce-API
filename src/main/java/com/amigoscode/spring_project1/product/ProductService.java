package com.amigoscode.spring_project1.product;

import com.amigoscode.spring_project1.category.Category;
import com.amigoscode.spring_project1.category.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    public Product register(ProductRequest request ){

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(()-> new RuntimeException("Categoria não encontrada"));
        Product product = Product.builder()
                .name(request.name())
                .description(request.description())
                .category(category)
                .price(request.price())
                .stock(request.stock())
                .imageUrl(request.imageUrl())
                .build();
        return productRepository.save(product);
    }

    public List<ProductResponse> getAllProducts(){
        return productRepository.findAll()
                .stream()
                .map(productMapper::toResponse).toList();

    }
    public List<ProductResponse> getByCategory(int categoryId){
        return productRepository.findByCategory_Id(categoryId)
                .stream()
                .map(productMapper::toResponse).toList();
    }
    public ProductResponse getProductById(int id){
        Product product = productRepository.findById(id)
                .orElseThrow(()->new RuntimeException("Id de produto não foi achado") );

        return productMapper.toResponse(product);
    }

    public  Product update(int id, ProductRequest request){
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        product.setName(request.name());
        product.setDescription(request.description());
        product.setCategory(category);
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setImageUrl(request.imageUrl());

        return productRepository.save(product);
    }

    public Product updateStock(int id, UpdateStockRequest request){
        Product product  = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
        if(request.stock()<0){
            throw new RuntimeException("Estoque não pode ser negativo");
        }
        product.setStock(request.stock());
        return productRepository.save(product);
    }



    public Product updatePrice(int id, UpdatePriceRequest request ){
        Product product  = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
        product.setPrice(request.price());
        return productRepository.save(product);
    }

    public void deleteProduct(int id){
        if(!productRepository.existsById(id)){
            throw new RuntimeException("Id de produto inexistente inexistente");
        }
        productRepository.deleteById(id);
    }

    public Page<ProductResponse> search(
            String name,
            Integer categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            int page,
            int size,
            String sortBy,
            String direction
    ){
        Specification<Product> spec =
                (root, query, cb) -> cb.conjunction();

        if (name != null && !name.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("name")),
                            "%" + name.toLowerCase() + "%"
                    ));
        }
        if (categoryId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("category").get("id"), categoryId
                    ));
        }

        if (minPrice != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("price"), minPrice
                    ));
        }
        if (maxPrice != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("price"), maxPrice));
        }

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return productRepository.findAll(spec, pageable)
                .map(productMapper::toResponse);
    }

}
