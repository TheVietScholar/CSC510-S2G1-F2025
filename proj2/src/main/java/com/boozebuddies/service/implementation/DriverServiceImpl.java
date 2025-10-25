package com.boozebuddies.service.implementation;

import com.boozebuddies.entity.Driver;
import com.boozebuddies.model.CertificationStatus;
import com.boozebuddies.repository.DriverRepository;
import com.boozebuddies.service.DriverService;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DriverServiceImpl implements DriverService {

  private final DriverRepository driverRepository;

  /** Registers a new driver in the system. */
  @Override
  public Driver registerDriver(Driver driver) {
    driver.setCertificationStatus(CertificationStatus.PENDING);
    driver.setAvailable(false);
    return driverRepository.save(driver);
  }

  /** Updates the certification status of a driver. */
  @Override
  public Driver updateCertificationStatus(Long driverId, CertificationStatus status) {
    Optional<Driver> driverOpt = driverRepository.findById(driverId);
    if (driverOpt.isEmpty()) {
      return null;
    }
    Driver driver = driverOpt.get();
    driver.setCertificationStatus(status);
    return driverRepository.save(driver);
  }

  /** Marks a driver as available or unavailable for deliveries. */
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

  /** Retrieves all currently available drivers for deliveries. */
  @Override
  public List<Driver> getAvailableDrivers() {
    return driverRepository.findByIsAvailable(true);
  }

  /** Retrieves a driver by their unique ID. */
  @Override
  public Driver getDriverById(Long driverId) {
    return driverRepository.findById(driverId).orElse(null);
  }

  /** Retrieves all registered drivers in the system. */
  @Override
  public List<Driver> getAllDrivers() {
    return driverRepository.findAll();
  }
}
