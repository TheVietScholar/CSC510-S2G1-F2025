package com.boozebuddies.repository;

import com.boozebuddies.entity.Product;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

  // Find all products by merchant
  Page<Product> findByMerchant_Id(Long merchantId, Pageable pageable);

  // Find available products by merchant
  Page<Product> findByMerchant_IdAndAvailableTrue(Long merchantId, Pageable pageable);

  // Find all available products
  Page<Product> findByAvailableTrue(Pageable pageable);

  // Search available products by name
  @Query(
      "SELECT p FROM Product p WHERE p.available = true AND LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%'))")
  Page<Product> searchAvailableByName(@Param("q") String keyword, Pageable pageable);

  // Find all alcoholic available products
  @Query("SELECT p FROM Product p WHERE p.isAlcohol = true AND p.available = true")
  Page<Product> findAllAlcoholicAvailable(Pageable pageable);

  // Find product by ID and merchant ID
  Optional<Product> findByIdAndMerchant_Id(Long id, Long merchantId);

  // Price range filter for available products
  Page<Product> findByAvailableTrueAndPriceBetween(
      BigDecimal min, BigDecimal max, Pageable pageable);

  // Find products by category
  Page<Product> findByCategory_IdAndAvailableTrue(Long categoryId, Pageable pageable);

  // Find products by alcohol status
  Page<Product> findByIsAlcoholAndAvailableTrue(boolean isAlcohol, Pageable pageable);

  // Find all available products (non-paged)
  List<Product> findByAvailableTrue();

  // Find available products by merchant (non-paged)
  List<Product> findByMerchant_IdAndAvailableTrue(Long merchantId);

  // Search products by name or category name (non-paged)
  @Query(
      "SELECT p FROM Product p WHERE "
          + "(LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
          + "LOWER(p.category.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND "
          + "p.available = true")
  List<Product> searchAvailableProducts(@Param("keyword") String keyword);

  // Count available products by merchant
  long countByMerchant_IdAndAvailableTrue(Long merchantId);

  // Count all available products
  long countByAvailableTrue();
}
