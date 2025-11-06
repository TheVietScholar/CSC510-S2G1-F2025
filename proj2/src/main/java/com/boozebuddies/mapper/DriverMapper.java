package com.boozebuddies.mapper;

import com.boozebuddies.dto.DriverDTO;
import com.boozebuddies.entity.Driver;
import com.boozebuddies.model.CertificationStatus;
import org.springframework.stereotype.Component;

/** Mapper for converting between Driver entities and DriverDTO objects. */
@Component
public class DriverMapper {

  /**
   * Converts a Driver entity to a DriverDTO.
   *
   * @param driver the driver entity to convert
   * @return the DriverDTO, or null if the input is null
   */
  public DriverDTO toDTO(Driver driver) {
    if (driver == null) return null;

    return DriverDTO.builder()
        .id(driver.getId())
        .name(driver.getName())
        .email(driver.getEmail())
        .phone(driver.getPhone())
        .vehicleType(driver.getVehicleType())
        .licensePlate(driver.getLicensePlate())
        .isAvailable(driver.isAvailable())
        .currentLatitude(driver.getCurrentLatitude())
        .currentLongitude(driver.getCurrentLongitude())
        .rating(driver.getRating())
        .totalDeliveries(driver.getTotalDeliveries())
        .certificationStatus(driver.getCertificationStatus().name())
        .certification(driver.getCertification())
        .createdAt(driver.getCreatedAt())
        .updatedAt(driver.getUpdatedAt())
        .build();
  }

  /**
   * Converts a DriverDTO to a Driver entity.
   *
   * @param driverDTO the DriverDTO to convert
   * @return the Driver entity, or null if the input is null
   */
  public Driver toEntity(DriverDTO driverDTO) {
    if (driverDTO == null) return null;

    return Driver.builder()
        .id(driverDTO.getId())
        .name(driverDTO.getName())
        .email(driverDTO.getEmail())
        .phone(driverDTO.getPhone())
        .vehicleType(driverDTO.getVehicleType())
        .licensePlate(driverDTO.getLicensePlate())
        .currentLatitude(driverDTO.getCurrentLatitude())
        .currentLongitude(driverDTO.getCurrentLongitude())
        .isAvailable(driverDTO.isAvailable())
        .rating(driverDTO.getRating())
        .totalDeliveries(driverDTO.getTotalDeliveries())
        .certificationStatus(CertificationStatus.valueOf(driverDTO.getCertificationStatus()))
        .certification(driverDTO.getCertification())
        .createdAt(driverDTO.getCreatedAt())
        .updatedAt(driverDTO.getUpdatedAt())
        .build();
  }
}
