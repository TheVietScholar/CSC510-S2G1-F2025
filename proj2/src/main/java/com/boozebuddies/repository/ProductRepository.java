package com.boozebuddies.repository;

import com.boozebuddies.entity.Product;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

  /** Find all available products. */
  List<Product> findByAvailableTrue();

  /** Find all products for a specific merchant. */
  List<Product> findByMerchantId(Long merchantId);

  /** Find available products for a specific merchant. */
  List<Product> findByMerchantIdAndAvailableTrue(Long merchantId);

  /** Find products by category. */
  List<Product> findByCategoryId(Long categoryId);

  /** Find available products by category. */
  List<Product> findByCategoryIdAndAvailableTrue(Long categoryId);

  /** Search products by keyword (searches name, description). Only returns available products. */
  @Query(
      "SELECT p FROM Product p WHERE p.available = true AND "
          + "(LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
          + "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
  List<Product> searchByKeyword(@Param("keyword") String keyword);

  /** Find products by price range. */
  @Query(
      "SELECT p FROM Product p WHERE p.available = true AND p.price BETWEEN :minPrice AND :maxPrice")
  List<Product> findByPriceRange(
      @Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice);

  /** Find products by alcohol content range (for beer types). */
  @Query(
      "SELECT p FROM Product p WHERE p.available = true AND "
          + "p.alcoholContent BETWEEN :minAlcohol AND :maxAlcohol")
  List<Product> findByAlcoholContentRange(
      @Param("minAlcohol") Double minAlcohol, @Param("maxAlcohol") Double maxAlcohol);

  /**
   * Find top-selling products (you'll need an OrderItem entity to fully implement this). This is a
   * placeholder for future implementation.
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

  /** Count available products for a merchant. */
  @Query("SELECT COUNT(p) FROM Product p WHERE p.merchant.id = :merchantId AND p.available = true")
  Long countAvailableProductsByMerchant(@Param("merchantId") Long merchantId);
}
