package com.boozebuddies.repository;

import com.boozebuddies.entity.Merchant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing {@link Merchant} entities within the BoozeBuddies system.
 *
 * <p>Provides CRUD operations through {@link JpaRepository}, along with custom query methods to
 * search, filter, and analyze merchant information such as activity status, cuisine type, and
 * ratings.
 *
 * <p>Custom query methods include:
 *
 * <ul>
 *   <li>{@link #findByName(String)} - Finds a merchant by exact name.
 *   <li>{@link #findByCuisineType(String)} - Retrieves merchants by cuisine type.
 *   <li>{@link #findByIsActive(boolean)} - Finds merchants by their active/inactive status.
 *   <li>{@link #findByNameContainingIgnoreCase(String)} - Searches merchants whose names contain a
 *       given substring (case-insensitive).
 *   <li>{@link #findActiveByCuisineType(String)} - Finds active merchants by a specific cuisine
 *       type.
 *   <li>{@link #searchActiveMerchants(String)} - Searches active merchants by keyword in name or
 *       cuisine type.
 *   <li>{@link #findTopRatedActiveMerchants()} - Retrieves top-rated merchants who are currently
 *       active.
 *   <li>{@link #findRecentActiveMerchants(int)} - Retrieves the most recently added active
 *       merchants, limited by count.
 *   <li>{@link #existsByName(String)} - Checks if a merchant exists with a specific name.
 *   <li>{@link #existsByEmail(String)} - Checks if a merchant exists with a specific email address.
 * </ul>
 *
 * <p>This repository supports merchant discovery, search, and performance analytics features across
 * the BoozeBuddies platform.
 */
@Repository
public interface MerchantRepository extends JpaRepository<Merchant, Long> {

  /** Finds a merchant by its exact name. */
  Optional<Merchant> findByName(String name);

  /** Retrieves merchants offering a specific cuisine type. */
  List<Merchant> findByCuisineType(String cuisineType);

  /** Finds merchants by whether they are currently active or inactive. */
  List<Merchant> findByIsActive(boolean isActive);

  /** Searches merchants whose names contain the specified substring, ignoring case. */
  List<Merchant> findByNameContainingIgnoreCase(String name);

  /**
   * Finds active merchants that match the given cuisine type (case-insensitive).
   *
   * @param cuisineType the type of cuisine to filter by
   * @return a list of active merchants matching the cuisine type
   */
  @Query(
      "SELECT m FROM Merchant m WHERE m.isActive = true AND LOWER(m.cuisineType) = LOWER(:cuisineType)")
  List<Merchant> findActiveByCuisineType(@Param("cuisineType") String cuisineType);

  /**
   * Searches for active merchants by keyword, matching either name or cuisine type. The search is
   * case-insensitive and uses partial matching.
   *
   * @param keyword the text to search within merchant names and cuisine types
   * @return a list of active merchants matching the keyword
   */
  @Query(
      "SELECT m FROM Merchant m WHERE m.isActive = true AND (LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(m.cuisineType) LIKE LOWER(CONCAT('%', :keyword, '%')))")
  List<Merchant> searchActiveMerchants(@Param("keyword") String keyword);

  /** Retrieves all active merchants, sorted by rating in descending order. */
  @Query("SELECT m FROM Merchant m WHERE m.isActive = true ORDER BY m.rating DESC")
  List<Merchant> findTopRatedActiveMerchants();

  /**
   * Retrieves a limited number of the most recently created active merchants. Uses a native SQL
   * query for performance optimization.
   *
   * @param limit the maximum number of merchants to return
   * @return a list of recent active merchants sorted by creation date
   */
  @Query(
      value =
          "SELECT * FROM merchants m WHERE m.is_active = true ORDER BY m.created_at DESC LIMIT :limit",
      nativeQuery = true)
  List<Merchant> findRecentActiveMerchants(@Param("limit") int limit);

  /** Checks whether a merchant with the specified name already exists. */
  boolean existsByName(String name);

  /** Checks whether a merchant with the specified email already exists. */
  boolean existsByEmail(String email);
}
