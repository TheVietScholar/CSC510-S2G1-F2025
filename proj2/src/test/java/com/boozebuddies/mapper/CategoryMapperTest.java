package com.boozebuddies.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.boozebuddies.dto.CategoryDTO;
import com.boozebuddies.entity.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CategoryMapperTest {
  private CategoryMapper categoryMapper;
  private Category testCategory;
  private CategoryDTO testCategoryDTO;

  @BeforeEach
  public void setUp() {
    categoryMapper = new CategoryMapper();

    testCategory =
        Category.builder()
            .id(1L)
            .name("Test Category")
            .description("This is a test category")
            .imageUrl("http://example.com/image.jpg")
            .build();

    testCategoryDTO =
        CategoryDTO.builder()
            .id(1L)
            .name("Test Category")
            .description("This is a test category")
            .imageUrl("http://example.com/image.jpg")
            .build();
  }

  @Test
  public void testCategoryToCategoryDTO() {
    CategoryDTO dto = categoryMapper.toDTO(testCategory);

    assertNotNull(dto);
    assertEquals(testCategory.getId(), dto.getId());
    assertEquals(testCategory.getName(), dto.getName());
    assertEquals(testCategory.getDescription(), dto.getDescription());
    assertEquals(testCategory.getImageUrl(), dto.getImageUrl());

    assertEquals(testCategoryDTO.getId(), dto.getId());
    assertEquals(testCategoryDTO.getName(), dto.getName());
    assertEquals(testCategoryDTO.getDescription(), dto.getDescription());
    assertEquals(testCategoryDTO.getImageUrl(), dto.getImageUrl());
  }

  @Test
  public void testCategoryDTOToCategory() {
    Category entity = categoryMapper.toEntity(testCategoryDTO);

    assertNotNull(entity);
    assertEquals(testCategory.getId(), entity.getId());
    assertEquals(testCategory.getName(), entity.getName());
    assertEquals(testCategory.getDescription(), entity.getDescription());
    assertEquals(testCategory.getImageUrl(), entity.getImageUrl());

    assertEquals(testCategoryDTO.getId(), entity.getId());
    assertEquals(testCategoryDTO.getName(), entity.getName());
    assertEquals(testCategoryDTO.getDescription(), entity.getDescription());
    assertEquals(testCategoryDTO.getImageUrl(), entity.getImageUrl());
  }

  @Test
  public void testNullCategoryToCategoryDTO() {
    CategoryDTO dto = categoryMapper.toDTO(null);
    assertNull(dto);
  }

  @Test
  public void testNullCategoryDTOToCategory() {
    Category entity = categoryMapper.toEntity(null);
    assertNull(entity);
  }
}
