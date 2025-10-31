package com.boozebuddies.service.implementation;

import com.boozebuddies.entity.Order;
import com.boozebuddies.entity.Payment;
import com.boozebuddies.entity.User;
import com.boozebuddies.model.PaymentStatus;
import com.boozebuddies.repository.PaymentRepository;
import com.boozebuddies.service.PaymentService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class PaymentServiceImpl implements PaymentService {

  private final PaymentRepository paymentRepository;

  @Autowired
  public PaymentServiceImpl(PaymentRepository paymentRepository) {
    this.paymentRepository = paymentRepository;
  }

  /** Processes a payment for an order. */
  @Override
  public Payment processPayment(Order order, String paymentMethod) {
    if (!validatePaymentMethod(order.getUser(), paymentMethod)) {
      throw new RuntimeException("Invalid payment method");
    }

    paymentRepository
        .findByOrder_Id(order.getId())
        .ifPresent(
            p -> {
              throw new RuntimeException("Payment already exists for order: " + order.getId());
            });

    Payment payment = new Payment();
    payment.setOrder(order);
    payment.setUser(order.getUser());
    payment.setAmount(order.getTotalAmount());
    payment.setPaymentMethod(paymentMethod);
    payment.setStatus(PaymentStatus.AUTHORIZED);
    payment.setCreatedAt(LocalDateTime.now());
    payment.setUpdatedAt(LocalDateTime.now());

    paymentRepository.save(payment);

    System.out.println(
        "[PAYMENT] Processed payment of "
            + payment.getAmount()
            + " for Order ID: "
            + order.getId()
            + " via "
            + paymentMethod);
    return payment;
  }

  /** Issues a refund for a specific order. */
  @Override
  public Payment refundPayment(Order order, String reason) {
    Optional<Payment> payment = getPaymentByOrderId(order.getId());
    if (!payment.isPresent()) {
      throw new RuntimeException("Payment not found for order: " + order.getId());
    }

    paymentRepository.delete(payment.get());

    Payment refund = new Payment();
    refund.setOrder(order);
    refund.setUser(order.getUser());
    refund.setAmount(payment.get().getAmount());
    refund.setPaymentMethod(payment.get().getPaymentMethod());
    refund.setStatus(PaymentStatus.REFUNDED);
    refund.setUpdatedAt(LocalDateTime.now());
    refund.setRefundReason(reason);

    paymentRepository.save(refund);
    System.out.println(
        "[PAYMENT] Refunded "
            + refund.getAmount()
            + " for Order ID: "
            + order.getId()
            + " Reason: "
            + reason);
    return refund;
  }

  /** Retrieves all payments made by a specific user. */
  @Override
  public Page<Payment> getPaymentsByUser(User user, Pageable pageable) {
    return paymentRepository.findByUser_Id(user.getId(), pageable);
  }

  /** Retrieves the payment details for a specific order. */
  @Override
  public Optional<Payment> getPaymentByOrderId(Long orderId) {
    return paymentRepository.findByOrder_Id(orderId);
  }

  /** Retrieves all payments in the system (paginated). */
  @Override
  public Page<Payment> getAllPayments(Pageable pageable) {
    return paymentRepository.findAll(pageable);
  }

  /** Calculates the total revenue generated within a given period. */
  @Override
  public BigDecimal calculateTotalRevenue(LocalDateTime startDate, LocalDateTime endDate) {
    return paymentRepository.findByCreatedAtBetween(startDate, endDate, Pageable.unpaged()).stream()
        .filter(p -> p.getStatus() == PaymentStatus.AUTHORIZED)
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