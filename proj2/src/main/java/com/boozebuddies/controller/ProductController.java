package com.boozebuddies.controller;

import com.boozebuddies.dto.CreateProductRequest;
import com.boozebuddies.dto.ProductDTO;
import com.boozebuddies.entity.Product;
import com.boozebuddies.mapper.ProductMapper;
import com.boozebuddies.service.ProductService;
import com.boozebuddies.service.PermissionService;
import com.boozebuddies.entity.User;
import com.boozebuddies.model.Role;
import com.boozebuddies.security.annotation.RoleAnnotations.*;
import org.springframework.security.core.Authentication;
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
  private final PermissionService permissionService;

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
  // Get all available products
  // -----------------------------
  @GetMapping("/available")
  public ResponseEntity<List<ProductDTO>> getAvailableProducts() {
    List<ProductDTO> products =
        productService.getAvailableProducts().stream()
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
  @IsAdminOrMerchantAdmin
  public ResponseEntity<ProductDTO> addProduct(
      @RequestBody CreateProductRequest request, Authentication authentication) {
    Product product = productMapper.toEntity(request);

    User user = permissionService.getAuthenticatedUser(authentication);
    // If merchant admin, ensure product merchant matches their merchant
    if (user != null && user.hasRole(Role.MERCHANT_ADMIN)) {
      if (product.getMerchant() == null || product.getMerchant().getId() == null
          || !product.getMerchant().getId().equals(user.getMerchantId())) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
      }
    }

    Product savedProduct = productService.addProduct(product);
    return ResponseEntity.status(HttpStatus.CREATED).body(productMapper.toDTO(savedProduct));
  }

  // -----------------------------
  // Update an existing product
  // -----------------------------
  @PutMapping("/{id}")
  @IsAdminOrMerchantAdmin
  public ResponseEntity<ProductDTO> updateProduct(
      @PathVariable Long id, @RequestBody ProductDTO productDTO, Authentication authentication) {
    Product existing = productService.getProductById(id);
    if (existing == null) {
      return ResponseEntity.notFound().build();
    }

    User user = permissionService.getAuthenticatedUser(authentication);
    if (user != null && user.hasRole(Role.MERCHANT_ADMIN)) {
      if (existing.getMerchant() == null || !existing.getMerchant().getId().equals(user.getMerchantId())) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
      }
    }

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
  @IsAdminOrMerchantAdmin
  public ResponseEntity<Void> deleteProduct(@PathVariable Long id, Authentication authentication) {
    Product existing = productService.getProductById(id);
    if (existing == null) {
      return ResponseEntity.notFound().build();
    }

    User user = permissionService.getAuthenticatedUser(authentication);
    if (user != null && user.hasRole(Role.MERCHANT_ADMIN)) {
      if (existing.getMerchant() == null || !existing.getMerchant().getId().equals(user.getMerchantId())) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
      }
    }

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
  // Check if product is available (simple boolean check)
  // -----------------------------
  @GetMapping("/{id}/available")
  public ResponseEntity<Boolean> isProductAvailable(@PathVariable Long id) {
    return ResponseEntity.ok(productService.isProductAvailable(id));
  }

  // -----------------------------
  // Get products by merchant
  // -----------------------------
  @GetMapping("/merchant/{merchantId}")
  public ResponseEntity<List<ProductDTO>> getProductsByMerchant(@PathVariable Long merchantId) {
    List<ProductDTO> products =
        productService.getProductsByMerchant(merchantId).stream()
            .map(productMapper::toDTO)
            .collect(Collectors.toList());
    return ResponseEntity.ok(products);
  }

  // -----------------------------
  // Get available products by merchant
  // -----------------------------
  @GetMapping("/merchant/{merchantId}/available")
  public ResponseEntity<List<ProductDTO>> getAvailableProductsByMerchant(
      @PathVariable Long merchantId) {
    List<ProductDTO> products =
        productService.getAvailableProductsByMerchant(merchantId).stream()
            .map(productMapper::toDTO)
            .collect(Collectors.toList());
    return ResponseEntity.ok(products);
  }
}
