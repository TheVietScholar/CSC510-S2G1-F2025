package com.boozebuddies.entity;

import com.boozebuddies.model.CertificationStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

/** Entity representing a driver who delivers orders. */
@Entity
@Table(name = "drivers")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Driver {
  /** The unique driver ID */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** Link to the User account for this driver. The User must have DRIVER role. */
  @OneToOne
  @JoinColumn(name = "user_id", nullable = false, unique = true)
  private User user;

  /** The driver's name */
  @Column(nullable = false)
  private String name;

  /** The driver's email address */
  @Column(nullable = false, unique = true)
  private String email;

  /** The driver's phone number */
  private String phone;

  /** The type of vehicle the driver uses */
  @Column(name = "vehicle_type")
  private String vehicleType;

  /** The driver's license plate number */
  @Column(name = "license_plate")
  private String licensePlate;

  /** Whether the driver is currently available for deliveries */
  @Builder.Default
  @Column(name = "is_available")
  private boolean isAvailable = true;

  /** The driver's current latitude coordinate */
  @Column(name = "current_latitude")
  private Double currentLatitude;

  /** The driver's current longitude coordinate */
  @Column(name = "current_longitude")
  private Double currentLongitude;

  /** The driver's average rating */
  @Builder.Default private Double rating = 0.0;

  /** The total number of deliveries completed by the driver */
  @Builder.Default
  @Column(name = "total_deliveries")
  private Integer totalDeliveries = 0;

  /**
   * Certification status for handling alcohol deliveries. Must be APPROVED before driver can accept
   * orders.
   */
  @Enumerated(EnumType.STRING)
  @Builder.Default
  @Column(name = "certification_status")
  private CertificationStatus certificationStatus = CertificationStatus.PENDING;

  /** The driver's certification details */
  @Embedded private Certification certification;

  /** When the driver record was created */
  @Builder.Default
  @Column(name = "created_at")
  private LocalDateTime createdAt = LocalDateTime.now();

  /** When the driver record was last updated */
  @Builder.Default
  @Column(name = "updated_at")
  private LocalDateTime updatedAt = LocalDateTime.now();

  /** The list of deliveries assigned to this driver */
  @OneToMany(mappedBy = "driver", cascade = CascadeType.ALL)
  @Builder.Default
  private List<Delivery> deliveries = new ArrayList<>();

  /** The list of ratings received by this driver */
  @OneToMany(mappedBy = "driver", cascade = CascadeType.ALL)
  @Builder.Default
  private List<Rating> ratings = new ArrayList<>();

  /** Updates the updatedAt timestamp before persisting changes. */
  @PreUpdate
  public void preUpdate() {
    this.updatedAt = LocalDateTime.now();
  }

  /**
   * Check if driver is certified and available to accept deliveries.
   *
   * @return true if driver can accept deliveries, false otherwise
   */
  public boolean canAcceptDeliveries() {
    return isAvailable
        && certificationStatus == CertificationStatus.APPROVED
        && user != null
        && user.isActive();
  }

  /**
   * Check if driver's certification is approved.
   *
   * @return true if certification status is APPROVED, false otherwise
   */
  public boolean isCertified() {
    return certificationStatus == CertificationStatus.APPROVED;
  }

  public Driver orElseThrow(Object object) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'orElseThrow'");
  }
}
