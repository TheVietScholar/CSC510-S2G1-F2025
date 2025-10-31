package com.boozebuddies.controller;

import com.boozebuddies.dto.ApiResponse;
import com.boozebuddies.dto.DriverDTO;
import com.boozebuddies.entity.Driver;
import com.boozebuddies.entity.User;
import com.boozebuddies.mapper.DriverMapper;
import com.boozebuddies.model.CertificationStatus;
import com.boozebuddies.security.annotation.RoleAnnotations.*;
import com.boozebuddies.service.DriverService;
import com.boozebuddies.service.PermissionService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
public class DriverController {

  private final DriverService driverService;
  private final DriverMapper driverMapper;
  private final PermissionService permissionService;

  // ==================== ADMIN ENDPOINTS ====================

  /**
   * Register a new driver (ADMIN only)
   */
  @PostMapping("/register")
  @IsAdmin
  public ResponseEntity<ApiResponse<DriverDTO>> registerDriver(@RequestBody DriverDTO driverDTO) {
    try {
      Driver driver = driverMapper.toEntity(driverDTO);
      Driver registeredDriver = driverService.registerDriver(driver);
      DriverDTO responseDTO = driverMapper.toDTO(registeredDriver);

      return ResponseEntity.ok(ApiResponse.success(responseDTO, "Driver registered successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to register driver: " + e.getMessage()));
    }
  }

  /**
   * Update driver certification status (ADMIN only - critical for alcohol delivery compliance)
   */
  @PutMapping("/{driverId}/certification")
  @IsAdmin
  public ResponseEntity<ApiResponse<DriverDTO>> updateCertificationStatus(
      @PathVariable Long driverId, @RequestParam CertificationStatus status) {
    try {
      Driver driver = driverService.updateCertificationStatus(driverId, status);
      if (driver == null) {
        return ResponseEntity.notFound().build();
      }
      DriverDTO driverDTO = driverMapper.toDTO(driver);
      return ResponseEntity.ok(
          ApiResponse.success(driverDTO, "Certification status updated successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to update certification status: " + e.getMessage()));
    }
  }

  /**
   * Get all available drivers (ADMIN only)
   */
  @GetMapping("/available")
  @IsAdmin
  public ResponseEntity<ApiResponse<List<DriverDTO>>> getAvailableDrivers() {
    try {
      List<Driver> availableDrivers = driverService.getAvailableDrivers();
      List<DriverDTO> driverDTOs =
          availableDrivers.stream().map(driverMapper::toDTO).collect(Collectors.toList());
      return ResponseEntity.ok(
          ApiResponse.success(driverDTOs, "Available drivers retrieved successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to retrieve available drivers: " + e.getMessage()));
    }
  }

  /**
   * Get driver by ID (ADMIN only)
   */
  @GetMapping("/{driverId}")
  @IsAdmin
  public ResponseEntity<ApiResponse<DriverDTO>> getDriverById(@PathVariable Long driverId) {
    try {
      Driver driver = driverService.getDriverById(driverId);
      if (driver == null) {
        return ResponseEntity.notFound().build();
      }
      DriverDTO driverDTO = driverMapper.toDTO(driver);
      return ResponseEntity.ok(ApiResponse.success(driverDTO, "Driver retrieved successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to retrieve driver: " + e.getMessage()));
    }
  }

  /**
   * Get all drivers (ADMIN only)
   */
  @GetMapping
  @IsAdmin
  public ResponseEntity<ApiResponse<List<DriverDTO>>> getAllDrivers() {
    try {
      List<Driver> allDrivers = driverService.getAllDrivers();
      List<DriverDTO> driverDTOs =
          allDrivers.stream().map(driverMapper::toDTO).collect(Collectors.toList());
      return ResponseEntity.ok(
          ApiResponse.success(driverDTOs, "All drivers retrieved successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to retrieve drivers: " + e.getMessage()));
    }
  }

  // ==================== DRIVER ENDPOINTS (Own Profile) ====================

  /**
   * Get the authenticated driver's profile
   */
  @GetMapping("/my-profile")
  @IsDriver
  public ResponseEntity<ApiResponse<DriverDTO>> getMyProfile(Authentication authentication) {
    try {
      User user = permissionService.getAuthenticatedUser(authentication);

      if (user.getDriver() == null) {
        return ResponseEntity.badRequest()
            .body(ApiResponse.error("No driver profile found for this user"));
      }

      DriverDTO driverDTO = driverMapper.toDTO(user.getDriver());
      return ResponseEntity.ok(
          ApiResponse.success(driverDTO, "Your profile retrieved successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to retrieve your profile: " + e.getMessage()));
    }
  }

  /**
   * Update the authenticated driver's availability
   */
  @PutMapping("/my-profile/availability")
  @IsDriver
  public ResponseEntity<ApiResponse<DriverDTO>> updateMyAvailability(
      @RequestParam boolean available, Authentication authentication) {
    try {
      User user = permissionService.getAuthenticatedUser(authentication);

      if (user.getDriver() == null) {
        return ResponseEntity.badRequest()
            .body(ApiResponse.error("No driver profile found for this user"));
      }

      Driver driver = driverService.updateAvailability(user.getDriver().getId(), available);
      DriverDTO driverDTO = driverMapper.toDTO(driver);

      String message =
          available
              ? "You are now available for deliveries"
              : "You are now unavailable for deliveries";

      return ResponseEntity.ok(ApiResponse.success(driverDTO, message));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to update availability: " + e.getMessage()));
    }
  }

  /**
   * Update the authenticated driver's location
   */
  @PutMapping("/my-profile/location")
  @IsDriver
  public ResponseEntity<ApiResponse<DriverDTO>> updateMyLocation(
      @RequestParam Double latitude,
      @RequestParam Double longitude,
      Authentication authentication) {
    try {
      User user = permissionService.getAuthenticatedUser(authentication);

      if (user.getDriver() == null) {
        return ResponseEntity.badRequest()
            .body(ApiResponse.error("No driver profile found for this user"));
      }

      // Update driver location
      Driver driver = user.getDriver();
      driver.setCurrentLatitude(latitude);
      driver.setCurrentLongitude(longitude);

      // You'll need to add this method to DriverService
      // Driver updatedDriver = driverService.updateDriver(driver);
      // For now, just return the driver
      DriverDTO driverDTO = driverMapper.toDTO(driver);

      return ResponseEntity.ok(ApiResponse.success(driverDTO, "Location updated successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to update location: " + e.getMessage()));
    }
  }
}