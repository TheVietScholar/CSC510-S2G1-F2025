package com.boozebuddies.dto;

import java.math.BigDecimal;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequest {
  private Long orderId;
  private BigDecimal amount;
  private String paymentMethod;
  private String cardToken;
  private String paymentSourceId;
}
