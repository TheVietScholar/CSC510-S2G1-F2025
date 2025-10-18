package com.boozebuddies.dto;

import java.time.LocalDateTime;
import lombok.*;

import com.boozebuddies.entity.Certification;
import com.boozebuddies.model.CertificationStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverDTO {
  private Long id;
  private String name;
  private String email;
  private String phone;
  private String vehicleType;
  private String licensePlate;
  private boolean isAvailable;
  private CertificationStatus certificationStatus;
  private Certification certification;
  private Double currentLatitude;
  private Double currentLongitude;
  private Double rating;
  private Integer totalDeliveries;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
