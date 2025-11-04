package com.boozebuddies.service;

import com.boozebuddies.entity.Product;
import java.util.List;

/** Service interface for managing products. */
public interface ProductService {

  /** Get all products in the system (including unavailable). Admin use only. */
  List<Product> getAllProducts();

  /** Get only available products (available = true). Public endpoint for customers to browse. */
  List<Product> getAvailableProducts();

  /** Find a product by its ID. */
  Product getProductById(Long id);

  /** Search products by keyword (searches name, description, etc.). */
  List<Product> searchProducts(String keyword);

  /** Get all products for a specific merchant (including unavailable). */
  List<Product> getProductsByMerchant(Long merchantId);

  /** Get only available products for a specific merchant. */
  List<Product> getAvailableProductsByMerchant(Long merchantId);

  /** Check if a product is available for purchase. */
  boolean isProductAvailable(Long id);

  /** Add a new product to the system. */
  Product addProduct(Product product);

  /** Update an existing product. */
  Product updateProduct(Long id, Product product);

  /** Delete a product. */
  void deleteProduct(Long id);

  /**
   * Get products by category. (You mentioned working on category if needed - this will be useful)
   */
  List<Product> getProductsByCategory(Long categoryId);

  /** Get available products by category. */
  List<Product> getAvailableProductsByCategory(Long categoryId);
}
