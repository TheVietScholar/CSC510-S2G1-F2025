package com.boozebuddies.dto;

import java.math.BigDecimal;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemDTO {
  private Long id;
  private Long productId;
  private Long orderId;
  private Integer quantity;
  private BigDecimal unitPrice;
  private BigDecimal subtotal;
}
