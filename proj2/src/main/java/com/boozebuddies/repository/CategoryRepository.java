package com.boozebuddies.repository;

import com.boozebuddies.entity.Category;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing {@link Category} entities within the BoozeBuddies system.
 *
 * <p>This interface provides CRUD operations and custom query methods for accessing category data
 * stored in the database. It extends {@link JpaRepository}, which includes standard persistence
 * methods such as {@code save}, {@code delete}, and {@code findAll}.
 *
 * <p>Custom query methods include:
 *
 * <ul>
 *   <li>{@link #findById(Long)} - Retrieves a category by its unique ID.
 *   <li>{@link #findAll()} - Returns a list of all categories in the system.
 *   <li>{@link #findByName(String)} - Finds a category by its name.
 * </ul>
 *
 * This repository is typically used by service classes to perform database operations related to
 * product categorization and filtering.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

  /** Retrieves a category by its unique identifier. */
  Optional<Category> findById(Long id);

  /** Returns a list of all available categories. */
  List<Category> findAll();

  /** Finds a category by its name. */
  Optional<Category> findByName(String name);
}
