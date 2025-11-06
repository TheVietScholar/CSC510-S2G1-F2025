package com.boozebuddies.mapper;

import com.boozebuddies.dto.MerchantDTO;
import com.boozebuddies.entity.Merchant;
import org.springframework.stereotype.Component;

/** Mapper for converting between Merchant entities and MerchantDTO objects. */
@Component
public class MerchantMapper {

  /**
   * Converts a Merchant entity to a MerchantDTO.
   *
   * @param merchant the merchant entity to convert
   * @return the MerchantDTO, or null if the input is null
   */
  public MerchantDTO toDTO(Merchant merchant) {
    if (merchant == null) return null;

    return MerchantDTO.builder()
        .id(merchant.getId())
        .name(merchant.getName())
        .description(merchant.getDescription())
        .address(merchant.getAddress())
        .phone(merchant.getPhone())
        .email(merchant.getEmail())
        .cuisineType(merchant.getCuisineType())
        .openingTime(merchant.getOpeningTime())
        .closingTime(merchant.getClosingTime())
        .isActive(merchant.isActive())
        .rating(merchant.getRating())
        .totalRatings(merchant.getTotalRatings())
        .imageUrl(merchant.getImageUrl())
        .latitude(merchant.getLatitude())
        .longitude(merchant.getLongitude())
        .build();
  }

  /**
   * Converts a MerchantDTO to a Merchant entity.
   *
   * @param merchantDTO the MerchantDTO to convert
   * @return the Merchant entity, or null if the input is null
   */
  public Merchant toEntity(MerchantDTO merchantDTO) {
    if (merchantDTO == null) return null;

    return Merchant.builder()
        .name(merchantDTO.getName())
        .description(merchantDTO.getDescription())
        .address(merchantDTO.getAddress())
        .phone(merchantDTO.getPhone())
        .email(merchantDTO.getEmail())
        .cuisineType(merchantDTO.getCuisineType())
        .openingTime(merchantDTO.getOpeningTime())
        .closingTime(merchantDTO.getClosingTime())
        .imageUrl(merchantDTO.getImageUrl())
        .latitude(merchantDTO.getLatitude())
        .longitude(merchantDTO.getLongitude())
        .build();
  }
}
