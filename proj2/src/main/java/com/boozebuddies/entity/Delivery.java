package com.boozebuddies.entity;

import com.boozebuddies.model.DeliveryStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

/** Entity representing a delivery for an order. */
@Entity
@Table(name = "deliveries")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Delivery {
  /** The unique delivery ID */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** The order being delivered */
  @OneToOne
  @JoinColumn(name = "order_id", unique = true, nullable = false)
  private Order order;

  /** The driver assigned to this delivery */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "driver_id")
  private Driver driver;

  /** The current delivery status */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private DeliveryStatus status;

  /** The delivery address */
  @Column(name = "delivery_address", nullable = false)
  private String deliveryAddress;

  // Destination coordinates (where order should be delivered)
  /** The latitude coordinate of the delivery destination */
  @Column(name = "delivery_latitude")
  private Double deliveryLatitude;

  /** The longitude coordinate of the delivery destination */
  @Column(name = "delivery_longitude")
  private Double deliveryLongitude;

  // Delivery lifecycle timestamps
  /** When the order was picked up */
  @Column(name = "pickup_time")
  private LocalDateTime pickupTime;

  /** When the order was delivered */
  @Column(name = "delivered_time")
  private LocalDateTime deliveredTime;

  /** The estimated time of delivery */
  @Column(name = "estimated_delivery_time")
  private LocalDateTime estimatedDeliveryTime;

  // ==================== AGE VERIFICATION (CRITICAL FOR ALCOHOL DELIVERY) ====================

  /** Whether age has been verified for alcohol delivery */
  @Builder.Default
  @Column(name = "age_verified")
  private Boolean ageVerified = false;

  /** The type of ID used for age verification */
  @Column(name = "id_type")
  private String idType; // e.g., "DRIVER_LICENSE", "PASSPORT", "STATE_ID"

  /** The last 4 digits of the ID number used for verification */
  @Column(name = "id_number")
  private String idNumber; // Last 4 digits only! e.g., "1234"

  /** When age verification occurred */
  @Column(name = "age_verified_at")
  private LocalDateTime ageVerifiedAt;

  // ==================== REAL-TIME TRACKING ====================

  // Current driver location (updates as driver moves)
  /** The current latitude of the driver */
  @Column(name = "current_latitude")
  private Double currentLatitude;

  /** The current longitude of the driver */
  @Column(name = "current_longitude")
  private Double currentLongitude;

  /** When the driver's location was last updated */
  @Column(name = "last_location_update")
  private LocalDateTime lastLocationUpdate;

  // ==================== CANCELLATION ====================

  /** The reason for cancellation, if applicable */
  @Builder.Default
  @Column(name = "cancellation_reason")
  private String cancellationReason = "";

  // ==================== AUDIT TIMESTAMPS ====================

  /** When the delivery was created */
  @Builder.Default
  @Column(name = "created_at")
  private LocalDateTime createdAt = LocalDateTime.now();

  /** When the delivery was last updated */
  @Builder.Default
  @Column(name = "updated_at")
  private LocalDateTime updatedAt = LocalDateTime.now();

  /** Updates the updatedAt timestamp before persisting changes. */
  @PreUpdate
  public void preUpdate() {
    this.updatedAt = LocalDateTime.now();
  }
}
