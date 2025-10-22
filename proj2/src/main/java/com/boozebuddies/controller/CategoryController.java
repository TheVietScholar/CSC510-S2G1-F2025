package com.boozebuddies.controller;

import com.boozebuddies.dto.CategoryDTO;
import com.boozebuddies.entity.Category;
import com.boozebuddies.mapper.CategoryMapper;
import com.boozebuddies.service.CategoryService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

  private final CategoryService categoryService;
  private final CategoryMapper categoryMapper;

  @Autowired
  public CategoryController(CategoryService categoryService, CategoryMapper categoryMapper) {
    this.categoryService = categoryService;
    this.categoryMapper = categoryMapper;
  }

  // ==================== RETRIEVE ====================

  @GetMapping
  public ResponseEntity<List<CategoryDTO>> getAllCategories() {
    try {
      List<Category> categories = categoryService.getAllCategories();
      List<CategoryDTO> dtos = categories.stream().map(categoryMapper::toDTO).toList();
      return ResponseEntity.ok(dtos);
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> getCategoryById(@PathVariable Long id) {
    try {
      Category category = categoryService.getCategoryById(id);
      return ResponseEntity.ok(categoryMapper.toDTO(category));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.badRequest().body("An error occurred retrieving category");
    }
  }

  // ==================== CREATE ====================

  @PostMapping
  public ResponseEntity<?> createCategory(@Valid @RequestBody CategoryDTO dto) {
    try {
      Category category = categoryMapper.toEntity(dto);
      Category saved = categoryService.createCategory(category);
      return ResponseEntity.status(HttpStatus.CREATED).body(categoryMapper.toDTO(saved));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.badRequest().body("An error occurred creating category");
    }
  }
}