package com.boozebuddies.controller;

import com.boozebuddies.dto.ApiResponse;
import com.boozebuddies.dto.CategoryDTO;
import com.boozebuddies.entity.Category;
import com.boozebuddies.mapper.CategoryMapper;
import com.boozebuddies.security.annotation.RoleAnnotations.*;
import com.boozebuddies.service.CategoryService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** REST controller for managing product categories. */
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

  private final CategoryService categoryService;
  private final CategoryMapper categoryMapper;

  /**
   * Constructor injection for category service and mapper.
   *
   * @param categoryService the category service
   * @param categoryMapper the category mapper
   */
  @Autowired
  public CategoryController(CategoryService categoryService, CategoryMapper categoryMapper) {
    this.categoryService = categoryService;
    this.categoryMapper = categoryMapper;
  }

  // ==================== RETRIEVE ====================

  /**
   * Retrieves all categories.
   *
   * @return a list of all categories
   */
  @GetMapping
  public ResponseEntity<ApiResponse<List<CategoryDTO>>> getAllCategories() {
    try {
      List<Category> categories = categoryService.getAllCategories();
      List<CategoryDTO> dtos = categories.stream().map(categoryMapper::toDTO).toList();
      return ResponseEntity.ok(ApiResponse.success(dtos, "Categories retrieved successfully"));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error("Failed to retrieve categories"));
    }
  }

  /**
   * Retrieves a category by ID.
   *
   * @param id the category ID
   * @return the category with the specified ID
   */
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<CategoryDTO>> getCategoryById(@PathVariable Long id) {
    try {
      Category category = categoryService.getCategoryById(id);
      CategoryDTO dto = categoryMapper.toDTO(category);
      return ResponseEntity.ok(ApiResponse.success(dto, "Category retrieved successfully"));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
    } catch (NoSuchElementException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error("An error occurred retrieving category"));
    }
  }

  // ==================== CREATE ====================

  /**
   * Creates a new category. Admin only.
   *
   * @param dto the category data
   * @return the created category
   */
  @PostMapping
  @IsAdmin
  public ResponseEntity<ApiResponse<CategoryDTO>> createCategory(
      @Valid @RequestBody CategoryDTO dto) {
    try {
      Category category = categoryMapper.toEntity(dto);
      Category saved = categoryService.createCategory(category);
      CategoryDTO savedDto = categoryMapper.toDTO(saved);
      return ResponseEntity.status(HttpStatus.CREATED)
          .body(ApiResponse.success(savedDto, "Category created successfully"));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error("An error occurred creating category"));
    }
  }
}
