package com.boozebuddies.controller;

import com.boozebuddies.dto.ApiResponse;
import com.boozebuddies.dto.PaymentDTO;
import com.boozebuddies.entity.Order;
import com.boozebuddies.entity.Payment;
import com.boozebuddies.entity.User;
import com.boozebuddies.mapper.PaymentMapper;
import com.boozebuddies.service.PaymentService;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

  private final PaymentService paymentService;
  private final PaymentMapper paymentMapper;

  // -----------------------------
  // Process a payment for an order
  // -----------------------------
  @PostMapping("/process")
  public ResponseEntity<ApiResponse<PaymentDTO>> processPayment(
      @RequestParam Long orderId, @RequestParam String paymentMethod) {

    Order order = new Order();
    order.setId(orderId);

    Payment payment = paymentService.processPayment(order, paymentMethod);
    return ResponseEntity.ok(ApiResponse.success(paymentMapper.toDTO(payment)));
  }

  // -----------------------------
  // Issue a refund for an order
  // -----------------------------
  @PostMapping("/refund")
  public ResponseEntity<ApiResponse<PaymentDTO>> refundPayment(
      @RequestParam Long orderId, @RequestParam String reason) {

    Order order = new Order();
    order.setId(orderId);

    Payment payment = paymentService.refundPayment(order, reason);
    return ResponseEntity.ok(ApiResponse.success(paymentMapper.toDTO(payment)));
  }

  // -----------------------------
  // Get payments by user
  // -----------------------------
  @GetMapping("/user/{userId}")
  public ResponseEntity<?> getPaymentsByUser(@PathVariable Long userId, Pageable pageable) {
    User user = new User();
    user.setId(userId);

    Page<Payment> payments = paymentService.getPaymentsByUser(user, pageable);

    return ResponseEntity.ok(ApiResponse.success(payments));
  }

  // -----------------------------
  // Get payment by order ID
  // -----------------------------
  @GetMapping("/order/{orderId}")
  public ResponseEntity<?> getPaymentByOrderId(@PathVariable Long orderId) {
    Optional<Payment> payment = paymentService.getPaymentByOrderId(orderId);

    if (payment.isEmpty()) {
      return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok(ApiResponse.success(paymentMapper.toDTO(payment.get())));
  }

  // -----------------------------
  // Calculate total revenue within a period
  // -----------------------------
  @GetMapping("/revenue")
  public ResponseEntity<?> calculateTotalRevenue(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime startDate,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime endDate) {

    return ResponseEntity.ok(
        ApiResponse.success(paymentService.calculateTotalRevenue(startDate, endDate)));
  }

  // -----------------------------
  // Validate a payment method
  // -----------------------------
  @PostMapping("/validate")
  public ResponseEntity<?> validatePaymentMethod(
      @RequestParam Long userId, @RequestParam String paymentMethod) {

    User user = new User();
    user.setId(userId);

    return ResponseEntity.ok(
        ApiResponse.success(paymentService.validatePaymentMethod(user, paymentMethod)));
  }
}
