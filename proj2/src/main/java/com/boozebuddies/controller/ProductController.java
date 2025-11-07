package com.boozebuddies.controller;

import com.boozebuddies.dto.ApiResponse;
import com.boozebuddies.dto.CreateProductRequest;
import com.boozebuddies.dto.ProductDTO;
import com.boozebuddies.entity.Product;
import com.boozebuddies.entity.User;
import com.boozebuddies.mapper.ProductMapper;
import com.boozebuddies.model.Role;
import com.boozebuddies.security.annotation.RoleAnnotations.*;
import com.boozebuddies.service.CategoryService;
import com.boozebuddies.service.MerchantService;
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

/** REST controller for managing products and product operations. */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

  private final ProductService productService;
  private final ProductMapper productMapper;
  private final PermissionService permissionService;
  private final MerchantService merchantService;
  private final CategoryService categoryService; // ADD THIS

  // ==================== PUBLIC ENDPOINTS (No authentication required) ====================

  /**
   * Retrieves all available products. Public endpoint.
   *
   * @return a list of all available products
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
   * Retrieves a product by ID. Public endpoint.
   *
   * @param id the product ID
   * @return the product with the specified ID
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
   * Searches for products by keyword. Public endpoint.
   *
   * @param keyword the search keyword
   * @return a list of products matching the keyword
   */
  @GetMapping("/search")
  public ResponseEntity<ApiResponse<List<ProductDTO>>> searchProducts(
      @RequestParam String keyword) {
    try {
      List<Product> products = productService.searchProducts(keyword);
      List<ProductDTO> productDTOs =
          products.stream().map(productMapper::toDTO).collect(Collectors.toList());
      return ResponseEntity.ok(ApiResponse.success(productDTOs, "Products found successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to search products: " + e.getMessage()));
    }
  }

  /**
   * Retrieves products by merchant. Public endpoint.
   *
   * @param merchantId the merchant ID
   * @return a list of available products for the specified merchant
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
   * Checks if a product is available. Public endpoint.
   *
   * @param id the product ID
   * @return whether the product is available
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
   * Retrieves all products including unavailable ones. Admin only.
   *
   * @return a list of all products
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
   * Retrieves all products by merchant including unavailable ones. Admin can see all, merchant
   * admin can only see their own.
   *
   * @param merchantId the merchant ID
   * @param authentication the authentication object
   * @return a list of all products for the specified merchant
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
          throw new AccessDeniedException("You can only view products for your own merchant");
        }
      }

      List<Product> products = productService.getProductsByMerchant(merchantId);
      List<ProductDTO> productDTOs =
          products.stream().map(productMapper::toDTO).collect(Collectors.toList());
      return ResponseEntity.ok(
          ApiResponse.success(productDTOs, "Merchant products retrieved successfully"));
    } catch (AccessDeniedException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to retrieve products: " + e.getMessage()));
    }
  }

  // ==================== ADMIN & MERCHANT_ADMIN ENDPOINTS ====================

  /**
   * Creates a new product. Admin can create for any merchant, merchant admin can only create for
   * their own merchant.
   *
   * @param request the product creation request
   * @param authentication the authentication object
   * @return the created product
   */
  @PostMapping
  @IsAdminOrMerchantAdmin
  public ResponseEntity<ApiResponse<ProductDTO>> createProduct(
      @RequestBody CreateProductRequest request, Authentication authentication) {
    try {
      System.out.println("DEBUG - CreateProductRequest received:");
      System.out.println("  Name: " + request.getName());
      System.out.println("  Category ID: " + request.getCategoryId());
      System.out.println("  Merchant ID: " + request.getMerchantId());
      System.out.println("  isAlcohol: " + request.isAlcohol());
      System.out.println("  alcoholContent: " + request.getAlcoholContent());

      User user = permissionService.getAuthenticatedUser(authentication);

      // Validate merchant ownership for merchant admins
      if (user != null && user.hasRole(Role.MERCHANT_ADMIN)) {
        if (!user.ownsMerchant(request.getMerchantId())) {
          throw new AccessDeniedException("You can only create products for your own merchant");
        }
      }

      // Convert to ProductDTO to use the new createProduct method
      ProductDTO productDTO =
          ProductDTO.builder()
              .name(request.getName())
              .description(request.getDescription())
              .price(request.getPrice())
              .categoryId(request.getCategoryId()) // This will be used by the service
              .merchantId(request.getMerchantId())
              .isAlcohol(request.isAlcohol())
              .alcoholContent(request.getAlcoholContent())
              .isAvailable(request.isAvailable())
              .imageUrl(request.getImageUrl())
              .build();

      // Use the new createProduct method that handles categories properly
      Product createdProduct = productService.createProduct(productDTO);

      System.out.println("DEBUG - After productService.createProduct():");
      System.out.println("  Product ID: " + createdProduct.getId());
      System.out.println(
          "  Category: "
              + (createdProduct.getCategory() != null
                  ? createdProduct.getCategory().getId()
                  : "null"));

      return ResponseEntity.status(HttpStatus.CREATED)
          .body(
              ApiResponse.success(
                  productMapper.toDTO(createdProduct), "Product created successfully"));
    } catch (AccessDeniedException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(e.getMessage()));
    } catch (Exception e) {
      e.printStackTrace(); // Print full stack trace for debugging
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to create product: " + e.getMessage()));
    }
  }

  /**
   * Updates an existing product. Admin can update any product, merchant admin can only update their
   * own products.
   *
   * @param id the product ID
   * @param productDTO the updated product data
   * @param authentication the authentication object
   * @return the updated product
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
        if (existing.getMerchant() == null || !user.ownsMerchant(existing.getMerchant().getId())) {
          throw new AccessDeniedException("You can only update products for your own merchant");
        }
      }

      // Use the new updateProduct method that handles DTOs
      Product updatedProduct = productService.updateProduct(id, productDTO);

      if (updatedProduct == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error("Failed to update product"));
      }

      return ResponseEntity.ok(
          ApiResponse.success(productMapper.toDTO(updatedProduct), "Product updated successfully"));
    } catch (AccessDeniedException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to update product: " + e.getMessage()));
    }
  }

  /**
   * Deletes a product. Admin can delete any product, merchant admin can only delete their own
   * products.
   *
   * @param id the product ID
   * @param authentication the authentication object
   * @return a success message
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
        if (existing.getMerchant() == null || !user.ownsMerchant(existing.getMerchant().getId())) {
          throw new AccessDeniedException("You can only delete products for your own merchant");
        }
      }

      productService.deleteProduct(id);
      return ResponseEntity.ok(ApiResponse.success(null, "Product deleted successfully"));
    } catch (AccessDeniedException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to delete product: " + e.getMessage()));
    }
  }
}
