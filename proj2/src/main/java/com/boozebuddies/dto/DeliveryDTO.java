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

  // Delivery location info
  private String deliveryAddress;
  private Double deliveryLatitude;
  private Double deliveryLongitude;

  // Delivery lifecycle timestamps
  private LocalDateTime pickupTime;
  private LocalDateTime deliveredTime;
  private LocalDateTime estimatedDeliveryTime;

  // Driver info (for customer to see)
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private String driverName;
  private String driverPhone;

  // Age verification (critical for alcohol delivery)
  private Boolean ageVerified;
  private String idType;
  private String idNumber; // Last 4 digits only
  private LocalDateTime ageVerifiedAt;

  // Real-time tracking
  private Double currentLatitude;
  private Double currentLongitude;
  private LocalDateTime lastLocationUpdate;

  // Cancellation
  private String cancellationReason;

  // Optional: tracking URL (if you implement deep linking later)
  private String trackingUrl;
}
