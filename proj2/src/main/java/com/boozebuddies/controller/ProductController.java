package com.boozebuddies.controller;

import com.boozebuddies.dto.CreateProductRequest;
import com.boozebuddies.dto.ProductDTO;
import com.boozebuddies.entity.Product;
import com.boozebuddies.mapper.ProductMapper;
import com.boozebuddies.service.ProductService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

  private final ProductService productService;
  private final ProductMapper productMapper;

  // -----------------------------
  // Get all products
  // -----------------------------
  @GetMapping
  public ResponseEntity<List<ProductDTO>> getAllProducts() {
    List<ProductDTO> products =
        productService.getAllProducts().stream()
            .map(productMapper::toDTO)
            .collect(Collectors.toList());
    return ResponseEntity.ok(products);
  }

  // -----------------------------
  // Get a product by ID
  // -----------------------------
  @GetMapping("/{id}")
  public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
    Product product = productService.getProductById(id);

    if (product == null) {
      return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok(productMapper.toDTO(product));
  }

  // -----------------------------
  // Add a new product
  // -----------------------------
  @PostMapping
  public ResponseEntity<ProductDTO> addProduct(@RequestBody CreateProductRequest request) {
    Product product = productMapper.toEntity(request);
    Product savedProduct = productService.addProduct(product);
    return ResponseEntity.status(HttpStatus.CREATED).body(productMapper.toDTO(savedProduct));
  }

  // -----------------------------
  // Update an existing product
  // -----------------------------
  @PutMapping("/{id}")
  public ResponseEntity<ProductDTO> updateProduct(
      @PathVariable Long id, @RequestBody ProductDTO productDTO) {
    Product product = productMapper.toEntity(productDTO);
    Product updatedProduct = productService.updateProduct(id, product);

    if (updatedProduct == null) {
      return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok(productMapper.toDTO(updatedProduct));
  }

  // -----------------------------
  // Delete a product
  // -----------------------------
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
    productService.deleteProduct(id);
    return ResponseEntity.noContent().build();
  }

  // -----------------------------
  // Search for products
  // -----------------------------
  @GetMapping("/search")
  public ResponseEntity<List<ProductDTO>> searchProducts(@RequestParam String keyword) {
    List<ProductDTO> products =
        productService.searchProducts(keyword).stream()
            .map(productMapper::toDTO)
            .collect(Collectors.toList());
    return ResponseEntity.ok(products);
  }

  // -----------------------------
  // Check if product is available in sufficient quantity
  // -----------------------------
  @GetMapping("/{id}/available")
  public ResponseEntity<Boolean> isProductAvailable(
      @PathVariable Long id, @RequestParam int quantity) {
    return ResponseEntity.ok(productService.isProductAvailable(id, quantity));
  }
}
