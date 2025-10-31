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
import jakarta.validation.Valid;
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

  @PostMapping("/register")
  @IsAdmin
  public ResponseEntity<ApiResponse<DriverDTO>> registerDriver(
      @Valid @RequestBody DriverDTO driverDTO) {
    Driver driver = driverMapper.toEntity(driverDTO);
    Driver registeredDriver = driverService.registerDriver(driver);
    DriverDTO responseDTO = driverMapper.toDTO(registeredDriver);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(responseDTO, "Driver registered successfully"));
  }

  @PutMapping("/{driverId}/certification")
  @IsAdmin
  public ResponseEntity<ApiResponse<DriverDTO>> updateCertificationStatus(
      @PathVariable Long driverId, @RequestParam CertificationStatus status) {

    Driver updatedDriver = driverService.updateCertificationStatus(driverId, status);
    DriverDTO driverDTO = driverMapper.toDTO(updatedDriver);
    return ResponseEntity.ok(
        ApiResponse.success(driverDTO, "Certification status updated successfully"));
  }

  @GetMapping("/available")
  @IsAdmin
  public ResponseEntity<ApiResponse<List<DriverDTO>>> getAvailableDrivers() {
    List<DriverDTO> drivers =
        driverService.getAvailableDrivers().stream()
            .map(driverMapper::toDTO)
            .collect(Collectors.toList());
    return ResponseEntity.ok(
        ApiResponse.success(drivers, "Available drivers retrieved successfully"));
  }

  @GetMapping("/{driverId}")
  @IsAdmin
  public ResponseEntity<ApiResponse<DriverDTO>> getDriverById(@PathVariable Long driverId) {
    Driver driver = driverService.getDriverById(driverId);
    DriverDTO driverDTO = driverMapper.toDTO(driver);
    return ResponseEntity.ok(ApiResponse.success(driverDTO, "Driver retrieved successfully"));
  }

  @GetMapping
  @IsAdmin
  public ResponseEntity<ApiResponse<List<DriverDTO>>> getAllDrivers() {
    List<DriverDTO> drivers =
        driverService.getAllDrivers().stream()
            .map(driverMapper::toDTO)
            .collect(Collectors.toList());
    return ResponseEntity.ok(ApiResponse.success(drivers, "All drivers retrieved successfully"));
  }

  // ==================== DRIVER ENDPOINTS ====================

  @GetMapping("/my-profile")
  @IsDriver
  public ResponseEntity<ApiResponse<DriverDTO>> getMyProfile(Authentication authentication) {
    User user = permissionService.getAuthenticatedUser(authentication);
    Driver driver = driverService.getDriverProfile(user);
    DriverDTO driverDTO = driverMapper.toDTO(driver);
    return ResponseEntity.ok(ApiResponse.success(driverDTO, "Your profile retrieved successfully"));
  }

  @PutMapping("/my-profile/availability")
  @IsDriver
  public ResponseEntity<ApiResponse<DriverDTO>> updateMyAvailability(
      @RequestParam boolean available, Authentication authentication) {

    User user = permissionService.getAuthenticatedUser(authentication);
    Driver driver = driverService.updateAvailability(user.getDriver().getId(), available);
    DriverDTO driverDTO = driverMapper.toDTO(driver);

    String message =
        available
            ? "You are now available for deliveries"
            : "You are now unavailable for deliveries";
    return ResponseEntity.ok(ApiResponse.success(driverDTO, message));
  }

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

  @GetMapping("/nearby")
  @IsUser
  public ResponseEntity<ApiResponse<List<DriverDTO>>> getNearbyDrivers(
      @RequestParam Double latitude,
      @RequestParam Double longitude,
      @RequestParam(defaultValue = "5000") Double radiusMeters) {

    List<DriverDTO> nearbyDrivers =
        driverService.getNearbyAvailableDrivers(latitude, longitude, radiusMeters).stream()
            .map(driverMapper::toDTO)
            .collect(Collectors.toList());

    return ResponseEntity.ok(
        ApiResponse.success(nearbyDrivers, "Nearby drivers retrieved successfully"));
  }
}
