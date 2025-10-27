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

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

  // Find by associated order id
  @Query("SELECT p FROM Payment p WHERE p.order.id = :orderId")
  Optional<Payment> findByOrder_Id(Long orderId);

  // Find payments for a user with pagination
  @Query("SELECT p FROM Payment p WHERE p.user.id = :userId")
  Page<Payment> findByUser_Id(Long userId, Pageable pageable);

  // Find by payment status
  @Query("SELECT p FROM Payment p WHERE p.status = :status")
  List<Payment> findAllByStatus(PaymentStatus status);

  // Find by status with paging
  @Query("SELECT p FROM Payment p WHERE p.status = :status")
  Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);

  // Find payments in a date range
  @Query("SELECT p FROM Payment p WHERE p.createdAt BETWEEN :start AND :end")
  Page<Payment> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

  // Find by transaction id
  @Query("SELECT p FROM Payment p WHERE p.transactionId = :transactionId")
  Optional<Payment> findByTransactionId(String transactionId);

  // Sum of amounts for a given status (returns zero when no rows)
  @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = :status")
  BigDecimal sumAmountByStatus(@Param("status") PaymentStatus status);
}
