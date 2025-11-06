package com.boozebuddies.controller;

import com.boozebuddies.dto.ApiResponse;
import com.boozebuddies.dto.CreateOrderRequest;
import com.boozebuddies.dto.OrderDTO;
import com.boozebuddies.entity.Order;
import com.boozebuddies.entity.User;
import com.boozebuddies.mapper.OrderMapper;
import com.boozebuddies.model.Role;
import com.boozebuddies.security.annotation.RoleAnnotations.*;
import com.boozebuddies.service.OrderService;
import com.boozebuddies.service.PermissionService;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/** REST controller for managing orders and order operations. */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

  private final OrderService orderService;
  private final PermissionService permissionService;
  private final OrderMapper orderMapper;

  // ==================== CREATE ORDER (USER ONLY) ====================

  /**
   * Creates a new order. Only users with USER role can place orders.
   *
   * @param createOrderRequest the order creation request
   * @param authentication the authentication object
   * @return the created order
   */
  @PostMapping
  @IsUser
  public ResponseEntity<ApiResponse<OrderDTO>> createOrder(
      @RequestBody CreateOrderRequest createOrderRequest, Authentication authentication) {
    try {
      User user = permissionService.getAuthenticatedUser(authentication);

      // Set the user ID from authenticated user if not provided
      if (createOrderRequest.getUserId() == null) {
        createOrderRequest.setUserId(user.getId());
      } else if (!createOrderRequest.getUserId().equals(user.getId())) {
        // If userId is provided but doesn't match authenticated user, reject it
        throw new AccessDeniedException("You can only create orders for yourself");
      }

      // Convert CreateOrderRequest to Order entity using mapper
      Order order = orderMapper.toEntity(createOrderRequest);
      Order createdOrder = orderService.createOrder(order);
      OrderDTO orderDTO = orderMapper.toDTO(createdOrder);
      return ResponseEntity.ok(ApiResponse.success(orderDTO, "Order created successfully"));
    } catch (AccessDeniedException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to create order: " + e.getMessage()));
    }
  }

  // ==================== RETRIEVE ORDERS ====================

  /**
   * Retrieves an order by ID. Users can view their own orders, merchant admins can view orders for
   * their merchant, drivers can view assigned orders, and admins can view all orders.
   *
   * @param orderId the order ID
   * @param authentication the authentication object
   * @return the order with the specified ID
   */
  @GetMapping("/{orderId}")
  @IsAuthenticated
  public ResponseEntity<ApiResponse<OrderDTO>> getOrderById(
      @PathVariable Long orderId, Authentication authentication) {
    try {
      if (orderId == null || orderId <= 0) {
        return ResponseEntity.badRequest().body(ApiResponse.error("Invalid order ID"));
      }

      User user = permissionService.getAuthenticatedUser(authentication);
      Optional<Order> orderOpt = orderService.getOrderById(orderId);

      if (orderOpt.isEmpty()) {
        return ResponseEntity.notFound().build();
      }

      Order order = orderOpt.get();

      // Check permissions: Admin can see all, otherwise check ownership/access
      boolean canAccess = false;

      if (user.hasRole(Role.ADMIN)) {
        canAccess = true;
      } else {
        // Get order relationships (loaded by JOIN FETCH query in repository)
        Long orderUserId = order.getUser() != null ? order.getUser().getId() : null;
        Long orderMerchantId = order.getMerchant() != null ? order.getMerchant().getId() : null;
        Long orderDriverId = order.getDriver() != null ? order.getDriver().getId() : null;

        // Check if user owns the order
        if (orderUserId != null && orderUserId.equals(user.getId())) {
          canAccess = true;
        }
        // Check if merchant admin can access (order belongs to their merchant)
        else if (user.hasRole(Role.MERCHANT_ADMIN)
            && orderMerchantId != null
            && user.getMerchantId() != null
            && user.getMerchantId().equals(orderMerchantId)) {
          canAccess = true;
        }
        // Check if driver can access (order is assigned to them)
        else if (user.hasRole(Role.DRIVER)
            && orderDriverId != null
            && user.getDriver() != null
            && orderDriverId.equals(user.getDriver().getId())) {
          canAccess = true;
        }
      }

      if (!canAccess) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ApiResponse.error("You don't have permission to view this order"));
      }

      OrderDTO orderDTO = orderMapper.toDTO(order);
      return ResponseEntity.ok(ApiResponse.success(orderDTO, "Order retrieved successfully"));
    } catch (AccessDeniedException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to retrieve order: " + e.getMessage()));
    }
  }

  /**
   * Retrieves all orders for the authenticated user. Users can only view their own orders.
   *
   * @param authentication the authentication object
   * @return a list of the user's orders
   */
  @GetMapping("/my-orders")
  @IsUser
  public ResponseEntity<ApiResponse<List<OrderDTO>>> getMyOrders(Authentication authentication) {
    try {
      User user = permissionService.getAuthenticatedUser(authentication);
      List<Order> orders = orderService.getOrdersByUser(user.getId());
      List<OrderDTO> orderDTOs =
          orders.stream().map(orderMapper::toDTO).collect(Collectors.toList());
      return ResponseEntity.ok(
          ApiResponse.success(orderDTOs, "Your orders retrieved successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to retrieve your orders: " + e.getMessage()));
    }
  }

  /**
   * Retrieves all orders for a specific user. Admin only.
   *
   * @param userId the user ID
   * @return a list of orders for the specified user
   */
  @GetMapping("/user/{userId}")
  @IsAdmin
  public ResponseEntity<ApiResponse<List<OrderDTO>>> getOrdersByUser(@PathVariable Long userId) {
    try {
      if (userId == null || userId <= 0) {
        return ResponseEntity.badRequest().body(ApiResponse.error("Invalid user ID"));
      }

      List<Order> orders = orderService.getOrdersByUser(userId);
      List<OrderDTO> orderDTOs =
          orders.stream().map(orderMapper::toDTO).collect(Collectors.toList());
      return ResponseEntity.ok(
          ApiResponse.success(orderDTOs, "User orders retrieved successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to retrieve user orders: " + e.getMessage()));
    }
  }

  /**
   * Retrieves all orders. Admin only.
   *
   * @return a list of all orders
   */
  @GetMapping
  @IsAdmin
  public ResponseEntity<ApiResponse<List<OrderDTO>>> getAllOrders() {
    try {
      List<Order> orders = orderService.getAllOrders();
      List<OrderDTO> orderDTOs =
          orders.stream().map(orderMapper::toDTO).collect(Collectors.toList());
      return ResponseEntity.ok(ApiResponse.success(orderDTOs, "All orders retrieved successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to retrieve orders: " + e.getMessage()));
    }
  }

  /**
   * Retrieves orders for the merchant managed by the authenticated merchant admin.
   *
   * @param authentication the authentication object
   * @return a list of orders for the managed merchant
   */
  @GetMapping("/merchant/my-orders")
  @IsMerchantAdmin
  public ResponseEntity<ApiResponse<List<OrderDTO>>> getMyMerchantOrders(
      Authentication authentication) {
    try {
      User user = permissionService.getAuthenticatedUser(authentication);

      if (user.getMerchantId() == null) {
        return ResponseEntity.badRequest()
            .body(ApiResponse.error("No merchant assigned to this admin"));
      }

      List<Order> orders = orderService.getOrdersByMerchant(user.getMerchantId());
      List<OrderDTO> orderDTOs =
          orders.stream().map(orderMapper::toDTO).collect(Collectors.toList());
      return ResponseEntity.ok(
          ApiResponse.success(orderDTOs, "Your merchant orders retrieved successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to retrieve merchant orders: " + e.getMessage()));
    }
  }

  /**
   * Retrieves orders for a specific merchant. Admin or merchant admin (if they own the merchant)
   * can access.
   *
   * @param merchantId the merchant ID
   * @param authentication the authentication object
   * @return a list of orders for the specified merchant
   */
  @GetMapping("/merchant/{merchantId}")
  @IsAdminOrMerchantAdmin
  public ResponseEntity<ApiResponse<List<OrderDTO>>> getOrdersByMerchant(
      @PathVariable Long merchantId, Authentication authentication) {
    try {
      if (merchantId == null || merchantId <= 0) {
        return ResponseEntity.badRequest().body(ApiResponse.error("Invalid merchant ID"));
      }

      User user = permissionService.getAuthenticatedUser(authentication);

      // MERCHANT_ADMIN can only view orders for their own merchant
      if (user.hasRole(Role.MERCHANT_ADMIN) && !user.ownsMerchant(merchantId)) {
        throw new AccessDeniedException("You can only view orders for your own merchant");
      }

      List<Order> orders = orderService.getOrdersByMerchant(merchantId);
      List<OrderDTO> orderDTOs =
          orders.stream().map(orderMapper::toDTO).collect(Collectors.toList());
      return ResponseEntity.ok(
          ApiResponse.success(orderDTOs, "Merchant orders retrieved successfully"));
    } catch (AccessDeniedException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to retrieve merchant orders: " + e.getMessage()));
    }
  }

  /**
   * Retrieves orders assigned to the authenticated driver.
   *
   * @param authentication the authentication object
   * @return a list of orders assigned to the driver
   */
  @GetMapping("/driver/assigned")
  @IsDriver
  public ResponseEntity<ApiResponse<List<OrderDTO>>> getDriverOrders(
      Authentication authentication) {
    try {
      User user = permissionService.getAuthenticatedUser(authentication);

      if (user.getDriver() == null) {
        return ResponseEntity.badRequest()
            .body(ApiResponse.error("No driver profile found for this user"));
      }

      List<Order> orders = orderService.getOrdersByDriver(user.getDriver().getId());
      List<OrderDTO> orderDTOs =
          orders.stream().map(orderMapper::toDTO).collect(Collectors.toList());
      return ResponseEntity.ok(
          ApiResponse.success(orderDTOs, "Your assigned orders retrieved successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to retrieve driver orders: " + e.getMessage()));
    }
  }

  // ==================== UPDATE ORDERS ====================

  /**
   * Cancels an order. Only the user who placed the order can cancel it.
   *
   * @param orderId the order ID
   * @param authentication the authentication object
   * @return the cancelled order
   */
  @PostMapping("/{orderId}/cancel")
  @IsUser
  public ResponseEntity<ApiResponse<OrderDTO>> cancelOrder(
      @PathVariable Long orderId, Authentication authentication) {
    try {
      if (orderId == null || orderId <= 0) {
        return ResponseEntity.badRequest().body(ApiResponse.error("Invalid order ID"));
      }

      User user = permissionService.getAuthenticatedUser(authentication);
      Optional<Order> orderOpt = orderService.getOrderById(orderId);

      if (orderOpt.isEmpty()) {
        return ResponseEntity.notFound().build();
      }

      Order order = orderOpt.get();

      // Check if user owns this order
      if (order.getUser() == null || !order.getUser().getId().equals(user.getId())) {
        throw new AccessDeniedException("You can only cancel your own orders");
      }

      Order cancelledOrder = orderService.cancelOrder(orderId);
      OrderDTO orderDTO = orderMapper.toDTO(cancelledOrder);
      return ResponseEntity.ok(ApiResponse.success(orderDTO, "Order cancelled successfully"));
    } catch (AccessDeniedException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to cancel order: " + e.getMessage()));
    }
  }

  /**
   * Updates order status. Admin can update any order, merchant admin can update orders for their
   * merchant.
   *
   * @param orderId the order ID
   * @param status the new order status
   * @param authentication the authentication object
   * @return the updated order
   */
  @PutMapping("/{orderId}/status")
  @org.springframework.security.access.prepost.PreAuthorize(
      "hasRole('ADMIN') or @permissionService.merchantCanAccessOrder(authentication, #orderId)")
  public ResponseEntity<ApiResponse<OrderDTO>> updateOrderStatus(
      @PathVariable Long orderId, @RequestParam String status, Authentication authentication) {
    try {
      if (orderId == null || orderId <= 0) {
        return ResponseEntity.badRequest().body(ApiResponse.error("Invalid order ID"));
      }

      Optional<Order> orderOpt = orderService.getOrderById(orderId);

      if (orderOpt.isEmpty()) {
        return ResponseEntity.notFound().build();
      }

      // Authorization for admin/merchant admin is handled by PreAuthorize above
      Order updatedOrder = orderService.updateOrderStatus(orderId, status);
      OrderDTO orderDTO = orderMapper.toDTO(updatedOrder);
      return ResponseEntity.ok(ApiResponse.success(orderDTO, "Order status updated successfully"));
    } catch (AccessDeniedException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to update order status: " + e.getMessage()));
    }
  }

  /**
   * Method for getting a list of available orders by distance
   *
   * @param latitude of driver
   * @param longitude of driver
   * @param radiusKm for distance from driver
   * @param authentication for driver user
   * @return ResponseEntity with List of Orders
   */
  @GetMapping("/by-distance")
  @IsDriver
  public ResponseEntity<ApiResponse<List<OrderDTO>>> getOrdersByDistance(
      @RequestParam Double latitude,
      @RequestParam Double longitude,
      @RequestParam Double radiusKm,
      Authentication authentication) {
    try {

      if (latitude == null
          || longitude == null
          || radiusKm == null
          || radiusKm <= 0
          || latitude < -90
          || latitude > 90
          || longitude < -180
          || longitude > 180) {
        return ResponseEntity.badRequest().body(ApiResponse.error("Invalid location or radius"));
      }

      List<Order> orders = orderService.getOrdersWithinDistance(latitude, longitude, radiusKm);
      List<OrderDTO> orderDTOs =
          orders.stream().map(orderMapper::toDTO).collect(Collectors.toList());
      return ResponseEntity.ok(
          ApiResponse.success(orderDTOs, "Orders within distance retrieved successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to retrieve orders: " + e.getMessage()));
    }
  }
}
