package com.boozebuddies.service.implementation;

import com.boozebuddies.dto.CategoryDTO;
import com.boozebuddies.entity.Category;
import com.boozebuddies.mapper.CategoryMapper;
import com.boozebuddies.repository.CategoryRepository;
import com.boozebuddies.service.CategoryService;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl implements CategoryService {
  @Autowired private CategoryRepository categoryRepository;

  @Autowired private CategoryMapper categoryMapper;

  @Override
  public List<CategoryDTO> getAllCategories() {
    return categoryRepository.findAll().stream()
        .map(categoryMapper::toDTO)
        .collect(Collectors.toList());
  }

  @Override
  public CategoryDTO getCategoryById(Long id) {
    return categoryRepository.findById(id).map(categoryMapper::toDTO).orElseThrow();
  }

  @Override
  public CategoryDTO createCategory(CategoryDTO dto) {
    Category category = categoryMapper.toEntity(dto);
    Category saved = categoryRepository.save(category);
    return categoryMapper.toDTO(saved);
  }
}
