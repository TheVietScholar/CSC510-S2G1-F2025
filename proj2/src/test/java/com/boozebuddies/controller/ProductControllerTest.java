package com.boozebuddies.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.boozebuddies.config.TestSecurityConfig;
import com.boozebuddies.dto.CreateProductRequest;
import com.boozebuddies.dto.ProductDTO;
import com.boozebuddies.entity.Merchant;
import com.boozebuddies.entity.Product;
import com.boozebuddies.entity.User;
import com.boozebuddies.mapper.ProductMapper;
import com.boozebuddies.model.Role;
import com.boozebuddies.security.JwtAuthenticationFilter;
import com.boozebuddies.service.MerchantService;
import com.boozebuddies.service.PermissionService;
import com.boozebuddies.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    controllers = ProductController.class,
    excludeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = JwtAuthenticationFilter.class))
@AutoConfigureMockMvc(addFilters = false)
@Import(TestSecurityConfig.class)
@DisplayName("ProductController Tests")
public class ProductControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private ProductService productService;
  @MockBean private ProductMapper productMapper;
  @MockBean private PermissionService permissionService;
  @MockBean private MerchantService merchantService;

  private Product testProduct;
  private ProductDTO testProductDTO;
  private CreateProductRequest testCreateRequest;
  private User adminUser;
  private User merchantAdminUser;
  private Merchant testMerchant;

  @BeforeEach
  void setUp() {
    testMerchant = Merchant.builder().id(1L).name("Test Liquor Store").build();

    testProduct =
        Product.builder()
            .id(1L)
            .name("Test Beer")
            .price(new BigDecimal("8.99"))
            .available(true)
            .merchant(testMerchant)
            .build();

    testProductDTO =
        ProductDTO.builder()
            .id(1L)
            .name("Test Beer")
            .price(new BigDecimal("8.99"))
            .isAvailable(true)
            .merchantId(1L)
            .build();

    testCreateRequest =
        CreateProductRequest.builder()
            .name("New Beer")
            .price(new BigDecimal("7.99"))
            .isAlcohol(true)
            .alcoholContent(5.5)
            .merchantId(1L)
            .build();

    adminUser = User.builder().id(99L).name("Admin").build();
    adminUser.setRoles(java.util.Set.of(Role.ADMIN));

    merchantAdminUser = User.builder().id(20L).name("Merchant Admin").merchantId(1L).build();
    merchantAdminUser.addRole(Role.MERCHANT_ADMIN);
  }

  // ==================== GET ALL AVAILABLE PRODUCTS TESTS ====================

  @Test
  @DisplayName("GET /api/products should return 200 with available products")
  void getAllAvailableProducts_Success() throws Exception {
    Product product2 = Product.builder().id(2L).name("Test Wine").available(true).build();
    ProductDTO productDTO2 = ProductDTO.builder().id(2L).name("Test Wine").build();

    when(productService.getAvailableProducts()).thenReturn(Arrays.asList(testProduct, product2));
    when(productMapper.toDTO(testProduct)).thenReturn(testProductDTO);
    when(productMapper.toDTO(product2)).thenReturn(productDTO2);

    mockMvc
        .perform(get("/api/products"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Available products retrieved successfully"))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data.length()").value(2));
  }

  @Test
  @DisplayName("GET /api/products should return 200 with empty list")
  void getAllAvailableProducts_EmptyList() throws Exception {
    when(productService.getAvailableProducts()).thenReturn(List.of());

    mockMvc
        .perform(get("/api/products"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data.length()").value(0));
  }

  @Test
  @DisplayName("GET /api/products should return 400 on exception")
  void getAllAvailableProducts_Exception() throws Exception {
    when(productService.getAvailableProducts()).thenThrow(new RuntimeException("Database error"));

    mockMvc
        .perform(get("/api/products"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.containsString("Failed to retrieve products")));
  }

  // ==================== GET PRODUCT BY ID TESTS ====================

  @Test
  @DisplayName("GET /api/products/{id} should return 200 with product")
  void getProductById_Success() throws Exception {
    when(productService.getProductById(1L)).thenReturn(testProduct);
    when(productMapper.toDTO(testProduct)).thenReturn(testProductDTO);

    mockMvc
        .perform(get("/api/products/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Product retrieved successfully"))
        .andExpect(jsonPath("$.data.id").value(1))
        .andExpect(jsonPath("$.data.name").value("Test Beer"));
  }

  @Test
  @DisplayName("GET /api/products/{id} should return 404 when not found")
  void getProductById_NotFound() throws Exception {
    when(productService.getProductById(999L)).thenReturn(null);

    mockMvc
        .perform(get("/api/products/999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Product not found"));
  }

  @Test
  @DisplayName("GET /api/products/{id} should return 400 on exception")
  void getProductById_Exception() throws Exception {
    when(productService.getProductById(1L)).thenThrow(new RuntimeException("Database error"));

    mockMvc
        .perform(get("/api/products/1"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.containsString("Failed to retrieve product")));
  }

  // ==================== SEARCH PRODUCTS TESTS ====================

  @Test
  @DisplayName("GET /api/products/search should return 200 with matching products")
  void searchProducts_Success() throws Exception {
    when(productService.searchProducts("beer")).thenReturn(List.of(testProduct));
    when(productMapper.toDTO(testProduct)).thenReturn(testProductDTO);

    mockMvc
        .perform(get("/api/products/search?keyword=beer"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Products found successfully"))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data[0].name").value("Test Beer"));
  }

  @Test
  @DisplayName("GET /api/products/search should return 200 with empty list when no matches")
  void searchProducts_NoMatches() throws Exception {
    when(productService.searchProducts("xyz")).thenReturn(List.of());

    mockMvc
        .perform(get("/api/products/search?keyword=xyz"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data.length()").value(0));
  }

  @Test
  @DisplayName("GET /api/products/search should return 400 on exception")
  void searchProducts_Exception() throws Exception {
    when(productService.searchProducts("beer")).thenThrow(new RuntimeException("Search error"));

    mockMvc
        .perform(get("/api/products/search?keyword=beer"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.containsString("Failed to search products")));
  }

  // ==================== GET PRODUCTS BY MERCHANT TESTS ====================

  @Test
  @DisplayName("GET /api/products/merchant/{merchantId} should return 200 with merchant's products")
  void getProductsByMerchant_Success() throws Exception {
    when(productService.getAvailableProductsByMerchant(1L)).thenReturn(List.of(testProduct));
    when(productMapper.toDTO(testProduct)).thenReturn(testProductDTO);

    mockMvc
        .perform(get("/api/products/merchant/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Merchant products retrieved successfully"))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data[0].name").value("Test Beer"));
  }

  @Test
  @DisplayName("GET /api/products/merchant/{merchantId} should return 200 with empty list")
  void getProductsByMerchant_EmptyList() throws Exception {
    when(productService.getAvailableProductsByMerchant(1L)).thenReturn(List.of());

    mockMvc
        .perform(get("/api/products/merchant/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data.length()").value(0));
  }

  @Test
  @DisplayName("GET /api/products/merchant/{merchantId} should return 400 on exception")
  void getProductsByMerchant_Exception() throws Exception {
    when(productService.getAvailableProductsByMerchant(1L))
        .thenThrow(new RuntimeException("Database error"));

    mockMvc
        .perform(get("/api/products/merchant/1"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(
                    org.hamcrest.Matchers.containsString("Failed to retrieve merchant products")));
  }

  // ==================== CHECK PRODUCT AVAILABILITY TESTS ====================

  @Test
  @DisplayName("GET /api/products/{id}/available should return true when available")
  void isProductAvailable_Available() throws Exception {
    when(productService.isProductAvailable(1L)).thenReturn(true);

    mockMvc
        .perform(get("/api/products/1/available"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Product availability checked successfully"))
        .andExpect(jsonPath("$.data").value(true));
  }

  @Test
  @DisplayName("GET /api/products/{id}/available should return false when not available")
  void isProductAvailable_NotAvailable() throws Exception {
    when(productService.isProductAvailable(2L)).thenReturn(false);

    mockMvc
        .perform(get("/api/products/2/available"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data").value(false));
  }

  @Test
  @DisplayName("GET /api/products/{id}/available should return 400 on exception")
  void isProductAvailable_Exception() throws Exception {
    when(productService.isProductAvailable(1L)).thenThrow(new RuntimeException("Check failed"));

    mockMvc
        .perform(get("/api/products/1/available"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.containsString("Failed to check availability")));
  }

  // ==================== GET ALL PRODUCTS (ADMIN) TESTS ====================

  @Test
  @DisplayName("GET /api/products/all should return 200 with all products")
  void getAllProducts_Success() throws Exception {
    Product unavailableProduct =
        Product.builder().id(2L).name("Unavailable Beer").available(false).build();
    ProductDTO unavailableDTO =
        ProductDTO.builder().id(2L).name("Unavailable Beer").isAvailable(false).build();

    when(productService.getAllProducts()).thenReturn(List.of(testProduct, unavailableProduct));
    when(productMapper.toDTO(testProduct)).thenReturn(testProductDTO);
    when(productMapper.toDTO(unavailableProduct)).thenReturn(unavailableDTO);

    mockMvc
        .perform(get("/api/products/all"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("All products retrieved successfully"))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data.length()").value(2));
  }

  @Test
  @DisplayName("GET /api/products/all should return 400 on exception")
  void getAllProducts_Exception() throws Exception {
    when(productService.getAllProducts()).thenThrow(new RuntimeException("Database error"));

    mockMvc
        .perform(get("/api/products/all"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.containsString("Failed to retrieve all products")));
  }

  // ==================== GET ALL PRODUCTS BY MERCHANT TESTS ====================

  @Test
  @DisplayName("GET /api/products/merchant/{merchantId}/all should return 200 for admin")
  void getAllProductsByMerchant_AdminAccess() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(adminUser);
    when(productService.getProductsByMerchant(1L)).thenReturn(List.of(testProduct));
    when(productMapper.toDTO(testProduct)).thenReturn(testProductDTO);

    mockMvc
        .perform(get("/api/products/merchant/1/all"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Merchant products retrieved successfully"))
        .andExpect(jsonPath("$.data").isArray());
  }

  @Test
  @DisplayName(
      "GET /api/products/merchant/{merchantId}/all should return 200 for merchant admin viewing own merchant")
  void getAllProductsByMerchant_MerchantAdminOwnMerchant() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(merchantAdminUser);
    when(productService.getProductsByMerchant(1L)).thenReturn(List.of(testProduct));
    when(productMapper.toDTO(testProduct)).thenReturn(testProductDTO);

    mockMvc
        .perform(get("/api/products/merchant/1/all"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));
  }

  @Test
  @DisplayName(
      "GET /api/products/merchant/{merchantId}/all should return 403 when merchant admin tries to view other merchant")
  void getAllProductsByMerchant_MerchantAdminAccessDenied() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(merchantAdminUser);

    mockMvc
        .perform(get("/api/products/merchant/999/all"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(
                    org.hamcrest.Matchers.containsString(
                        "only view products for your own merchant")));
  }

  @Test
  @DisplayName("GET /api/products/merchant/{merchantId}/all should return 400 on exception")
  void getAllProductsByMerchant_Exception() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(adminUser);
    when(productService.getProductsByMerchant(1L))
        .thenThrow(new RuntimeException("Database error"));

    mockMvc
        .perform(get("/api/products/merchant/1/all"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.containsString("Failed to retrieve products")));
  }

  // ==================== ADD PRODUCT TESTS ====================

  @Test
  @DisplayName("POST /api/products should return 201 for admin")
  void addProduct_AdminSuccess() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(adminUser);
    when(productMapper.toEntity(any(CreateProductRequest.class))).thenReturn(testProduct);
    when(merchantService.getMerchantById(1L)).thenReturn(testMerchant);
    when(productService.addProduct(any(Product.class))).thenReturn(testProduct);
    when(productMapper.toDTO(testProduct)).thenReturn(testProductDTO);

    mockMvc
        .perform(
            post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCreateRequest)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Product added successfully"))
        .andExpect(jsonPath("$.data.id").value(1));
  }

  @Test
  @DisplayName("POST /api/products should return 201 for merchant admin adding to own merchant")
  void addProduct_MerchantAdminOwnMerchant() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(merchantAdminUser);
    when(productMapper.toEntity(any(CreateProductRequest.class))).thenReturn(testProduct);
    when(merchantService.getMerchantById(1L)).thenReturn(testMerchant);
    when(productService.addProduct(any(Product.class))).thenReturn(testProduct);
    when(productMapper.toDTO(testProduct)).thenReturn(testProductDTO);

    mockMvc
        .perform(
            post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCreateRequest)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true));
  }

  @Test
  @DisplayName(
      "POST /api/products should return 403 when merchant admin tries to add to other merchant")
  void addProduct_MerchantAdminAccessDenied() throws Exception {
    CreateProductRequest otherMerchantRequest =
        CreateProductRequest.builder()
            .name("New Beer")
            .price(new BigDecimal("7.99"))
            .isAlcohol(true)
            .alcoholContent(5.5)
            .merchantId(999L)
            .build();

    when(permissionService.getAuthenticatedUser(any())).thenReturn(merchantAdminUser);
    when(productMapper.toEntity(any(CreateProductRequest.class))).thenReturn(testProduct);
    when(merchantService.getMerchantById(999L)).thenReturn(Merchant.builder().id(999L).build());

    mockMvc
        .perform(
            post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(otherMerchantRequest)))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(
                    org.hamcrest.Matchers.containsString(
                        "only add products for your own merchant")));
  }

  @Test
  @DisplayName("POST /api/products should return 400 when merchant not found")
  void addProduct_MerchantNotFound() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(adminUser);
    when(productMapper.toEntity(any(CreateProductRequest.class))).thenReturn(testProduct);
    when(merchantService.getMerchantById(1L)).thenReturn(null);

    mockMvc
        .perform(
            post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCreateRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Merchant not found"));
  }

  @Test
  @DisplayName("POST /api/products should return 400 on exception")
  void addProduct_Exception() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(adminUser);
    when(productMapper.toEntity(any(CreateProductRequest.class))).thenReturn(testProduct);
    when(merchantService.getMerchantById(1L)).thenReturn(testMerchant);
    when(productService.addProduct(any(Product.class)))
        .thenThrow(new RuntimeException("Database error"));

    mockMvc
        .perform(
            post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCreateRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.containsString("Failed to add product")));
  }

  // ==================== UPDATE PRODUCT TESTS ====================

  @Test
  @DisplayName("PUT /api/products/{id} should return 200 for admin")
  void updateProduct_AdminSuccess() throws Exception {
    Product updatedProduct =
        Product.builder().id(1L).name("Updated Beer").merchant(testMerchant).build();
    ProductDTO updatedDTO = ProductDTO.builder().id(1L).name("Updated Beer").build();

    when(productService.getProductById(1L)).thenReturn(testProduct);
    when(permissionService.getAuthenticatedUser(any())).thenReturn(adminUser);
    when(productMapper.toEntity(any(ProductDTO.class))).thenReturn(testProduct);
    when(productService.updateProduct(eq(1L), any(Product.class))).thenReturn(updatedProduct);
    when(productMapper.toDTO(updatedProduct)).thenReturn(updatedDTO);

    mockMvc
        .perform(
            put("/api/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testProductDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Product updated successfully"));
  }

  @Test
  @DisplayName("PUT /api/products/{id} should return 200 for merchant admin updating own product")
  void updateProduct_MerchantAdminOwnProduct() throws Exception {
    when(productService.getProductById(1L)).thenReturn(testProduct);
    when(permissionService.getAuthenticatedUser(any())).thenReturn(merchantAdminUser);
    when(productMapper.toEntity(any(ProductDTO.class))).thenReturn(testProduct);
    when(productService.updateProduct(eq(1L), any(Product.class))).thenReturn(testProduct);
    when(productMapper.toDTO(testProduct)).thenReturn(testProductDTO);

    mockMvc
        .perform(
            put("/api/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testProductDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));
  }

  @Test
  @DisplayName("PUT /api/products/{id} should return 404 when product not found")
  void updateProduct_NotFound() throws Exception {
    when(productService.getProductById(999L)).thenReturn(null);

    mockMvc
        .perform(
            put("/api/products/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testProductDTO)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Product not found"));
  }

  @Test
  @DisplayName(
      "PUT /api/products/{id} should return 403 when merchant admin tries to update other merchant's product")
  void updateProduct_MerchantAdminAccessDenied() throws Exception {
    Merchant otherMerchant = Merchant.builder().id(999L).build();
    Product otherMerchantProduct = Product.builder().id(1L).merchant(otherMerchant).build();

    when(productService.getProductById(1L)).thenReturn(otherMerchantProduct);
    when(permissionService.getAuthenticatedUser(any())).thenReturn(merchantAdminUser);

    mockMvc
        .perform(
            put("/api/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testProductDTO)))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(
                    org.hamcrest.Matchers.containsString(
                        "only update products for your own merchant")));
  }

  @Test
  @DisplayName("PUT /api/products/{id} should return 403 when product has null merchant")
  void updateProduct_NullMerchant() throws Exception {
    Product productWithNullMerchant = Product.builder().id(1L).merchant(null).build();

    when(productService.getProductById(1L)).thenReturn(productWithNullMerchant);
    when(permissionService.getAuthenticatedUser(any())).thenReturn(merchantAdminUser);

    mockMvc
        .perform(
            put("/api/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testProductDTO)))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.success").value(false));
  }

  @Test
  @DisplayName("PUT /api/products/{id} should return 404 when update returns null")
  void updateProduct_UpdateReturnsNull() throws Exception {
    when(productService.getProductById(1L)).thenReturn(testProduct);
    when(permissionService.getAuthenticatedUser(any())).thenReturn(adminUser);
    when(productMapper.toEntity(any(ProductDTO.class))).thenReturn(testProduct);
    when(productService.updateProduct(eq(1L), any(Product.class))).thenReturn(null);

    mockMvc
        .perform(
            put("/api/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testProductDTO)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Failed to update product"));
  }

  @Test
  @DisplayName("PUT /api/products/{id} should return 400 on exception")
  void updateProduct_Exception() throws Exception {
    when(productService.getProductById(1L)).thenReturn(testProduct);
    when(permissionService.getAuthenticatedUser(any())).thenReturn(adminUser);
    when(productMapper.toEntity(any(ProductDTO.class))).thenReturn(testProduct);
    when(productService.updateProduct(eq(1L), any(Product.class)))
        .thenThrow(new RuntimeException("Update failed"));

    mockMvc
        .perform(
            put("/api/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testProductDTO)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.containsString("Failed to update product")));
  }

  // ==================== DELETE PRODUCT TESTS ====================

  @Test
  @DisplayName("DELETE /api/products/{id} should return 200 for admin")
  void deleteProduct_AdminSuccess() throws Exception {
    when(productService.getProductById(1L)).thenReturn(testProduct);
    when(permissionService.getAuthenticatedUser(any())).thenReturn(adminUser);
    doNothing().when(productService).deleteProduct(1L);

    mockMvc
        .perform(delete("/api/products/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Product deleted successfully"));

    verify(productService, times(1)).deleteProduct(1L);
  }

  @Test
  @DisplayName(
      "DELETE /api/products/{id} should return 200 for merchant admin deleting own product")
  void deleteProduct_MerchantAdminOwnProduct() throws Exception {
    when(productService.getProductById(1L)).thenReturn(testProduct);
    when(permissionService.getAuthenticatedUser(any())).thenReturn(merchantAdminUser);
    doNothing().when(productService).deleteProduct(1L);

    mockMvc
        .perform(delete("/api/products/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    verify(productService, times(1)).deleteProduct(1L);
  }

  @Test
  @DisplayName("DELETE /api/products/{id} should return 404 when product not found")
  void deleteProduct_NotFound() throws Exception {
    when(productService.getProductById(999L)).thenReturn(null);

    mockMvc
        .perform(delete("/api/products/999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Product not found"));

    verify(productService, never()).deleteProduct(any());
  }

  @Test
  @DisplayName(
      "DELETE /api/products/{id} should return 403 when merchant admin tries to delete other merchant's product")
  void deleteProduct_MerchantAdminAccessDenied() throws Exception {
    Merchant otherMerchant = Merchant.builder().id(999L).build();
    Product otherMerchantProduct = Product.builder().id(1L).merchant(otherMerchant).build();

    when(productService.getProductById(1L)).thenReturn(otherMerchantProduct);
    when(permissionService.getAuthenticatedUser(any())).thenReturn(merchantAdminUser);

    mockMvc
        .perform(delete("/api/products/1"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(
                    org.hamcrest.Matchers.containsString(
                        "only delete products for your own merchant")));

    verify(productService, never()).deleteProduct(any());
  }

  @Test
  @DisplayName("DELETE /api/products/{id} should return 403 when product has null merchant")
  void deleteProduct_NullMerchant() throws Exception {
    Product productWithNullMerchant = Product.builder().id(1L).merchant(null).build();

    when(productService.getProductById(1L)).thenReturn(productWithNullMerchant);
    when(permissionService.getAuthenticatedUser(any())).thenReturn(merchantAdminUser);

    mockMvc
        .perform(delete("/api/products/1"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.success").value(false));

    verify(productService, never()).deleteProduct(any());
  }

  @Test
  @DisplayName("DELETE /api/products/{id} should return 400 on exception")
  void deleteProduct_Exception() throws Exception {
    when(productService.getProductById(1L)).thenReturn(testProduct);
    when(permissionService.getAuthenticatedUser(any())).thenReturn(adminUser);
    doThrow(new RuntimeException("Delete failed")).when(productService).deleteProduct(1L);

    mockMvc
        .perform(delete("/api/products/1"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.containsString("Failed to delete product")));
  }
}
