package com.boozebuddies.controller;

import com.boozebuddies.dto.PaymentDTO;
import com.boozebuddies.entity.Payment;
import com.boozebuddies.entity.User;
import com.boozebuddies.entity.Order;
import com.boozebuddies.mapper.PaymentMapper;
import com.boozebuddies.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

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
    public ResponseEntity<PaymentDTO> processPayment(
            @RequestParam Long orderId,
            @RequestParam String paymentMethod) {

        Order order = new Order();
        order.setOrderId(orderId);

        Payment payment = paymentService.processPayment(order, paymentMethod);
        return ResponseEntity.ok(paymentMapper.toDTO(payment));
    }

    // -----------------------------
    // Issue a refund for an order
    // -----------------------------
    @PostMapping("/refund")
    public ResponseEntity<PaymentDTO> refundPayment(
            @RequestParam Long orderId,
            @RequestParam String reason) {

        Order order = new Order();
        order.setOrderId(orderId);

        Payment payment = paymentService.refundPayment(order, reason);
        return ResponseEntity.ok(paymentMapper.toDTO(payment));
    }

    // -----------------------------
    // Get payments by user
    // -----------------------------
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentDTO>> getPaymentsByUser(@PathVariable Long userId) {
        User user = new User();
        user.setUserId(userId);
        
        List<PaymentDTO> payments = paymentService.getPaymentsByUser(user)
                .stream()
                .map(paymentMapper::toDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(payments);
    }

    // -----------------------------
    // Get payment by order ID
    // -----------------------------
    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentDTO> getPaymentByOrderId(@PathVariable Long orderId) {
        Payment payment = paymentService.getPaymentByOrderId(orderId);
        
        if (payment == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(paymentMapper.toDTO(payment));
    }

    // -----------------------------
    // Calculate total revenue within a period
    // -----------------------------
    @GetMapping("/revenue")
    public ResponseEntity<BigDecimal> calculateTotalRevenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        return ResponseEntity.ok(paymentService.calculateTotalRevenue(startDate, endDate));
    }

    // -----------------------------
    // Validate a payment method
    // -----------------------------
    @PostMapping("/validate")
    public ResponseEntity<Boolean> validatePaymentMethod(
            @RequestParam Long userId,
            @RequestParam String paymentMethod) {

        User user = new User();
        user.setUserId(userId);

        return ResponseEntity.ok(paymentService.validatePaymentMethod(user, paymentMethod));
    }
}