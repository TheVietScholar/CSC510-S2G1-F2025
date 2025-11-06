package com.boozebuddies.repository;

import com.boozebuddies.entity.Driver;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing {@link Driver} entities within the BoozeBuddies system.
 *
 * <p>This interface provides CRUD operations through {@link JpaRepository} and includes custom
 * query methods for retrieving, filtering, and validating driver information.
 *
 * <p>Custom query methods include:
 *
 * <ul>
 *   <li>{@link #findByEmail(String)} - Retrieves a driver by their email address.
 *   <li>{@link #findByPhone(String)} - Retrieves a driver by their phone number.
 *   <li>{@link #findByIsAvailable(boolean)} - Finds drivers based on their availability status.
 *   <li>{@link #findByVehicleType(String)} - Finds drivers by their vehicle type (e.g., car, van,
 *       bike).
 *   <li>{@link #findAvailableCertifiedDrivers()} - Retrieves all available drivers with approved
 *       certification.
 *   <li>{@link #findAvailableDriversByRating()} - Finds all available drivers sorted by rating in
 *       descending order.
 *   <li>{@link #findNearbyAvailableDrivers(Double, Double, Double)} - Finds available drivers
 *       within a specific radius using geolocation.
 *   <li>{@link #findByMinDeliveries(Integer)} - Retrieves drivers who have completed a minimum
 *       number of deliveries.
 *   <li>{@link #existsByEmail(String)} - Checks if a driver exists with the specified email.
 *   <li>{@link #existsByPhone(String)} - Checks if a driver exists with the specified phone number.
 *   <li>{@link #existsByLicensePlate(String)} - Checks if a driver exists with the specified
 *       license plate.
 * </ul>
 *
 * <p>This repository supports driver management, assignment logic, and analytics features across
 * the BoozeBuddies platform.
 */
@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {

  /** Retrieves a driver by their unique email address. */
  Optional<Driver> findByEmail(String email);

  /** Retrieves a driver by their phone number. */
  Optional<Driver> findByPhone(String phone);

  /** Find driver by their UserId */
  Optional<Driver> findByUserId(Long userId);

  /** Finds drivers based on whether they are currently available for delivery. */
  List<Driver> findByIsAvailable(boolean isAvailable);

  /** Finds drivers by their vehicle type (e.g., car, van, bike). */
  List<Driver> findByVehicleType(String vehicleType);

  /** Retrieves all available drivers who have an approved certification status. */
  @Query("SELECT d FROM Driver d WHERE d.isAvailable = true AND d.certificationStatus = 'APPROVED'")
  List<Driver> findAvailableCertifiedDrivers();

  /** Finds all currently available drivers, sorted by rating in descending order. */
  @Query("SELECT d FROM Driver d WHERE d.isAvailable = true ORDER BY d.rating DESC")
  List<Driver> findAvailableDriversByRating();

  /**
   * Finds available drivers within a specific radius from a given location using geospatial
   * distance.
   *
   * @param latitude the latitude of the reference point
   * @param longitude the longitude of the reference point
   * @param radiusInMeters the search radius in meters
   * @return a list of nearby available drivers within the specified distance
   */
  @Query(
      value =
          "SELECT * FROM drivers d WHERE d.is_available = true "
              + "AND ST_Distance_Sphere(point(d.current_longitude, d.current_latitude), point(:lng, :lat)) <= :radius",
      nativeQuery = true)
  List<Driver> findNearbyAvailableDrivers(
      @Param("lat") Double latitude,
      @Param("lng") Double longitude,
      @Param("radius") Double radiusInMeters);

  /**
   * Finds drivers who have completed at least the specified minimum number of deliveries.
   *
   * @param minDeliveries the minimum number of completed deliveries
   * @return a list of drivers meeting the criteria
   */
  @Query("SELECT d FROM Driver d WHERE d.totalDeliveries >= :minDeliveries")
  List<Driver> findByMinDeliveries(@Param("minDeliveries") Integer minDeliveries);

  /** Checks if a driver exists with the specified email address. */
  boolean existsByEmail(String email);

  /** Checks if a driver exists with the specified phone number. */
  boolean existsByPhone(String phone);

  /** Checks if a driver exists with the specified license plate. */
  boolean existsByLicensePlate(String licensePlate);
}
