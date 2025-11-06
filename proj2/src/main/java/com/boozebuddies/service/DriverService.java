package com.boozebuddies.service;

import com.boozebuddies.entity.Driver;
import com.boozebuddies.entity.User;
import com.boozebuddies.model.CertificationStatus;
import java.util.List;
import java.util.Optional;

/**
 * Service interface responsible for managing driver-related operations such as registration,
 * certification, availability updates, and location tracking.
 */
public interface DriverService {

  /**
   * Registers a new driver in the system.
   *
   * @param driver The driver to register.
   * @return The registered Driver object with an assigned ID.
   */
  Driver registerDriver(Driver driver);

  /**
   * Updates the certification status of a driver.
   *
   * @param driverId The ID of the driver to update.
   * @param status The new certification status (e.g., PENDING, APPROVED, REVOKED).
   * @return The updated Driver object.
   */
  Driver updateCertificationStatus(Long driverId, CertificationStatus status);

  /**
   * Marks a driver as available or unavailable for deliveries.
   *
   * @param driverId The ID of the driver.
   * @param available True if the driver is available, false otherwise.
   * @return The updated Driver object.
   */
  Driver updateAvailability(Long driverId, boolean available);

  /**
   * Retrieves all currently available drivers for deliveries.
   *
   * @return A list of available drivers.
   */
  List<Driver> getAvailableDrivers();

  /**
   * Retrieves a driver by their unique ID.
   *
   * @param driverId The ID of the driver.
   * @return The corresponding Driver object, or null if not found.
   */
  Optional<Driver> getDriverById(Long driverId);

  /**
   * Retrieves a driver by their associated user ID.
   *
   * @param userId The user ID linked to the driver.
   * @return The corresponding Driver object, or null if not found.
   */
  Optional<Driver> getDriverByUserId(Long userId);

  /**
   * Retrieves all registered drivers in the system.
   *
   * @return A list of all drivers.
   */
  List<Driver> getAllDrivers();

  /**
   * Updates driver information such as name, contact details, or vehicle data.
   *
   * @param driver The driver object containing updated information.
   * @return The updated Driver object.
   */
  Driver updateDriver(Driver driver);

  /**
   * Retrieves the driver profile associated with a given user account.
   *
   * @param user The user whose driver profile is being retrieved.
   * @return The corresponding Driver object, or null if the user is not registered as a driver.
   */
  Driver getDriverProfile(User user);

  /**
   * Retrieves a list of nearby available drivers based on a geographic location.
   *
   * @param latitude The latitude coordinate to search from.
   * @param longitude The longitude coordinate to search from.
   * @param radiusMeters The radius (in meters) within which to search for available drivers.
   * @return A list of nearby drivers who are available for delivery.
   */
  List<Driver> getNearbyAvailableDrivers(Double latitude, Double longitude, Double radiusMeters);

  /**
   * Updates the current geographic location of a driver.
   *
   * @param userId The ID of the user associated with the driver.
   * @param latitude The driver's current latitude.
   * @param longitude The driver's current longitude.
   * @return The updated Driver object with the new location.
   */
  Driver updateDriverLocation(Long userId, Double latitude, Double longitude);
}
