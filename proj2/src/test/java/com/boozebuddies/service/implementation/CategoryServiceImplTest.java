package com.boozebuddies.service.implementation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.boozebuddies.entity.Category;
import com.boozebuddies.repository.CategoryRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

  @Mock private CategoryRepository categoryRepository;

  @InjectMocks private CategoryServiceImpl categoryService;

  private Category testCategory;

  @BeforeEach
  void setUp() {
    testCategory =
        Category.builder()
            .id(1L)
            .name("Beer")
            .description("Alcoholic beverages made from grains")
            .imageUrl("https://example.com/beer.jpg")
            .products(new ArrayList<>())
            .build();
  }

  // Tests for getAllCategories()
  @Test
  void testGetAllCategories_Success() {
    List<Category> categories = List.of(testCategory);
    when(categoryRepository.findAll()).thenReturn(categories);

    List<Category> result = categoryService.getAllCategories();

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("Beer", result.get(0).getName());
    verify(categoryRepository, times(1)).findAll();
  }

  @Test
  void testGetAllCategories_EmptyList() {
    when(categoryRepository.findAll()).thenReturn(new ArrayList<>());

    List<Category> result = categoryService.getAllCategories();

    assertNotNull(result);
    assertEquals(0, result.size());
    verify(categoryRepository, times(1)).findAll();
  }

  @Test
  void testGetAllCategories_MultipleCategories() {
    Category wineCategory =
        Category.builder()
            .id(2L)
            .name("Wine")
            .description("Wine beverages")
            .imageUrl("https://example.com/wine.jpg")
            .products(new ArrayList<>())
            .build();

    List<Category> categories = List.of(testCategory, wineCategory);
    when(categoryRepository.findAll()).thenReturn(categories);

    List<Category> result = categoryService.getAllCategories();

    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals("Beer", result.get(0).getName());
    assertEquals("Wine", result.get(1).getName());
  }

  // Tests for getCategoryById()
  @Test
  void testGetCategoryById_Success() {
    when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));

    Category result = categoryService.getCategoryById(1L);

    assertNotNull(result);
    assertEquals("Beer", result.getName());
    assertEquals(1L, result.getId());
    verify(categoryRepository, times(1)).findById(1L);
  }

  @Test
  void testGetCategoryById_NotFound() {
    when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(
        java.util.NoSuchElementException.class,
        () -> categoryService.getCategoryById(999L));

    verify(categoryRepository, times(1)).findById(999L);
  }

  @Test
  void testGetCategoryById_WithDifferentId() {
    Category liquorCategory =
        Category.builder()
            .id(3L)
            .name("Liquor")
            .description("Strong spirits")
            .imageUrl("https://example.com/liquor.jpg")
            .products(new ArrayList<>())
            .build();

    when(categoryRepository.findById(3L)).thenReturn(Optional.of(liquorCategory));

    Category result = categoryService.getCategoryById(3L);

    assertEquals("Liquor", result.getName());
    assertEquals(3L, result.getId());
  }

  // Tests for createCategory()
  @Test
  void testCreateCategory_Success() {
    when(categoryRepository.save(testCategory)).thenReturn(testCategory);

    Category result = categoryService.createCategory(testCategory);

    assertNotNull(result);
    assertEquals("Beer", result.getName());
    assertEquals(1L, result.getId());
    verify(categoryRepository, times(1)).save(testCategory);
  }

  @Test
  void testCreateCategory_WithNewData() {
    Category newCategory =
        Category.builder()
            .name("Vodka")
            .description("Clear distilled spirit")
            .imageUrl("https://example.com/vodka.jpg")
            .products(new ArrayList<>())
            .build();

    Category savedCategory =
        Category.builder()
            .id(4L)
            .name("Vodka")
            .description("Clear distilled spirit")
            .imageUrl("https://example.com/vodka.jpg")
            .products(new ArrayList<>())
            .build();

    when(categoryRepository.save(newCategory)).thenReturn(savedCategory);

    Category result = categoryService.createCategory(newCategory);

    assertNotNull(result);
    assertEquals("Vodka", result.getName());
    assertEquals(4L, result.getId());
  }

  @Test
  void testCreateCategory_SaveThrowsException() {
    when(categoryRepository.save(any(Category.class)))
        .thenThrow(new RuntimeException("Database error"));

    assertThrows(RuntimeException.class, () -> categoryService.createCategory(testCategory));

    verify(categoryRepository, times(1)).save(any(Category.class));
  }

  // Edge Case Tests
  @Test
  void testCreateCategory_NullCategory() {
    assertThrows(IllegalArgumentException.class, () -> categoryService.createCategory(null));
  }

  @Test
  void testCreateCategory_DuplicateNameThrowsException() {
    when(categoryRepository.save(any(Category.class)))
        .thenThrow(
            new RuntimeException("Unique constraint violation: Category name already exists"));

    assertThrows(RuntimeException.class, () -> categoryService.createCategory(testCategory));

    verify(categoryRepository, times(1)).save(any(Category.class));
  }

  @Test
  void testGetCategoryById_NegativeId() {
    when(categoryRepository.findById(-1L)).thenReturn(Optional.empty());

    assertThrows(
        java.util.NoSuchElementException.class, () -> categoryService.getCategoryById(-1L));

    verify(categoryRepository, times(1)).findById(-1L);
  }

  @Test
  void testGetCategoryById_ZeroId() {
    when(categoryRepository.findById(0L)).thenReturn(Optional.empty());

    assertThrows(
        java.util.NoSuchElementException.class, () -> categoryService.getCategoryById(0L));

    verify(categoryRepository, times(1)).findById(0L);
  }

  @Test
  void testGetCategoryById_LargeId() {
    Long largeId = Long.MAX_VALUE;
    when(categoryRepository.findById(largeId)).thenReturn(Optional.empty());

    assertThrows(
        java.util.NoSuchElementException.class, () -> categoryService.getCategoryById(largeId));

    verify(categoryRepository, times(1)).findById(largeId);
  }

  @Test
  void testCreateCategory_WithSpecialCharactersInName() {
    Category categoryWithSpecialChars =
        Category.builder()
            .id(5L)
            .name("Beer@#$%^&*()")
            .description("Special characters test")
            .imageUrl("https://example.com/image.jpg")
            .products(new ArrayList<>())
            .build();

    when(categoryRepository.save(any(Category.class))).thenReturn(categoryWithSpecialChars);

    Category result = categoryService.createCategory(categoryWithSpecialChars);

    assertNotNull(result);
    assertEquals("Beer@#$%^&*()", result.getName());
  }

  @Test
  void testCreateCategory_WithNullDescription() {
    Category categoryWithNullDesc =
        Category.builder()
            .id(6L)
            .name("Beer")
            .description(null)
            .imageUrl("https://example.com/image.jpg")
            .products(new ArrayList<>())
            .build();

    when(categoryRepository.save(categoryWithNullDesc)).thenReturn(categoryWithNullDesc);

    Category result = categoryService.createCategory(categoryWithNullDesc);

    assertNotNull(result);
    assertNull(result.getDescription());
  }

  @Test
  void testCreateCategory_WithNullImageUrl() {
    Category categoryWithNullImage =
        Category.builder()
            .id(7L)
            .name("Beer")
            .description("Description")
            .imageUrl(null)
            .products(new ArrayList<>())
            .build();

    when(categoryRepository.save(categoryWithNullImage)).thenReturn(categoryWithNullImage);

    Category result = categoryService.createCategory(categoryWithNullImage);

    assertNotNull(result);
    assertNull(result.getImageUrl());
  }
}