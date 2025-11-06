package com.boozebuddies.entity;

import com.boozebuddies.model.PaymentStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

/** Entity representing a payment for an order. */
@Entity
@Table(name = "payments")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Payment {
  /** The unique payment ID */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** The order this payment is for */
  @OneToOne
  @JoinColumn(name = "order_id", nullable = false)
  private Order order;

  /** The payment amount */
  @Column(precision = 10, scale = 2, nullable = false)
  private BigDecimal amount;

  /** The current payment status */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PaymentStatus status;

  /** The user making the payment */
  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  /** The payment method used */
  @Column(name = "payment_method")
  private String paymentMethod;

  /** The transaction ID from the payment processor */
  @Column(name = "transaction_id")
  private String transactionId;

  /** The reason for payment failure, if applicable */
  @Builder.Default
  @Column(name = "failure_reason")
  private String failureReason = "";

  /** The reason for refund, if applicable */
  @Builder.Default
  @Column(name = "refund_reason")
  private String refundReason = "";

  /** When the payment record was created */
  @Builder.Default
  @Column(name = "created_at")
  private LocalDateTime createdAt = LocalDateTime.now();

  /** When the payment record was last updated */
  @Builder.Default
  @Column(name = "updated_at")
  private LocalDateTime updatedAt = LocalDateTime.now();

  /** When the payment was processed */
  @Column(name = "payment_date")
  private LocalDateTime paymentDate;

  /** Updates the updatedAt timestamp before persisting changes. */
  @PreUpdate
  public void preUpdate() {
    this.updatedAt = LocalDateTime.now();
  }
}
