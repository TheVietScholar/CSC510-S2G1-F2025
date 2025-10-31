package com.boozebuddies.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.boozebuddies.config.TestSecurityConfig;
import com.boozebuddies.dto.CategoryDTO;
import com.boozebuddies.entity.Category;
import com.boozebuddies.mapper.CategoryMapper;
import com.boozebuddies.security.JwtAuthenticationFilter;
import com.boozebuddies.service.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    controllers = CategoryController.class,
    excludeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = JwtAuthenticationFilter.class))
@AutoConfigureMockMvc(addFilters = false) // ⛔ disables all Spring Security filters
@Import(TestSecurityConfig.class) // ✅ imports your test security config
@DisplayName("CategoryController Tests")
class CategoryControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private CategoryService categoryService;

  @MockBean private CategoryMapper categoryMapper;

  private Category testCategory;
  private CategoryDTO testCategoryDTO;

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

    testCategoryDTO =
        CategoryDTO.builder()
            .id(1L)
            .name("Beer")
            .description("Alcoholic beverages made from grains")
            .imageUrl("https://example.com/beer.jpg")
            .build();
  }

  // ==================== getAllCategories() Tests ====================

  @Test
  @DisplayName(
      "GET /api/categories should return 200 with ApiResponse containing list of categories")
  void testGetAllCategories_Success() throws Exception {
    List<Category> categories = List.of(testCategory);
    when(categoryService.getAllCategories()).thenReturn(categories);
    when(categoryMapper.toDTO(testCategory)).thenReturn(testCategoryDTO);

    mockMvc
        .perform(get("/api/categories"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Categories retrieved successfully"))
        .andExpect(jsonPath("$.data[0].id").value(1L))
        .andExpect(jsonPath("$.data[0].name").value("Beer"))
        .andExpect(jsonPath("$.data[0].description").value("Alcoholic beverages made from grains"));

    verify(categoryService, times(1)).getAllCategories();
    verify(categoryMapper, times(1)).toDTO(testCategory);
  }

  @Test
  @DisplayName("GET /api/categories should return 200 with empty list")
  void testGetAllCategories_EmptyList() throws Exception {
    when(categoryService.getAllCategories()).thenReturn(new ArrayList<>());

    mockMvc
        .perform(get("/api/categories"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Categories retrieved successfully"))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data.length()").value(0));

    verify(categoryService, times(1)).getAllCategories();
  }

  @Test
  @DisplayName("GET /api/categories should return 200 with three fixed categories")
  void testGetAllCategories_ThreeCategories() throws Exception {
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

    CategoryDTO wineDTO =
        CategoryDTO.builder()
            .id(2L)
            .name("Wine")
            .description("Wine beverages")
            .imageUrl("https://example.com/wine.jpg")
            .build();

    CategoryDTO liquorDTO =
        CategoryDTO.builder()
            .id(3L)
            .name("Liquor")
            .description("Strong spirits")
            .imageUrl("https://example.com/liquor.jpg")
            .build();

    List<Category> categories = List.of(testCategory, wineCategory, liquorCategory);
    when(categoryService.getAllCategories()).thenReturn(categories);
    when(categoryMapper.toDTO(testCategory)).thenReturn(testCategoryDTO);
    when(categoryMapper.toDTO(wineCategory)).thenReturn(wineDTO);
    when(categoryMapper.toDTO(liquorCategory)).thenReturn(liquorDTO);

    mockMvc
        .perform(get("/api/categories"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Categories retrieved successfully"))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data.length()").value(3))
        .andExpect(jsonPath("$.data[0].name").value("Beer"))
        .andExpect(jsonPath("$.data[1].name").value("Wine"))
        .andExpect(jsonPath("$.data[2].name").value("Liquor"));
  }

  @Test
  @DisplayName("GET /api/categories should handle service exception")
  void testGetAllCategories_ServiceThrowsException() throws Exception {
    when(categoryService.getAllCategories()).thenThrow(new RuntimeException("Database error"));

    mockMvc
        .perform(get("/api/categories"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Failed to retrieve categories"));

    verify(categoryService, times(1)).getAllCategories();
  }

  // ==================== getCategoryById() Tests ====================

  @Test
  @DisplayName("GET /api/categories/{id} should return 200 with category data")
  void testGetCategoryById_Success() throws Exception {
    when(categoryService.getCategoryById(1L)).thenReturn(testCategory);
    when(categoryMapper.toDTO(testCategory)).thenReturn(testCategoryDTO);

    mockMvc
        .perform(get("/api/categories/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Category retrieved successfully"))
        .andExpect(jsonPath("$.data.id").value(1L))
        .andExpect(jsonPath("$.data.name").value("Beer"))
        .andExpect(jsonPath("$.data.description").value("Alcoholic beverages made from grains"));

    verify(categoryService, times(1)).getCategoryById(1L);
    verify(categoryMapper, times(1)).toDTO(testCategory);
  }

  @Test
  @DisplayName("GET /api/categories/{id} should return 400 when category not found")
  void testGetCategoryById_NotFound() throws Exception {
    when(categoryService.getCategoryById(999L))
        .thenThrow(new java.util.NoSuchElementException("Category not found"));

    mockMvc
        .perform(get("/api/categories/999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Category not found"))
        .andExpect(jsonPath("$.data").doesNotExist());

    verify(categoryService, times(1)).getCategoryById(999L);
  }

  @Test
  @DisplayName("GET /api/categories/{id} should return 400 when ID is invalid")
  void testGetCategoryById_InvalidId() throws Exception {
    when(categoryService.getCategoryById(0L))
        .thenThrow(new IllegalArgumentException("Invalid category ID"));

    mockMvc
        .perform(get("/api/categories/0"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Invalid category ID"))
        .andExpect(jsonPath("$.data").doesNotExist());

    verify(categoryService, times(1)).getCategoryById(0L);
  }

  @Test
  @DisplayName("GET /api/categories/{id} should return 400 when ID is negative")
  void testGetCategoryById_NegativeId() throws Exception {
    when(categoryService.getCategoryById(-1L))
        .thenThrow(new IllegalArgumentException("Invalid category ID"));

    mockMvc
        .perform(get("/api/categories/-1"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Invalid category ID"))
        .andExpect(jsonPath("$.data").doesNotExist());

    verify(categoryService, times(1)).getCategoryById(-1L);
  }

  @Test
  @DisplayName("GET /api/categories/{id} should handle service exception")
  void testGetCategoryById_ServiceThrowsException() throws Exception {
    when(categoryService.getCategoryById(1L)).thenThrow(new RuntimeException("Database error"));

    mockMvc
        .perform(get("/api/categories/1"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("An error occurred retrieving category"))
        .andExpect(jsonPath("$.data").doesNotExist());

    verify(categoryService, times(1)).getCategoryById(1L);
  }

  @Test
  @DisplayName("GET /api/categories/{id} should retrieve different categories")
  void testGetCategoryById_DifferentCategories() throws Exception {
    Category wineCategory =
        Category.builder()
            .id(2L)
            .name("Wine")
            .description("Wine beverages")
            .imageUrl("https://example.com/wine.jpg")
            .products(new ArrayList<>())
            .build();

    CategoryDTO wineDTO =
        CategoryDTO.builder()
            .id(2L)
            .name("Wine")
            .description("Wine beverages")
            .imageUrl("https://example.com/wine.jpg")
            .build();

    when(categoryService.getCategoryById(2L)).thenReturn(wineCategory);
    when(categoryMapper.toDTO(wineCategory)).thenReturn(wineDTO);

    mockMvc
        .perform(get("/api/categories/2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Category retrieved successfully"))
        .andExpect(jsonPath("$.data.id").value(2L))
        .andExpect(jsonPath("$.data.name").value("Wine"))
        .andExpect(jsonPath("$.data.description").value("Wine beverages"));

    verify(categoryService, times(1)).getCategoryById(2L);
  }

  // ==================== createCategory() Tests ====================

  @Test
  @DisplayName("POST /api/categories should return 201 on successful creation")
  void testCreateCategory_Success() throws Exception {
    when(categoryMapper.toEntity(testCategoryDTO)).thenReturn(testCategory);
    when(categoryService.createCategory(testCategory)).thenReturn(testCategory);
    when(categoryMapper.toDTO(testCategory)).thenReturn(testCategoryDTO);

    mockMvc
        .perform(
            post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCategoryDTO)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Category created successfully"))
        .andExpect(jsonPath("$.data.id").value(1L))
        .andExpect(jsonPath("$.data.name").value("Beer"))
        .andExpect(jsonPath("$.data.description").value("Alcoholic beverages made from grains"));

    verify(categoryMapper, times(1)).toEntity(testCategoryDTO);
    verify(categoryService, times(1)).createCategory(testCategory);
    verify(categoryMapper, times(1)).toDTO(testCategory);
  }

  @Test
  @DisplayName("POST /api/categories should return 400 when name is null")
  void testCreateCategory_NullName() throws Exception {
    CategoryDTO nullNameDTO =
        CategoryDTO.builder()
            .name(null)
            .description("Description")
            .imageUrl("https://example.com/image.jpg")
            .build();

    Category categoryWithNullName =
        Category.builder()
            .name(null)
            .description("Description")
            .imageUrl("https://example.com/image.jpg")
            .products(new ArrayList<>())
            .build();

    when(categoryMapper.toEntity(nullNameDTO)).thenReturn(categoryWithNullName);
    when(categoryService.createCategory(categoryWithNullName))
        .thenThrow(new IllegalArgumentException("Category name is required"));

    mockMvc
        .perform(
            post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nullNameDTO)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Category name is required"))
        .andExpect(jsonPath("$.data").doesNotExist());

    verify(categoryService, times(1)).createCategory(any());
  }

  @Test
  @DisplayName("POST /api/categories should return 400 when name is empty")
  void testCreateCategory_EmptyName() throws Exception {
    CategoryDTO emptyNameDTO =
        CategoryDTO.builder()
            .name("")
            .description("Description")
            .imageUrl("https://example.com/image.jpg")
            .build();

    Category categoryWithEmptyName =
        Category.builder()
            .name("")
            .description("Description")
            .imageUrl("https://example.com/image.jpg")
            .products(new ArrayList<>())
            .build();

    when(categoryMapper.toEntity(emptyNameDTO)).thenReturn(categoryWithEmptyName);
    when(categoryService.createCategory(categoryWithEmptyName))
        .thenThrow(new IllegalArgumentException("Category name is required"));

    mockMvc
        .perform(
            post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyNameDTO)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Category name is required"))
        .andExpect(jsonPath("$.data").doesNotExist());

    verify(categoryService, times(1)).createCategory(any());
  }

  @Test
  @DisplayName("POST /api/categories should return 400 when name is blank")
  void testCreateCategory_BlankName() throws Exception {
    CategoryDTO blankNameDTO =
        CategoryDTO.builder()
            .name("   ")
            .description("Description")
            .imageUrl("https://example.com/image.jpg")
            .build();

    Category categoryWithBlankName =
        Category.builder()
            .name("   ")
            .description("Description")
            .imageUrl("https://example.com/image.jpg")
            .products(new ArrayList<>())
            .build();

    when(categoryMapper.toEntity(blankNameDTO)).thenReturn(categoryWithBlankName);
    when(categoryService.createCategory(categoryWithBlankName))
        .thenThrow(new IllegalArgumentException("Category name is required"));

    mockMvc
        .perform(
            post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(blankNameDTO)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Category name is required"))
        .andExpect(jsonPath("$.data").doesNotExist());

    verify(categoryService, times(1)).createCategory(any());
  }

  @Test
  @DisplayName("POST /api/categories should return 400 when category is null")
  void testCreateCategory_NullCategory() throws Exception {
    when(categoryService.createCategory(null))
        .thenThrow(new IllegalArgumentException("Category cannot be null"));

    mockMvc
        .perform(post("/api/categories").contentType(MediaType.APPLICATION_JSON).content("null"))
        .andExpect(status().is4xxClientError());
  }

  @Test
  @DisplayName("POST /api/categories should handle duplicate name")
  void testCreateCategory_DuplicateName() throws Exception {
    when(categoryMapper.toEntity(testCategoryDTO)).thenReturn(testCategory);
    when(categoryService.createCategory(testCategory))
        .thenThrow(
            new RuntimeException("Unique constraint violation: Category name already exists"));

    mockMvc
        .perform(
            post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCategoryDTO)))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("An error occurred creating category"))
        .andExpect(jsonPath("$.data").doesNotExist());

    verify(categoryService, times(1)).createCategory(testCategory);
  }

  @Test
  @DisplayName("POST /api/categories should handle database error")
  void testCreateCategory_DatabaseError() throws Exception {
    when(categoryMapper.toEntity(testCategoryDTO)).thenReturn(testCategory);
    when(categoryService.createCategory(testCategory))
        .thenThrow(new RuntimeException("Database connection error"));

    mockMvc
        .perform(
            post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCategoryDTO)))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("An error occurred creating category"))
        .andExpect(jsonPath("$.data").doesNotExist());
  }

  @Test
  @DisplayName("POST /api/categories should accept null description")
  void testCreateCategory_NullDescription() throws Exception {
    CategoryDTO nullDescDTO =
        CategoryDTO.builder()
            .name("Beer")
            .description(null)
            .imageUrl("https://example.com/image.jpg")
            .build();

    Category categoryWithNullDesc =
        Category.builder()
            .id(1L)
            .name("Beer")
            .description(null)
            .imageUrl("https://example.com/image.jpg")
            .products(new ArrayList<>())
            .build();

    CategoryDTO resultDTO =
        CategoryDTO.builder()
            .id(1L)
            .name("Beer")
            .description(null)
            .imageUrl("https://example.com/image.jpg")
            .build();

    when(categoryMapper.toEntity(nullDescDTO)).thenReturn(categoryWithNullDesc);
    when(categoryService.createCategory(categoryWithNullDesc)).thenReturn(categoryWithNullDesc);
    when(categoryMapper.toDTO(categoryWithNullDesc)).thenReturn(resultDTO);

    mockMvc
        .perform(
            post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nullDescDTO)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Category created successfully"))
        .andExpect(jsonPath("$.data.id").value(1L))
        .andExpect(jsonPath("$.data.name").value("Beer"))
        .andExpect(jsonPath("$.data.description").doesNotExist());
  }

  @Test
  @DisplayName("POST /api/categories should accept null image URL")
  void testCreateCategory_NullImageUrl() throws Exception {
    CategoryDTO nullImageDTO =
        CategoryDTO.builder().name("Wine").description("Wine beverages").imageUrl(null).build();

    Category categoryWithNullImage =
        Category.builder()
            .id(2L)
            .name("Wine")
            .description("Wine beverages")
            .imageUrl(null)
            .products(new ArrayList<>())
            .build();

    CategoryDTO resultDTO =
        CategoryDTO.builder()
            .id(2L)
            .name("Wine")
            .description("Wine beverages")
            .imageUrl(null)
            .build();

    when(categoryMapper.toEntity(nullImageDTO)).thenReturn(categoryWithNullImage);
    when(categoryService.createCategory(categoryWithNullImage)).thenReturn(categoryWithNullImage);
    when(categoryMapper.toDTO(categoryWithNullImage)).thenReturn(resultDTO);

    mockMvc
        .perform(
            post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nullImageDTO)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Category created successfully"))
        .andExpect(jsonPath("$.data.id").value(2L))
        .andExpect(jsonPath("$.data.name").value("Wine"))
        .andExpect(jsonPath("$.data.imageUrl").doesNotExist());
  }

  @Test
  @DisplayName("POST /api/categories should handle malformed JSON")
  void testCreateCategory_MalformedJSON() throws Exception {
    mockMvc
        .perform(
            post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
        .andExpect(status().is4xxClientError());
  }

  @Test
  @DisplayName("POST /api/categories should handle wrong content type")
  void testCreateCategory_WrongContentType() throws Exception {
    mockMvc
        .perform(post("/api/categories").contentType(MediaType.TEXT_PLAIN).content("invalid"))
        .andExpect(status().is4xxClientError());
  }

  @Test
  @DisplayName("POST /api/categories should accept special characters in name")
  void testCreateCategory_SpecialCharactersInName() throws Exception {
    CategoryDTO specialCharDTO =
        CategoryDTO.builder()
            .name("Beer@#$")
            .description("Description")
            .imageUrl("https://example.com/image.jpg")
            .build();

    Category categoryWithSpecialChars =
        Category.builder()
            .id(4L)
            .name("Beer@#$")
            .description("Description")
            .imageUrl("https://example.com/image.jpg")
            .products(new ArrayList<>())
            .build();

    CategoryDTO resultDTO =
        CategoryDTO.builder()
            .id(4L)
            .name("Beer@#$")
            .description("Description")
            .imageUrl("https://example.com/image.jpg")
            .build();

    when(categoryMapper.toEntity(specialCharDTO)).thenReturn(categoryWithSpecialChars);
    when(categoryService.createCategory(categoryWithSpecialChars))
        .thenReturn(categoryWithSpecialChars);
    when(categoryMapper.toDTO(categoryWithSpecialChars)).thenReturn(resultDTO);

    mockMvc
        .perform(
            post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(specialCharDTO)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Category created successfully"))
        .andExpect(jsonPath("$.data.id").value(4L))
        .andExpect(jsonPath("$.data.name").value("Beer@#$"))
        .andExpect(jsonPath("$.data.description").value("Description"))
        .andExpect(jsonPath("$.data.imageUrl").value("https://example.com/image.jpg"));

    verify(categoryMapper, times(1)).toEntity(specialCharDTO);
    verify(categoryService, times(1)).createCategory(categoryWithSpecialChars);
    verify(categoryMapper, times(1)).toDTO(categoryWithSpecialChars);
  }
}
