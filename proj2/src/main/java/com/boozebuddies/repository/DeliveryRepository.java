package com.boozebuddies.repository;

import com.boozebuddies.entity.Delivery;
import com.boozebuddies.model.DeliveryStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

  Optional<Delivery> findByOrderId(Long orderId);

  List<Delivery> findByDriverId(Long driverId);

  List<Delivery> findByStatus(DeliveryStatus status);

  List<Delivery> findByDriverIdAndStatus(Long driverId, DeliveryStatus status);

  @Query("SELECT d FROM Delivery d WHERE d.createdAt BETWEEN :startDate AND :endDate")
  List<Delivery> findByDeliveryDateBetween(
      @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

  @Query("SELECT d FROM Delivery d WHERE d.driver.id = :driverId AND d.status IN :statuses")
  List<Delivery> findByDriverIdAndStatusIn(
      @Param("driverId") Long driverId, @Param("statuses") List<DeliveryStatus> statuses);

  @Query(
      "SELECT COUNT(d) FROM Delivery d WHERE d.driver.id = :driverId AND d.status = 'DELIVERED' AND d.deliveredTime BETWEEN :startDate AND :endDate")
  Long countCompletedDeliveriesByDriverAndDateRange(
      @Param("driverId") Long driverId,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);

  @Query(
      "SELECT d FROM Delivery d WHERE d.estimatedDeliveryTime < :currentTime AND d.status NOT IN ('DELIVERED', 'CANCELLED')")
  List<Delivery> findOverdueDeliveries(@Param("currentTime") LocalDateTime currentTime);

  @Query(
      value =
          "SELECT * FROM deliveries d WHERE d.driver_id = :driverId ORDER BY d.created_at DESC LIMIT :limit",
      nativeQuery = true)
  List<Delivery> findRecentDeliveriesByDriver(
      @Param("driverId") Long driverId, @Param("limit") int limit);

  @Query(
      "SELECT AVG(TIMESTAMPDIFF(MINUTE, d.pickupTime, d.deliveredTime)) FROM Delivery d WHERE d.status = 'DELIVERED' AND d.driver.id = :driverId")
  Double findAverageDeliveryTimeByDriver(@Param("driverId") Long driverId);
}
