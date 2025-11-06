package com.boozebuddies.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.*;

/** Data transfer object for order information. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDTO {
  /** The unique order ID */
  private Long id;

  /** The ID of the user who placed the order */
  private Long userId;

  /** The ID of the merchant fulfilling the order */
  private Long merchantId;

  /** The ID of the driver delivering the order */
  private Long driverId;

  /** The total amount of the order */
  private BigDecimal totalAmount;

  /** The current order status */
  private String status;

  /** The delivery address for the order */
  private String deliveryAddress;

  /** The list of items in the order */
  private List<OrderItemDTO> items;

  /** When the order was created */
  private LocalDateTime createdAt;

  /** When the order was last updated */
  private LocalDateTime updatedAt;

  /** The estimated time of delivery */
  private LocalDateTime estimatedDeliveryTime;
}
