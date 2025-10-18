package com.boozebuddies.mapper;

import com.boozebuddies.dto.DriverDTO;
import com.boozebuddies.entity.Driver;
import org.springframework.stereotype.Component;

@Component
public class DriverMapper {

  public DriverDTO toDTO(Driver driver) {
    if (driver == null) return null;

    return DriverDTO.builder()
        .id(driver.getDriverId())
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
        .certificationStatus(driver.getCertificationStatus())
        .createdAt(driver.getCreatedAt())
        .updatedAt(driver.getUpdatedAt())
        .build();
  }

  public Driver toEntity(DriverDTO driverDTO) {
    if (driverDTO == null) return null;

    return Driver.builder()
        .name(driverDTO.getName())
        .email(driverDTO.getEmail())
        .phone(driverDTO.getPhone())
        .vehicleType(driverDTO.getVehicleType())
        .licensePlate(driverDTO.getLicensePlate())
        .currentLatitude(driverDTO.getCurrentLatitude())
        .currentLongitude(driverDTO.getCurrentLongitude())
        .build();
  }
}
