package com.boozebuddies.service.implementation;

import com.boozebuddies.entity.Category;
import com.boozebuddies.repository.CategoryRepository;
import com.boozebuddies.service.CategoryService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl implements CategoryService {
  @Autowired private CategoryRepository categoryRepository;

  @Override
  public List<Category> getAllCategories() {
    return categoryRepository.findAll();
  }

  @Override
  public Category getCategoryById(Long id) {
    if (id == null || id <= 0) {
      throw new IllegalArgumentException("Invalid category ID");
    }
    return categoryRepository.findById(id).orElseThrow();
  }

  @Override
  public Category createCategory(Category category) {
    if (category == null) {
      throw new IllegalArgumentException("Category cannot be null");
    }
    if (category.getName() == null || category.getName().isBlank()) {
      throw new IllegalArgumentException("Category name is required");
    }
    return categoryRepository.save(category);
  }
}
