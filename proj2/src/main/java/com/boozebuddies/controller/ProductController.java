package com.boozebuddies.controller;

import com.boozebuddies.dto.ApiResponse;
import com.boozebuddies.dto.CreateProductRequest;
import com.boozebuddies.dto.ProductDTO;
import com.boozebuddies.entity.Product;
import com.boozebuddies.entity.User;
import com.boozebuddies.mapper.ProductMapper;
import com.boozebuddies.model.Role;
import com.boozebuddies.security.annotation.RoleAnnotations.*;
import com.boozebuddies.service.PermissionService;
import com.boozebuddies.service.ProductService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

  private final ProductService productService;
  private final ProductMapper productMapper;
  private final PermissionService permissionService;

  // ==================== PUBLIC ENDPOINTS (No authentication required) ====================

  /**
   * Get all available products.
   * Public endpoint - anyone can browse available products.
   */
  @GetMapping
  public ResponseEntity<ApiResponse<List<ProductDTO>>> getAllAvailableProducts() {
    try {
      List<Product> products = productService.getAvailableProducts();
      List<ProductDTO> productDTOs =
          products.stream().map(productMapper::toDTO).collect(Collectors.toList());
      return ResponseEntity.ok(
          ApiResponse.success(productDTOs, "Available products retrieved successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to retrieve products: " + e.getMessage()));
    }
  }

  /**
   * Get a product by ID.
   * Public endpoint - anyone can view product details.
   */
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<ProductDTO>> getProductById(@PathVariable Long id) {
    try {
      Product product = productService.getProductById(id);

      if (product == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error("Product not found"));
      }

      return ResponseEntity.ok(
          ApiResponse.success(productMapper.toDTO(product), "Product retrieved successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to retrieve product: " + e.getMessage()));
    }
  }

  /**
   * Search for products by keyword.
   * Public endpoint - anyone can search products.
   */
  @GetMapping("/search")
  public ResponseEntity<ApiResponse<List<ProductDTO>>> searchProducts(
      @RequestParam String keyword) {
    try {
      List<Product> products = productService.searchProducts(keyword);
      List<ProductDTO> productDTOs =
          products.stream().map(productMapper::toDTO).collect(Collectors.toList());
      return ResponseEntity.ok(
          ApiResponse.success(productDTOs, "Products found successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to search products: " + e.getMessage()));
    }
  }

  /**
   * Get products by merchant.
   * Public endpoint - anyone can browse a merchant's products.
   */
  @GetMapping("/merchant/{merchantId}")
  public ResponseEntity<ApiResponse<List<ProductDTO>>> getProductsByMerchant(
      @PathVariable Long merchantId) {
    try {
      List<Product> products = productService.getAvailableProductsByMerchant(merchantId);
      List<ProductDTO> productDTOs =
          products.stream().map(productMapper::toDTO).collect(Collectors.toList());
      return ResponseEntity.ok(
          ApiResponse.success(productDTOs, "Merchant products retrieved successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to retrieve merchant products: " + e.getMessage()));
    }
  }



  /**
   * Check if a product is available.
   * Public endpoint - anyone can check availability.
   */
  @GetMapping("/{id}/available")
  public ResponseEntity<ApiResponse<Boolean>> isProductAvailable(@PathVariable Long id) {
    try {
      boolean available = productService.isProductAvailable(id);
      return ResponseEntity.ok(
          ApiResponse.success(available, "Product availability checked successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to check availability: " + e.getMessage()));
    }
  }

  // ==================== ADMIN ENDPOINTS ====================

  /**
   * Get ALL products (including unavailable ones).
   * Admin only - for management purposes.
   */
  @GetMapping("/all")
  @IsAdmin
  public ResponseEntity<ApiResponse<List<ProductDTO>>> getAllProducts() {
    try {
      List<Product> products = productService.getAllProducts();
      List<ProductDTO> productDTOs =
          products.stream().map(productMapper::toDTO).collect(Collectors.toList());
      return ResponseEntity.ok(
          ApiResponse.success(productDTOs, "All products retrieved successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to retrieve all products: " + e.getMessage()));
    }
  }

  /**
   * Get all products by merchant (including unavailable ones).
   * Admin can see all, merchant admin can only see their own.
   */
  @GetMapping("/merchant/{merchantId}/all")
  @IsAdminOrMerchantAdmin
  public ResponseEntity<ApiResponse<List<ProductDTO>>> getAllProductsByMerchant(
      @PathVariable Long merchantId, Authentication authentication) {
    try {
      User user = permissionService.getAuthenticatedUser(authentication);

      // Check permission for merchant admin
      if (user != null && user.hasRole(Role.MERCHANT_ADMIN)) {
        if (!user.ownsMerchant(merchantId)) {
          throw new AccessDeniedException(
              "You can only view products for your own merchant");
        }
      }

      List<Product> products = productService.getProductsByMerchant(merchantId);
      List<ProductDTO> productDTOs =
          products.stream().map(productMapper::toDTO).collect(Collectors.toList());
      return ResponseEntity.ok(
          ApiResponse.success(productDTOs, "Merchant products retrieved successfully"));
    } catch (AccessDeniedException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(ApiResponse.error(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to retrieve products: " + e.getMessage()));
    }
  }

  // ==================== ADMIN & MERCHANT_ADMIN ENDPOINTS ====================

  /**
   * Add a new product.
   * Admin can add for any merchant, merchant admin can only add for their own merchant.
   */
  @PostMapping
  @IsAdminOrMerchantAdmin
  public ResponseEntity<ApiResponse<ProductDTO>> addProduct(
      @RequestBody CreateProductRequest request, Authentication authentication) {
    try {
      User user = permissionService.getAuthenticatedUser(authentication);
      Product product = productMapper.toEntity(request);

      // Validate merchant ownership for merchant admins
      if (user != null && user.hasRole(Role.MERCHANT_ADMIN)) {
        if (product.getMerchant() == null
            || product.getMerchant().getId() == null
            || !user.ownsMerchant(product.getMerchant().getId())) {
          throw new AccessDeniedException(
              "You can only add products for your own merchant");
        }
      }

      Product savedProduct = productService.addProduct(product);
      return ResponseEntity.status(HttpStatus.CREATED)
          .body(ApiResponse.success(
              productMapper.toDTO(savedProduct), "Product added successfully"));
    } catch (AccessDeniedException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(ApiResponse.error(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to add product: " + e.getMessage()));
    }
  }

  /**
   * Update an existing product.
   * Admin can update any product, merchant admin can only update their own products.
   */
  @PutMapping("/{id}")
  @IsAdminOrMerchantAdmin
  public ResponseEntity<ApiResponse<ProductDTO>> updateProduct(
      @PathVariable Long id, @RequestBody ProductDTO productDTO, Authentication authentication) {
    try {
      Product existing = productService.getProductById(id);
      if (existing == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error("Product not found"));
      }

      User user = permissionService.getAuthenticatedUser(authentication);

      // Check permission for merchant admin
      if (user != null && user.hasRole(Role.MERCHANT_ADMIN)) {
        if (existing.getMerchant() == null
            || !user.ownsMerchant(existing.getMerchant().getId())) {
          throw new AccessDeniedException(
              "You can only update products for your own merchant");
        }
      }

      Product product = productMapper.toEntity(productDTO);
      Product updatedProduct = productService.updateProduct(id, product);

      if (updatedProduct == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error("Failed to update product"));
      }

      return ResponseEntity.ok(
          ApiResponse.success(productMapper.toDTO(updatedProduct), "Product updated successfully"));
    } catch (AccessDeniedException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(ApiResponse.error(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to update product: " + e.getMessage()));
    }
  }

  /**
   * Delete a product.
   * Admin can delete any product, merchant admin can only delete their own products.
   */
  @DeleteMapping("/{id}")
  @IsAdminOrMerchantAdmin
  public ResponseEntity<ApiResponse<Void>> deleteProduct(
      @PathVariable Long id, Authentication authentication) {
    try {
      Product existing = productService.getProductById(id);
      if (existing == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error("Product not found"));
      }

      User user = permissionService.getAuthenticatedUser(authentication);

      // Check permission for merchant admin
      if (user != null && user.hasRole(Role.MERCHANT_ADMIN)) {
        if (existing.getMerchant() == null
            || !user.ownsMerchant(existing.getMerchant().getId())) {
          throw new AccessDeniedException(
              "You can only delete products for your own merchant");
        }
      }

      productService.deleteProduct(id);
      return ResponseEntity.ok(ApiResponse.success(null, "Product deleted successfully"));
    } catch (AccessDeniedException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(ApiResponse.error(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to delete product: " + e.getMessage()));
    }
  }
}