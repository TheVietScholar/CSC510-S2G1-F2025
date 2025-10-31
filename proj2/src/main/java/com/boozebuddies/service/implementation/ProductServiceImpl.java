package com.boozebuddies.service.implementation;

import com.boozebuddies.entity.Product;
import com.boozebuddies.repository.ProductRepository;
import com.boozebuddies.service.ProductService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

  private final ProductRepository productRepository;

  @Override
  public List<Product> getAllProducts() {
    return productRepository.findAll();
  }

  @Override
  public List<Product> getAvailableProducts() {
    return productRepository.findByAvailableTrue();
  }

  @Override
  public Product getProductById(Long id) {
    return productRepository.findById(id).orElse(null);
  }

  @Override
  public List<Product> searchProducts(String keyword) {
    if (keyword == null || keyword.trim().isEmpty()) {
      return getAvailableProducts();
    }
    return productRepository.searchByKeyword(keyword);
  }

  @Override
  public List<Product> getProductsByMerchant(Long merchantId) {
    return productRepository.findByMerchantId(merchantId);
  }

  @Override
  public List<Product> getAvailableProductsByMerchant(Long merchantId) {
    return productRepository.findByMerchantIdAndAvailableTrue(merchantId);
  }

  @Override
  public boolean isProductAvailable(Long id) {
    Product product = getProductById(id);
    return product != null && product.isAvailable();
  }

  @Override
  @Transactional
  public Product addProduct(Product product) {
    if (product == null) {
      throw new IllegalArgumentException("Product cannot be null");
    }

    if (product.getMerchant() == null) {
      throw new IllegalArgumentException("Product must be associated with a merchant");
    }

    validateProduct(product);
    return productRepository.save(product);
  }

  @Override
  @Transactional
  public Product updateProduct(Long id, Product product) {
    Product existing = getProductById(id);
    if (existing == null) {
      throw new RuntimeException("Product not found with id: " + id);
    }

    validateProduct(product);

    existing.setName(product.getName());
    existing.setDescription(product.getDescription());
    existing.setPrice(product.getPrice());
    existing.setAvailable(product.isAvailable());
    existing.setImageUrl(product.getImageUrl());
    existing.setAlcoholContent(product.getAlcoholContent());
    existing.setVolume(product.getVolume());

    if (product.getCategory() != null) {
      existing.setCategory(product.getCategory());
    }

    return productRepository.save(existing);
  }

  @Override
  @Transactional
  public void deleteProduct(Long id) {
    Product product = getProductById(id);
    if (product == null) {
      throw new RuntimeException("Product not found with id: " + id);
    }

    // Soft delete for MVP: mark as unavailable instead of removing from DB
    product.setAvailable(false);
    productRepository.save(product);
  }

  @Override
  public List<Product> getProductsByCategory(Long categoryId) {
    return productRepository.findByCategoryId(categoryId);
  }

  @Override
  public List<Product> getAvailableProductsByCategory(Long categoryId) {
    return productRepository.findByCategoryIdAndAvailableTrue(categoryId);
  }

  // ==================== HELPER METHODS ====================

  /**
   * Validate product data before saving.
   */
  private void validateProduct(Product product) {
    if (product.getName() == null || product.getName().trim().isEmpty()) {
      throw new IllegalArgumentException("Product name is required");
    }

    if (product.getPrice() == null || product.getPrice().doubleValue() < 0) {
      throw new IllegalArgumentException("Product price must be non-negative");
    }

    if (product.getAlcoholContent() != null
        && (product.getAlcoholContent() < 0 || product.getAlcoholContent() > 100)) {
      throw new IllegalArgumentException("Alcohol content must be between 0 and 100");
    }

    if (product.getName() == null || product.getName().trim().isEmpty()) {
      throw new IllegalArgumentException("Product name is required");
    }

    if (product.getPrice() == null || product.getPrice().doubleValue() < 0) {
      throw new IllegalArgumentException("Product price must be non-negative");
    }

    if (product.getAlcoholContent() != null
        && (product.getAlcoholContent() < 0 || product.getAlcoholContent() > 100)) {
      throw new IllegalArgumentException("Alcohol content must be between 0 and 100");
    }

    if (product.getVolume() != null && product.getVolume() <= 0) {
      throw new IllegalArgumentException("Volume must be greater than zero");
    }


  }

  /**
   * Calculate distance between two points using the Haversine formula.
   * Returns distance in kilometers.
   */
  private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
    final int EARTH_RADIUS_KM = 6371;

    double dLat = Math.toRadians(lat2 - lat1);
    double dLon = Math.toRadians(lon2 - lon1);

    double a =
        Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);

    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return EARTH_RADIUS_KM * c;
  }

}
