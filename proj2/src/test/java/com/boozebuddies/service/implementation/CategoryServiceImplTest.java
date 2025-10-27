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

  // ==================== getAllCategories() Tests ====================

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

    Category liquorCategory =
        Category.builder()
            .id(3L)
            .name("Liquor")
            .description("Strong spirits")
            .imageUrl("https://example.com/liquor.jpg")
            .products(new ArrayList<>())
            .build();

    List<Category> categories = List.of(testCategory, wineCategory, liquorCategory);
    when(categoryRepository.findAll()).thenReturn(categories);

    List<Category> result = categoryService.getAllCategories();

    assertNotNull(result);
    assertEquals(3, result.size());
    assertEquals("Beer", result.get(0).getName());
    assertEquals("Wine", result.get(1).getName());
    assertEquals("Liquor", result.get(2).getName());
  }

  // ==================== getCategoryById() Tests ====================

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
        java.util.NoSuchElementException.class, () -> categoryService.getCategoryById(999L));

    verify(categoryRepository, times(1)).findById(999L);
  }

  @Test
  void testGetCategoryById_NullId() {
    assertThrows(IllegalArgumentException.class, () -> categoryService.getCategoryById(null));

    verify(categoryRepository, never()).findById(null);
  }

  @Test
  void testGetCategoryById_ZeroId() {
    assertThrows(IllegalArgumentException.class, () -> categoryService.getCategoryById(0L));

    verify(categoryRepository, never()).findById(0L);
  }

  @Test
  void testGetCategoryById_NegativeId() {
    assertThrows(IllegalArgumentException.class, () -> categoryService.getCategoryById(-1L));

    verify(categoryRepository, never()).findById(-1L);
  }

  @Test
  void testGetCategoryById_LargeId() {
    Long largeId = Long.MAX_VALUE;
    when(categoryRepository.findById(largeId)).thenReturn(Optional.empty());

    assertThrows(
        java.util.NoSuchElementException.class, () -> categoryService.getCategoryById(largeId));

    verify(categoryRepository, times(1)).findById(largeId);
  }

  // ==================== createCategory() Tests ====================

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
  void testCreateCategory_NullCategory() {
    assertThrows(IllegalArgumentException.class, () -> categoryService.createCategory(null));

    verify(categoryRepository, never()).save(any());
  }

  @Test
  void testCreateCategory_NullName() {
    Category categoryWithNullName =
        Category.builder()
            .id(4L)
            .name(null)
            .description("Description")
            .imageUrl("https://example.com/image.jpg")
            .products(new ArrayList<>())
            .build();

    assertThrows(
        IllegalArgumentException.class, () -> categoryService.createCategory(categoryWithNullName));

    verify(categoryRepository, never()).save(any());
  }

  @Test
  void testCreateCategory_EmptyName() {
    Category categoryWithEmptyName =
        Category.builder()
            .id(5L)
            .name("")
            .description("Description")
            .imageUrl("https://example.com/image.jpg")
            .products(new ArrayList<>())
            .build();

    assertThrows(
        IllegalArgumentException.class,
        () -> categoryService.createCategory(categoryWithEmptyName));

    verify(categoryRepository, never()).save(any());
  }

  @Test
  void testCreateCategory_BlankName() {
    Category categoryWithBlankName =
        Category.builder()
            .id(6L)
            .name("   ")
            .description("Description")
            .imageUrl("https://example.com/image.jpg")
            .products(new ArrayList<>())
            .build();

    assertThrows(
        IllegalArgumentException.class,
        () -> categoryService.createCategory(categoryWithBlankName));

    verify(categoryRepository, never()).save(any());
  }

  @Test
  void testCreateCategory_WithNullDescription() {
    Category categoryWithNullDesc =
        Category.builder()
            .id(7L)
            .name("Beer")
            .description(null)
            .imageUrl("https://example.com/image.jpg")
            .products(new ArrayList<>())
            .build();

    when(categoryRepository.save(categoryWithNullDesc)).thenReturn(categoryWithNullDesc);

    Category result = categoryService.createCategory(categoryWithNullDesc);

    assertNotNull(result);
    assertNull(result.getDescription());
    verify(categoryRepository, times(1)).save(categoryWithNullDesc);
  }

  @Test
  void testCreateCategory_WithNullImageUrl() {
    Category categoryWithNullImage =
        Category.builder()
            .id(8L)
            .name("Wine")
            .description("Description")
            .imageUrl(null)
            .products(new ArrayList<>())
            .build();

    when(categoryRepository.save(categoryWithNullImage)).thenReturn(categoryWithNullImage);

    Category result = categoryService.createCategory(categoryWithNullImage);

    assertNotNull(result);
    assertNull(result.getImageUrl());
    verify(categoryRepository, times(1)).save(categoryWithNullImage);
  }

  @Test
  void testCreateCategory_WithSpecialCharactersInName() {
    Category categoryWithSpecialChars =
        Category.builder()
            .id(9L)
            .name("Beer@#$%^&*()")
            .description("Special characters test")
            .imageUrl("https://example.com/image.jpg")
            .products(new ArrayList<>())
            .build();

    when(categoryRepository.save(categoryWithSpecialChars)).thenReturn(categoryWithSpecialChars);

    Category result = categoryService.createCategory(categoryWithSpecialChars);

    assertNotNull(result);
    assertEquals("Beer@#$%^&*()", result.getName());
    verify(categoryRepository, times(1)).save(categoryWithSpecialChars);
  }

  @Test
  void testCreateCategory_RepositoryThrowsException() {
    when(categoryRepository.save(any(Category.class)))
        .thenThrow(new RuntimeException("Database error"));

    assertThrows(RuntimeException.class, () -> categoryService.createCategory(testCategory));

    verify(categoryRepository, times(1)).save(testCategory);
  }

  @Test
  void testCreateCategory_DuplicateNameConstraintViolation() {
    when(categoryRepository.save(any(Category.class)))
        .thenThrow(
            new RuntimeException("Unique constraint violation: Category name already exists"));

    assertThrows(RuntimeException.class, () -> categoryService.createCategory(testCategory));

    verify(categoryRepository, times(1)).save(testCategory);
  }

  @Test
  void testCreateCategory_VeryLongCategoryName() {
    String veryLongName = "B".repeat(500);
    Category categoryWithLongName =
        Category.builder()
            .id(10L)
            .name(veryLongName)
            .description("Long name test")
            .imageUrl("https://example.com/image.jpg")
            .products(new ArrayList<>())
            .build();

    when(categoryRepository.save(categoryWithLongName)).thenReturn(categoryWithLongName);

    Category result = categoryService.createCategory(categoryWithLongName);

    assertNotNull(result);
    assertEquals(500, result.getName().length());
    verify(categoryRepository, times(1)).save(categoryWithLongName);
  }

  @Test
  void testCreateCategory_SingleCharacterName() {
    Category categoryWithSingleChar =
        Category.builder()
            .id(11L)
            .name("B")
            .description("Single character test")
            .imageUrl("https://example.com/image.jpg")
            .products(new ArrayList<>())
            .build();

    when(categoryRepository.save(categoryWithSingleChar)).thenReturn(categoryWithSingleChar);

    Category result = categoryService.createCategory(categoryWithSingleChar);

    assertNotNull(result);
    assertEquals("B", result.getName());
    verify(categoryRepository, times(1)).save(categoryWithSingleChar);
  }
}
