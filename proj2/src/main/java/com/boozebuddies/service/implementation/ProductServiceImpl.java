package com.boozebuddies.service.implementation;

import com.boozebuddies.dto.ProductDTO;
import com.boozebuddies.entity.Category;
import com.boozebuddies.entity.Merchant;
import com.boozebuddies.entity.Product;
import com.boozebuddies.mapper.ProductMapper;
import com.boozebuddies.repository.CategoryRepository;
import com.boozebuddies.repository.MerchantRepository;
import com.boozebuddies.repository.ProductRepository;
import com.boozebuddies.service.ProductService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for managing products. Provides CRUD operations, product searches, and
 * availability checks.
 */
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

  private final ProductRepository productRepository;
  private final CategoryRepository categoryRepository;
  private final MerchantRepository merchantRepository;
  private final ProductMapper productMapper;

  /**
   * Retrieves all products from the system.
   *
   * @return a list of all products
   */
  @Override
  public List<Product> getAllProducts() {
    return productRepository.findAll();
  }

  /**
   * Retrieves all products that are currently available.
   *
   * @return a list of available products
   */
  @Override
  public List<Product> getAvailableProducts() {
    return productRepository.findByAvailableTrue();
  }

  /**
   * Retrieves a product by its ID.
   *
   * @param id the product ID
   * @return the product if found, or null otherwise
   */
  @Override
  public Product getProductById(Long id) {
    return productRepository.findById(id).orElse(null);
  }

  /**
   * Searches for products based on a keyword. Returns all available products if the keyword is null
   * or empty.
   *
   * @param keyword the search keyword
   * @return a list of matching products
   */
  @Override
  public List<Product> searchProducts(String keyword) {
    if (keyword == null || keyword.trim().isEmpty()) {
      return getAvailableProducts();
    }
    return productRepository.searchByKeyword(keyword);
  }

  /**
   * Retrieves all products for a specific merchant.
   *
   * @param merchantId the merchant ID
   * @return a list of products for the merchant
   */
  @Override
  public List<Product> getProductsByMerchant(Long merchantId) {
    return productRepository.findByMerchantId(merchantId);
  }

  /**
   * Retrieves all available products for a specific merchant.
   *
   * @param merchantId the merchant ID
   * @return a list of available products for the merchant
   */
  @Override
  public List<Product> getAvailableProductsByMerchant(Long merchantId) {
    return productRepository.findByMerchantIdAndAvailableTrue(merchantId);
  }

  /**
   * Checks if a product is available.
   *
   * @param id the product ID
   * @return true if the product exists and is available, false otherwise
   */
  @Override
  public boolean isProductAvailable(Long id) {
    Product product = getProductById(id);
    return product != null && product.isAvailable();
  }

  /**
   * Adds a new product to the system.
   *
   * @param product the product to add
   * @return the saved product
   * @throws IllegalArgumentException if product or merchant is null or product is invalid
   */
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

  /**
   * Creates a new product from a ProductDTO.
   *
   * @param productDTO the product data
   * @return the created product
   * @throws IllegalArgumentException if category or merchant not found
   */
  @Override
  @Transactional
  public Product createProduct(ProductDTO productDTO) {
    if (productDTO == null) {
      throw new IllegalArgumentException("ProductDTO cannot be null");
    }

    // Convert to entity first
    Product product = productMapper.toEntity(productDTO);

    // Set managed category entity
    if (productDTO.getCategoryId() != null) {
      Category category =
          categoryRepository
              .findById(productDTO.getCategoryId())
              .orElseThrow(
                  () ->
                      new IllegalArgumentException(
                          "Category not found with id: " + productDTO.getCategoryId()));
      product.setCategory(category);
    } else {
      throw new IllegalArgumentException("Category ID is required");
    }

    // Set managed merchant entity
    if (productDTO.getMerchantId() != null) {
      Merchant merchant =
          merchantRepository
              .findById(productDTO.getMerchantId())
              .orElseThrow(
                  () ->
                      new IllegalArgumentException(
                          "Merchant not found with id: " + productDTO.getMerchantId()));
      product.setMerchant(merchant);
    } else {
      throw new IllegalArgumentException("Merchant ID is required");
    }

    validateProduct(product);
    return productRepository.save(product);
  }

  /**
   * Updates an existing product.
   *
   * @param id the ID of the product to update
   * @param product the updated product information
   * @return the saved product
   * @throws RuntimeException if the product does not exist
   */
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

  /**
   * Updates an existing product from a ProductDTO.
   *
   * @param id the ID of the product to update
   * @param productDTO the updated product data
   * @return the saved product
   * @throws RuntimeException if the product does not exist
   */
  @Override
  @Transactional
  public Product updateProduct(Long id, ProductDTO productDTO) {
    Product existing = getProductById(id);
    if (existing == null) {
      throw new RuntimeException("Product not found with id: " + id);
    }

    // Update basic fields
    existing.setName(productDTO.getName());
    existing.setDescription(productDTO.getDescription());
    existing.setPrice(productDTO.getPrice());
    existing.setAvailable(productDTO.isAvailable());
    existing.setImageUrl(productDTO.getImageUrl());
    existing.setAlcohol(productDTO.isAlcohol());
    existing.setAlcoholContent(productDTO.getAlcoholContent());
    existing.setVolume(productDTO.getVolume());

    // Update category if provided
    if (productDTO.getCategoryId() != null) {
      Category category =
          categoryRepository
              .findById(productDTO.getCategoryId())
              .orElseThrow(
                  () ->
                      new IllegalArgumentException(
                          "Category not found with id: " + productDTO.getCategoryId()));
      existing.setCategory(category);
    }

    validateProduct(existing);
    return productRepository.save(existing);
  }

  /**
   * Soft deletes a product by marking it as unavailable.
   *
   * @param id the ID of the product to delete
   * @throws RuntimeException if the product does not exist
   */
  @Override
  @Transactional
  public void deleteProduct(Long id) {
    Product product = getProductById(id);
    if (product == null) {
      throw new RuntimeException("Product not found with id: " + id);
    }

    productRepository.deleteById(id);
  }

  /**
   * Retrieves all products for a specific category.
   *
   * @param categoryId the category ID
   * @return a list of products in the category
   */
  @Override
  public List<Product> getProductsByCategory(Long categoryId) {
    return productRepository.findByCategoryId(categoryId);
  }

  /**
   * Retrieves all available products for a specific category.
   *
   * @param categoryId the category ID
   * @return a list of available products in the category
   */
  @Override
  public List<Product> getAvailableProductsByCategory(Long categoryId) {
    return productRepository.findByCategoryIdAndAvailableTrue(categoryId);
  }

  // ==================== HELPER METHODS ====================

  /**
   * Validates a product before saving.
   *
   * @param product the product to validate
   * @throws IllegalArgumentException if product name, price, alcohol content, or volume is invalid
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

    if (product.getVolume() != null && product.getVolume() <= 0) {
      throw new IllegalArgumentException("Volume must be greater than zero");
    }
  }
}
