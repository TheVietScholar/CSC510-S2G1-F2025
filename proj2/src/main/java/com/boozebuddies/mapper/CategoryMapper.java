package com.boozebuddies.mapper;

import com.boozebuddies.dto.CategoryDTO;
import com.boozebuddies.entity.Category;
import org.springframework.stereotype.Component;

/** Mapper for converting between Category entities and CategoryDTO objects. */
@Component
public class CategoryMapper {
  /**
   * Converts a Category entity to a CategoryDTO.
   *
   * @param category the category entity to convert
   * @return the CategoryDTO, or null if the input is null
   */
  public CategoryDTO toDTO(Category category) {
    if (category == null) return null;

    return CategoryDTO.builder()
        .id(category.getId())
        .name(category.getName())
        .description(category.getDescription())
        .imageUrl(category.getImageUrl())
        .build();
  }

  /**
   * Converts a CategoryDTO to a Category entity.
   *
   * @param categoryDTO the CategoryDTO to convert
   * @return the Category entity, or null if the input is null
   */
  public Category toEntity(CategoryDTO categoryDTO) {
    if (categoryDTO == null) return null;

    Category category = new Category();
    category.setId(categoryDTO.getId());
    category.setName(categoryDTO.getName());
    category.setDescription(categoryDTO.getDescription());
    category.setImageUrl(categoryDTO.getImageUrl());
    return category;
  }
}
