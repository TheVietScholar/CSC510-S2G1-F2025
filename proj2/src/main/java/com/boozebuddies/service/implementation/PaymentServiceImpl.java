package com.boozebuddies.service.implementation;

import com.boozebuddies.entity.Order;
import com.boozebuddies.entity.Payment;
import com.boozebuddies.entity.User;
import com.boozebuddies.service.PaymentService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class PaymentServiceImpl implements PaymentService {

  private final List<Payment> payments = new ArrayList<>();

  /** Processes a payment for an order. */
  @Override
  public Payment processPayment(Order order, String paymentMethod) {
    if (!validatePaymentMethod(order.getUser(), paymentMethod)) {
      throw new RuntimeException("Invalid payment method");
    }

    Payment payment = new Payment();
    payment.setOrder(order);
    payment.setUser(order.getUser());
    payment.setAmount(order.getTotalAmount());
    payment.setPaymentMethod(paymentMethod);
    payment.setStatus("COMPLETED");
    payment.setPaymentDate(LocalDate.now());

    payments.add(payment);
    System.out.println(
        "[PAYMENT] Processed payment of "
            + payment.getAmount()
            + " for Order ID: "
            + order.getOrderId()
            + " via "
            + paymentMethod);
    return payment;
  }

  /** Issues a refund for a specific order. */
  @Override
  public Payment refundPayment(Order order, String reason) {
    Payment payment = getPaymentByOrderId(order.getOrderId());
    if (payment == null) {
      throw new RuntimeException("Payment not found for order: " + order.getOrderId());
    }

    Payment refund = new Payment();
    refund.setOrder(order);
    refund.setUser(order.getUser());
    refund.setAmount(payment.getAmount());
    refund.setPaymentMethod(payment.getPaymentMethod());
    refund.setStatus("REFUNDED");
    refund.setPaymentDate(LocalDate.now());
    refund.setRefundReason(reason);

    payments.add(refund);
    System.out.println(
        "[PAYMENT] Refunded "
            + refund.getAmount()
            + " for Order ID: "
            + order.getOrderId()
            + " Reason: "
            + reason);
    return refund;
  }

  /** Retrieves all payments made by a specific user. */
  @Override
  public List<Payment> getPaymentsByUser(User user) {
    return payments.stream().filter(p -> p.getUser().equals(user)).collect(Collectors.toList());
  }

  /** Retrieves the payment details for a specific order. */
  @Override
  public Payment getPaymentByOrderId(Long orderId) {
    return payments.stream()
        .filter(p -> p.getOrder() != null && p.getOrder().getOrderId().equals(orderId))
        .findFirst()
        .orElse(null);
  }

  /** Calculates the total revenue generated within a given period. */
  @Override
  public BigDecimal calculateTotalRevenue(LocalDate startDate, LocalDate endDate) {
    return payments.stream()
        .filter(
            p ->
                p.getPaymentDate() != null
                    && !p.getPaymentDate().isBefore(startDate)
                    && !p.getPaymentDate().isAfter(endDate)
                    && "COMPLETED".equals(p.getStatus()))
        .map(Payment::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  /**
   * Validates a payment method before processing. For now, any non-null, non-empty string is
   * considered valid.
   */
  @Override
  public boolean validatePaymentMethod(User user, String paymentMethod) {
    return user != null && paymentMethod != null && !paymentMethod.trim().isEmpty();
  }
}
