package com.boozebuddies.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.boozebuddies.dto.CreateProductRequest;
import com.boozebuddies.dto.ProductDTO;
import com.boozebuddies.entity.Product;
import com.boozebuddies.mapper.ProductMapper;
import com.boozebuddies.service.ProductService;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
public class ProductControllerTest {

  @Mock private ProductService productService;

  @Mock private ProductMapper productMapper;

  @InjectMocks private ProductController productController;

  private Product testProduct;
  private ProductDTO testProductDTO;
  private CreateProductRequest testCreateRequest;

  @BeforeEach
  void setUp() {
    testProduct =
        Product.builder()
            .id(1L)
            .name("Test Beer")
            .price(new BigDecimal("8.99"))
            .available(true)
            .build();

    testProductDTO =
        ProductDTO.builder()
            .id(1L)
            .name("Test Beer")
            .price(new BigDecimal("8.99"))
            .available(true)
            .build();

    testCreateRequest =
        CreateProductRequest.builder()
            .name("New Beer")
            .price(new BigDecimal("7.99"))
            .isAlcohol(true)
            .alcoholContent(5.5)
            .build();
  }

  @Test
  void testGetAllProducts_ReturnsProducts() {
    List<Product> products = Arrays.asList(testProduct);
    List<ProductDTO> productDTOs = Arrays.asList(testProductDTO);

    when(productService.getAllProducts()).thenReturn(products);
    when(productMapper.toDTO(testProduct)).thenReturn(testProductDTO);

    ResponseEntity<List<ProductDTO>> response = productController.getAllProducts();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().size());
    assertEquals("Test Beer", response.getBody().get(0).getName());

    verify(productService, times(1)).getAllProducts();
    verify(productMapper, times(1)).toDTO(testProduct);
  }

  @Test
  void testGetAllProducts_EmptyList_ReturnsEmptyList() {
    when(productService.getAllProducts()).thenReturn(Collections.emptyList());

    ResponseEntity<List<ProductDTO>> response = productController.getAllProducts();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().isEmpty());

    verify(productService, times(1)).getAllProducts();
  }

  @Test
  void testGetAvailableProducts_ReturnsAvailableProducts() {
    List<Product> availableProducts = Arrays.asList(testProduct);
    List<ProductDTO> productDTOs = Arrays.asList(testProductDTO);

    when(productService.getAvailableProducts()).thenReturn(availableProducts);
    when(productMapper.toDTO(testProduct)).thenReturn(testProductDTO);

    ResponseEntity<List<ProductDTO>> response = productController.getAvailableProducts();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().size());
    assertTrue(response.getBody().get(0).isAvailable());

    verify(productService, times(1)).getAvailableProducts();
    verify(productMapper, times(1)).toDTO(testProduct);
  }

  @Test
  void testGetProductById_ExistingProduct_ReturnsProduct() {
    when(productService.getProductById(1L)).thenReturn(testProduct);
    when(productMapper.toDTO(testProduct)).thenReturn(testProductDTO);

    ResponseEntity<ProductDTO> response = productController.getProductById(1L);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1L, response.getBody().getId());
    assertEquals("Test Beer", response.getBody().getName());

    verify(productService, times(1)).getProductById(1L);
    verify(productMapper, times(1)).toDTO(testProduct);
  }

  @Test
  void testGetProductById_NonExistingProduct_ReturnsNotFound() {
    when(productService.getProductById(999L)).thenReturn(null);

    ResponseEntity<ProductDTO> response = productController.getProductById(999L);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertNull(response.getBody());

    verify(productService, times(1)).getProductById(999L);
    verify(productMapper, never()).toDTO(any());
  }

  @Test
  void testAddProduct_ValidRequest_ReturnsCreatedProduct() {
    Product savedProduct = Product.builder().id(1L).name("New Beer").build();

    when(productMapper.toEntity(testCreateRequest)).thenReturn(testProduct);
    when(productService.addProduct(testProduct)).thenReturn(savedProduct);
    when(productMapper.toDTO(savedProduct)).thenReturn(testProductDTO);

    ResponseEntity<ProductDTO> response = productController.addProduct(testCreateRequest);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1L, response.getBody().getId());

    verify(productMapper, times(1)).toEntity(testCreateRequest);
    verify(productService, times(1)).addProduct(testProduct);
    verify(productMapper, times(1)).toDTO(savedProduct);
  }

  @Test
  void testUpdateProduct_ExistingProduct_ReturnsUpdatedProduct() {
    Product updatedProduct = Product.builder().id(1L).name("Updated Beer").build();

    when(productMapper.toEntity(testProductDTO)).thenReturn(testProduct);
    when(productService.updateProduct(eq(1L), any(Product.class))).thenReturn(updatedProduct);
    when(productMapper.toDTO(updatedProduct)).thenReturn(testProductDTO);

    ResponseEntity<ProductDTO> response = productController.updateProduct(1L, testProductDTO);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());

    verify(productMapper, times(1)).toEntity(testProductDTO);
    verify(productService, times(1)).updateProduct(eq(1L), any(Product.class));
    verify(productMapper, times(1)).toDTO(updatedProduct);
  }

  @Test
  void testUpdateProduct_NonExistingProduct_ReturnsNotFound() {
    when(productMapper.toEntity(testProductDTO)).thenReturn(testProduct);
    when(productService.updateProduct(eq(999L), any(Product.class))).thenReturn(null);

    ResponseEntity<ProductDTO> response = productController.updateProduct(999L, testProductDTO);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertNull(response.getBody());

    verify(productMapper, times(1)).toEntity(testProductDTO);
    verify(productService, times(1)).updateProduct(eq(999L), any(Product.class));
    verify(productMapper, never()).toDTO(any());
  }

  @Test
  void testDeleteProduct_CallsService() {
    doNothing().when(productService).deleteProduct(1L);

    ResponseEntity<Void> response = productController.deleteProduct(1L);

    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    assertNull(response.getBody());

    verify(productService, times(1)).deleteProduct(1L);
  }

  @Test
  void testSearchProducts_WithKeyword_ReturnsMatchingProducts() {
    List<Product> searchResults = Arrays.asList(testProduct);
    List<ProductDTO> productDTOs = Arrays.asList(testProductDTO);

    when(productService.searchProducts("beer")).thenReturn(searchResults);
    when(productMapper.toDTO(testProduct)).thenReturn(testProductDTO);

    ResponseEntity<List<ProductDTO>> response = productController.searchProducts("beer");

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().size());

    verify(productService, times(1)).searchProducts("beer");
    verify(productMapper, times(1)).toDTO(testProduct);
  }

  @Test
  void testIsProductAvailable_AvailableProduct_ReturnsTrue() {
    when(productService.isProductAvailable(1L)).thenReturn(true);

    ResponseEntity<Boolean> response = productController.isProductAvailable(1L);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody());

    verify(productService, times(1)).isProductAvailable(1L);
  }

  @Test
  void testIsProductAvailable_NotAvailableProduct_ReturnsFalse() {
    when(productService.isProductAvailable(2L)).thenReturn(false);

    ResponseEntity<Boolean> response = productController.isProductAvailable(2L);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertFalse(response.getBody());

    verify(productService, times(1)).isProductAvailable(2L);
  }

  @Test
  void testGetProductsByMerchant_ReturnsMerchantProducts() {
    List<Product> merchantProducts = Arrays.asList(testProduct);
    List<ProductDTO> productDTOs = Arrays.asList(testProductDTO);

    when(productService.getProductsByMerchant(1L)).thenReturn(merchantProducts);
    when(productMapper.toDTO(testProduct)).thenReturn(testProductDTO);

    ResponseEntity<List<ProductDTO>> response = productController.getProductsByMerchant(1L);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().size());

    verify(productService, times(1)).getProductsByMerchant(1L);
    verify(productMapper, times(1)).toDTO(testProduct);
  }

  @Test
  void testGetAvailableProductsByMerchant_ReturnsAvailableMerchantProducts() {
    List<Product> availableProducts = Arrays.asList(testProduct);
    List<ProductDTO> productDTOs = Arrays.asList(testProductDTO);

    when(productService.getAvailableProductsByMerchant(1L)).thenReturn(availableProducts);
    when(productMapper.toDTO(testProduct)).thenReturn(testProductDTO);

    ResponseEntity<List<ProductDTO>> response =
        productController.getAvailableProductsByMerchant(1L);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().size());

    verify(productService, times(1)).getAvailableProductsByMerchant(1L);
    verify(productMapper, times(1)).toDTO(testProduct);
  }
}
