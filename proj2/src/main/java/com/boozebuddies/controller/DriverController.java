package com.boozebuddies.controller;

import com.boozebuddies.dto.ApiResponse;
import com.boozebuddies.dto.DriverDTO;
import com.boozebuddies.entity.Driver;
import com.boozebuddies.entity.User;
import com.boozebuddies.exception.DriverNotFoundException;
import com.boozebuddies.mapper.DriverMapper;
import com.boozebuddies.model.CertificationStatus;
import com.boozebuddies.security.annotation.RoleAnnotations.*;
import com.boozebuddies.service.DriverService;
import com.boozebuddies.service.PermissionService;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/** REST controller for managing drivers and driver operations. */
@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
public class DriverController {

  private final DriverService driverService;
  private final DriverMapper driverMapper;
  private final PermissionService permissionService;

  // ==================== ADMIN ENDPOINTS ====================

  /**
   * Registers a new driver. Admin only.
   *
   * @param driverDTO the driver data
   * @return the registered driver
   */
  @PostMapping("/register")
  @IsAdmin
  public ResponseEntity<ApiResponse<DriverDTO>> registerDriver(@RequestBody DriverDTO driverDTO) {
    try {
      Driver driver = driverMapper.toEntity(driverDTO);
      Driver registeredDriver = driverService.registerDriver(driver);
      DriverDTO responseDTO = driverMapper.toDTO(registeredDriver);

      return ResponseEntity.status(HttpStatus.CREATED)
          .body(ApiResponse.success(responseDTO, "Driver registered successfully"));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.error("Driver registration failed: " + e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.error("Driver registration failed: " + e.getMessage()));
    }
  }

  /**
   * Updates a driver's certification status. Admin only.
   *
   * @param driverId the driver ID
   * @param status the new certification status
   * @return the updated driver
   */
  @PutMapping("/{driverId}/certification")
  @IsAdmin
  public ResponseEntity<ApiResponse<DriverDTO>> updateCertificationStatus(
      @PathVariable Long driverId, @RequestParam("status") CertificationStatus status) {
    try {
      Driver updatedDriver = driverService.updateCertificationStatus(driverId, status);
      DriverDTO driverDTO = driverMapper.toDTO(updatedDriver);
      return ResponseEntity.ok(
          ApiResponse.success(driverDTO, "Certification status updated successfully"));
    } catch (DriverNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(ApiResponse.error("Driver not found with ID: " + driverId));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.error("Failed to update certification status: " + e.getMessage()));
    }
  }

  /**
   * Retrieves all available drivers. Admin only.
   *
   * @return a list of available drivers
   */
  @GetMapping("/available")
  @IsAdmin
  public ResponseEntity<ApiResponse<List<DriverDTO>>> getAvailableDrivers() {
    try {
      List<DriverDTO> drivers =
          driverService.getAvailableDrivers().stream()
              .map(driverMapper::toDTO)
              .collect(Collectors.toList());
      return ResponseEntity.ok(
          ApiResponse.success(drivers, "Available drivers retrieved successfully"));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.error("Failed to retrieve available drivers: " + e.getMessage()));
    }
  }

  /**
   * Retrieves a driver by ID. Admin only.
   *
   * @param driverId the driver ID
   * @return the driver with the specified ID
   */
  @GetMapping("/{driverId}")
  @IsAdmin
  public ResponseEntity<ApiResponse<DriverDTO>> getDriverById(@PathVariable Long driverId) {
    try {
      Driver driver = driverService.getDriverById(driverId).get();
      DriverDTO driverDTO = driverMapper.toDTO(driver);
      return ResponseEntity.ok(ApiResponse.success(driverDTO, "Driver retrieved successfully"));
    } catch (DriverNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(ApiResponse.error("Driver not found with ID: " + driverId));
    } catch (NoSuchElementException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(ApiResponse.error("Driver not found with ID: " + driverId));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.error("Failed to retrieve driver: " + e.getMessage()));
    }
  }

  /**
   * Retrieves all drivers. Admin only.
   *
   * @return a list of all drivers
   */
  @GetMapping
  @IsAdmin
  public ResponseEntity<ApiResponse<List<DriverDTO>>> getAllDrivers() {
    try {
      List<DriverDTO> drivers =
          driverService.getAllDrivers().stream()
              .map(driverMapper::toDTO)
              .collect(Collectors.toList());
      return ResponseEntity.ok(ApiResponse.success(drivers, "All drivers retrieved successfully"));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.error("Failed to retrieve drivers: " + e.getMessage()));
    }
  }

  // ==================== DRIVER ENDPOINTS ====================

  /**
   * Retrieves the authenticated driver's profile.
   *
   * @param authentication the authentication object
   * @return the driver's profile
   */
  @GetMapping("/my-profile")
  @IsSelfOrAdmin
  public ResponseEntity<ApiResponse<DriverDTO>> getMyProfile(Authentication authentication) {
    User user = permissionService.getAuthenticatedUser(authentication);
    Driver driver = driverService.getDriverProfile(user);
    DriverDTO driverDTO = driverMapper.toDTO(driver);
    return ResponseEntity.ok(ApiResponse.success(driverDTO, "Your profile retrieved successfully"));
  }

  /**
   * Updates the authenticated driver's availability status.
   *
   * @param available whether the driver is available for deliveries
   * @param authentication the authentication object
   * @return the updated driver profile
   */
  @PutMapping("/my-profile/availability")
  @IsSelfOrAdmin
  public ResponseEntity<ApiResponse<DriverDTO>> updateMyAvailability(
      @RequestParam("available") boolean available, Authentication authentication) {
    try {
      User user = permissionService.getAuthenticatedUser(authentication);
      Driver driver = driverService.updateAvailability(user.getDriver().getId(), available);
      DriverDTO driverDTO = driverMapper.toDTO(driver);

      String message =
          available
              ? "You are now available for deliveries"
              : "You are now unavailable for deliveries";
      return ResponseEntity.ok(ApiResponse.success(driverDTO, message));
    } catch (DriverNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(ApiResponse.error("Driver not found for the authenticated user"));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.error("Failed to update availability: " + e.getMessage()));
    }
  }

  /**
   * Updates the authenticated driver's current location.
   *
   * @param latitude the current latitude
   * @param longitude the current longitude
   * @param authentication the authentication object
   * @return the updated driver profile
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

      Driver updatedDriver =
          driverService.updateDriverLocation(user.getDriver().getId(), latitude, longitude);
      DriverDTO driverDTO = driverMapper.toDTO(updatedDriver);

      return ResponseEntity.ok(ApiResponse.success(driverDTO, "Location updated successfully"));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.error("Failed to update location: " + e.getMessage()));
    }
  }
}
