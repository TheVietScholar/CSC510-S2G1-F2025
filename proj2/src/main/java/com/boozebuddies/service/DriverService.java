package com.boozebuddies.service;

import com.boozebuddies.entity.Driver;
import com.boozebuddies.model.CertificationStatus;
import java.util.List;

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
  Driver getDriverById(Long driverId);

  /**
   * Retrieves all registered drivers in the system.
   *
   * @return A list of all drivers.
   */
  List<Driver> getAllDrivers();
}
