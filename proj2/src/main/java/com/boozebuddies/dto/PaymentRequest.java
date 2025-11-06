package com.boozebuddies.dto;

import java.math.BigDecimal;
import lombok.*;

/** Data transfer object for processing a payment. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequest {
  /** The ID of the order to pay for */
  private Long orderId;

  /** The amount to charge */
  private BigDecimal amount;

  /** The payment method to use */
  private String paymentMethod;

  /** The tokenized card information */
  private String cardToken;

  /** The payment source ID from the payment processor */
  private String paymentSourceId;
}
