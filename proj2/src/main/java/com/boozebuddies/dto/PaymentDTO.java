package com.boozebuddies.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDTO {
  private Long id;
  private Long orderId;
  private BigDecimal amount;
  private String status;
  private String paymentMethod;
  private String transactionId;
  private LocalDateTime paymentDate;
  private String failureReason;
}
