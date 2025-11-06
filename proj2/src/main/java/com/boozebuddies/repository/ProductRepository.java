package com.boozebuddies.repository;

import com.boozebuddies.entity.Product;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing {@link Product} entities within the BoozeBuddies platform.
 *
 * <p>Extends {@link JpaRepository} to provide CRUD operations and adds specialized queries for
 * retrieving, filtering, and analyzing products by availability, category, price, and merchant.
 *
 * <p>Used primarily for marketplace product listings, search, and merchant catalog management.
 *
 * <p>Key use cases include:
 *
 * <ul>
 *   <li>Listing available products by merchant or category
 *   <li>Keyword-based product searches (name or description)
 *   <li>Filtering products by price or alcohol content range
 *   <li>Counting available products per merchant
 *   <li>Retrieving top-selling products (future enhancement)
 * </ul>
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

  /**
   * Retrieves all products that are currently available for purchase.
   *
   * @return a list of available {@link Product} entities
   */
  List<Product> findByAvailableTrue();

  /**
   * Retrieves all products belonging to a specific merchant.
   *
   * @param merchantId the ID of the merchant
   * @return a list of {@link Product} entities for the given merchant
   */
  List<Product> findByMerchantId(Long merchantId);

  /**
   * Retrieves all available products for a specific merchant.
   *
   * @param merchantId the ID of the merchant
   * @return a list of available {@link Product} entities for the merchant
   */
  List<Product> findByMerchantIdAndAvailableTrue(Long merchantId);

  /**
   * Retrieves all products associated with a specific category.
   *
   * @param categoryId the ID of the category
   * @return a list of {@link Product} entities in the given category
   */
  List<Product> findByCategoryId(Long categoryId);

  /**
   * Retrieves all available products within a specific category.
   *
   * @param categoryId the ID of the category
   * @return a list of available {@link Product} entities in that category
   */
  List<Product> findByCategoryIdAndAvailableTrue(Long categoryId);

  /**
   * Searches for available products whose names or descriptions match a given keyword
   * (case-insensitive).
   *
   * @param keyword the keyword to search for
   * @return a list of matching available {@link Product} entities
   */
  @Query(
      "SELECT p FROM Product p WHERE p.available = true AND "
          + "(LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
          + "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
  List<Product> searchByKeyword(@Param("keyword") String keyword);

  /**
   * Retrieves all available products within a specified price range.
   *
   * @param minPrice the minimum price (inclusive)
   * @param maxPrice the maximum price (inclusive)
   * @return a list of available {@link Product} entities within the price range
   */
  @Query(
      "SELECT p FROM Product p WHERE p.available = true AND p.price BETWEEN :minPrice AND :maxPrice")
  List<Product> findByPriceRange(
      @Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice);

  /**
   * Retrieves available products whose alcohol content falls within the specified range.
   *
   * <p>This is primarily used for filtering beverage products such as beers or wines.
   *
   * @param minAlcohol the minimum alcohol percentage (inclusive)
   * @param maxAlcohol the maximum alcohol percentage (inclusive)
   * @return a list of {@link Product} entities matching the alcohol content range
   */
  @Query(
      "SELECT p FROM Product p WHERE p.available = true AND "
          + "p.alcoholContent BETWEEN :minAlcohol AND :maxAlcohol")
  List<Product> findByAlcoholContentRange(
      @Param("minAlcohol") Double minAlcohol, @Param("maxAlcohol") Double maxAlcohol);

  /**
   * Retrieves the top-selling products based on order history.
   *
   * <p>This query joins the {@code order_items} table and counts product occurrences, ordering
   * results by sales volume in descending order.
   *
   * <p><b>Note:</b> This query is a placeholder that depends on an {@code OrderItem} entity for
   * full implementation.
   *
   * @param limit the maximum number of top-selling products to return
   * @return a list of the most frequently purchased {@link Product} entities
   */
  @Query(
      value =
          "SELECT p.* FROM products p "
              + "LEFT JOIN order_items oi ON p.id = oi.product_id "
              + "WHERE p.available = true "
              + "GROUP BY p.id "
              + "ORDER BY COUNT(oi.id) DESC "
              + "LIMIT :limit",
      nativeQuery = true)
  List<Product> findTopSellingProducts(@Param("limit") int limit);

  /**
   * Counts the number of available products offered by a given merchant.
   *
   * @param merchantId the ID of the merchant
   * @return the number of available {@link Product} entities for that merchant
   */
  @Query("SELECT COUNT(p) FROM Product p WHERE p.merchant.id = :merchantId AND p.available = true")
  Long countAvailableProductsByMerchant(@Param("merchantId") Long merchantId);
}
