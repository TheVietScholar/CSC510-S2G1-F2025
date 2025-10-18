package com.boozebuddies.dto;

import java.time.LocalDateTime;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryDTO {
  private Long id;
  private Long orderId;
  private Long driverId;
  private String status;
  private String deliveryAddress;
  private Double deliveryLatitude;
  private Double deliveryLongitude;
  private LocalDateTime pickupTime;
  private LocalDateTime deliveredTime;
  private LocalDateTime estimatedDeliveryTime;
  private String driverName;
  private String driverPhone;
  private String trackingUrl;
}
