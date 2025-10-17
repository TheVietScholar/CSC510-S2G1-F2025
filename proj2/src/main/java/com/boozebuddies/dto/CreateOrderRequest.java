package com.boozebuddies.dto;

import lombok.*;
import java.util.List;

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