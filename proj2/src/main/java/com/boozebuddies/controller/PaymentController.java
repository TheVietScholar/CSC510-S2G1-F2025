package com.boozebuddies.controller;

import com.boozebuddies.entity.Payment;
import com.boozebuddies.entity.User;
import com.boozebuddies.entity.Order;
import com.boozebuddies.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    // -----------------------------
    // Process a payment for an order
    // -----------------------------
    @PostMapping("/process")
    public Payment processPayment(
            @RequestParam Long orderId,
            @RequestParam String paymentMethod) {

        // Normally you would fetch the order from a service or repository
        Order order = new Order();
        order.setOrderId(orderId);

        return paymentService.processPayment(order, paymentMethod);
    }

    // -----------------------------
    // Issue a refund for an order
    // -----------------------------
    @PostMapping("/refund")
    public Payment refundPayment(
            @RequestParam Long orderId,
            @RequestParam String reason) {

        Order order = new Order();
        order.setOrderId(orderId);

        return paymentService.refundPayment(order, reason);
    }

    // -----------------------------
    // Get payments by user
    // -----------------------------
    @GetMapping("/user/{userId}")
    public List<Payment> getPaymentsByUser(@PathVariable Long userId) {
        User user = new User();
        user.setUserId(userId);
        return paymentService.getPaymentsByUser(user);
    }

    // -----------------------------
    // Get payment by order ID
    // -----------------------------
    @GetMapping("/order/{orderId}")
    public Payment getPaymentByOrderId(@PathVariable Long orderId) {
        return paymentService.getPaymentByOrderId(orderId);
    }

    // -----------------------------
    // Calculate total revenue within a period
    // -----------------------------
    @GetMapping("/revenue")
    public BigDecimal calculateTotalRevenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        return paymentService.calculateTotalRevenue(startDate, endDate);
    }

    // -----------------------------
    // Validate a payment method
    // -----------------------------
    @PostMapping("/validate")
    public boolean validatePaymentMethod(
            @RequestParam Long userId,
            @RequestParam String paymentMethod) {

        User user = new User();
        user.setUserId(userId);

        return paymentService.validatePaymentMethod(user, paymentMethod);
    }
}
