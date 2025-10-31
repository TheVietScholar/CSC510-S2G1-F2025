package com.boozebuddies.entity;

import com.boozebuddies.model.DeliveryStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "deliveries")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Delivery {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne
  @JoinColumn(name = "order_id", unique = true, nullable = false)
  private Order order;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "driver_id")
  private Driver driver;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private DeliveryStatus status;

  @Column(name = "delivery_address", nullable = false)
  private String deliveryAddress;

  // Destination coordinates (where order should be delivered)
  @Column(name = "delivery_latitude")
  private Double deliveryLatitude;

  @Column(name = "delivery_longitude")
  private Double deliveryLongitude;

  // Delivery lifecycle timestamps
  @Column(name = "pickup_time")
  private LocalDateTime pickupTime;

  @Column(name = "delivered_time")
  private LocalDateTime deliveredTime;

  @Column(name = "estimated_delivery_time")
  private LocalDateTime estimatedDeliveryTime;

  // ==================== AGE VERIFICATION (CRITICAL FOR ALCOHOL DELIVERY) ====================
  
  @Builder.Default
  @Column(name = "age_verified")
  private Boolean ageVerified = false;

  @Column(name = "id_type")
  private String idType;  // e.g., "DRIVER_LICENSE", "PASSPORT", "STATE_ID"

  @Column(name = "id_number")
  private String idNumber;  // Last 4 digits only! e.g., "1234"

  @Column(name = "age_verified_at")
  private LocalDateTime ageVerifiedAt;

  // ==================== REAL-TIME TRACKING ====================
  
  // Current driver location (updates as driver moves)
  @Column(name = "current_latitude")
  private Double currentLatitude;

  @Column(name = "current_longitude")
  private Double currentLongitude;

  @Column(name = "last_location_update")
  private LocalDateTime lastLocationUpdate;

  // ==================== CANCELLATION ====================
  
  @Builder.Default
  @Column(name = "cancellation_reason")
  private String cancellationReason = "";

  // ==================== AUDIT TIMESTAMPS ====================
  
  @Builder.Default
  @Column(name = "created_at")
  private LocalDateTime createdAt = LocalDateTime.now();

  @Builder.Default
  @Column(name = "updated_at")
  private LocalDateTime updatedAt = LocalDateTime.now();

  @PreUpdate
  public void preUpdate() {
    this.updatedAt = LocalDateTime.now();
  }
}