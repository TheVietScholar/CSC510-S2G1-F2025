package com.boozebuddies.service;

import com.boozebuddies.entity.Category;
import java.util.List;

/**
 * Service interface for managing product or item categories within the BoozeBuddies system.
 * Provides methods for retrieving, creating, and managing categories used by merchants and
 * products.
 */
public interface CategoryService {

  /**
   * Retrieves all categories available in the system.
   *
   * @return A list of all {@link Category} objects.
   */
  List<Category> getAllCategories();

  /**
   * Retrieves a specific category by its unique identifier.
   *
   * @param id The ID of the category to retrieve.
   * @return The {@link Category} object if found, or {@code null} if no such category exists.
   */
  Category getCategoryById(Long id);

  /**
   * Creates a new category and saves it to the database.
   *
   * @param category The {@link Category} object to create.
   * @return The newly created {@link Category} with an assigned ID.
   */
  Category createCategory(Category category);
}
