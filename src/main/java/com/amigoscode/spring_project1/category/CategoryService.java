package com.amigoscode.spring_project1.category;

import com.amigoscode.spring_project1.auth.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

     public Category register(CategoryRequest request){
         Category category = Category.builder()
                 .name(request.name())
                 .build();
         return categoryRepository.save(category);
     }
     public List<CategoryResponse> getAll(){
         return categoryRepository.findAll()
                 .stream()
                 .map(category -> new CategoryResponse(
                         category.getName()
                 )).toList();
     }
     public CategoryResponse findById(int id){
         Category category = categoryRepository.findById(id)
                 .orElseThrow(()-> new RuntimeException("Categoria não encontrada"));
         return new CategoryResponse(
                 category.getName()
         );
     }
     public void  deleteCategory(int id){
         if(!categoryRepository.existsById(id)){
             throw new RuntimeException("Id de categoria inexistente");
         }
         categoryRepository.deleteById(id);
     }
}
