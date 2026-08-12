package com.amigoscode.spring_project1.category;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryService categoryService;
    private Category category;


    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id(1)
                .name("Eletrônicos")
                .build();
    }

    // Register

    @Test
    void shouldRegisterNewCategory(){
        CategoryRequest request = new CategoryRequest("Eletrônicos");

        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        Category result = categoryService.register(request);
        assertThat(result.getName()).isEqualTo("Eletrônicos");
        verify(categoryRepository).save(any(Category.class));
    }

    // GetAll
    @Test
    void shouldReturnAllCategories(){
        when(categoryRepository.findAll()).thenReturn(List.of(category));
        when(categoryMapper.toResponse(category)).thenReturn(new CategoryResponse(
                "Eletrônicos"
        ));
        List<CategoryResponse> result = categoryService.getAll();
        assertThat(result.get(0).name()).isEqualTo("Eletrônicos");
        assertThat(result).hasSize(1);


    }

    // FindById

    @Test
    void shouldReturnCategoryWhenIdExists(){
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(categoryMapper.toResponse(category)).thenReturn(new CategoryResponse(
                "Eletrônicos"
        ));

        CategoryResponse result = categoryService.findById(1);
        assertThat(result.name()).isEqualTo("Eletrônicos");


    }
    @Test
    void shouldThrowExceptionWhenCategoryIdDoesNotExists(){
        when(categoryRepository.findById(99)).thenReturn(Optional.empty());
       assertThatThrownBy(()-> categoryService.findById(99))
               .isInstanceOf(RuntimeException.class)
               .hasMessageContaining("não encontrada");
    }

    // DeleteCategory
    @Test
    void shouldDeleteCategoryWhenIdExists(){
        when(categoryRepository.existsById(1)).thenReturn(true);
        categoryService.deleteCategory(1);
        verify(categoryRepository).deleteById(1);

    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentCategory(){
        when(categoryRepository.existsById(99)).thenReturn(false);
        assertThatThrownBy(()-> categoryService.deleteCategory(99))
                .isInstanceOf(RuntimeException.class);
        verify(categoryRepository, never()).deleteById(anyInt());
    }

}
