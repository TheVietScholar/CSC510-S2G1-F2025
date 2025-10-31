package com.boozebuddies.controller;

import com.boozebuddies.dto.ApiResponse;
import com.boozebuddies.dto.DeliveryDTO;
import com.boozebuddies.entity.Delivery;
import com.boozebuddies.entity.Driver;
import com.boozebuddies.entity.Order;
import com.boozebuddies.entity.User;
import com.boozebuddies.mapper.DeliveryMapper;
import com.boozebuddies.model.DeliveryStatus;
import com.boozebuddies.security.annotation.RoleAnnotations.*;
import com.boozebuddies.service.DeliveryService;
import com.boozebuddies.service.PermissionService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

  private final DeliveryService deliveryService;
  private final DeliveryMapper deliveryMapper;
  private final PermissionService permissionService;

  // ==================== ADMIN ENDPOINTS ====================

  /**
   * Assign a driver to an order and create delivery record (ADMIN only)
   */
  @PostMapping("/assign")
  @IsAdmin
  public ResponseEntity<ApiResponse<DeliveryDTO>> assignDriverToOrder(
      @RequestParam Long orderId, @RequestParam Long driverId) {
    try {
      // In a real implementation, you'd fetch Order and Driver entities from repositories
      Order order = new Order();
      order.setId(orderId);

      Driver driver = new Driver();
      driver.setId(driverId);

      Delivery delivery = deliveryService.assignDriverToOrder(order, driver);
      DeliveryDTO deliveryDTO = deliveryMapper.toDTO(delivery);

      return ResponseEntity.ok(ApiResponse.success(deliveryDTO, "Driver assigned successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to assign driver: " + e.getMessage()));
    }
  }

  /**
   * Get all active deliveries (ADMIN only)
   */
  @GetMapping("/active")
  @IsAdmin
  public ResponseEntity<ApiResponse<List<DeliveryDTO>>> getActiveDeliveries() {
    try {
      List<Delivery> activeDeliveries = deliveryService.getActiveDeliveries();
      List<DeliveryDTO> deliveryDTOs =
          activeDeliveries.stream().map(deliveryMapper::toDTO).collect(Collectors.toList());
      return ResponseEntity.ok(
          ApiResponse.success(deliveryDTOs, "Active deliveries retrieved successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to retrieve active deliveries: " + e.getMessage()));
    }
  }

  /**
   * Get delivery by ID (ADMIN only for now - could be extended to allow drivers to view their own)
   */
  @GetMapping("/{deliveryId}")
  @PreAuthorize("hasRole('ADMIN') or @permissionService.driverCanAccessDelivery(authentication, #deliveryId)")
  public ResponseEntity<ApiResponse<DeliveryDTO>> getDeliveryById(@PathVariable Long deliveryId) {
    try {
      Delivery delivery = deliveryService.getDeliveryById(deliveryId);
      if (delivery == null) {
        return ResponseEntity.notFound().build();
      }
      DeliveryDTO deliveryDTO = deliveryMapper.toDTO(delivery);
      return ResponseEntity.ok(ApiResponse.success(deliveryDTO, "Delivery retrieved successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to retrieve delivery: " + e.getMessage()));
    }
  }

  // ==================== DRIVER ENDPOINTS ====================

  /**
   * Get all deliveries for the authenticated driver
   */
  @GetMapping("/driver/my-deliveries")
  @IsDriver
  public ResponseEntity<ApiResponse<List<DeliveryDTO>>> getMyDeliveries(
      Authentication authentication) {
    try {
      User user = permissionService.getAuthenticatedUser(authentication);

      if (user.getDriver() == null) {
        return ResponseEntity.badRequest()
            .body(ApiResponse.error("No driver profile found for this user"));
      }

      List<Delivery> deliveries = deliveryService.getDeliveriesByDriver(user.getDriver().getId());
      List<DeliveryDTO> deliveryDTOs =
          deliveries.stream().map(deliveryMapper::toDTO).collect(Collectors.toList());
      return ResponseEntity.ok(
          ApiResponse.success(deliveryDTOs, "Your deliveries retrieved successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to retrieve deliveries: " + e.getMessage()));
    }
  }

  /**
   * Update delivery status (DRIVER only - for their own deliveries)
   */
  @PutMapping("/{deliveryId}/status")
  @PreAuthorize("hasRole('ADMIN') or @permissionService.driverCanAccessDelivery(authentication, #deliveryId)")
  public ResponseEntity<ApiResponse<DeliveryDTO>> updateDeliveryStatus(
      @PathVariable Long deliveryId,
      @RequestParam DeliveryStatus status,
      Authentication authentication) {
    try {
      User user = permissionService.getAuthenticatedUser(authentication);

      if (user.getDriver() == null) {
        return ResponseEntity.badRequest()
            .body(ApiResponse.error("No driver profile found for this user"));
      }

      // Verify this delivery belongs to the authenticated driver
      Delivery delivery = deliveryService.getDeliveryById(deliveryId);
      if (delivery == null) {
        return ResponseEntity.notFound().build();
      }

      if (!delivery.getDriver().getId().equals(user.getDriver().getId())) {
        throw new AccessDeniedException("You can only update your own deliveries");
      }

      Delivery updatedDelivery = deliveryService.updateDeliveryStatus(deliveryId, status);
      DeliveryDTO deliveryDTO = deliveryMapper.toDTO(updatedDelivery);
      return ResponseEntity.ok(
          ApiResponse.success(deliveryDTO, "Delivery status updated successfully"));
    } catch (AccessDeniedException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to update delivery status: " + e.getMessage()));
    }
  }

  /**
   * Mark delivery as picked up (DRIVER only)
   */
  @PostMapping("/{deliveryId}/pickup")
  @PreAuthorize("hasRole('ADMIN') or @permissionService.driverCanAccessDelivery(authentication, #deliveryId)")
  public ResponseEntity<ApiResponse<DeliveryDTO>> markAsPickedUp(
      @PathVariable Long deliveryId, Authentication authentication) {
    try {
      User user = permissionService.getAuthenticatedUser(authentication);

      if (user.getDriver() == null) {
        return ResponseEntity.badRequest()
            .body(ApiResponse.error("No driver profile found for this user"));
      }

      // Verify this delivery belongs to the authenticated driver
      Delivery delivery = deliveryService.getDeliveryById(deliveryId);
      if (delivery == null) {
        return ResponseEntity.notFound().build();
      }

      if (!delivery.getDriver().getId().equals(user.getDriver().getId())) {
        throw new AccessDeniedException("You can only update your own deliveries");
      }

      Delivery updatedDelivery =
          deliveryService.updateDeliveryStatus(deliveryId, DeliveryStatus.PICKED_UP);
      DeliveryDTO deliveryDTO = deliveryMapper.toDTO(updatedDelivery);
      return ResponseEntity.ok(
          ApiResponse.success(deliveryDTO, "Order marked as picked up successfully"));
    } catch (AccessDeniedException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to mark as picked up: " + e.getMessage()));
    }
  }

  /**
   * Mark delivery as delivered (DRIVER only)
   */
  @PostMapping("/{deliveryId}/deliver")
  @PreAuthorize("hasRole('ADMIN') or @permissionService.driverCanAccessDelivery(authentication, #deliveryId)")
  public ResponseEntity<ApiResponse<DeliveryDTO>> markAsDelivered(
      @PathVariable Long deliveryId, Authentication authentication) {
    try {
      User user = permissionService.getAuthenticatedUser(authentication);

      if (user.getDriver() == null) {
        return ResponseEntity.badRequest()
            .body(ApiResponse.error("No driver profile found for this user"));
      }

      // Verify this delivery belongs to the authenticated driver
      Delivery delivery = deliveryService.getDeliveryById(deliveryId);
      if (delivery == null) {
        return ResponseEntity.notFound().build();
      }

      if (!delivery.getDriver().getId().equals(user.getDriver().getId())) {
        throw new AccessDeniedException("You can only update your own deliveries");
      }

      Delivery updatedDelivery =
          deliveryService.updateDeliveryStatus(deliveryId, DeliveryStatus.DELIVERED);
      DeliveryDTO deliveryDTO = deliveryMapper.toDTO(updatedDelivery);
      return ResponseEntity.ok(
          ApiResponse.success(deliveryDTO, "Order marked as delivered successfully"));
    } catch (AccessDeniedException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to mark as delivered: " + e.getMessage()));
    }
  }

  /**
   * Verify customer age (DRIVER only - critical for alcohol delivery)
   */
  @PostMapping("/{deliveryId}/verify-age")
  @PreAuthorize("hasRole('ADMIN') or @permissionService.driverCanAccessDelivery(authentication, #deliveryId)")
  public ResponseEntity<ApiResponse<String>> verifyCustomerAge(
      @PathVariable Long deliveryId,
      @RequestParam boolean ageVerified,
      Authentication authentication) {
    try {
      User user = permissionService.getAuthenticatedUser(authentication);

      if (user.getDriver() == null) {
        return ResponseEntity.badRequest()
            .body(ApiResponse.error("No driver profile found for this user"));
      }

      // Verify this delivery belongs to the authenticated driver
      Delivery delivery = deliveryService.getDeliveryById(deliveryId);
      if (delivery == null) {
        return ResponseEntity.notFound().build();
      }

      if (!delivery.getDriver().getId().equals(user.getDriver().getId())) {
        throw new AccessDeniedException("You can only verify age for your own deliveries");
      }

      // In a real implementation, you'd update the delivery record with age verification status
      // For now, just return success
      String message =
          ageVerified
              ? "Customer age verified successfully"
              : "Customer age verification failed - delivery cannot be completed";

      return ResponseEntity.ok(ApiResponse.success(null, message));
    } catch (AccessDeniedException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to verify age: " + e.getMessage()));
    }
  }

  /**
   * Cancel a delivery with reason (DRIVER only)
   */
  @PostMapping("/{deliveryId}/cancel")
  @PreAuthorize("hasRole('ADMIN') or @permissionService.driverCanAccessDelivery(authentication, #deliveryId)")
  public ResponseEntity<ApiResponse<DeliveryDTO>> cancelDelivery(
      @PathVariable Long deliveryId,
      @RequestParam String reason,
      Authentication authentication) {
    try {
      User user = permissionService.getAuthenticatedUser(authentication);

      if (user.getDriver() == null) {
        return ResponseEntity.badRequest()
            .body(ApiResponse.error("No driver profile found for this user"));
      }

      // Verify this delivery belongs to the authenticated driver
      Delivery delivery = deliveryService.getDeliveryById(deliveryId);
      if (delivery == null) {
        return ResponseEntity.notFound().build();
      }

      if (!delivery.getDriver().getId().equals(user.getDriver().getId())) {
        throw new AccessDeniedException("You can only cancel your own deliveries");
      }

      Delivery cancelledDelivery = deliveryService.cancelDelivery(deliveryId, reason);
      DeliveryDTO deliveryDTO = deliveryMapper.toDTO(cancelledDelivery);
      return ResponseEntity.ok(ApiResponse.success(deliveryDTO, "Delivery cancelled successfully"));
    } catch (AccessDeniedException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to cancel delivery: " + e.getMessage()));
    }
  }

  // ==================== LEGACY ENDPOINT (Consider removing or updating) ====================

  /**
   * Get deliveries by driver ID (Keep for backward compatibility, but prefer /driver/my-deliveries)
   */
  @GetMapping("/driver/{driverId}")
  @IsAdmin // Only admins can view other drivers' deliveries
  public ResponseEntity<ApiResponse<List<DeliveryDTO>>> getDeliveriesByDriver(
      @PathVariable Long driverId) {
    try {
      List<Delivery> deliveries = deliveryService.getDeliveriesByDriver(driverId);
      List<DeliveryDTO> deliveryDTOs =
          deliveries.stream().map(deliveryMapper::toDTO).collect(Collectors.toList());
      return ResponseEntity.ok(
          ApiResponse.success(deliveryDTOs, "Deliveries retrieved successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to retrieve deliveries: " + e.getMessage()));
    }
  }
}
