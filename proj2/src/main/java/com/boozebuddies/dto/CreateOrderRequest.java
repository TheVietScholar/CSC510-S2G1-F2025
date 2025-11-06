package com.boozebuddies.dto;

import java.util.List;
import lombok.*;

/** DTO for creating a new order. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {
  /** The ID of the user placing the order. */
  private Long userId;

  /** The ID of the merchant. */
  private Long merchantId;

  /** The delivery address. */
  private String deliveryAddress;

  /** The list of items in the order. */
  private List<OrderItemRequest> items;

  /** Special instructions for the order. */
  private String specialInstructions;
}
