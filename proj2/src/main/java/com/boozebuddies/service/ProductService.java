package com.boozebuddies.service;

import com.boozebuddies.entity.Product;
import java.util.List;

public interface ProductService {

  /**
   * Retrieves a list of all available products.
   *
   * @return A list of all products.
   */
  List<Product> getAllProducts();

  /**
   * Retrieves a product by its unique ID.
   *
   * @param productId The ID of the product.
   * @return The product if found, otherwise null.
   */
  Product getProductById(Long productId);

  /**
   * Adds a new product to the system.
   *
   * @param product The product to add.
   * @return The added product with its generated ID.
   */
  Product addProduct(Product product);

  /**
   * Updates an existing product's information.
   *
   * @param productId The ID of the product to update.
   * @param product The product object with updated information.
   * @return The updated product.
   */
  Product updateProduct(Long productId, Product product);

  /**
   * Deletes a product from the system.
   *
   * @param productId The ID of the product to delete.
   */
  void deleteProduct(Long productId);

  /**
   * Searches for products by name, type, or other criteria.
   *
   * @param keyword The search keyword.
   * @return A list of products matching the search criteria.
   */
  List<Product> searchProducts(String keyword);

  /**
   * Checks if a product is available for ordering.
   *
   * @param productId The ID of the product.
   * @return True if the product is available, false otherwise.
   */
  boolean isProductAvailable(Long productId);

  /**
   * Retrieves all available products.
   *
   * @return A list of available products.
   */
  List<Product> getAvailableProducts();

  /**
   * Retrieves all products for a specific merchant.
   *
   * @param merchantId The ID of the merchant.
   * @return A list of products for the merchant.
   */
  List<Product> getProductsByMerchant(Long merchantId);

  /**
   * Retrieves available products for a specific merchant.
   *
   * @param merchantId The ID of the merchant.
   * @return A list of available products for the merchant.
   */
  List<Product> getAvailableProductsByMerchant(Long merchantId);
}