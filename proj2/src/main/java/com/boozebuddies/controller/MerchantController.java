package com.boozebuddies.controller;

import com.boozebuddies.dto.ApiResponse;
import com.boozebuddies.dto.MerchantDTO;
import com.boozebuddies.entity.Merchant;
import com.boozebuddies.entity.Order;
import com.boozebuddies.mapper.MerchantMapper;
import com.boozebuddies.service.MerchantService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/merchants")
public class MerchantController {

  private final MerchantService merchantService;
  private final MerchantMapper merchantMapper;

  @Autowired
  public MerchantController(MerchantService merchantService, MerchantMapper merchantMapper) {
    this.merchantService = merchantService;
    this.merchantMapper = merchantMapper;
  }

  // ==================== REGISTER ====================

  @PostMapping("/register")
  public ResponseEntity<?> registerMerchant(@RequestBody MerchantDTO merchantDTO) {
    try {
      Merchant merchant = merchantMapper.toEntity(merchantDTO);
      Merchant registered = merchantService.registerMerchant(merchant);
      return ResponseEntity.status(HttpStatus.CREATED)
          .body(
              ApiResponse.success(
                  merchantMapper.toDTO(registered), "Merchant registered successfully"));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("An error occurred during registration"));
    }
  }

  // ==================== VERIFY ====================

  @PutMapping("/{id}/verify")
  public ResponseEntity<?> verifyMerchant(@PathVariable Long id, @RequestParam boolean verified) {
    try {
      Merchant verifiedMerchant = merchantService.verifyMerchant(id, verified);
      return ResponseEntity.ok(
          ApiResponse.success(
              merchantMapper.toDTO(verifiedMerchant), "Merchant verified successfully"));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("An error occurred during verification"));
    }
  }

  // ==================== RETRIEVE ====================

  @GetMapping("/{id}")
  public ResponseEntity<?> getMerchantById(@PathVariable Long id) {
    try {
      if (id == null || id <= 0) {
        return ResponseEntity.badRequest().body(ApiResponse.error("Invalid merchant ID"));
      }
      Merchant merchant = merchantService.getMerchantById(id);
      return ResponseEntity.ok(
          ApiResponse.success(merchantMapper.toDTO(merchant), "Merchant retrieved successfully"));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("An error occurred retrieving merchant"));
    }
  }

  @GetMapping
  public ResponseEntity<?> getAllMerchants() {
    try {
      List<MerchantDTO> merchants =
          merchantService.getAllMerchants().stream().map(merchantMapper::toDTO).toList();
      return ResponseEntity.ok(ApiResponse.success(merchants, "Merchants retrieved successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("An error occurred retrieving merchants"));
    }
  }

  // ==================== DELETE ====================

  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteMerchant(@PathVariable Long id) {
    try {
      if (id == null || id <= 0) {
        return ResponseEntity.badRequest().body(ApiResponse.error("Invalid merchant ID"));
      }
      boolean deleted = merchantService.deleteMerchant(id);
      if (deleted) {
        return ResponseEntity.ok(ApiResponse.success(null, "Merchant deleted successfully"));
      } else {
        return ResponseEntity.notFound().build();
      }
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("An error occurred during deletion"));
    }
  }

  // ==================== ORDERS BY MERCHANT ====================

  @GetMapping("/{id}/orders")
  public ResponseEntity<?> getOrdersByMerchant(
      @PathVariable Long id,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    try {
      if (id == null || id <= 0) {
        return ResponseEntity.badRequest().body(ApiResponse.error("Invalid merchant ID"));
      }
      Pageable pageable = PageRequest.of(page, size);
      Page<Order> orders = merchantService.getOrdersByMerchant(id, pageable);
      return ResponseEntity.ok(ApiResponse.success(orders, "Orders retrieved successfully"));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("An error occurred retrieving orders"));
    }
  }
}
