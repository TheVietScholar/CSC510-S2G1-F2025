package com.boozebuddies.mapper;

import org.springframework.stereotype.Component;
import com.boozebuddies.dto.CategoryDTO;
import com.boozebuddies.entity.Category;

@Component
public class CategoryMapper {
    public CategoryDTO toDTO(Category category) {
        if (category == null) return null;

        return CategoryDTO.builder()
            .id(category.getId())
            .name(category.getName())
            .description(category.getDescription())
            .imageUrl(category.getImageUrl())
            .build();
    }

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
