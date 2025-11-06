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
import com.boozebuddies.service.DriverService;
import com.boozebuddies.service.OrderService;
import com.boozebuddies.service.PermissionService;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/** REST controller for managing deliveries and driver operations. */
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
   * Assigns a driver to an order and creates a delivery record. Admin only.
   *
   * @param orderId the ID of the order
   * @param driverId the ID of the driver to assign
   * @return the created delivery
   */
  @PostMapping("/assign")
  @IsAdmin
  public ResponseEntity<ApiResponse<DeliveryDTO>> assignDriverToOrder(
      @RequestParam Long orderId, @RequestParam Long driverId) {
    try {
      // Fetch actual entities
      Order order =
          orderService
              .getOrderById(orderId)
              .orElseThrow(() -> new RuntimeException("Order not found"));

      Driver driver = driverService.getDriverById(driverId).get(); // ← FIXED

      Delivery delivery = deliveryService.assignDriverToOrder(order, driver);
      DeliveryDTO deliveryDTO = deliveryMapper.toDTO(delivery);

      return ResponseEntity.ok(ApiResponse.success(deliveryDTO, "Driver assigned successfully"));
    } catch (NoSuchElementException e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Driver not found with ID: " + driverId));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to assign driver: " + e.getMessage()));
    }
  }

  /**
   * Retrieves all active deliveries. Admin only.
   *
   * @return a list of active deliveries
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
   * Retrieves all deliveries in the system. Admin only.
   *
   * @return a list of all deliveries
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
   * Retrieves deliveries by driver ID. Admin can view any driver's deliveries.
   *
   * @param driverId the driver ID
   * @return a list of deliveries for the specified driver
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
   * Method for getting a Delivery by the attached Order Id
   *
   * @param orderId to search by
   * @param authentication of the user
   * @return Response Entity with retrieval data
   */
  @GetMapping("/order/{orderId}")
  @IsAuthenticated
  public ResponseEntity<ApiResponse<DeliveryDTO>> getDeliveryByOrderId(
      @PathVariable Long orderId, Authentication authentication) {
    try {
      User user = permissionService.getAuthenticatedUser(authentication);
      Delivery delivery = deliveryService.getDeliveryByOrderId(orderId);

      if (delivery == null) {
        return ResponseEntity.notFound().build();
      }

      // Check if user can access this delivery (owner of order, driver, or admin)
      boolean canAccess =
          user.hasRole(Role.ADMIN)
              || // Admins can see all
              (delivery.getOrder() != null
                  && delivery.getOrder().getUser() != null
                  && delivery.getOrder().getUser().getId().equals(user.getId()))
              || // User owns the order
              (user.hasRole(Role.DRIVER)
                  && user.getDriver() != null
                  && delivery.getDriver() != null
                  && delivery
                      .getDriver()
                      .getId()
                      .equals(user.getDriver().getId())); // Driver's own delivery

      if (!canAccess) {
        throw new AccessDeniedException("You don't have permission to view this delivery");
      }

      DeliveryDTO deliveryDTO = deliveryMapper.toDTO(delivery);
      return ResponseEntity.ok(ApiResponse.success(deliveryDTO, "Delivery retrieved successfully"));
    } catch (AccessDeniedException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to retrieve delivery: " + e.getMessage()));
    }
  }

  /**
   * Retrieves a delivery by ID. Drivers can view their own deliveries, admins can view all.
   *
   * @param deliveryId the delivery ID
   * @param authentication the authentication object
   * @return the delivery with the specified ID
   */
  @GetMapping("/{deliveryId}")
  @IsAuthenticated
  public ResponseEntity<ApiResponse<DeliveryDTO>> getDeliveryById(
      @PathVariable Long deliveryId, Authentication authentication) {
    try {
      User user = permissionService.getAuthenticatedUser(authentication);
      Delivery delivery = deliveryService.getDeliveryById(deliveryId);

      if (delivery == null) {
        return ResponseEntity.notFound().build();
      }

      // Check if user can access this delivery
      boolean canAccess =
          user.hasRole(Role.ADMIN)
              || // Admins can see all
              (user.hasRole(Role.DRIVER)
                  && user.getDriver() != null
                  && delivery.getDriver() != null
                  && delivery
                      .getDriver()
                      .getId()
                      .equals(user.getDriver().getId())); // Driver's own delivery

      if (!canAccess) {
        throw new AccessDeniedException("You don't have permission to view this delivery");
      }

      DeliveryDTO deliveryDTO = deliveryMapper.toDTO(delivery);
      return ResponseEntity.ok(ApiResponse.success(deliveryDTO, "Delivery retrieved successfully"));
    } catch (AccessDeniedException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to retrieve delivery: " + e.getMessage()));
    }
  }

  // ==================== DRIVER ENDPOINTS ====================

  /**
   * Retrieves all deliveries for the authenticated driver. Drivers can only view their own
   * deliveries.
   *
   * @param authentication the authentication object
   * @return a list of deliveries for the authenticated driver
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
   * Updates delivery status. Drivers can update their own deliveries, admins can update any.
   *
   * @param deliveryId the delivery ID
   * @param status the new delivery status
   * @param authentication the authentication object
   * @return the updated delivery
   */
  @PutMapping("/{deliveryId}/status")
  @IsAuthenticated
  public ResponseEntity<ApiResponse<DeliveryDTO>> updateDeliveryStatus(
      @PathVariable Long deliveryId,
      @RequestParam("status") DeliveryStatus status,
      Authentication authentication) {
    try {
      User user = permissionService.getAuthenticatedUser(authentication);
      Delivery delivery = deliveryService.getDeliveryById(deliveryId);

      if (delivery == null) {
        return ResponseEntity.notFound().build();
      }

      // Check ownership for non-admins
      if (!user.hasRole(Role.ADMIN)) {
        if (user.getDriver() == null
            || delivery.getDriver() == null
            || !delivery.getDriver().getId().equals(user.getDriver().getId())) {
          throw new AccessDeniedException("You can only update your own deliveries");
        }
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
   * Marks delivery as picked up. Drivers can pickup their own deliveries.
   *
   * @param deliveryId the delivery ID
   * @param authentication the authentication object
   * @return the updated delivery
   */
  @PostMapping("/{deliveryId}/pickup")
  @IsDriver
  public ResponseEntity<ApiResponse<DeliveryDTO>> markAsPickedUp(
      @PathVariable Long deliveryId, Authentication authentication) {
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
      if (delivery.getDriver() == null
          || !delivery.getDriver().getId().equals(user.getDriver().getId())) {
        throw new AccessDeniedException("You can only pickup your own deliveries");
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
   * Marks delivery as delivered. Drivers can deliver their own deliveries.
   *
   * @param deliveryId the delivery ID
   * @param authentication the authentication object
   * @return the updated delivery
   */
  @PostMapping("/{deliveryId}/deliver")
  @IsDriver
  public ResponseEntity<ApiResponse<DeliveryDTO>> markAsDelivered(
      @PathVariable Long deliveryId, Authentication authentication) {
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
      if (delivery.getDriver() == null
          || !delivery.getDriver().getId().equals(user.getDriver().getId())) {
        throw new AccessDeniedException("You can only deliver your own orders");
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
   * Verifies customer age at delivery. Critical for alcohol delivery compliance.
   *
   * @param deliveryId the delivery ID
   * @param ageVerified whether the customer's age was verified
   * @param idType the type of ID presented
   * @param idNumber the ID number (last 4 digits)
   * @param authentication the authentication object
   * @return the updated delivery
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
      if (delivery.getDriver() == null
          || !delivery.getDriver().getId().equals(user.getDriver().getId())) {
        throw new AccessDeniedException("You can only verify age for your own deliveries");
      }

      // Update delivery with age verification
      delivery.setAgeVerified(ageVerified);
      if (ageVerified) {
        delivery.setIdType(idType);
        delivery.setIdNumber(idNumber); // Store last 4 digits only in production!
      }

      Delivery updatedDelivery =
          deliveryService.updateDeliveryWithAgeVerification(
              deliveryId, ageVerified, idType, idNumber);

      String message =
          ageVerified
              ? "Customer age verified successfully"
              : "Customer age verification failed - delivery cannot be completed";

      DeliveryDTO deliveryDTO = deliveryMapper.toDTO(updatedDelivery);
      return ResponseEntity.ok(ApiResponse.success(deliveryDTO, message));
    } catch (AccessDeniedException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to verify age: " + e.getMessage()));
    }
  }

  /**
   * Cancels a delivery with a reason. Drivers can cancel their own deliveries.
   *
   * @param deliveryId the delivery ID
   * @param reason the cancellation reason
   * @param authentication the authentication object
   * @return the cancelled delivery
   */
  @PostMapping("/{deliveryId}/cancel")
  @IsDriver
  public ResponseEntity<ApiResponse<DeliveryDTO>> cancelDelivery(
      @PathVariable Long deliveryId, @RequestParam String reason, Authentication authentication) {
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
      if (delivery.getDriver() == null
          || !delivery.getDriver().getId().equals(user.getDriver().getId())) {
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

  /**
   * Updates delivery location for real-time tracking. Drivers update their current location while
   * delivering.
   *
   * @param deliveryId the delivery ID
   * @param latitude the current latitude
   * @param longitude the current longitude
   * @param authentication the authentication object
   * @return a success message
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
      if (delivery.getDriver() == null
          || !delivery.getDriver().getId().equals(user.getDriver().getId())) {
        throw new AccessDeniedException("You can only update location for your own deliveries");
      }

      // Update location
      deliveryService.updateDeliveryLocation(deliveryId, latitude, longitude);

      return ResponseEntity.ok(ApiResponse.success(null, "Location updated successfully"));
    } catch (AccessDeniedException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to update location: " + e.getMessage()));
    }
  }
}
