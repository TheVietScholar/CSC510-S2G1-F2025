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

@Service
@RequiredArgsConstructor
public class DriverServiceImpl implements DriverService {

  private final DriverRepository driverRepository;

  /** Registers a new driver in the system. */
  @Override
  public Driver registerDriver(Driver driver) {
    if (driver == null) {
      throw new IllegalArgumentException("Driver cannot be null");
    }
    driver.setCertificationStatus(CertificationStatus.PENDING);
    driver.setAvailable(false);
    return driverRepository.save(driver);
  }

  /** Updates the certification status of a driver. */
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

  @Transactional
  public Driver updateDriver(Driver driver) {
    return driverRepository.save(driver);
  }

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

  @Override
  public Driver getDriverProfile(User user) {
    return driverRepository
        .findById(user.getId())
        .orElseThrow(() -> new IllegalArgumentException("Driver not found"));
  }

  @Override
  public List<Driver> getNearbyAvailableDrivers(
      Double latitude, Double longitude, Double radiusMeters) {
    return driverRepository.findNearbyAvailableDrivers(latitude, longitude, radiusMeters);
  }
}
