package com.boozebuddies.service.implementation;

import com.boozebuddies.entity.Driver;
import com.boozebuddies.entity.User;
import com.boozebuddies.exception.DriverNotFoundException;
import com.boozebuddies.model.CertificationStatus;
import com.boozebuddies.repository.DriverRepository;
import com.boozebuddies.service.DriverService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the {@link DriverService} interface.
 *
 * <p>This service manages driver registration, availability, certification status, and location
 * updates. It acts as the main business logic layer for driver-related operations in the
 * BoozeBuddies system.
 */
@Service
@RequiredArgsConstructor
public class DriverServiceImpl implements DriverService {

  private final DriverRepository driverRepository;

  /**
   * Registers a new driver in the system with default values.
   *
   * <p>New drivers are marked as unavailable and their certification status is set to {@code
   * PENDING} until verified by an administrator.
   *
   * @param driver the driver entity to register
   * @return the saved {@link Driver} entity
   * @throws IllegalArgumentException if the driver is null
   */
  @Override
  public Driver registerDriver(Driver driver) {
    if (driver == null) {
      throw new IllegalArgumentException("Driver cannot be null");
    }
    driver.setCertificationStatus(CertificationStatus.PENDING);
    driver.setAvailable(false);
    return driverRepository.save(driver);
  }

  /**
   * Updates the certification status of a specific driver.
   *
   * @param driverId the ID of the driver to update
   * @param status the new {@link CertificationStatus} value
   * @return the updated {@link Driver} entity
   * @throws DriverNotFoundException if no driver is found with the provided ID
   */
  @Override
  public Driver updateCertificationStatus(Long driverId, CertificationStatus status) {
    Optional<Driver> driverOpt = driverRepository.findById(driverId);
    if (driverOpt.isEmpty()) {
      throw new DriverNotFoundException("Driver not found with ID: " + driverId);
    }
    Driver driver = driverOpt.get();
    driver.setCertificationStatus(status);
    return driverRepository.save(driver);
  }

  /**
   * Updates the availability status of a driver for delivery assignments.
   *
   * @param driverId the ID of the driver to update
   * @param available {@code true} to mark as available; {@code false} otherwise
   * @return the updated {@link Driver}, or {@code null} if no driver is found
   */
  @Override
  public Driver updateAvailability(Long driverId, boolean available) {
    Optional<Driver> driverOpt = driverRepository.findById(driverId);
    if (driverOpt.isEmpty()) {
      return null;
    }
    Driver driver = driverOpt.get();
    driver.setAvailable(available);
    return driverRepository.save(driver);
  }

  /**
   * Retrieves all drivers who are currently available for deliveries.
   *
   * @return a list of available {@link Driver} entities
   */
  @Override
  public List<Driver> getAvailableDrivers() {
    return driverRepository.findByIsAvailable(true);
  }

  /**
   * Retrieves a driver by their unique identifier.
   *
   * @param driverId the ID of the driver to retrieve
   * @return the matching {@link Driver} entity, or {@code null} if not found
   */
  @Override
  public Optional<Driver> getDriverById(Long driverId) {
    return driverRepository.findById(driverId);
  }

  /** Retrieves a driver by their associated user ID. */
  @Override
  public Optional<Driver> getDriverByUserId(Long userId) {
    return driverRepository.findByUserId(userId);
  }

  /**
   * Retrieves all registered drivers in the system.
   *
   * @return a list of all {@link Driver} entities
   */
  @Override
  public List<Driver> getAllDrivers() {
    return driverRepository.findAll();
  }

  /**
   * Updates a driver's information in the database.
   *
   * <p>This method is transactional to ensure data consistency.
   *
   * @param driver the {@link Driver} entity with updated data
   * @return the updated {@link Driver} record
   */
  @Transactional
  public Driver updateDriver(Driver driver) {
    return driverRepository.save(driver);
  }

  /**
   * Updates a driver's current geographic location.
   *
   * <p>The driver's {@code latitude}, {@code longitude}, and {@code updatedAt} timestamp are
   * modified and saved in a transactional operation.
   *
   * @param userId the ID of the driver (same as the associated {@link User} ID)
   * @param latitude the current latitude of the driver
   * @param longitude the current longitude of the driver
   * @return the updated {@link Driver} entity
   * @throws IllegalArgumentException if the driver is not found
   */
  @Transactional
  public Driver updateDriverLocation(Long userId, Double latitude, Double longitude) {
    Driver driver =
        driverRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Driver not found"));
    driver.setCurrentLatitude(latitude);
    driver.setCurrentLongitude(longitude);
    driver.setUpdatedAt(LocalDateTime.now());
    return driverRepository.save(driver);
  }

  /**
   * Retrieves a driver's profile based on their associated {@link User} account.
   *
   * @param user the user linked to the driver profile
   * @return the corresponding {@link Driver} entity
   * @throws IllegalArgumentException if the driver is not found
   */
  @Override
  public Driver getDriverProfile(User user) {
    return driverRepository
        .findById(user.getId())
        .orElseThrow(() -> new IllegalArgumentException("Driver not found"));
  }

  /**
   * Retrieves all available drivers within a certain distance of a given location.
   *
   * @param latitude the latitude of the search origin
   * @param longitude the longitude of the search origin
   * @param radiusMeters the radius (in meters) within which to find available drivers
   * @return a list of nearby available {@link Driver} entities
   */
  @Override
  public List<Driver> getNearbyAvailableDrivers(
      Double latitude, Double longitude, Double radiusMeters) {
    return driverRepository.findNearbyAvailableDrivers(latitude, longitude, radiusMeters);
  }
}
