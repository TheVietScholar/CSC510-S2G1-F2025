package com.boozebuddies.service.implementation;

import com.boozebuddies.entity.Product;
import com.boozebuddies.repository.ProductRepository;
import com.boozebuddies.service.ProductService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    /** Retrieves all products. */
    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    /** Retrieves a product by its ID. */
    @Override
    public Product getProductById(Long productId) {
        return productRepository.findById(productId).orElse(null);
    }

    /** Adds a new product. */
    @Override
    public Product addProduct(Product product) {
        return productRepository.save(product);
    }

    /** Updates an existing product. */
    @Override
    public Product updateProduct(Long productId, Product updatedProduct) {
        return productRepository.findById(productId).map(existingProduct -> {
            existingProduct.setName(updatedProduct.getName());
            existingProduct.setCategory(updatedProduct.getCategory());
            existingProduct.setPrice(updatedProduct.getPrice());
            existingProduct.setStockQuantity(updatedProduct.getStockQuantity());
            existingProduct.setAlcohol(updatedProduct.isAlcohol());
            existingProduct.setAvailable(updatedProduct.isAvailable());
            existingProduct.setDescription(updatedProduct.getDescription());
            existingProduct.setAlcoholContent(updatedProduct.getAlcoholContent());
            existingProduct.setImageUrl(updatedProduct.getImageUrl());
            return productRepository.save(existingProduct);
        }).orElse(null);
    }

    /** Deletes a product by ID. */
    @Override
    public void deleteProduct(Long productId) {
        productRepository.deleteById(productId);
    }

    /** Searches products by name or category name. */
    @Override
    public List<Product> searchProducts(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return getAllProducts();
        }
        String lowerKeyword = keyword.toLowerCase();
        return productRepository.findAll().stream()
                .filter(p ->
                        (p.getName() != null && p.getName().toLowerCase().contains(lowerKeyword)) ||
                        (p.getCategory() != null && p.getCategory().getName() != null &&
                                p.getCategory().getName().toLowerCase().contains(lowerKeyword))
                ).collect(Collectors.toList());
    }

    /** Checks if a product is available in the requested quantity. */
    @Override
    public boolean isProductAvailable(Long productId, int quantity) {
        return productRepository.findById(productId)
                .map(product -> product.isAvailable() && product.getStockQuantity() >= quantity)
                .orElse(false);
    }
}
