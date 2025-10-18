package com.boozebuddies.dto;

import java.util.List;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {
  private Long userId;
  private Long merchantId;
  private String deliveryAddress;
  private List<OrderItemRequest> items;
  private String specialInstructions;
}
