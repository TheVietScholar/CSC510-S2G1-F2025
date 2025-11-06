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

/**
 * Repository interface for managing {@link Delivery} entities in the BoozeBuddies system.
 *
 * <p>This interface provides built-in CRUD operations via {@link JpaRepository} and custom query
 * methods for managing deliveries, tracking driver activity, and analyzing delivery performance.
 *
 * <p>Custom query methods include:
 *
 * <ul>
 *   <li>{@link #findByOrderId(Long)} - Retrieves a delivery associated with a specific order.
 *   <li>{@link #findByDriverId(Long)} - Returns all deliveries assigned to a given driver.
 *   <li>{@link #findByStatus(DeliveryStatus)} - Retrieves deliveries filtered by their current
 *       status.
 *   <li>{@link #findByDriverIdAndStatus(Long, DeliveryStatus)} - Finds deliveries by both driver
 *       and status.
 *   <li>{@link #findByDeliveryDateBetween(LocalDateTime, LocalDateTime)} - Retrieves deliveries
 *       created within a specific date range.
 *   <li>{@link #findByDriverIdAndStatusIn(Long, List)} - Retrieves deliveries for a driver with
 *       multiple possible statuses.
 *   <li>{@link #countCompletedDeliveriesByDriverAndDateRange(Long, LocalDateTime, LocalDateTime)} -
 *       Counts completed deliveries for a driver within a given timeframe.
 *   <li>{@link #findOverdueDeliveries(LocalDateTime)} - Finds deliveries that are overdue based on
 *       their estimated delivery time.
 *   <li>{@link #findRecentDeliveriesByDriver(Long, int)} - Retrieves a limited number of recent
 *       deliveries for a driver.
 *   <li>{@link #findAverageDeliveryTimeByDriver(Long)} - Calculates the average delivery time (in
 *       minutes) for completed deliveries by a specific driver.
 * </ul>
 *
 * This repository supports analytical reporting, operational dashboards, and driver performance
 * monitoring features.
 */
@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

  /** Retrieves a delivery associated with a specific order. */
  Optional<Delivery> findByOrderId(Long orderId);

  /** Returns all deliveries assigned to a given driver. */
  List<Delivery> findByDriverId(Long driverId);

  /** Retrieves all deliveries that match a given delivery status. */
  List<Delivery> findByStatus(DeliveryStatus status);

  /** Finds all deliveries assigned to a driver that match a specific status. */
  List<Delivery> findByDriverIdAndStatus(Long driverId, DeliveryStatus status);

  /**
   * Retrieves deliveries created within a specific date range.
   *
   * @param startDate the start of the time window
   * @param endDate the end of the time window
   * @return a list of deliveries created within the specified range
   */
  @Query("SELECT d FROM Delivery d WHERE d.createdAt BETWEEN :startDate AND :endDate")
  List<Delivery> findByDeliveryDateBetween(
      @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

  /**
   * Finds deliveries for a given driver that have any of the specified statuses.
   *
   * @param driverId the driver's unique identifier
   * @param statuses the list of statuses to match
   * @return a list of deliveries matching the criteria
   */
  @Query("SELECT d FROM Delivery d WHERE d.driver.id = :driverId AND d.status IN :statuses")
  List<Delivery> findByDriverIdAndStatusIn(
      @Param("driverId") Long driverId, @Param("statuses") List<DeliveryStatus> statuses);

  /**
   * Counts the number of completed (delivered) deliveries for a driver within a given date range.
   *
   * @param driverId the driver's unique identifier
   * @param startDate the start of the time window
   * @param endDate the end of the time window
   * @return the number of completed deliveries
   */
  @Query(
      "SELECT COUNT(d) FROM Delivery d WHERE d.driver.id = :driverId AND d.status = 'DELIVERED' AND d.deliveredTime BETWEEN :startDate AND :endDate")
  Long countCompletedDeliveriesByDriverAndDateRange(
      @Param("driverId") Long driverId,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);

  /**
   * Finds deliveries that are overdue — i.e., where the estimated delivery time has passed but the
   * status is not {@code DELIVERED} or {@code CANCELLED}.
   *
   * @param currentTime the current system time used for comparison
   * @return a list of overdue deliveries
   */
  @Query(
      "SELECT d FROM Delivery d WHERE d.estimatedDeliveryTime < :currentTime AND d.status NOT IN ('DELIVERED', 'CANCELLED')")
  List<Delivery> findOverdueDeliveries(@Param("currentTime") LocalDateTime currentTime);

  /**
   * Retrieves a limited list of the most recent deliveries assigned to a driver. Uses a native SQL
   * query for performance optimization.
   *
   * @param driverId the driver's unique identifier
   * @param limit the maximum number of records to return
   * @return a list of recent deliveries sorted by creation date
   */
  @Query(
      value =
          "SELECT * FROM deliveries d WHERE d.driver_id = :driverId ORDER BY d.created_at DESC LIMIT :limit",
      nativeQuery = true)
  List<Delivery> findRecentDeliveriesByDriver(
      @Param("driverId") Long driverId, @Param("limit") int limit);

  /**
   * Calculates the average delivery time (in minutes) for all completed deliveries handled by a
   * specific driver.
   *
   * @param driverId the driver's unique identifier
   * @return the average delivery duration in minutes, or {@code null} if no data is available
   */
  @Query(
      "SELECT AVG(TIMESTAMPDIFF(MINUTE, d.pickupTime, d.deliveredTime)) FROM Delivery d WHERE d.status = 'DELIVERED' AND d.driver.id = :driverId")
  Double findAverageDeliveryTimeByDriver(@Param("driverId") Long driverId);
}
