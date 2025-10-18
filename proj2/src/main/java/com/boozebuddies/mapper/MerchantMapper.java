package com.boozebuddies.mapper;

import com.boozebuddies.dto.MerchantDTO;
import com.boozebuddies.entity.Merchant;
import org.springframework.stereotype.Component;

@Component
public class MerchantMapper {

    public MerchantDTO toDTO(Merchant merchant) {
        if (merchant == null) return null;
        
        return MerchantDTO.builder()
                .id(merchant.getMerchantId())
                .name(merchant.getName())
                .description(merchant.getDescription())
                .address(merchant.getAddress())
                .phone(merchant.getPhone())
                .email(merchant.getEmail())
                .cuisineType(merchant.getCuisineType())
                .openingTime(merchant.getOpeningTime())
                .closingTime(merchant.getClosingTime())
                .isActive(merchant.isActive())
                .isVerified(merchant.isVerified())
                .rating(merchant.getRating())
                .totalRatings(merchant.getTotalRatings())
                .imageUrl(merchant.getImageUrl())
                .inventoryDetails(merchant.getInventoryDetails())
                .build();
    }

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
                .build();
    }
}