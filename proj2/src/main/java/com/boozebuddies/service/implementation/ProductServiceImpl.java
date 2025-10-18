package com.boozebuddies.service.implementation;

import com.boozebuddies.entity.Product;
import com.boozebuddies.service.ProductService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl implements ProductService {

  private final List<Product> products = new ArrayList<>();
  private long nextProductId = 1;

  /** Retrieves a list of all available products. */
  @Override
  public List<Product> getAllProducts() {
    return new ArrayList<>(products);
  }

  /** Retrieves a product by its unique ID. */
  @Override
  public Product getProductById(Long productId) {
    Optional<Product> productOpt =
        products.stream().filter(p -> p.getId().equals(productId)).findFirst();
    return productOpt.orElse(null);
  }

  /** Adds a new product to the system. */
  @Override
  public Product addProduct(Product product) {
    product.setId(nextProductId++);
    products.add(product);
    return product;
  }

  /** Updates an existing product's information. */
  @Override
  public Product updateProduct(Long productId, Product updatedProduct) {
    Product existingProduct = getProductById(productId);
    if (existingProduct != null) {
      existingProduct.setName(updatedProduct.getName());
      existingProduct.setCategory(updatedProduct.getCategory());
      existingProduct.setPrice(updatedProduct.getPrice());
      existingProduct.setStockQuantity(updatedProduct.getStockQuantity());
      existingProduct.setAlcohol(updatedProduct.isAlcohol());
    }
    return existingProduct;
  }

  /** Deletes a product from the system. */
  @Override
  public void deleteProduct(Long productId) {
    Product product = getProductById(productId);
    if (product != null) {
      products.remove(product);
    }
  }

  /** Searches for products by name, type, or other criteria. */
  @Override
  public List<Product> searchProducts(String keyword) {
    if (keyword == null || keyword.isEmpty()) {
      return getAllProducts();
    }
    String lowerKeyword = keyword.toLowerCase();
    return products.stream()
        .filter(
            p ->
                (p.getName() != null && p.getName().toLowerCase().contains(lowerKeyword))
                    || (p.getCategory() != null && p.getCategory().getName().toLowerCase().contains(lowerKeyword)))
        .collect(Collectors.toList());
  }

  /** Checks if a product is available in sufficient quantity. */
  @Override
  public boolean isProductAvailable(Long productId, int quantity) {
    Product product = getProductById(productId);
    return product != null && product.getStockQuantity() >= quantity;
  }
}
