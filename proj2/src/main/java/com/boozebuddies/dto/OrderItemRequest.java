package com.boozebuddies.dto;

import java.math.BigDecimal;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemRequest {
  private Long productId;
  private Integer quantity;
  private BigDecimal unitPrice;
}
