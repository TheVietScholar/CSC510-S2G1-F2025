package com.boozebuddies.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.*;

/** DTO for orders displayed to drivers, includes distance and ETA calculations. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverOrderDTO {
  private Long id;
  private Long userId;
  private Long merchantId;
  private String merchantName; // Merchant name for display
  private String customerName; // Customer name for display
  private String deliveryAddress;
  private BigDecimal totalAmount;
  private String status;
  private List<OrderItemDTO> items;
  private Double distanceKm; // Distance from driver to merchant in kilometers
  private Integer etaMin; // Estimated time of arrival in minutes
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private LocalDateTime estimatedDeliveryTime;
}
