package com.boozebuddies.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemRequest {
    private Long productId;
    private Integer quantity;
    private BigDecimal unitPrice;
}