package com.boozebuddies.repository;

import com.boozebuddies.entity.Product;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByMerchant_Id(Long merchantId, Pageable pageable);

    Page<Product> findByMerchant_IdAndAvailableTrue(Long merchantId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.available = true AND LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%'))")
    Page<Product> searchAvailableByName(@Param("q") String keyword, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.isAlcohol = true AND p.available = true")
    Page<Product> findAllAlcoholicAvailable(Pageable pageable);

    Optional<Product> findByIdAndMerchant_Id(Long id, Long merchantId);

    // Price band (for simple filters)
    Page<Product> findByAvailableTrueAndPriceBetween(BigDecimal min, BigDecimal max, Pageable pageable);

    // Fast stock decrement/increment (optional)
    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity - :qty WHERE p.id = :productId AND (p.stockQuantity IS NULL OR p.stockQuantity >= :qty)")
    int tryReserveStock(@Param("productId") Long productId, @Param("qty") int qty);

    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity + :qty WHERE p.id = :productId AND p.stockQuantity IS NOT NULL")
    int releaseStock(@Param("productId") Long productId, @Param("qty") int qty);
}
