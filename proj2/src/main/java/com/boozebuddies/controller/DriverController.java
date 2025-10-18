package com.boozebuddies.controller;

import com.boozebuddies.dto.ApiResponse;
import com.boozebuddies.dto.DriverDTO;
import com.boozebuddies.entity.Driver;
import com.boozebuddies.model.CertificationStatus;
import com.boozebuddies.service.DriverService;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/drivers")
public class DriverController {

  @Autowired private DriverService driverService;

  /** Register a new driver */
  @PostMapping("/register")
  public ResponseEntity<ApiResponse<DriverDTO>> registerDriver(@RequestBody DriverDTO driverDTO) {
    try {
      Driver driver = convertToEntity(driverDTO);
      Driver registeredDriver = driverService.registerDriver(driver);
      DriverDTO responseDTO = convertToDTO(registeredDriver);
      return ResponseEntity.ok(ApiResponse.success(responseDTO, "Driver registered successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to register driver: " + e.getMessage()));
    }
  }

  /** Update driver certification status */
  @PutMapping("/{driverId}/certification")
  public ResponseEntity<ApiResponse<DriverDTO>> updateCertificationStatus(
      @PathVariable Long driverId, @RequestParam CertificationStatus status) {
    try {
      Driver driver = driverService.updateCertificationStatus(driverId, status);
      if (driver == null) {
        return ResponseEntity.notFound().build();
      }
      DriverDTO driverDTO = convertToDTO(driver);
      return ResponseEntity.ok(
          ApiResponse.success(driverDTO, "Certification status updated successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to update certification status: " + e.getMessage()));
    }
  }

  /** Update driver availability */
  @PutMapping("/{driverId}/availability")
  public ResponseEntity<ApiResponse<DriverDTO>> updateAvailability(
      @PathVariable Long driverId, @RequestParam boolean available) {
    try {
      Driver driver = driverService.updateAvailability(driverId, available);
      if (driver == null) {
        return ResponseEntity.notFound().build();
      }
      DriverDTO driverDTO = convertToDTO(driver);
      return ResponseEntity.ok(ApiResponse.success(driverDTO, "Availability updated successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to update availability: " + e.getMessage()));
    }
  }

  /** Get all available drivers */
  @GetMapping("/available")
  public ResponseEntity<ApiResponse<List<DriverDTO>>> getAvailableDrivers() {
    try {
      List<Driver> availableDrivers = driverService.getAvailableDrivers();
      List<DriverDTO> driverDTOs =
          availableDrivers.stream().map(this::convertToDTO).collect(Collectors.toList());
      return ResponseEntity.ok(
          ApiResponse.success(driverDTOs, "Available drivers retrieved successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to retrieve available drivers: " + e.getMessage()));
    }
  }

  /** Get driver by ID */
  @GetMapping("/{driverId}")
  public ResponseEntity<ApiResponse<DriverDTO>> getDriverById(@PathVariable Long driverId) {
    try {
      Driver driver = driverService.getDriverById(driverId);
      if (driver == null) {
        return ResponseEntity.notFound().build();
      }
      DriverDTO driverDTO = convertToDTO(driver);
      return ResponseEntity.ok(ApiResponse.success(driverDTO, "Driver retrieved successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to retrieve driver: " + e.getMessage()));
    }
  }

  /** Get all drivers */
  @GetMapping
  public ResponseEntity<ApiResponse<List<DriverDTO>>> getAllDrivers() {
    try {
      List<Driver> allDrivers = driverService.getAllDrivers();
      List<DriverDTO> driverDTOs =
          allDrivers.stream().map(this::convertToDTO).collect(Collectors.toList());
      return ResponseEntity.ok(
          ApiResponse.success(driverDTOs, "All drivers retrieved successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to retrieve drivers: " + e.getMessage()));
    }
  }

  /** Convert DriverDTO to Driver entity */
  private Driver convertToEntity(DriverDTO driverDTO) {
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

  /** Convert Driver entity to DriverDTO */
  private DriverDTO convertToDTO(Driver driver) {
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
}
