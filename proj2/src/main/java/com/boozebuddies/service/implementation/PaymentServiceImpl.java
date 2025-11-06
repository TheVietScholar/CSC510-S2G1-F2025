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

/**
 * Implementation of the {@link PaymentService} interface that handles payment processing, refund
 * management, and revenue calculations.
 *
 * <p>This service interacts with the {@link PaymentRepository} to persist and retrieve payment
 * data. It supports creating, refunding, validating, and aggregating payment transactions.
 *
 * <p>All payments are associated with {@link Order} and {@link User} entities and maintain their
 * corresponding {@link PaymentStatus}.
 */
@Service
public class PaymentServiceImpl implements PaymentService {

  /** Repository used for performing CRUD operations on {@link Payment} entities. */
  private final PaymentRepository paymentRepository;

  /**
   * Constructs a new {@code PaymentServiceImpl} with the specified payment repository.
   *
   * @param paymentRepository the {@link PaymentRepository} to use for database interactions
   */
  @Autowired
  public PaymentServiceImpl(PaymentRepository paymentRepository) {
    this.paymentRepository = paymentRepository;
  }

  /**
   * Processes a new payment for the specified order.
   *
   * <p>Validates the payment method, ensures that a payment does not already exist for the order,
   * and then creates a new {@link Payment} record with status {@link PaymentStatus#AUTHORIZED}.
   *
   * @param order the order being paid for
   * @param paymentMethod the payment method used (e.g., "Credit Card", "PayPal")
   * @return the newly created {@link Payment}
   * @throws RuntimeException if the payment method is invalid or if a payment already exists for
   *     the order
   */
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

  /**
   * Issues a refund for a specific order.
   *
   * <p>The original payment (if found) is deleted and replaced by a new {@link Payment} entry with
   * status {@link PaymentStatus#REFUNDED}. The refund reason is recorded for audit purposes.
   *
   * @param order the order being refunded
   * @param reason the reason for issuing the refund
   * @return the {@link Payment} record representing the refund
   * @throws RuntimeException if no payment is found for the given order
   */
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

  /**
   * Retrieves all payments made by a specific user.
   *
   * @param user the user whose payments should be retrieved
   * @param pageable the pagination and sorting configuration
   * @return a {@link Page} of {@link Payment} records belonging to the user
   */
  @Override
  public Page<Payment> getPaymentsByUser(User user, Pageable pageable) {
    return paymentRepository.findByUser_Id(user.getId(), pageable);
  }

  /**
   * Retrieves the payment details associated with a specific order.
   *
   * @param orderId the ID of the order
   * @return an {@link Optional} containing the payment if found, or empty if no payment exists
   */
  @Override
  public Optional<Payment> getPaymentByOrderId(Long orderId) {
    return paymentRepository.findByOrder_Id(orderId);
  }

  /**
   * Retrieves all payments stored in the system in a paginated format.
   *
   * @param pageable the pagination and sorting configuration
   * @return a {@link Page} of all {@link Payment} records
   */
  @Override
  public Page<Payment> getAllPayments(Pageable pageable) {
    return paymentRepository.findAll(pageable);
  }

  /**
   * Calculates the total revenue generated from authorized payments within a specified time range.
   *
   * @param startDate the start date (inclusive)
   * @param endDate the end date (inclusive)
   * @return the total revenue as a {@link BigDecimal}
   */
  @Override
  public BigDecimal calculateTotalRevenue(LocalDateTime startDate, LocalDateTime endDate) {
    return paymentRepository.findByCreatedAtBetween(startDate, endDate, Pageable.unpaged()).stream()
        .filter(p -> p.getStatus() == PaymentStatus.AUTHORIZED)
        .map(Payment::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  /**
   * Validates a payment method for a given user before processing a transaction.
   *
   * <p>Currently, this method only checks that the payment method is not null or blank and that the
   * user is valid. Future implementations could include more complex checks (e.g., card validation,
   * account verification, etc.).
   *
   * @param user the user attempting to make a payment
   * @param paymentMethod the payment method provided
   * @return true if the payment method is valid, false otherwise
   */
  @Override
  public boolean validatePaymentMethod(User user, String paymentMethod) {
    if (user == null) {
      return false;
    }
    if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
      return false;
    }
    String method = paymentMethod.trim().toLowerCase();
    // Accept test payment methods for testing purposes
    if (method.equals("test_payment") || method.equals("test")) {
      return true;
    }
    // For now, any non-empty string is considered valid (for future real payment
    // integrations)
    return true;
  }
}
