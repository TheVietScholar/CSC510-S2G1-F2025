package com.boozebuddies.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.boozebuddies.entity.Merchant;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant, Long> {
    
    Optional<Merchant> findByName(String name);
    
    List<Merchant> findByCuisineType(String cuisineType);
    
    List<Merchant> findByIsActive(boolean isActive);
    
    List<Merchant> findByNameContainingIgnoreCase(String name);
    
    @Query("SELECT m FROM Merchant m WHERE m.isActive = true AND LOWER(m.cuisineType) = LOWER(:cuisineType)")
    List<Merchant> findActiveByCuisineType(@Param("cuisineType") String cuisineType);
    
    @Query("SELECT m FROM Merchant m WHERE m.isActive = true AND (LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(m.cuisineType) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Merchant> searchActiveMerchants(@Param("keyword") String keyword);
    
    @Query("SELECT m FROM Merchant m WHERE m.isActive = true ORDER BY m.rating DESC")
    List<Merchant> findTopRatedActiveMerchants();
    
    @Query(value = "SELECT * FROM merchants m WHERE m.is_active = true ORDER BY m.created_at DESC LIMIT :limit", nativeQuery = true)
    List<Merchant> findRecentActiveMerchants(@Param("limit") int limit);
    
    boolean existsByName(String name);
    
    boolean existsByEmail(String email);
}