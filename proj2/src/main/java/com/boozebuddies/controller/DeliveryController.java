package com.boozebuddies.controller;

import com.boozebuddies.dto.ApiResponse;
import com.boozebuddies.dto.DeliveryDTO;
import com.boozebuddies.entity.Delivery;
import com.boozebuddies.entity.Driver;
import com.boozebuddies.entity.Order;
import com.boozebuddies.entity.User;
import com.boozebuddies.mapper.DeliveryMapper;
import com.boozebuddies.model.DeliveryStatus;
import com.boozebuddies.model.Role;
import com.boozebuddies.security.annotation.RoleAnnotations.*;
import com.boozebuddies.service.DeliveryService;
import com.boozebuddies.service.OrderService;
import com.boozebuddies.service.PermissionService;
import com.boozebuddies.service.DriverService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

  private final DeliveryService deliveryService;
  private final DeliveryMapper deliveryMapper;
  private final PermissionService permissionService;
  private final OrderService orderService;
  private final DriverService driverService;

  // ==================== ADMIN ENDPOINTS ====================

  /**
   * Assign a driver to an order and create delivery record.
   * Admin only.
   */
  @PostMapping("/assign")
  @IsAdmin
  public ResponseEntity<ApiResponse<DeliveryDTO>> assignDriverToOrder(
      @RequestParam Long orderId,
      @RequestParam Long driverId) {
    try {
      // Fetch actual entities
      Order order = orderService.getOrderById(orderId)
          .orElseThrow(() -> new RuntimeException("Order not found"));

      Driver driver = driverService.getDriverById(driverId); // ← FIXED
      if (driver == null) {
        throw new RuntimeException("Driver not found");
      }

      Delivery delivery = deliveryService.assignDriverToOrder(order, driver);
      DeliveryDTO deliveryDTO = deliveryMapper.toDTO(delivery);

      return ResponseEntity.ok(ApiResponse.success(deliveryDTO, "Driver assigned successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to assign driver: " + e.getMessage()));
    }
  }

  /**
   * Get all active deliveries.
   * Admin only.
   */
  @GetMapping("/active")
  @IsSelfOrAdmin
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
   * Get all deliveries in the system.
   * Admin only.
   */
  @GetMapping
  @IsAdmin
  public ResponseEntity<ApiResponse<List<DeliveryDTO>>> getAllDeliveries() {
    try {
      List<Delivery> deliveries = deliveryService.getAllDeliveries();
      List<DeliveryDTO> deliveryDTOs =
          deliveries.stream().map(deliveryMapper::toDTO).collect(Collectors.toList());
      return ResponseEntity.ok(
          ApiResponse.success(deliveryDTOs, "All deliveries retrieved successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to retrieve deliveries: " + e.getMessage()));
    }
  }

  /**
   * Get deliveries by driver ID.
   * Admin can view any driver's deliveries.
   */
  @GetMapping("/driver/{driverId}")
  @IsAdmin
  public ResponseEntity<ApiResponse<List<DeliveryDTO>>> getDeliveriesByDriver(
      @PathVariable Long driverId) {
    try {
      List<Delivery> deliveries = deliveryService.getDeliveriesByDriver(driverId);
      List<DeliveryDTO> deliveryDTOs =
          deliveries.stream().map(deliveryMapper::toDTO).collect(Collectors.toList());
      return ResponseEntity.ok(
          ApiResponse.success(deliveryDTOs, "Driver deliveries retrieved successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to retrieve deliveries: " + e.getMessage()));
    }
  }

  /**
   * Get delivery by ID.
   * Drivers can view their own deliveries, admins can view all.
   */
  @GetMapping("/{deliveryId}")
  @IsAuthenticated
  public ResponseEntity<ApiResponse<DeliveryDTO>> getDeliveryById(
      @PathVariable Long deliveryId,
      Authentication authentication) {
    try {
      User user = permissionService.getAuthenticatedUser(authentication);
      Delivery delivery = deliveryService.getDeliveryById(deliveryId);
      
      if (delivery == null) {
        return ResponseEntity.notFound().build();
      }

      // Check if user can access this delivery
      boolean canAccess = user.hasRole(Role.ADMIN) || // Admins can see all
          (user.hasRole(Role.DRIVER) && 
           user.getDriver() != null && 
           delivery.getDriver() != null &&
           delivery.getDriver().getId().equals(user.getDriver().getId())); // Driver's own delivery

      if (!canAccess) {
        throw new AccessDeniedException("You don't have permission to view this delivery");
      }

      DeliveryDTO deliveryDTO = deliveryMapper.toDTO(delivery);
      return ResponseEntity.ok(ApiResponse.success(deliveryDTO, "Delivery retrieved successfully"));
    } catch (AccessDeniedException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(ApiResponse.error(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to retrieve delivery: " + e.getMessage()));
    }
  }

  // ==================== DRIVER ENDPOINTS ====================

  /**
   * Get all deliveries for the authenticated driver.
   * Driver can only view their own deliveries.
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
   * Update delivery status.
   * Driver can update their own deliveries, admin can update any.
   */
  @PutMapping("/{deliveryId}/status")
  @IsAuthenticated
  public ResponseEntity<ApiResponse<DeliveryDTO>> updateDeliveryStatus(
      @PathVariable Long deliveryId,
      @RequestParam DeliveryStatus status,
      Authentication authentication) {
    try {
      User user = permissionService.getAuthenticatedUser(authentication);
      Delivery delivery = deliveryService.getDeliveryById(deliveryId);
      
      if (delivery == null) {
        return ResponseEntity.notFound().build();
      }

      // Check ownership for non-admins
      if (!user.hasRole(Role.ADMIN)) {
        if (user.getDriver() == null || 
            delivery.getDriver() == null ||
            !delivery.getDriver().getId().equals(user.getDriver().getId())) {
          throw new AccessDeniedException("You can only update your own deliveries");
        }
      }

      Delivery updatedDelivery = deliveryService.updateDeliveryStatus(deliveryId, status);
      DeliveryDTO deliveryDTO = deliveryMapper.toDTO(updatedDelivery);
      return ResponseEntity.ok(
          ApiResponse.success(deliveryDTO, "Delivery status updated successfully"));
    } catch (AccessDeniedException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(ApiResponse.error(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to update delivery status: " + e.getMessage()));
    }
  }

  /**
   * Mark delivery as picked up.
   * Driver can pickup their own deliveries, admin can mark any as picked up.
   */
  @PostMapping("/{deliveryId}/pickup")
  @IsDriver
  public ResponseEntity<ApiResponse<DeliveryDTO>> markAsPickedUp(
      @PathVariable Long deliveryId,
      Authentication authentication) {
    try {
      User user = permissionService.getAuthenticatedUser(authentication);

      if (user.getDriver() == null) {
        return ResponseEntity.badRequest()
            .body(ApiResponse.error("No driver profile found for this user"));
      }

      Delivery delivery = deliveryService.getDeliveryById(deliveryId);
      if (delivery == null) {
        return ResponseEntity.notFound().build();
      }

      // Verify ownership
      if (delivery.getDriver() == null || 
          !delivery.getDriver().getId().equals(user.getDriver().getId())) {
        throw new AccessDeniedException("You can only pickup your own deliveries");
      }

      Delivery updatedDelivery =
          deliveryService.updateDeliveryStatus(deliveryId, DeliveryStatus.PICKED_UP);
      DeliveryDTO deliveryDTO = deliveryMapper.toDTO(updatedDelivery);
      return ResponseEntity.ok(
          ApiResponse.success(deliveryDTO, "Order marked as picked up successfully"));
    } catch (AccessDeniedException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(ApiResponse.error(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to mark as picked up: " + e.getMessage()));
    }
  }

  /**
   * Mark delivery as delivered.
   * Driver can deliver their own deliveries, admin can mark any as delivered.
   */
  @PostMapping("/{deliveryId}/deliver")
  @IsDriver
  public ResponseEntity<ApiResponse<DeliveryDTO>> markAsDelivered(
      @PathVariable Long deliveryId,
      Authentication authentication) {
    try {
      User user = permissionService.getAuthenticatedUser(authentication);

      if (user.getDriver() == null) {
        return ResponseEntity.badRequest()
            .body(ApiResponse.error("No driver profile found for this user"));
      }

      Delivery delivery = deliveryService.getDeliveryById(deliveryId);
      if (delivery == null) {
        return ResponseEntity.notFound().build();
      }

      // Verify ownership
      if (delivery.getDriver() == null || 
          !delivery.getDriver().getId().equals(user.getDriver().getId())) {
        throw new AccessDeniedException("You can only deliver your own orders");
      }

      Delivery updatedDelivery =
          deliveryService.updateDeliveryStatus(deliveryId, DeliveryStatus.DELIVERED);
      DeliveryDTO deliveryDTO = deliveryMapper.toDTO(updatedDelivery);
      return ResponseEntity.ok(
          ApiResponse.success(deliveryDTO, "Order marked as delivered successfully"));
    } catch (AccessDeniedException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(ApiResponse.error(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to mark as delivered: " + e.getMessage()));
    }
  }

  /**
   * Verify customer age at delivery.
   * Driver only - critical for alcohol delivery compliance.
   */
  @PostMapping("/{deliveryId}/verify-age")
  @IsDriver
  public ResponseEntity<ApiResponse<DeliveryDTO>> verifyCustomerAge(
      @PathVariable Long deliveryId,
      @RequestParam boolean ageVerified,
      @RequestParam(required = false) String idType,
      @RequestParam(required = false) String idNumber,
      Authentication authentication) {
    try {
      User user = permissionService.getAuthenticatedUser(authentication);

      if (user.getDriver() == null) {
        return ResponseEntity.badRequest()
            .body(ApiResponse.error("No driver profile found for this user"));
      }

      Delivery delivery = deliveryService.getDeliveryById(deliveryId);
      if (delivery == null) {
        return ResponseEntity.notFound().build();
      }

      // Verify ownership
      if (delivery.getDriver() == null || 
          !delivery.getDriver().getId().equals(user.getDriver().getId())) {
        throw new AccessDeniedException("You can only verify age for your own deliveries");
      }

      // Update delivery with age verification
      delivery.setAgeVerified(ageVerified);
      if (ageVerified) {
        delivery.setIdType(idType);
        delivery.setIdNumber(idNumber); // Store last 4 digits only in production!
      }
      
      Delivery updatedDelivery = deliveryService.updateDeliveryWithAgeVerification(
          deliveryId, ageVerified, idType, idNumber);
      
      String message = ageVerified
          ? "Customer age verified successfully"
          : "Customer age verification failed - delivery cannot be completed";

      DeliveryDTO deliveryDTO = deliveryMapper.toDTO(updatedDelivery);
      return ResponseEntity.ok(ApiResponse.success(deliveryDTO, message));
    } catch (AccessDeniedException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(ApiResponse.error(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to verify age: " + e.getMessage()));
    }
  }

  /**
   * Cancel a delivery with reason.
   * Driver can cancel their own deliveries with reason.
   */
  @PostMapping("/{deliveryId}/cancel")
  @IsDriver
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

      Delivery delivery = deliveryService.getDeliveryById(deliveryId);
      if (delivery == null) {
        return ResponseEntity.notFound().build();
      }

      // Verify ownership
      if (delivery.getDriver() == null || 
          !delivery.getDriver().getId().equals(user.getDriver().getId())) {
        throw new AccessDeniedException("You can only cancel your own deliveries");
      }

      Delivery cancelledDelivery = deliveryService.cancelDelivery(deliveryId, reason);
      DeliveryDTO deliveryDTO = deliveryMapper.toDTO(cancelledDelivery);
      return ResponseEntity.ok(ApiResponse.success(deliveryDTO, "Delivery cancelled successfully"));
    } catch (AccessDeniedException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(ApiResponse.error(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to cancel delivery: " + e.getMessage()));
    }
  }

  /**
   * Update delivery location (for real-time tracking).
   * Driver updates their current location while delivering.
   */
  @PutMapping("/{deliveryId}/location")
  @IsDriver
  public ResponseEntity<ApiResponse<String>> updateDeliveryLocation(
      @PathVariable Long deliveryId,
      @RequestParam Double latitude,
      @RequestParam Double longitude,
      Authentication authentication) {
    try {
      User user = permissionService.getAuthenticatedUser(authentication);

      if (user.getDriver() == null) {
        return ResponseEntity.badRequest()
            .body(ApiResponse.error("No driver profile found for this user"));
      }

      Delivery delivery = deliveryService.getDeliveryById(deliveryId);
      if (delivery == null) {
        return ResponseEntity.notFound().build();
      }

      // Verify ownership
      if (delivery.getDriver() == null || 
          !delivery.getDriver().getId().equals(user.getDriver().getId())) {
        throw new AccessDeniedException("You can only update location for your own deliveries");
      }

      // Update location
      deliveryService.updateDeliveryLocation(deliveryId, latitude, longitude);
      
      return ResponseEntity.ok(
          ApiResponse.success(null, "Location updated successfully"));
    } catch (AccessDeniedException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(ApiResponse.error(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to update location: " + e.getMessage()));
    }
  }
}
