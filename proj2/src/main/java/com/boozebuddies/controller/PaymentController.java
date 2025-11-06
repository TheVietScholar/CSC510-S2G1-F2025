package com.boozebuddies.controller;

import com.boozebuddies.dto.ApiResponse;
import com.boozebuddies.dto.PaymentDTO;
import com.boozebuddies.entity.Order;
import com.boozebuddies.entity.Payment;
import com.boozebuddies.entity.User;
import com.boozebuddies.mapper.PaymentMapper;
import com.boozebuddies.model.Role;
import com.boozebuddies.security.annotation.RoleAnnotations.*;
import com.boozebuddies.service.OrderService;
import com.boozebuddies.service.PaymentService;
import com.boozebuddies.service.PermissionService;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/** REST controller for managing payments and payment operations. */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

  private final PaymentService paymentService;
  private final PaymentMapper paymentMapper;
  private final PermissionService permissionService;
  private final OrderService orderService;

  // ==================== PROCESS PAYMENT ====================

  /**
   * Processes a payment for an order. Only users can pay for their own orders.
   *
   * @param orderId the order ID
   * @param paymentMethod the payment method
   * @param authentication the authentication object
   * @return the processed payment
   */
  @PostMapping("/process")
  @IsUser
  public ResponseEntity<ApiResponse<PaymentDTO>> processPayment(
      @RequestParam Long orderId,
      @RequestParam String paymentMethod,
      Authentication authentication) {
    try {
      User user = permissionService.getAuthenticatedUser(authentication);

      // Verify the order exists and belongs to the authenticated user
      Order order =
          orderService
              .getOrderById(orderId)
              .orElseThrow(() -> new RuntimeException("Order not found"));

      if (order.getUser() == null || !order.getUser().getId().equals(user.getId())) {
        throw new AccessDeniedException("You can only pay for your own orders");
      }

      Payment payment = paymentService.processPayment(order, paymentMethod);
      return ResponseEntity.ok(
          ApiResponse.success(paymentMapper.toDTO(payment), "Payment processed successfully"));
    } catch (AccessDeniedException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to process payment: " + e.getMessage()));
    }
  }

  // ==================== REFUND PAYMENT ====================

  /**
   * Issues a refund for an order. Admin only.
   *
   * @param orderId the order ID
   * @param reason the refund reason
   * @return the refund payment
   */
  @PostMapping("/refund")
  @IsAdmin
  public ResponseEntity<ApiResponse<PaymentDTO>> refundPayment(
      @RequestParam Long orderId, @RequestParam String reason) {
    try {
      Order order =
          orderService
              .getOrderById(orderId)
              .orElseThrow(() -> new RuntimeException("Order not found"));

      Payment payment = paymentService.refundPayment(order, reason);
      return ResponseEntity.ok(
          ApiResponse.success(paymentMapper.toDTO(payment), "Refund processed successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to process refund: " + e.getMessage()));
    }
  }

  // ==================== RETRIEVE PAYMENTS ====================

  /**
   * Retrieves the authenticated user's payment history. Users can only view their own payments.
   *
   * @param authentication the authentication object
   * @param pageable the pagination information
   * @return a paginated list of the user's payments
   */
  @GetMapping("/my-payments")
  @IsUser
  public ResponseEntity<ApiResponse<Page<PaymentDTO>>> getMyPayments(
      Authentication authentication, Pageable pageable) {
    try {
      User user = permissionService.getAuthenticatedUser(authentication);
      Page<Payment> payments = paymentService.getPaymentsByUser(user, pageable);
      Page<PaymentDTO> paymentDTOs = payments.map(paymentMapper::toDTO);
      return ResponseEntity.ok(
          ApiResponse.success(paymentDTOs, "Your payments retrieved successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to retrieve payments: " + e.getMessage()));
    }
  }

  /**
   * Retrieves payments by user ID. Users can view their own payments, admins can view any user's
   * payments.
   *
   * @param userId the user ID
   * @param authentication the authentication object
   * @param pageable the pagination information
   * @return a paginated list of payments for the specified user
   */
  @GetMapping("/user/{userId}")
  @IsAuthenticated
  public ResponseEntity<ApiResponse<Page<PaymentDTO>>> getPaymentsByUser(
      @PathVariable Long userId, Authentication authentication, Pageable pageable) {
    try {
      User authenticatedUser = permissionService.getAuthenticatedUser(authentication);

      // Check if user is accessing their own payments or is an admin
      if (!authenticatedUser.getId().equals(userId) && !authenticatedUser.hasRole(Role.ADMIN)) {
        throw new AccessDeniedException("You can only view your own payment history");
      }

      User user = new User();
      user.setId(userId);
      Page<Payment> payments = paymentService.getPaymentsByUser(user, pageable);
      Page<PaymentDTO> paymentDTOs = payments.map(paymentMapper::toDTO);
      return ResponseEntity.ok(ApiResponse.success(paymentDTOs, "Payments retrieved successfully"));
    } catch (AccessDeniedException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to retrieve payments: " + e.getMessage()));
    }
  }

  /**
   * Retrieves payment by order ID. Users can view payment for their own orders, admins can view any
   * payment.
   *
   * @param orderId the order ID
   * @param authentication the authentication object
   * @return the payment for the specified order
   */
  @GetMapping("/order/{orderId}")
  @IsAuthenticated
  public ResponseEntity<ApiResponse<PaymentDTO>> getPaymentByOrderId(
      @PathVariable Long orderId, Authentication authentication) {
    try {
      User user = permissionService.getAuthenticatedUser(authentication);

      // Get the order to verify ownership
      Order order =
          orderService
              .getOrderById(orderId)
              .orElseThrow(() -> new RuntimeException("Order not found"));

      // Check if user owns the order or is an admin
      if (order.getUser() == null
          || (!order.getUser().getId().equals(user.getId()) && !user.hasRole(Role.ADMIN))) {
        throw new AccessDeniedException("You can only view payments for your own orders");
      }

      Optional<Payment> paymentOpt = paymentService.getPaymentByOrderId(orderId);
      if (paymentOpt.isEmpty()) {
        return ResponseEntity.notFound().build();
      }

      return ResponseEntity.ok(
          ApiResponse.success(
              paymentMapper.toDTO(paymentOpt.get()), "Payment retrieved successfully"));
    } catch (AccessDeniedException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to retrieve payment: " + e.getMessage()));
    }
  }

  // ==================== ADMIN OPERATIONS ====================

  /**
   * Calculates total revenue within a period. Admin only.
   *
   * @param startDate the start date
   * @param endDate the end date
   * @return the total revenue for the specified period
   */
  @GetMapping("/revenue")
  @IsAdmin
  public ResponseEntity<ApiResponse<Object>> calculateTotalRevenue(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
    try {
      Object revenue = paymentService.calculateTotalRevenue(startDate, endDate);
      return ResponseEntity.ok(ApiResponse.success(revenue, "Revenue calculated successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to calculate revenue: " + e.getMessage()));
    }
  }

  /**
   * Retrieves all payments. Admin only.
   *
   * @param pageable the pagination information
   * @return a paginated list of all payments
   */
  @GetMapping
  @IsAdmin
  public ResponseEntity<ApiResponse<Page<PaymentDTO>>> getAllPayments(Pageable pageable) {
    try {
      Page<Payment> payments = paymentService.getAllPayments(pageable);
      Page<PaymentDTO> paymentDTOs = payments.map(paymentMapper::toDTO);
      return ResponseEntity.ok(
          ApiResponse.success(paymentDTOs, "All payments retrieved successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to retrieve payments: " + e.getMessage()));
    }
  }

  // ==================== PAYMENT METHOD VALIDATION ====================

  /**
   * Validates a payment method. Users can validate their own payment methods, admins can validate
   * for any user.
   *
   * @param userId the user ID
   * @param paymentMethod the payment method to validate
   * @param authentication the authentication object
   * @return whether the payment method is valid
   */
  @PostMapping("/validate")
  @IsAuthenticated
  public ResponseEntity<ApiResponse<Boolean>> validatePaymentMethod(
      @RequestParam Long userId,
      @RequestParam String paymentMethod,
      Authentication authentication) {
    try {
      User authenticatedUser = permissionService.getAuthenticatedUser(authentication);

      // Check if user is validating their own payment method or is an admin
      if (!authenticatedUser.getId().equals(userId) && !authenticatedUser.hasRole(Role.ADMIN)) {
        throw new AccessDeniedException("You can only validate your own payment methods");
      }

      User user = new User();
      user.setId(userId);
      boolean isValid = paymentService.validatePaymentMethod(user, paymentMethod);
      return ResponseEntity.ok(
          ApiResponse.success(isValid, "Payment method validated successfully"));
    } catch (AccessDeniedException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to validate payment method: " + e.getMessage()));
    }
  }
}
