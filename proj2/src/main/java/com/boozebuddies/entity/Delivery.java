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
  @JoinColumn(name = "order_id", nullable = false)
  private Order order;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "driver_id")
  private Driver driver;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private DeliveryStatus status;

  @Column(name = "delivery_address", nullable = false)
  private String deliveryAddress;

  @Column(name = "delivery_latitude")
  private Double deliveryLatitude;

  @Column(name = "delivery_longitude")
  private Double deliveryLongitude;

  @Column(name = "cancellation_reason")
  @Builder.Default
  private String cancellationReason = "";

  @Column(name = "pickup_time")
  private LocalDateTime pickupTime;

  @Column(name = "delivered_time")
  private LocalDateTime deliveredTime;

  @Column(name = "estimated_delivery_time")
  private LocalDateTime estimatedDeliveryTime;

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
