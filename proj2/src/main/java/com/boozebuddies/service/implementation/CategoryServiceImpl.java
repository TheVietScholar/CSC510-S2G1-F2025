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
    return categoryRepository.findById(id).orElseThrow();
  }

  @Override
  public Category createCategory(Category category) {
    if (category == null) {
        throw new IllegalArgumentException("Category cannot be null");
    }
    return categoryRepository.save(category);
  }
}