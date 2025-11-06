package com.boozebuddies.dto;

import com.boozebuddies.entity.Certification;
import java.time.LocalDateTime;
import lombok.*;

/** Data transfer object for driver information. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverDTO {
  /** The unique driver ID */
  private Long id;

  /** The driver's name */
  private String name;

  /** The driver's email address */
  private String email;

  /** The driver's phone number */
  private String phone;

  /** The type of vehicle the driver uses */
  private String vehicleType;

  /** The driver's license plate number */
  private String licensePlate;

  /** Whether the driver is currently available for deliveries */
  private boolean isAvailable;

  /** The status of the driver's certification */
  private String certificationStatus;

  /** The driver's certification details */
  private Certification certification;

  /** The driver's current latitude coordinate */
  private Double currentLatitude;

  /** The driver's current longitude coordinate */
  private Double currentLongitude;

  /** The driver's average rating */
  private Double rating;

  /** The total number of deliveries completed by the driver */
  private Integer totalDeliveries;

  /** When the driver record was created */
  private LocalDateTime createdAt;

  /** When the driver record was last updated */
  private LocalDateTime updatedAt;
}
