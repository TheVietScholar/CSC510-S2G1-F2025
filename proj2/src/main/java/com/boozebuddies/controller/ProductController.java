package com.boozebuddies.controller;

import com.boozebuddies.entity.Product;
import com.boozebuddies.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    // -----------------------------
    // Get all products
    // -----------------------------
    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    // -----------------------------
    // Get a product by ID
    // -----------------------------
    @GetMapping("/{id}")
    public Product getProductById(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    // -----------------------------
    // Add a new product
    // -----------------------------
    @PostMapping
    public Product addProduct(@RequestBody Product product) {
        return productService.addProduct(product);
    }

    // -----------------------------
    // Update an existing product
    // -----------------------------
    @PutMapping("/{id}")
    public Product updateProduct(@PathVariable Long id, @RequestBody Product product) {
        return productService.updateProduct(id, product);
    }

    // -----------------------------
    // Delete a product
    // -----------------------------
    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
    }

    // -----------------------------
    // Search for products
    // -----------------------------
    @GetMapping("/search")
    public List<Product> searchProducts(@RequestParam String keyword) {
        return productService.searchProducts(keyword);
    }

    // -----------------------------
    // Check if product is available in sufficient quantity
    // -----------------------------
    @GetMapping("/{id}/available")
    public boolean isProductAvailable(@PathVariable Long id, @RequestParam int quantity) {
        return productService.isProductAvailable(id, quantity);
    }
}
