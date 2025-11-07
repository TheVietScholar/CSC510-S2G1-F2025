package com.boozebuddies.service.implementation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.boozebuddies.dto.ProductDTO;
import com.boozebuddies.entity.Category;
import com.boozebuddies.entity.Merchant;
import com.boozebuddies.entity.Product;
import com.boozebuddies.mapper.ProductMapper;
import com.boozebuddies.repository.CategoryRepository;
import com.boozebuddies.repository.MerchantRepository;
import com.boozebuddies.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

  @Mock private ProductRepository productRepository;
  @Mock private CategoryRepository categoryRepository;
  @Mock private MerchantRepository merchantRepository;
  @Mock private ProductMapper productMapper;

  private ProductServiceImpl productService;

  private Merchant testMerchant;
  private Category testCategory;

  @BeforeEach
  void setUp() {
    productService =
        new ProductServiceImpl(
            productRepository, categoryRepository, merchantRepository, productMapper);

    testMerchant = Merchant.builder().id(1L).name("Test Merchant").build();
    testCategory = Category.builder().id(1L).name("Beer").build();
  }

  @Test
  void testGetAllProducts_ReturnsAllProducts() {
    Product product1 =
        Product.builder()
            .id(1L)
            .name("IPA")
            .price(new BigDecimal("8.99"))
            .merchant(testMerchant)
            .category(testCategory)
            .available(true)
            .build();

    Product product2 =
        Product.builder()
            .id(2L)
            .name("Stout")
            .price(new BigDecimal("9.99"))
            .merchant(testMerchant)
            .category(testCategory)
            .available(false)
            .build();

    when(productRepository.findAll()).thenReturn(Arrays.asList(product1, product2));

    List<Product> result = productService.getAllProducts();

    assertEquals(2, result.size());
    verify(productRepository, times(1)).findAll();
  }

  @Test
  void testGetProductById_ExistingId_ReturnsProduct() {
    Product product =
        Product.builder()
            .id(1L)
            .name("IPA")
            .price(new BigDecimal("8.99"))
            .merchant(testMerchant)
            .category(testCategory)
            .available(true)
            .build();

    when(productRepository.findById(1L)).thenReturn(Optional.of(product));

    Product result = productService.getProductById(1L);

    assertNotNull(result);
    assertEquals(1L, result.getId());
    assertEquals("IPA", result.getName());
    verify(productRepository, times(1)).findById(1L);
  }

  @Test
  void testGetProductById_NonExistingId_ReturnsNull() {
    when(productRepository.findById(999L)).thenReturn(Optional.empty());

    Product result = productService.getProductById(999L);

    assertNull(result);
    verify(productRepository, times(1)).findById(999L);
  }

  @Test
  void testAddProduct_ValidProduct_ReturnsSavedProduct() {
    Product productToSave =
        Product.builder()
            .name("New Beer")
            .price(new BigDecimal("7.99"))
            .merchant(testMerchant)
            .category(testCategory)
            .available(true)
            .build();

    Product savedProduct =
        Product.builder()
            .id(1L)
            .name("New Beer")
            .price(new BigDecimal("7.99"))
            .merchant(testMerchant)
            .category(testCategory)
            .available(true)
            .build();

    when(productRepository.save(productToSave)).thenReturn(savedProduct);

    Product result = productService.addProduct(productToSave);

    assertNotNull(result);
    assertEquals(1L, result.getId());
    assertEquals("New Beer", result.getName());
    verify(productRepository, times(1)).save(productToSave);
  }

  @Test
  void testCreateProduct_ValidProductDTO_ReturnsSavedProduct() {
    ProductDTO productDTO =
        ProductDTO.builder()
            .name("New Beer")
            .description("Test description")
            .price(new BigDecimal("7.99"))
            .categoryId(1L)
            .merchantId(1L)
            .isAlcohol(true)
            .alcoholContent(5.5)
            .isAvailable(true)
            .imageUrl("/default.jpg")
            .build();

    Product productEntity =
        Product.builder()
            .name("New Beer")
            .description("Test description")
            .price(new BigDecimal("7.99"))
            .isAlcohol(true)
            .alcoholContent(5.5)
            .available(true)
            .imageUrl("/default.jpg")
            .build();

    Product savedProduct =
        Product.builder()
            .id(1L)
            .name("New Beer")
            .description("Test description")
            .price(new BigDecimal("7.99"))
            .merchant(testMerchant)
            .category(testCategory)
            .isAlcohol(true)
            .alcoholContent(5.5)
            .available(true)
            .imageUrl("/default.jpg")
            .build();

    when(productMapper.toEntity(productDTO)).thenReturn(productEntity);
    when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
    when(merchantRepository.findById(1L)).thenReturn(Optional.of(testMerchant));
    when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

    Product result = productService.createProduct(productDTO);

    assertNotNull(result);
    assertEquals(1L, result.getId());
    assertEquals("New Beer", result.getName());
    assertEquals(testMerchant, result.getMerchant());
    assertEquals(testCategory, result.getCategory());
    verify(productRepository, times(1)).save(any(Product.class));
  }

  @Test
  void testCreateProduct_CategoryNotFound_ThrowsException() {
    ProductDTO productDTO =
        ProductDTO.builder().name("New Beer").categoryId(999L).merchantId(1L).build();

    Product productEntity = Product.builder().name("New Beer").build();

    when(productMapper.toEntity(productDTO)).thenReturn(productEntity);
    when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class, () -> productService.createProduct(productDTO));
    verify(productRepository, never()).save(any());
  }

  @Test
  void testCreateProduct_MerchantNotFound_ThrowsException() {
    ProductDTO productDTO =
        ProductDTO.builder().name("New Beer").categoryId(1L).merchantId(999L).build();

    Product productEntity = Product.builder().name("New Beer").build();

    when(productMapper.toEntity(productDTO)).thenReturn(productEntity);
    when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
    when(merchantRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class, () -> productService.createProduct(productDTO));
    verify(productRepository, never()).save(any());
  }

  @Test
  void testUpdateProduct_ExistingProduct_ReturnsUpdatedProduct() {
    Product existingProduct =
        Product.builder()
            .id(1L)
            .name("Old Name")
            .price(new BigDecimal("5.99"))
            .merchant(testMerchant)
            .category(testCategory)
            .available(true)
            .build();

    Product updatedProduct =
        Product.builder()
            .name("New Name")
            .price(new BigDecimal("6.99"))
            .merchant(testMerchant)
            .category(testCategory)
            .available(false)
            .isAlcohol(true)
            .alcoholContent(5.5)
            .description("New description")
            .imageUrl("new-image.jpg")
            .build();

    when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
    when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

    Product result = productService.updateProduct(1L, updatedProduct);

    assertNotNull(result);
    assertEquals("New Name", result.getName());
    assertEquals(new BigDecimal("6.99"), result.getPrice());
    assertFalse(result.isAvailable());
    assertTrue(result.isAlcohol());
    assertEquals(5.5, result.getAlcoholContent());
    assertEquals("New description", result.getDescription());
    assertEquals("new-image.jpg", result.getImageUrl());
    verify(productRepository, times(1)).findById(1L);
    verify(productRepository, times(1)).save(existingProduct);
  }

  @Test
  void testUpdateProductWithDTO_ValidData_ReturnsUpdatedProduct() {
    Product existingProduct =
        Product.builder()
            .id(1L)
            .name("Old Name")
            .price(new BigDecimal("5.99"))
            .merchant(testMerchant)
            .category(testCategory)
            .available(true)
            .build();

    ProductDTO productDTO =
        ProductDTO.builder()
            .name("New Name")
            .description("New description")
            .price(new BigDecimal("6.99"))
            .categoryId(1L)
            .isAvailable(false)
            .isAlcohol(true)
            .alcoholContent(5.5)
            .imageUrl("new-image.jpg")
            .build();

    Product updatedProduct =
        Product.builder()
            .id(1L)
            .name("New Name")
            .description("New description")
            .price(new BigDecimal("6.99"))
            .merchant(testMerchant)
            .category(testCategory)
            .available(false)
            .isAlcohol(true)
            .alcoholContent(5.5)
            .imageUrl("new-image.jpg")
            .build();

    when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
    when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
    when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

    Product result = productService.updateProduct(1L, productDTO);

    assertNotNull(result);
    assertEquals("New Name", result.getName());
    assertEquals(new BigDecimal("6.99"), result.getPrice());
    assertFalse(result.isAvailable());
    verify(productRepository, times(1)).findById(1L);
    verify(productRepository, times(1)).save(any(Product.class));
  }

  @Test
  void testUpdateProduct_NonExistingProduct_ThrowsException() {
    Product updatedProduct =
        Product.builder()
            .name("New Name")
            .price(new BigDecimal("6.99"))
            .merchant(testMerchant)
            .category(testCategory)
            .build();

    when(productRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(RuntimeException.class, () -> productService.updateProduct(999L, updatedProduct));

    verify(productRepository, times(1)).findById(999L);
    verify(productRepository, never()).save(any());
  }

  @Test
  void testDeleteProduct_CallsRepositoryDelete() {
    Product testProduct =
        Product.builder()
            .id(1L)
            .name("To Be Deleted")
            .price(new BigDecimal("4.99"))
            .merchant(testMerchant)
            .category(testCategory)
            .available(true)
            .build();

    when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

    productService.deleteProduct(1L);

    verify(productRepository, times(1)).deleteById(1L);
  }

  @Test
  void testSearchProducts_WithKeyword_ReturnsMatchingProducts() {
    Product matchingProduct1 =
        Product.builder()
            .id(1L)
            .name("Craft IPA Beer")
            .price(new BigDecimal("8.99"))
            .merchant(testMerchant)
            .category(testCategory)
            .available(true)
            .build();

    Product matchingProduct2 =
        Product.builder()
            .id(2L)
            .name("Lager")
            .price(new BigDecimal("7.99"))
            .merchant(testMerchant)
            .category(testCategory)
            .available(true)
            .build();

    when(productRepository.searchByKeyword("beer"))
        .thenReturn(Arrays.asList(matchingProduct1, matchingProduct2));

    List<Product> result = productService.searchProducts("beer");

    assertEquals(2, result.size());
    assertTrue(result.stream().anyMatch(p -> p.getName().contains("IPA")));
    verify(productRepository, times(1)).searchByKeyword("beer");
  }

  @Test
  void testSearchProducts_EmptyKeyword_ReturnsAllProducts() {
    Product product1 = Product.builder().id(1L).name("Product1").build();
    Product product2 = Product.builder().id(2L).name("Product2").build();

    when(productRepository.findByAvailableTrue()).thenReturn(Arrays.asList(product1, product2));

    List<Product> result = productService.searchProducts("");

    assertEquals(2, result.size());
    verify(productRepository, times(1)).findByAvailableTrue();
  }

  @Test
  void testSearchProducts_NullKeyword_ReturnsAllProducts() {
    Product product1 = Product.builder().id(1L).name("Product1").build();
    Product product2 = Product.builder().id(2L).name("Product2").build();

    when(productRepository.findByAvailableTrue()).thenReturn(Arrays.asList(product1, product2));

    List<Product> result = productService.searchProducts(null);

    assertEquals(2, result.size());
    verify(productRepository, times(1)).findByAvailableTrue();
  }

  @Test
  void testIsProductAvailable_AvailableProduct_ReturnsTrue() {
    Product availableProduct =
        Product.builder().id(1L).name("Available Beer").available(true).build();

    when(productRepository.findById(1L)).thenReturn(Optional.of(availableProduct));

    boolean result = productService.isProductAvailable(1L);

    assertTrue(result);
    verify(productRepository, times(1)).findById(1L);
  }

  @Test
  void testIsProductAvailable_NotAvailableProduct_ReturnsFalse() {
    Product unavailableProduct =
        Product.builder().id(1L).name("Unavailable Beer").available(false).build();

    when(productRepository.findById(1L)).thenReturn(Optional.of(unavailableProduct));

    boolean result = productService.isProductAvailable(1L);

    assertFalse(result);
    verify(productRepository, times(1)).findById(1L);
  }

  @Test
  void testIsProductAvailable_NonExistingProduct_ReturnsFalse() {
    when(productRepository.findById(999L)).thenReturn(Optional.empty());

    boolean result = productService.isProductAvailable(999L);

    assertFalse(result);
    verify(productRepository, times(1)).findById(999L);
  }

  @Test
  void testGetAvailableProducts_ReturnsOnlyAvailableProducts() {
    Product availableProduct1 =
        Product.builder().id(1L).name("Available 1").available(true).build();

    Product availableProduct2 =
        Product.builder().id(2L).name("Available 2").available(true).build();

    Product unavailableProduct =
        Product.builder().id(3L).name("Unavailable").available(false).build();

    when(productRepository.findByAvailableTrue())
        .thenReturn(Arrays.asList(availableProduct1, availableProduct2));

    List<Product> result = productService.getAvailableProducts();

    assertEquals(2, result.size());
    assertTrue(result.stream().allMatch(Product::isAvailable));
    assertTrue(result.stream().noneMatch(p -> !p.isAvailable()));
  }

  @Test
  void testGetProductsByMerchant_ReturnsCorrectMerchantProducts() {
    Merchant merchant1 = Merchant.builder().id(1L).build();
    Merchant merchant2 = Merchant.builder().id(2L).build();

    Product merchant1Product =
        Product.builder()
            .id(1L)
            .name("Merchant1 Product")
            .merchant(merchant1)
            .available(true)
            .build();

    Product merchant2Product =
        Product.builder()
            .id(2L)
            .name("Merchant2 Product")
            .merchant(merchant2)
            .available(true)
            .build();

    when(productRepository.findByMerchantId(1L)).thenReturn(Arrays.asList(merchant1Product));

    List<Product> result = productService.getProductsByMerchant(1L);

    assertEquals(1, result.size());
    assertEquals("Merchant1 Product", result.get(0).getName());
    assertEquals(1L, result.get(0).getMerchant().getId());
  }

  @Test
  void testGetAvailableProductsByMerchant_ReturnsAvailableProductsForMerchant() {
    Merchant merchant1 = Merchant.builder().id(1L).build();

    Product availableProduct =
        Product.builder()
            .id(1L)
            .name("Available Product")
            .merchant(merchant1)
            .available(true)
            .build();

    Product unavailableProduct =
        Product.builder()
            .id(2L)
            .name("Unavailable Product")
            .merchant(merchant1)
            .available(false)
            .build();

    when(productRepository.findByMerchantIdAndAvailableTrue(1L))
        .thenReturn(Arrays.asList(availableProduct));

    List<Product> result = productService.getAvailableProductsByMerchant(1L);

    assertEquals(1, result.size());
    assertEquals("Available Product", result.get(0).getName());
    assertTrue(result.get(0).isAvailable());
  }

  @Test
  void testAddProduct_NullProduct_ThrowsException() {
    assertThrows(IllegalArgumentException.class, () -> productService.addProduct(null));
    verify(productRepository, never()).save(any());
  }

  @Test
  void testAddProduct_NoMerchant_ThrowsException() {
    Product product =
        Product.builder()
            .name("No Merchant Beer")
            .price(new BigDecimal("5.00"))
            .available(true)
            .build();

    assertThrows(IllegalArgumentException.class, () -> productService.addProduct(product));
    verify(productRepository, never()).save(any());
  }

  @Test
  void testAddProduct_InvalidPrice_ThrowsException() {
    Product product =
        Product.builder()
            .name("Cheap Beer")
            .price(new BigDecimal("-1.00"))
            .merchant(testMerchant)
            .category(testCategory)
            .available(true)
            .build();

    assertThrows(IllegalArgumentException.class, () -> productService.addProduct(product));
    verify(productRepository, never()).save(any());
  }

  @Test
  void testAddProduct_InvalidAlcoholContent_ThrowsException() {
    Product product =
        Product.builder()
            .name("Crazy Beer")
            .price(new BigDecimal("5.00"))
            .merchant(testMerchant)
            .category(testCategory)
            .available(true)
            .alcoholContent(120.0)
            .build();

    assertThrows(IllegalArgumentException.class, () -> productService.addProduct(product));
    verify(productRepository, never()).save(any());
  }

  @Test
  void testAddProduct_InvalidVolume_ThrowsException() {
    Product product =
        Product.builder()
            .name("Tiny Beer")
            .price(new BigDecimal("5.00"))
            .merchant(testMerchant)
            .category(testCategory)
            .available(true)
            .volume(0)
            .build();

    assertThrows(IllegalArgumentException.class, () -> productService.addProduct(product));
    verify(productRepository, never()).save(any());
  }

  @Test
  void testDeleteProduct_ProductNotFound_ThrowsException() {
    when(productRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(RuntimeException.class, () -> productService.deleteProduct(999L));
    verify(productRepository, times(1)).findById(999L);
    verify(productRepository, never()).save(any());
  }

  @Test
  void testGetProductsByCategory_ReturnsProducts() {
    Product p1 = Product.builder().id(1L).name("IPA").category(testCategory).build();
    Product p2 = Product.builder().id(2L).name("Stout").category(testCategory).build();

    when(productRepository.findByCategoryId(1L)).thenReturn(Arrays.asList(p1, p2));

    List<Product> result = productService.getProductsByCategory(1L);

    assertEquals(2, result.size());
    assertEquals("IPA", result.get(0).getName());
    verify(productRepository, times(1)).findByCategoryId(1L);
  }

  @Test
  void testGetAvailableProductsByCategory_ReturnsAvailableOnly() {
    Product available = Product.builder().id(1L).name("Available IPA").available(true).build();
    when(productRepository.findByCategoryIdAndAvailableTrue(1L))
        .thenReturn(Arrays.asList(available));

    List<Product> result = productService.getAvailableProductsByCategory(1L);

    assertEquals(1, result.size());
    assertTrue(result.get(0).isAvailable());
    verify(productRepository, times(1)).findByCategoryIdAndAvailableTrue(1L);
  }
}
