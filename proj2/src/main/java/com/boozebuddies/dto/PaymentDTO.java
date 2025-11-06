package com.boozebuddies.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

/** Data transfer object for payment information. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDTO {
  /** The unique payment ID */
  private Long id;

  /** The ID of the associated order */
  private Long orderId;

  /** The payment amount */
  private BigDecimal amount;

  /** The current payment status */
  private String status;

  /** The ID of the user making the payment */
  private Long userId;

  /** The payment method used */
  private String paymentMethod;

  /** The transaction ID from the payment processor */
  private String transactionId;

  /** When the payment was made */
  private LocalDateTime paymentDate;

  /** The reason for payment failure, if applicable */
  private String failureReason;

  /** The reason for refund, if applicable */
  private String refundReason;
}
