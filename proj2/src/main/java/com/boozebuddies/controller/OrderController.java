package com.boozebuddies.controller;

import com.boozebuddies.dto.ApiResponse;
import com.boozebuddies.dto.CreateOrderRequest;
import com.boozebuddies.dto.OrderDTO;
import com.boozebuddies.entity.Merchant;
import com.boozebuddies.entity.Order;
import com.boozebuddies.entity.OrderItem;
import com.boozebuddies.entity.User;
import com.boozebuddies.model.Role;
import com.boozebuddies.security.annotation.RoleAnnotations.*;
import com.boozebuddies.service.OrderService;
import com.boozebuddies.service.PermissionService;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

  @Autowired private OrderService orderService;
  @Autowired private PermissionService permissionService;

  // ==================== CREATE ORDER (USER ONLY) ====================
  
  /**
   * Create a new order.
   * Only users with USER role can place orders.
   */
  @PostMapping
  @IsUser
  public ResponseEntity<ApiResponse<OrderDTO>> createOrder(
      @RequestBody CreateOrderRequest createOrderRequest,
      Authentication authentication) {
    try {
      User user = permissionService.getAuthenticatedUser(authentication);
      
      // Ensure the order is being created for the authenticated user
      if (createOrderRequest.getUserId() != null 
          && !createOrderRequest.getUserId().equals(user.getId())) {
        throw new AccessDeniedException("You can only create orders for yourself");
      }
      
      // Set the user ID from authenticated user if not provided
      createOrderRequest.setUserId(user.getId());
      
      // Convert CreateOrderRequest to Order entity
      Order order = convertToEntity(createOrderRequest);
      Order createdOrder = orderService.createOrder(order);
      OrderDTO orderDTO = convertToDTO(createdOrder);
      return ResponseEntity.ok(ApiResponse.success(orderDTO, "Order created successfully"));
    } catch (AccessDeniedException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(ApiResponse.error(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to create order: " + e.getMessage()));
    }
  }

  // ==================== RETRIEVE ORDERS ====================

  /**
   * Get order by ID.
   * Users can view their own orders, merchant admins can view orders for their merchant,
   * drivers can view assigned orders, and admins can view all orders.
   */
  @GetMapping("/{orderId}")
  @IsAuthenticated
  public ResponseEntity<ApiResponse<OrderDTO>> getOrderById(
      @PathVariable Long orderId,
      Authentication authentication) {
    try {
      if (orderId == null || orderId <= 0) {
        return ResponseEntity.badRequest()
            .body(ApiResponse.error("Invalid order ID"));
      }

      User user = permissionService.getAuthenticatedUser(authentication);
      Optional<Order> orderOpt = orderService.getOrderById(orderId);
      
      if (orderOpt.isEmpty()) {
        return ResponseEntity.notFound().build();
      }
      
      Order order = orderOpt.get();
      
      // Check if user has permission to view this order
      boolean canAccess = user.hasRole(Role.ADMIN) || // Admins can see all
          (order.getUser() != null && order.getUser().getId().equals(user.getId())) || // Owner
          (user.hasRole(Role.MERCHANT_ADMIN) && 
              order.getMerchant() != null && 
              user.ownsMerchant(order.getMerchant().getId())) || // Merchant admin
          (user.hasRole(Role.DRIVER) && 
              order.getDriver() != null && 
              user.getDriver() != null &&
              order.getDriver().getId().equals(user.getDriver().getId())); // Assigned driver
      
      if (!canAccess) {
        throw new AccessDeniedException("You don't have permission to view this order");
      }
      
      OrderDTO orderDTO = convertToDTO(order);
      return ResponseEntity.ok(ApiResponse.success(orderDTO, "Order retrieved successfully"));
    } catch (AccessDeniedException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(ApiResponse.error(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to retrieve order: " + e.getMessage()));
    }
  }

  /**
   * Get all orders for the authenticated user.
   * Users can only view their own orders.
   */
  @GetMapping("/my-orders")
  @IsUser
  public ResponseEntity<ApiResponse<List<OrderDTO>>> getMyOrders(
      Authentication authentication) {
    try {
      User user = permissionService.getAuthenticatedUser(authentication);
      List<Order> orders = orderService.getOrdersByUser(user.getId());
      List<OrderDTO> orderDTOs =
          orders.stream().map(this::convertToDTO).collect(Collectors.toList());
      return ResponseEntity.ok(
          ApiResponse.success(orderDTOs, "Your orders retrieved successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to retrieve your orders: " + e.getMessage()));
    }
  }

  /**
   * Get all orders for a specific user.
   * Admin only - to view any user's orders.
   */
  @GetMapping("/user/{userId}")
  @IsAdmin
  public ResponseEntity<ApiResponse<List<OrderDTO>>> getOrdersByUser(
      @PathVariable Long userId) {
    try {
      if (userId == null || userId <= 0) {
        return ResponseEntity.badRequest()
            .body(ApiResponse.error("Invalid user ID"));
      }
      
      List<Order> orders = orderService.getOrdersByUser(userId);
      List<OrderDTO> orderDTOs =
          orders.stream().map(this::convertToDTO).collect(Collectors.toList());
      return ResponseEntity.ok(
          ApiResponse.success(orderDTOs, "User orders retrieved successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to retrieve user orders: " + e.getMessage()));
    }
  }

  /**
   * Get all orders (Admin only).
   */
  @GetMapping
  @IsAdmin
  public ResponseEntity<ApiResponse<List<OrderDTO>>> getAllOrders() {
    try {
      List<Order> orders = orderService.getAllOrders();
      List<OrderDTO> orderDTOs =
          orders.stream().map(this::convertToDTO).collect(Collectors.toList());
      return ResponseEntity.ok(ApiResponse.success(orderDTOs, "All orders retrieved successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to retrieve orders: " + e.getMessage()));
    }
  }

  /**
   * Get orders for the merchant managed by the authenticated merchant admin.
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
          orders.stream().map(this::convertToDTO).collect(Collectors.toList());
      return ResponseEntity.ok(
          ApiResponse.success(orderDTOs, "Your merchant orders retrieved successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to retrieve merchant orders: " + e.getMessage()));
    }
  }

  /**
   * Get orders for a specific merchant.
   * Admin or merchant admin (if they own the merchant) can access.
   */
  @GetMapping("/merchant/{merchantId}")
  @IsAdminOrMerchantAdmin
  public ResponseEntity<ApiResponse<List<OrderDTO>>> getOrdersByMerchant(
      @PathVariable Long merchantId,
      Authentication authentication) {
    try {
      if (merchantId == null || merchantId <= 0) {
        return ResponseEntity.badRequest()
            .body(ApiResponse.error("Invalid merchant ID"));
      }

      User user = permissionService.getAuthenticatedUser(authentication);

      // MERCHANT_ADMIN can only view orders for their own merchant
      if (user.hasRole(Role.MERCHANT_ADMIN) && !user.ownsMerchant(merchantId)) {
        throw new AccessDeniedException("You can only view orders for your own merchant");
      }

      List<Order> orders = orderService.getOrdersByMerchant(merchantId);
      List<OrderDTO> orderDTOs =
          orders.stream().map(this::convertToDTO).collect(Collectors.toList());
      return ResponseEntity.ok(
          ApiResponse.success(orderDTOs, "Merchant orders retrieved successfully"));
    } catch (AccessDeniedException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(ApiResponse.error(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to retrieve merchant orders: " + e.getMessage()));
    }
  }

  /**
   * Get orders assigned to the authenticated driver.
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
          orders.stream().map(this::convertToDTO).collect(Collectors.toList());
      return ResponseEntity.ok(
          ApiResponse.success(orderDTOs, "Your assigned orders retrieved successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to retrieve driver orders: " + e.getMessage()));
    }
  }

  // ==================== UPDATE ORDERS ====================

  /**
   * Cancel an order.
   * Only the user who placed the order can cancel it.
   */
  @PostMapping("/{orderId}/cancel")
  @IsUser
  public ResponseEntity<ApiResponse<OrderDTO>> cancelOrder(
      @PathVariable Long orderId,
      Authentication authentication) {
    try {
      if (orderId == null || orderId <= 0) {
        return ResponseEntity.badRequest()
            .body(ApiResponse.error("Invalid order ID"));
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
      OrderDTO orderDTO = convertToDTO(cancelledOrder);
      return ResponseEntity.ok(ApiResponse.success(orderDTO, "Order cancelled successfully"));
    } catch (AccessDeniedException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(ApiResponse.error(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to cancel order: " + e.getMessage()));
    }
  }

  /**
   * Update order status.
   * Admin can update any order, merchant admin can update orders for their merchant.
   */
  @PutMapping("/{orderId}/status")
  @IsAdminOrMerchantAdmin
  public ResponseEntity<ApiResponse<OrderDTO>> updateOrderStatus(
      @PathVariable Long orderId,
      @RequestParam String status,
      Authentication authentication) {
    try {
      if (orderId == null || orderId <= 0) {
        return ResponseEntity.badRequest()
            .body(ApiResponse.error("Invalid order ID"));
      }

      User user = permissionService.getAuthenticatedUser(authentication);
      Optional<Order> orderOpt = orderService.getOrderById(orderId);
      
      if (orderOpt.isEmpty()) {
        return ResponseEntity.notFound().build();
      }
      
      Order order = orderOpt.get();
      
      // MERCHANT_ADMIN can only update orders for their own merchant
      if (user.hasRole(Role.MERCHANT_ADMIN)) {
        if (order.getMerchant() == null || !user.ownsMerchant(order.getMerchant().getId())) {
          throw new AccessDeniedException("You can only update orders for your own merchant");
        }
      }
      
      Order updatedOrder = orderService.updateOrderStatus(orderId, status);
      OrderDTO orderDTO = convertToDTO(updatedOrder);
      return ResponseEntity.ok(ApiResponse.success(orderDTO, "Order status updated successfully"));
    } catch (AccessDeniedException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(ApiResponse.error(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to update order status: " + e.getMessage()));
    }
  }

  // ==================== HELPER METHODS ====================

  /** Convert CreateOrderRequest to Order entity */
  private Order convertToEntity(CreateOrderRequest createOrderRequest) {
    Order order = new Order();

    // Set user (you would fetch from repository in real implementation)
    User user = new User();
    user.setId(createOrderRequest.getUserId());
    order.setUser(user);

    // Set merchant (you would fetch from repository in real implementation)
    Merchant merchant = new Merchant();
    merchant.setId(createOrderRequest.getMerchantId());
    order.setMerchant(merchant);

    order.setDeliveryAddress(createOrderRequest.getDeliveryAddress());
    order.setSpecialInstructions(createOrderRequest.getSpecialInstructions());

    // Convert order items
    if (createOrderRequest.getItems() != null) {
      List<OrderItem> orderItems =
          createOrderRequest.getItems().stream()
              .map(
                  itemRequest -> {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrder(order);
                    // Set product (you would fetch from repository)
                    // orderItem.setProduct(productRepository.findById(itemRequest.getProductId()).orElseThrow());
                    orderItem.setQuantity(itemRequest.getQuantity());
                    orderItem.setUnitPrice(itemRequest.getUnitPrice());
                    return orderItem;
                  })
              .collect(Collectors.toList());
      order.setItems(orderItems);
    }

    return order;
  }

  /** Convert Order entity to OrderDTO */
  private OrderDTO convertToDTO(Order order) {
    return OrderDTO.builder()
        .id(order.getId())
        .userId(order.getUser() != null ? order.getUser().getId() : null)
        .merchantId(order.getMerchant() != null ? order.getMerchant().getId() : null)
        .driverId(order.getDriver() != null ? order.getDriver().getId() : null)
        .totalAmount(order.getTotalAmount())
        .status(order.getStatus().name())
        .deliveryAddress(order.getDeliveryAddress())
        .createdAt(order.getCreatedAt())
        .updatedAt(order.getUpdatedAt())
        .estimatedDeliveryTime(order.getEstimatedDeliveryTime())
        // Note: You might want to convert order items to OrderItemDTOs as well
        .build();
  }
}
