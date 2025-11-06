package com.boozebuddies.dto;

import java.time.LocalDateTime;
import lombok.*;

/** Data transfer object for delivery information. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryDTO {
  /** The unique delivery ID */
  private Long id;

  /** The ID of the associated order */
  private Long orderId;

  /** The ID of the assigned driver */
  private Long driverId;

  /** The current delivery status */
  private String status;

  // Delivery location info
  /** The delivery address */
  private String deliveryAddress;

  /** The latitude coordinate of the delivery address */
  private Double deliveryLatitude;

  /** The longitude coordinate of the delivery address */
  private Double deliveryLongitude;

  // Delivery lifecycle timestamps
  /** When the order was picked up */
  private LocalDateTime pickupTime;

  /** When the order was delivered */
  private LocalDateTime deliveredTime;

  /** The estimated time of delivery */
  private LocalDateTime estimatedDeliveryTime;

  // Driver info (for customer to see)
  /** When the delivery was created */
  private LocalDateTime createdAt;

  /** When the delivery was last updated */
  private LocalDateTime updatedAt;

  /** The name of the driver */
  private String driverName;

  /** The phone number of the driver */
  private String driverPhone;

  // Age verification (critical for alcohol delivery)
  /** Whether age has been verified */
  private Boolean ageVerified;

  /** The type of ID used for verification */
  private String idType;

  /** The last 4 digits of the ID number */
  private String idNumber;

  /** When age verification occurred */
  private LocalDateTime ageVerifiedAt;

  // Real-time tracking
  /** The current latitude of the driver */
  private Double currentLatitude;

  /** The current longitude of the driver */
  private Double currentLongitude;

  /** When the location was last updated */
  private LocalDateTime lastLocationUpdate;

  // Cancellation
  /** The reason for cancellation, if applicable */
  private String cancellationReason;

  // Optional: tracking URL (if you implement deep linking later)
  /** URL for tracking the delivery */
  private String trackingUrl;
}
