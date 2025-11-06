package com.boozebuddies.repository;

import com.boozebuddies.entity.Payment;
import com.boozebuddies.model.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing {@link Payment} entities within the BoozeBuddies platform.
 *
 * <p>Extends {@link JpaRepository} to provide standard CRUD operations, along with custom query
 * methods for retrieving and analyzing payment records based on user, order, status, and
 * transaction data.
 *
 * <p>This repository supports both paginated and non-paginated queries for use in reporting
 * dashboards, user billing history, and back-office analytics.
 *
 * <p>Common use cases include:
 *
 * <ul>
 *   <li>Retrieving payments by associated order or user
 *   <li>Filtering transactions by {@link PaymentStatus}
 *   <li>Aggregating payment totals for financial summaries
 *   <li>Tracking refunds, failed payments, and pending authorizations
 * </ul>
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

  /**
   * Retrieves the payment associated with a specific order.
   *
   * @param orderId the ID of the related order
   * @return an {@link Optional} containing the associated {@link Payment}, if found
   */
  @Query("SELECT p FROM Payment p WHERE p.order.id = :orderId")
  Optional<Payment> findByOrder_Id(Long orderId);

  /**
   * Retrieves a paginated list of payments associated with a specific user.
   *
   * @param userId the ID of the user
   * @param pageable pagination and sorting configuration
   * @return a {@link Page} of payments made by the user
   */
  @Query("SELECT p FROM Payment p WHERE p.user.id = :userId")
  Page<Payment> findByUser_Id(Long userId, Pageable pageable);

  /**
   * Retrieves all payments matching the specified {@link PaymentStatus}.
   *
   * @param status the status to filter payments by
   * @return a list of payments with the given status
   */
  @Query("SELECT p FROM Payment p WHERE p.status = :status")
  List<Payment> findAllByStatus(PaymentStatus status);

  /**
   * Retrieves a paginated list of payments filtered by {@link PaymentStatus}.
   *
   * @param status the payment status to filter by
   * @param pageable pagination and sorting configuration
   * @return a {@link Page} of payments matching the given status
   */
  @Query("SELECT p FROM Payment p WHERE p.status = :status")
  Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);

  /**
   * Retrieves a paginated list of payments created within a specified date range.
   *
   * @param start the start of the date range
   * @param end the end of the date range
   * @param pageable pagination and sorting configuration
   * @return a {@link Page} of payments created between the given timestamps
   */
  @Query("SELECT p FROM Payment p WHERE p.createdAt BETWEEN :start AND :end")
  Page<Payment> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

  /**
   * Retrieves a payment by its unique transaction identifier.
   *
   * @param transactionId the external or internal transaction ID
   * @return an {@link Optional} containing the {@link Payment} if it exists
   */
  @Query("SELECT p FROM Payment p WHERE p.transactionId = :transactionId")
  Optional<Payment> findByTransactionId(String transactionId);

  /**
   * Calculates the total sum of payment amounts for a given {@link PaymentStatus}.
   *
   * <p>Returns {@code 0} if no matching payments exist.
   *
   * @param status the payment status to filter by
   * @return the total monetary value of payments with the given status
   */
  @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = :status")
  BigDecimal sumAmountByStatus(@Param("status") PaymentStatus status);
}
