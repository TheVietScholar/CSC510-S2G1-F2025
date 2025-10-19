package com.boozebuddies.controller;

import com.boozebuddies.dto.ApiResponse;
import com.boozebuddies.dto.MerchantDTO;
import com.boozebuddies.dto.OrderDTO;
import com.boozebuddies.entity.Merchant;
import com.boozebuddies.entity.Order;
import com.boozebuddies.service.MerchantService;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/merchants")
public class MerchantController {

  @Autowired private MerchantService merchantService;

  /** Register a new merchant */
  @PostMapping("/register")
  public ResponseEntity<ApiResponse<MerchantDTO>> registerMerchant(
      @RequestBody MerchantDTO merchantDTO) {
    try {
      Merchant merchant = convertToEntity(merchantDTO);
      Merchant registeredMerchant = merchantService.registerMerchant(merchant);
      MerchantDTO responseDTO = convertToDTO(registeredMerchant);
      return ResponseEntity.ok(
          ApiResponse.success(responseDTO, "Merchant registered successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to register merchant: " + e.getMessage()));
    }
  }

  // /** Verify a merchant */
  // @PutMapping("/{merchantId}/verify")
  // public ResponseEntity<ApiResponse<MerchantDTO>> verifyMerchant(
  //     @PathVariable Long merchantId, @RequestParam boolean verified) {
  //   try {
  //     Merchant merchant = merchantService.verifyMerchant(merchantId, verified);
  //     if (merchant == null) {
  //       return ResponseEntity.notFound().build();
  //     }
  //     MerchantDTO merchantDTO = convertToDTO(merchant);
  //     String message =
  //         verified ? "Merchant verified successfully" : "Merchant verification revoked";
  //     return ResponseEntity.ok(ApiResponse.success(merchantDTO, message));
  //   } catch (Exception e) {
  //     return ResponseEntity.badRequest()
  //         .body(ApiResponse.error("Failed to update merchant verification: " + e.getMessage()));
  //   }
  // }

  /** Get merchant by ID */
  @GetMapping("/{merchantId}")
  public ResponseEntity<ApiResponse<MerchantDTO>> getMerchantById(@PathVariable Long merchantId) {
    try {
      Merchant merchant = merchantService.getMerchantById(merchantId);
      if (merchant == null) {
        return ResponseEntity.notFound().build();
      }
      MerchantDTO merchantDTO = convertToDTO(merchant);
      return ResponseEntity.ok(ApiResponse.success(merchantDTO, "Merchant retrieved successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to retrieve merchant: " + e.getMessage()));
    }
  }

  /** Get all merchants */
  @GetMapping
  public ResponseEntity<ApiResponse<List<MerchantDTO>>> getAllMerchants() {
    try {
      List<Merchant> allMerchants = merchantService.getAllMerchants();
      List<MerchantDTO> merchantDTOs =
          allMerchants.stream().map(this::convertToDTO).collect(Collectors.toList());
      return ResponseEntity.ok(
          ApiResponse.success(merchantDTOs, "All merchants retrieved successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to retrieve merchants: " + e.getMessage()));
    }
  }

  /** Get all orders for a specific merchant */
  @GetMapping("/{merchantId}/orders")
  public ResponseEntity<ApiResponse<List<OrderDTO>>> getOrdersByMerchant(
      @PathVariable Long merchantId) {
    try {
      List<Order> orders = merchantService.getOrdersByMerchant(merchantId);
      List<OrderDTO> orderDTOs =
          orders.stream().map(this::convertOrderToDTO).collect(Collectors.toList());
      return ResponseEntity.ok(
          ApiResponse.success(orderDTOs, "Merchant orders retrieved successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to retrieve merchant orders: " + e.getMessage()));
    }
  }

  /** Delete a merchant */
  @DeleteMapping("/{merchantId}")
  public ResponseEntity<ApiResponse<Void>> deleteMerchant(@PathVariable Long merchantId) {
    try {
      boolean deleted = merchantService.deleteMerchant(merchantId);
      if (!deleted) {
        return ResponseEntity.notFound().build();
      }
      return ResponseEntity.ok(ApiResponse.success(null, "Merchant deleted successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to delete merchant: " + e.getMessage()));
    }
  }

  /** Convert MerchantDTO to Merchant entity */
  private Merchant convertToEntity(MerchantDTO merchantDTO) {
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

  /** Convert Merchant entity to MerchantDTO */
  private MerchantDTO convertToDTO(Merchant merchant) {
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
        .build();
  }

  /** Convert Order entity to OrderDTO (simplified for merchant orders) */
  private OrderDTO convertOrderToDTO(Order order) {
    return OrderDTO.builder()
        .id(order.getId())
        .userId(order.getUser() != null ? order.getUser().getId() : null)
        .merchantId(order.getMerchant() != null ? order.getMerchant().getId() : null)
        .totalAmount(order.getTotalAmount())
        .status(order.getStatus().name())
        .deliveryAddress(order.getDeliveryAddress())
        .createdAt(order.getCreatedAt())
        .build();
  }
}
