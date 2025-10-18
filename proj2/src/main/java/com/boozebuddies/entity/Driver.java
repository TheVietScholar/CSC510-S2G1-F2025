package com.boozebuddies.entity;

import com.boozebuddies.model.CertificationStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Entity
@Table(name = "drivers")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Driver {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false, unique = true)
  private String email;

  private String phone;

  @Column(name = "vehicle_type")
  private String vehicleType;

  @Column(name = "license_plate")
  private String licensePlate;

  @Builder.Default
  @Column(name = "is_available")
  private boolean isAvailable = true;

  @Column(name = "current_latitude")
  private Double currentLatitude;

  @Column(name = "current_longitude")
  private Double currentLongitude;

  @Builder.Default private Double rating = 0.0;

  @Builder.Default
  @Column(name = "total_deliveries")
  private Integer totalDeliveries = 0;

  @Builder.Default
  @Column(name = "certification_status")
  private CertificationStatus certificationStatus = CertificationStatus.PENDING;

  @Embedded private Certification certification;

  @Builder.Default
  @Column(name = "created_at")
  private LocalDateTime createdAt = LocalDateTime.now();

  @Builder.Default
  @Column(name = "updated_at")
  private LocalDateTime updatedAt = LocalDateTime.now();

  @OneToMany(mappedBy = "driver", cascade = CascadeType.ALL)
  @Builder.Default
  private List<Delivery> deliveries = new ArrayList<>();

  @OneToMany(mappedBy = "driver", cascade = CascadeType.ALL)
  @Builder.Default
  private List<Rating> ratings = new ArrayList<>();

  @PreUpdate
  public void preUpdate() {
    this.updatedAt = LocalDateTime.now();
  }
}
