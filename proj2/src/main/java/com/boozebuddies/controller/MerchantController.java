package com.boozebuddies.controller;

import com.boozebuddies.dto.ApiResponse;
import com.boozebuddies.dto.MerchantDTO;
import com.boozebuddies.entity.Merchant;
import com.boozebuddies.entity.Order;
import com.boozebuddies.entity.User;
import com.boozebuddies.mapper.MerchantMapper;
import com.boozebuddies.security.annotation.RoleAnnotations.*;
import com.boozebuddies.service.MerchantService;
import com.boozebuddies.service.PermissionService;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/** REST controller for managing merchants and merchant operations. */
@RestController
@RequestMapping("/api/merchants")
public class MerchantController {

  private final MerchantService merchantService;
  private final MerchantMapper merchantMapper;
  private final PermissionService permissionService;

  /**
   * Constructor injection for merchant services.
   *
   * @param merchantService the merchant service
   * @param merchantMapper the merchant mapper
   * @param permissionService the permission service
   */
  @Autowired
  public MerchantController(
      MerchantService merchantService,
      MerchantMapper merchantMapper,
      PermissionService permissionService) {
    this.merchantService = merchantService;
    this.merchantMapper = merchantMapper;
    this.permissionService = permissionService;
  }

  // ==================== REGISTER (ADMIN ONLY) ====================

  /**
   * Registers a new merchant. Admin only.
   *
   * @param merchantDTO the merchant data
   * @return the registered merchant
   */
  @PostMapping("/register")
  @IsAdmin
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

  // ==================== VERIFY (ADMIN ONLY) ====================

  /**
   * Verifies or unverifies a merchant. Admin only.
   *
   * @param id the merchant ID
   * @param verified whether the merchant is verified
   * @return the updated merchant
   */
  @PutMapping("/{id}/verify")
  @IsAdmin
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

  // ==================== RETRIEVE (ALL AUTHENTICATED USERS) ====================

  /**
   * Retrieves a merchant by ID. Admin only.
   *
   * @param id the merchant ID
   * @return the merchant with the specified ID
   */
  @GetMapping("/{id}")
  @IsAdmin
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

  /**
   * Retrieves a merchant by name. Authenticated users only.
   *
   * @param name the merchant name
   * @return the merchant with the specified name
   */
  @GetMapping("/name/{name}")
  @IsAuthenticated
  public ResponseEntity<ApiResponse<MerchantDTO>> getMerchantByName(@PathVariable String name) {
    try {
      Merchant merchant = merchantService.getMerchantByName(name);

      if (merchant == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error("Merchant not found"));
      }

      return ResponseEntity.ok(
          ApiResponse.success(merchantMapper.toDTO(merchant), "Merchant retrieved successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to retrieve merchant: " + e.getMessage()));
    }
  }

  /**
   * Retrieves all merchants sorted by distance from the authenticated user's location.
   *
   * @param authentication the authentication object
   * @return a list of merchants sorted by distance
   */
  @GetMapping("/by-distance")
  @IsAuthenticated
  public ResponseEntity<ApiResponse<List<MerchantDTO>>> getMerchantsByDistanceFromUser(
      Authentication authentication) {
    try {
      User user = permissionService.getAuthenticatedUser(authentication);

      if (user == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.error("User not authenticated"));
      }

      if (user.getLatitude() == null || user.getLongitude() == null) {
        return ResponseEntity.badRequest()
            .body(
                ApiResponse.error(
                    "User location not set. Please update your profile with your location."));
      }

      List<Merchant> merchants =
          merchantService.getMerchantsSortedByDistance(user.getLatitude(), user.getLongitude());
      List<MerchantDTO> merchantDTOs =
          merchants.stream().map(merchantMapper::toDTO).collect(Collectors.toList());
      return ResponseEntity.ok(
          ApiResponse.success(merchantDTOs, "Merchants sorted by distance from your location"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to retrieve merchants: " + e.getMessage()));
    }
  }

  /**
   * Retrieves all merchants. Authenticated users only.
   *
   * @return a list of all merchants
   */
  @GetMapping
  @IsAuthenticated
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

  // ==================== DELETE (ADMIN ONLY) ====================

  /**
   * Deletes a merchant. Admin only.
   *
   * @param id the merchant ID
   * @return a success message
   */
  @DeleteMapping("/{id}")
  @IsAdmin
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

  /**
   * Retrieves orders for a specific merchant. Admin or merchant owner only.
   *
   * @param id the merchant ID
   * @param page the page number
   * @param size the page size
   * @param authentication the authentication object
   * @return a paginated list of orders
   */
  @GetMapping("/{id}/orders")
  @org.springframework.security.access.prepost.PreAuthorize(
      "hasRole('ADMIN') or @permissionService.ownsMerchant(authentication, #id)")
  public ResponseEntity<?> getOrdersByMerchant(
      @PathVariable Long id,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      Authentication authentication) {
    try {
      if (id == null || id <= 0) {
        return ResponseEntity.badRequest().body(ApiResponse.error("Invalid merchant ID"));
      }

      Pageable pageable = PageRequest.of(page, size);
      Page<Order> orders = merchantService.getOrdersByMerchant(id, pageable);
      return ResponseEntity.ok(ApiResponse.success(orders, "Orders retrieved successfully"));
    } catch (AccessDeniedException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(e.getMessage()));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("An error occurred retrieving orders"));
    }
  }

  // ==================== MERCHANT_ADMIN ENDPOINTS ====================

  /**
   * Retrieves the merchant managed by the authenticated merchant admin.
   *
   * @param authentication the authentication object
   * @return the merchant managed by this admin
   */
  @GetMapping("/my-merchant")
  @IsMerchantAdmin
  public ResponseEntity<?> getMyMerchant(Authentication authentication) {
    try {
      User user = permissionService.getAuthenticatedUser(authentication);

      if (user.getMerchantId() == null) {
        return ResponseEntity.badRequest()
            .body(ApiResponse.error("No merchant assigned to this admin"));
      }

      Merchant merchant = merchantService.getMerchantById(user.getMerchantId());
      return ResponseEntity.ok(
          ApiResponse.success(
              merchantMapper.toDTO(merchant), "Your merchant retrieved successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("An error occurred retrieving your merchant"));
    }
  }

  /**
   * Retrieves orders for the merchant managed by the authenticated merchant admin.
   *
   * @param page the page number
   * @param size the page size
   * @param authentication the authentication object
   * @return a paginated list of orders for the managed merchant
   */
  @GetMapping("/my-merchant/orders")
  @IsMerchantAdmin
  public ResponseEntity<?> getMyMerchantOrders(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      Authentication authentication) {
    try {
      User user = permissionService.getAuthenticatedUser(authentication);

      if (user.getMerchantId() == null) {
        return ResponseEntity.badRequest()
            .body(ApiResponse.error("No merchant assigned to this admin"));
      }

      Pageable pageable = PageRequest.of(page, size);
      Page<Order> orders = merchantService.getOrdersByMerchant(user.getMerchantId(), pageable);
      return ResponseEntity.ok(
          ApiResponse.success(orders, "Your merchant orders retrieved successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("An error occurred retrieving orders"));
    }
  }
}
