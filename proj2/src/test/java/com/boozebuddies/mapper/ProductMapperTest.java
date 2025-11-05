package com.boozebuddies.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.boozebuddies.dto.CreateProductRequest;
import com.boozebuddies.dto.ProductDTO;
import com.boozebuddies.entity.Merchant;
import com.boozebuddies.entity.Product;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProductMapperTest {

  private ProductMapper productMapper;

  @BeforeEach
  void setUp() {
    productMapper = new ProductMapper();
  }

  @Test
  @DisplayName("toDTO converts Product entity to ProductDTO correctly")
  void testToDTO() {
    Merchant merchant = Merchant.builder().id(10L).name("Test Merchant").build();

    Product product =
        Product.builder()
            .id(1L)
            .name("Beer")
            .description("Craft beer")
            .price(new BigDecimal("5.99"))
            .isAlcohol(true)
            .alcoholContent(5.5)
            .imageUrl("http://image.url/beer.png")
            .available(true)
            .merchant(merchant)
            .build();

    ProductDTO dto = productMapper.toDTO(product);

    assertNotNull(dto);
    assertEquals(1L, dto.getId());
    assertEquals("Beer", dto.getName());
    assertEquals("Craft beer", dto.getDescription());
    assertEquals(new BigDecimal("5.99"), dto.getPrice());
    assertTrue(dto.isAlcohol());
    assertEquals(5.5, dto.getAlcoholContent());
    assertEquals("http://image.url/beer.png", dto.getImageUrl());
    assertTrue(dto.isAvailable());
    assertEquals(10L, dto.getMerchantId());
    assertEquals("Test Merchant", dto.getMerchantName());
  }

  @Test
  @DisplayName("toEntity converts ProductDTO to Product entity correctly")
  void testToEntityFromDTO() {
    ProductDTO dto =
        ProductDTO.builder()
            .id(1L)
            .name("Beer")
            .description("Craft beer")
            .price(new BigDecimal("5.99"))
            .isAlcohol(true)
            .alcoholContent(5.5)
            .imageUrl("http://image.url/beer.png")
            .isAvailable(true)
            .build();

    Product product = productMapper.toEntity(dto);

    assertNotNull(product);
    assertEquals(1L, product.getId());
    assertEquals("Beer", product.getName());
    assertEquals("Craft beer", product.getDescription());
    assertEquals(new BigDecimal("5.99"), product.getPrice());
    assertTrue(product.isAlcohol());
    assertEquals(5.5, product.getAlcoholContent());
    assertEquals("http://image.url/beer.png", product.getImageUrl());
    assertTrue(product.isAvailable());
  }

  @Test
  @DisplayName("toEntity returns null when ProductDTO is null")
  void testToEntityFromDTONull() {
    assertNull(productMapper.toEntity((ProductDTO) null));
  }

  @Test
  @DisplayName("toEntity converts CreateProductRequest to Product entity correctly")
  void testToEntityFromCreateRequest() {
    CreateProductRequest request = new CreateProductRequest();
    request.setName("Beer");
    request.setDescription("Craft beer");
    request.setPrice(new BigDecimal("5.99"));
    request.setAlcohol(true);
    request.setAlcoholContent(5.5);
    request.setImageUrl("http://image.url/beer.png");
    request.setAvailable(true);

    Product product = productMapper.toEntity(request);

    assertNotNull(product);
    assertEquals("Beer", product.getName());
    assertEquals("Craft beer", product.getDescription());
    assertEquals(new BigDecimal("5.99"), product.getPrice());
    assertTrue(product.isAlcohol());
    assertEquals(5.5, product.getAlcoholContent());
    assertEquals("http://image.url/beer.png", product.getImageUrl());
    assertTrue(product.isAvailable()); // default set to true
  }

  @Test
  @DisplayName("toEntity returns null when CreateProductRequest is null")
  void testToEntityFromCreateRequestNull() {
    assertNull(productMapper.toEntity((CreateProductRequest) null));
  }
}
