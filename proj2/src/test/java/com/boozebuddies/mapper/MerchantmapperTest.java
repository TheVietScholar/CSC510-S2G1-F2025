package com.boozebuddies.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.boozebuddies.dto.MerchantDTO;
import com.boozebuddies.entity.Merchant;
import java.time.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MerchantMapperTest {

  private MerchantMapper merchantMapper;

  @BeforeEach
  void setUp() {
    merchantMapper = new MerchantMapper();
  }

  @Test
  @DisplayName("toDTO should map all fields correctly")
  void testToDTO() {
    Merchant merchant =
        Merchant.builder()
            .id(1L)
            .name("Test Merchant")
            .description("Test Description")
            .address("123 Main St")
            .phone("555-1234")
            .email("merchant@test.com")
            .cuisineType("Italian")
            .openingTime(LocalTime.of(9, 0))
            .closingTime(LocalTime.of(21, 0))
            .isActive(true)
            .rating(4.5)
            .totalRatings(100)
            .imageUrl("http://image.url")
            .latitude(40.7128)
            .longitude(-74.0060)
            .build();

    MerchantDTO dto = merchantMapper.toDTO(merchant);

    assertNotNull(dto);
    assertEquals(merchant.getId(), dto.getId());
    assertEquals(merchant.getName(), dto.getName());
    assertEquals(merchant.getDescription(), dto.getDescription());
    assertEquals(merchant.getAddress(), dto.getAddress());
    assertEquals(merchant.getPhone(), dto.getPhone());
    assertEquals(merchant.getEmail(), dto.getEmail());
    assertEquals(merchant.getCuisineType(), dto.getCuisineType());
    assertEquals(merchant.getOpeningTime(), dto.getOpeningTime());
    assertEquals(merchant.getClosingTime(), dto.getClosingTime());
    assertEquals(merchant.isActive(), dto.isActive());
    assertEquals(merchant.getRating(), dto.getRating());
    assertEquals(merchant.getTotalRatings(), dto.getTotalRatings());
    assertEquals(merchant.getImageUrl(), dto.getImageUrl());
    assertEquals(merchant.getLatitude(), dto.getLatitude());
    assertEquals(merchant.getLongitude(), dto.getLongitude());
  }

  @Test
  @DisplayName("toDTO should return null if input is null")
  void testToDTONull() {
    assertNull(merchantMapper.toDTO(null));
  }

  @Test
  @DisplayName("toEntity should map all fields correctly")
  void testToEntity() {
    MerchantDTO dto =
        MerchantDTO.builder()
            .id(1L) // optional, ignored in entity
            .name("Test Merchant")
            .description("Test Description")
            .address("123 Main St")
            .phone("555-1234")
            .email("merchant@test.com")
            .cuisineType("Italian")
            .openingTime(LocalTime.of(9, 0))
            .closingTime(LocalTime.of(21, 0))
            .imageUrl("http://image.url")
            .latitude(40.7128)
            .longitude(-74.0060)
            .build();

    Merchant merchant = merchantMapper.toEntity(dto);

    assertNotNull(merchant);
    assertEquals(dto.getName(), merchant.getName());
    assertEquals(dto.getDescription(), merchant.getDescription());
    assertEquals(dto.getAddress(), merchant.getAddress());
    assertEquals(dto.getPhone(), merchant.getPhone());
    assertEquals(dto.getEmail(), merchant.getEmail());
    assertEquals(dto.getCuisineType(), merchant.getCuisineType());
    assertEquals(dto.getOpeningTime(), merchant.getOpeningTime());
    assertEquals(dto.getClosingTime(), merchant.getClosingTime());
    assertEquals(dto.getImageUrl(), merchant.getImageUrl());
    assertEquals(dto.getLatitude(), merchant.getLatitude());
    assertEquals(dto.getLongitude(), merchant.getLongitude());
  }

  @Test
  @DisplayName("toEntity should return null if input is null")
  void testToEntityNull() {
    assertNull(merchantMapper.toEntity(null));
  }
}
