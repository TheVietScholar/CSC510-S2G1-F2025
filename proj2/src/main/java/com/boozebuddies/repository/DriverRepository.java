package com.boozebuddies.repository;

import com.boozebuddies.entity.Driver;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {

  Optional<Driver> findByEmail(String email);

  Optional<Driver> findByPhone(String phone);

  @Query("SELECT d FROM Driver d WHERE d.user.id = :userId")
  Optional<Driver> findByUserId(@Param("userId") Long userId);

  List<Driver> findByIsAvailable(boolean isAvailable);

  List<Driver> findByVehicleType(String vehicleType);

  @Query("SELECT d FROM Driver d WHERE d.isAvailable = true AND d.certificationStatus = 'APPROVED'")
  List<Driver> findAvailableCertifiedDrivers();

  @Query("SELECT d FROM Driver d WHERE d.isAvailable = true ORDER BY d.rating DESC")
  List<Driver> findAvailableDriversByRating();

  @Query(
      value =
          "SELECT * FROM drivers d WHERE d.is_available = true "
              + "AND ST_Distance_Sphere(point(d.current_longitude, d.current_latitude), point(:lng, :lat)) <= :radius",
      nativeQuery = true)
  List<Driver> findNearbyAvailableDrivers(
      @Param("lat") Double latitude,
      @Param("lng") Double longitude,
      @Param("radius") Double radiusInMeters);

  @Query("SELECT d FROM Driver d WHERE d.totalDeliveries >= :minDeliveries")
  List<Driver> findByMinDeliveries(@Param("minDeliveries") Integer minDeliveries);

  boolean existsByEmail(String email);

  boolean existsByPhone(String phone);

  boolean existsByLicensePlate(String licensePlate);
}
