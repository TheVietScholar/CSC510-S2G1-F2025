package com.boozebuddies.dto;

import java.math.BigDecimal;
import lombok.*;

/** Data transfer object for creating an order item. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemRequest {
  /** The ID of the product to order */
  private Long productId;

  /** The quantity to order */
  private Integer quantity;

  /** The unit price of the product */
  private BigDecimal unitPrice;
}
