package com.boozebuddies.service.implementation;

import com.boozebuddies.entity.Driver;
import com.boozebuddies.model.CertificationStatus;
import com.boozebuddies.service.DriverService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class DriverServiceImpl implements DriverService {

  private final List<Driver> drivers = new ArrayList<>();
  private long nextDriverId = 1;

  /** Registers a new driver in the system. */
  @Override
  public Driver registerDriver(Driver driver) {
    driver.setDriverId(nextDriverId++);
    driver.setCertificationStatus(CertificationStatus.PENDING);
    driver.setAvailable(false);
    drivers.add(driver);
    return driver;
  }

  /** Updates the certification status of a driver. */
  @Override
  public Driver updateCertificationStatus(Long driverId, CertificationStatus status) {
    Driver driver = getDriverById(driverId);
    if (driver != null) {
      driver.setCertificationStatus(status);
    }
    return driver;
  }

  /** Marks a driver as available or unavailable for deliveries. */
  @Override
  public Driver updateAvailability(Long driverId, boolean available) {
    Driver driver = getDriverById(driverId);
    if (driver != null) {
      driver.setAvailable(available);
    }
    return driver;
  }

  /** Retrieves all currently available drivers for deliveries. */
  @Override
  public List<Driver> getAvailableDrivers() {
    return drivers.stream().filter(Driver::isAvailable).collect(Collectors.toList());
  }

  /** Retrieves a driver by their unique ID. */
  @Override
  public Driver getDriverById(Long driverId) {
    Optional<Driver> driverOpt =
        drivers.stream().filter(d -> d.getDriverId().equals(driverId)).findFirst();
    return driverOpt.orElse(null);
  }

  /** Retrieves all registered drivers in the system. */
  @Override
  public List<Driver> getAllDrivers() {
    return new ArrayList<>(drivers);
  }
}
