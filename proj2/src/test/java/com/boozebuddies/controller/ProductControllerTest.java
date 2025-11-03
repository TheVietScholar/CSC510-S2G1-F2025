package com.boozebuddies.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.boozebuddies.dto.ApiResponse;
import com.boozebuddies.dto.CreateProductRequest;
import com.boozebuddies.dto.ProductDTO;
import com.boozebuddies.entity.Product;
import com.boozebuddies.entity.User;
import com.boozebuddies.mapper.ProductMapper;
import com.boozebuddies.model.Role;
import com.boozebuddies.service.PermissionService;
import com.boozebuddies.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductControllerTest {

    @Mock private ProductService productService;
    @Mock private ProductMapper productMapper;
    @Mock private PermissionService permissionService;
    @InjectMocks private ProductController productController;

    private Product testProduct;
    private ProductDTO testProductDTO;
    private CreateProductRequest testCreateRequest;
    private Authentication mockAuth;
    private User mockUser;

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
                .id(1L)
                .name("Test Beer")
                .price(new BigDecimal("8.99"))
                .available(true)
                .build();

        testProductDTO = ProductDTO.builder()
                .id(1L)
                .name("Test Beer")
                .price(new BigDecimal("8.99"))
                .available(true)
                .build();

        testCreateRequest = CreateProductRequest.builder()
                .name("New Beer")
                .price(new BigDecimal("7.99"))
                .isAlcohol(true)
                .alcoholContent(5.5)
                .build();

        mockAuth = mock(Authentication.class);
        mockUser = mock(User.class);
    }

    private <T> T getData(ApiResponse<T> response) {
        return response != null ? response.getData() : null;
    }

    @Test
    void testGetAllAvailableProducts_ReturnsProducts() {
        List<Product> products = Arrays.asList(testProduct);
        when(productService.getAvailableProducts()).thenReturn(products);
        when(productMapper.toDTO(testProduct)).thenReturn(testProductDTO);

        var response = productController.getAllAvailableProducts();
        List<ProductDTO> data = getData(response.getBody());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(data);
        assertEquals(1, data.size());
        assertEquals("Test Beer", data.get(0).getName());

        verify(productService, times(1)).getAvailableProducts();
        verify(productMapper, times(1)).toDTO(testProduct);
    }

    @Test
    void testGetProductById_ExistingProduct_ReturnsProduct() {
        when(productService.getProductById(1L)).thenReturn(testProduct);
        when(productMapper.toDTO(testProduct)).thenReturn(testProductDTO);

        var response = productController.getProductById(1L);
        ProductDTO data = getData(response.getBody());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(data);
        assertEquals(1L, data.getId());
        assertEquals("Test Beer", data.getName());

        verify(productService, times(1)).getProductById(1L);
        verify(productMapper, times(1)).toDTO(testProduct);
    }

    @Test
    void testGetProductById_NonExistingProduct_ReturnsNotFound() {
        when(productService.getProductById(999L)).thenReturn(null);

        var response = productController.getProductById(999L);
        ProductDTO data = getData(response.getBody());

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(data);

        verify(productService, times(1)).getProductById(999L);
        verify(productMapper, never()).toDTO(any());
    }

    @Test
    void testSearchProducts_WithKeyword_ReturnsMatchingProducts() {
        List<Product> searchResults = Arrays.asList(testProduct);
        when(productService.searchProducts("beer")).thenReturn(searchResults);
        when(productMapper.toDTO(testProduct)).thenReturn(testProductDTO);

        var response = productController.searchProducts("beer");
        List<ProductDTO> data = getData(response.getBody());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(data);
        assertEquals(1, data.size());

        verify(productService, times(1)).searchProducts("beer");
        verify(productMapper, times(1)).toDTO(testProduct);
    }

    @Test
    void testIsProductAvailable_AvailableProduct_ReturnsTrue() {
        when(productService.isProductAvailable(1L)).thenReturn(true);

        var response = productController.isProductAvailable(1L);
        Boolean available = getData(response.getBody());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(available);
        assertTrue(available);

        verify(productService, times(1)).isProductAvailable(1L);
    }

    @Test
    void testIsProductAvailable_NotAvailableProduct_ReturnsFalse() {
        when(productService.isProductAvailable(2L)).thenReturn(false);

        var response = productController.isProductAvailable(2L);
        Boolean available = getData(response.getBody());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(available);
        assertFalse(available);

        verify(productService, times(1)).isProductAvailable(2L);
    }

    @Test
    void testAddProduct_ValidRequest_ReturnsCreatedProduct() {
        when(permissionService.getAuthenticatedUser(mockAuth)).thenReturn(mockUser);
        when(productMapper.toEntity(testCreateRequest)).thenReturn(testProduct);
        when(productService.addProduct(testProduct)).thenReturn(testProduct);
        when(productMapper.toDTO(testProduct)).thenReturn(testProductDTO);

        var response = productController.addProduct(testCreateRequest, mockAuth);
        ProductDTO data = getData(response.getBody());

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(data);
        assertEquals(1L, data.getId());

        verify(productMapper, times(1)).toEntity(testCreateRequest);
        verify(productService, times(1)).addProduct(testProduct);
        verify(productMapper, times(1)).toDTO(testProduct);
    }

    @Test
    void testUpdateProduct_ExistingProduct_ReturnsUpdatedProduct() {
        Product updatedProduct = Product.builder().id(1L).name("Updated Beer").build();
        when(productService.getProductById(1L)).thenReturn(testProduct);
        when(permissionService.getAuthenticatedUser(mockAuth)).thenReturn(mockUser);
        when(productMapper.toEntity(testProductDTO)).thenReturn(testProduct);
        when(productService.updateProduct(eq(1L), any(Product.class))).thenReturn(updatedProduct);
        when(productMapper.toDTO(updatedProduct)).thenReturn(testProductDTO);

        var response = productController.updateProduct(1L, testProductDTO, mockAuth);
        ProductDTO data = getData(response.getBody());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(data);

        verify(productMapper, times(1)).toEntity(testProductDTO);
        verify(productService, times(1)).updateProduct(eq(1L), any(Product.class));
        verify(productMapper, times(1)).toDTO(updatedProduct);
    }

    @Test
    void testUpdateProduct_NonExistingProduct_ReturnsNotFound() {
        when(productService.getProductById(999L)).thenReturn(null);
        // when(permissionService.getAuthenticatedUser(mockAuth)).thenReturn(mockUser);

        var response = productController.updateProduct(999L, testProductDTO, mockAuth);
        ProductDTO data = getData(response.getBody());

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(data);
    }

    @Test
    void testDeleteProduct_ExistingProduct_ReturnsSuccess() {
        when(productService.getProductById(1L)).thenReturn(testProduct);
        when(permissionService.getAuthenticatedUser(mockAuth)).thenReturn(mockUser);
        doNothing().when(productService).deleteProduct(1L);

        var response = productController.deleteProduct(1L, mockAuth);
        Void data = getData(response.getBody());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(data);

        verify(productService, times(1)).deleteProduct(1L);
    }

    @Test
    void testDeleteProduct_NonExistingProduct_ReturnsNotFound() {
        when(productService.getProductById(999L)).thenReturn(null);
        // when(permissionService.getAuthenticatedUser(mockAuth)).thenReturn(mockUser);

        var response = productController.deleteProduct(999L, mockAuth);
        Void data = getData(response.getBody());

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(data);
    }

    @Test
    void testGetAllProducts_Admin_ReturnsAllProducts() {
        List<Product> products = Arrays.asList(testProduct);
        when(productService.getAllProducts()).thenReturn(products);
        when(productMapper.toDTO(testProduct)).thenReturn(testProductDTO);

        var response = productController.getAllProducts();
        List<ProductDTO> data = getData(response.getBody());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(data);
        assertEquals(1, data.size());
    }

    @Test
    void testGetProductsByMerchant_ReturnsProducts() {
        List<Product> products = Arrays.asList(testProduct);
        when(productService.getAvailableProductsByMerchant(1L)).thenReturn(products);
        when(productMapper.toDTO(testProduct)).thenReturn(testProductDTO);

        var response = productController.getProductsByMerchant(1L);
        List<ProductDTO> data = getData(response.getBody());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(data);
        assertEquals(1, data.size());
    }
}
