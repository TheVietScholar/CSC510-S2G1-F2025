package com.boozebuddies.service.implementation;

import com.boozebuddies.entity.Category;
import com.boozebuddies.repository.CategoryRepository;
import com.boozebuddies.service.CategoryService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation of the {@link CategoryService} interface.
 *
 * <p>This service handles operations related to product or drink categories in the BoozeBuddies
 * system, including retrieval, creation, and validation of category data.
 */
@Service
public class CategoryServiceImpl implements CategoryService {

  @Autowired private CategoryRepository categoryRepository;

  /**
   * Retrieves all available categories from the database.
   *
   * @return a list of all {@link Category} entities stored in the repository.
   */
  @Override
  public List<Category> getAllCategories() {
    return categoryRepository.findAll();
  }

  /**
   * Retrieves a category by its unique identifier.
   *
   * @param id the unique ID of the category to retrieve.
   * @return the {@link Category} associated with the given ID.
   * @throws IllegalArgumentException if the ID is null or less than or equal to zero.
   * @throws java.util.NoSuchElementException if no category is found with the given ID.
   */
  @Override
  public Category getCategoryById(Long id) {
    if (id == null || id <= 0) {
      throw new IllegalArgumentException("Invalid category ID");
    }
    return categoryRepository.findById(id).orElseThrow();
  }

  /**
   * Creates and saves a new category in the system.
   *
   * @param category the {@link Category} object containing category details to be saved.
   * @return the newly created {@link Category} entity.
   * @throws IllegalArgumentException if the category is null or its name is missing or blank.
   */
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
