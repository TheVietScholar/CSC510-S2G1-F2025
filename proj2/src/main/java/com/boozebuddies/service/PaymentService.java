package com.boozebuddies.service;

import com.boozebuddies.entity.Order;
import com.boozebuddies.entity.Payment;
import com.boozebuddies.entity.User;
import java.math.BigDecimal;
import java.util.List;

public interface PaymentService {

  /**
   * Processes a payment for an order.
   *
   * @param order The order for which payment is being processed.
   * @param paymentMethod The payment method used (e.g., "CREDIT_CARD", "APPLE_PAY").
   * @return The resulting Payment object with status details.
   */
  Payment processPayment(Order order, String paymentMethod);

  /**
   * Issues a refund for a specific order.
   *
   * @param order The order to refund.
   * @param reason The reason for the refund.
   * @return The Payment object representing the refund transaction.
   */
  Payment refundPayment(Order order, String reason);

  /**
   * Retrieves all payments made by a specific user.
   *
   * @param user The user whose payment history is being retrieved.
   * @return A list of Payment objects linked to the user.
   */
  List<Payment> getPaymentsByUser(User user);

  /**
   * Retrieves the payment details for a specific order.
   *
   * @param orderId The ID of the order.
   * @return The associated Payment object, or null if not found.
   */
  Payment getPaymentByOrderId(Long orderId);

  /**
   * Calculates the total revenue generated within a given period.
   *
   * @param startDate Start date for the report (inclusive).
   * @param endDate End date for the report (inclusive).
   * @return The total revenue as a BigDecimal.
   */
  BigDecimal calculateTotalRevenue(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);

  /**
   * Validates a payment method before processing.
   *
   * @param user The user attempting payment.
   * @param paymentMethod The payment method (e.g., card or wallet info).
   * @return True if valid, false otherwise.
   */
  boolean validatePaymentMethod(User user, String paymentMethod);
}
