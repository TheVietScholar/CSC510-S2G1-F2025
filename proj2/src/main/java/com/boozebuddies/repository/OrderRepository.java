package com.boozebuddies.repository;

import com.boozebuddies.entity.Order;
import com.boozebuddies.model.OrderStatus;
import java.util.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing {@link Order} entities within the BoozeBuddies platform.
 *
 * <p>Provides CRUD operations through {@link JpaRepository}, along with custom query methods to
 * retrieve, search, and analyze order data for customers, merchants, and drivers.
 *
 * <p>Custom queries support filtering by status, merchant, and driver assignments, as well as
 * aggregating financial data for reporting and analytics.
 *
 * <p>Key use cases include:
 *
 * <ul>
 *   <li>Fetching customer order history by user ID
 *   <li>Merchant dashboards with active and past orders
 *   <li>Driver delivery management
 *   <li>Search across orders by keyword or merchant name
 *   <li>Reporting total delivered order amounts per merchant
 * </ul>
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

  /** Retrieves all orders matching the specified {@link OrderStatus}. */
  List<Order> findByStatus(OrderStatus status);

  /** Return a list of open orders */
  @Query(
      "SELECT o FROM Order o WHERE o.status = PENDING or o.status = CONFIRMED or o.status = PREPARING or o.status = READY_FOR_PICKUP AND o.driver is null")
  List<Order> findAvailableForAssignment();

  /**
   * Retrieves paginated orders by status.
   *
   * @param status the order status to filter by
   * @param pageable pagination and sorting configuration
   * @return a paginated list of orders with the given status
   */
  Page<Order> findByStatus(OrderStatus status, Pageable pageable);

  /**
   * Retrieves all orders belonging to a specific customer, sorted by creation date (most recent
   * first).
   *
   * @param customerId the ID of the customer
   * @return a list of the customer's orders
   */
  @Query("SELECT o FROM Order o WHERE o.user.id = :customerId ORDER BY o.createdAt DESC")
  List<Order> findByCustomerId(@Param("customerId") Long customerId);

  /**
   * Retrieves a paginated list of orders placed with a specific merchant.
   *
   * @param merchantId the ID of the merchant
   * @param pageable pagination and sorting configuration
   * @return a paginated list of merchant orders
   */
  @Query("SELECT o FROM Order o WHERE o.merchant.id = :merchantId")
  Page<Order> findByMerchantId(@Param("merchantId") Long merchantId, Pageable pageable);

  /**
   * Retrieves all orders for a specific merchant, sorted by creation date in descending order.
   *
   * <p>Non-paginated version used by service-level logic.
   *
   * @param merchantId the ID of the merchant
   * @return a list of orders for the given merchant
   */
  @Query("SELECT o FROM Order o WHERE o.merchant.id = :merchantId ORDER BY o.createdAt DESC")
  List<Order> findByMerchantId(@Param("merchantId") Long merchantId);

  /**
   * Retrieves all orders assigned to a given driver, sorted by creation date (most recent first).
   *
   * @param driverId the ID of the driver
   * @return a list of orders assigned to the driver
   */
  @Query("SELECT o FROM Order o WHERE o.driver.id = :driverId ORDER BY o.createdAt DESC")
  List<Order> findByDriverId(@Param("driverId") Long driverId);

  /**
   * Retrieves active (non-delivered) orders for a merchant that match the provided list of
   * statuses. Typically used for dispatch and kitchen board displays.
   *
   * @param merchantId the ID of the merchant
   * @param statuses a list of statuses considered "active" (e.g., PENDING, PREPARING)
   * @return a list of active orders for the merchant
   */
  @Query(
      "SELECT o FROM Order o WHERE o.merchant.id = :merchantId AND o.status IN :statuses ORDER BY o.createdAt ASC")
  List<Order> findActiveByMerchant(
      @Param("merchantId") Long merchantId, @Param("statuses") List<OrderStatus> statuses);

  /**
   * Searches orders by item name or merchant name, supporting partial matches (case-insensitive).
   * Useful for customer and merchant order search features.
   *
   * @param keyword the keyword to search by
   * @param pageable pagination and sorting configuration
   * @return a paginated list of matching orders
   */
  @Query(
      """
        SELECT DISTINCT o FROM Order o
        JOIN o.items i
        WHERE LOWER(i.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(o.merchant.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
        """)
  Page<Order> searchOrders(@Param("keyword") String keyword, Pageable pageable);

  /**
   * Retrieves a specific order along with its related items and product details.
   *
   * <p>Uses a {@code LEFT JOIN FETCH} to eagerly load items and products, preventing multiple
   * roundtrips when rendering detailed order views.
   *
   * @param id the ID of the order
   * @return an optional containing the order and its items, if found
   */
  @Query(
      """
        SELECT o FROM Order o
        LEFT JOIN FETCH o.items i
        LEFT JOIN FETCH i.product
        WHERE o.id = :id
        """)
  Optional<Order> findWithItems(@Param("id") Long id);

  // Find order with user, merchant, and driver relationships loaded for permission checks
  @Query(
      """
                          SELECT o FROM Order o
                          LEFT JOIN FETCH o.user
                          LEFT JOIN FETCH o.merchant
                          LEFT JOIN FETCH o.driver
                          WHERE o.id = :id
                        """)
  Optional<Order> findByIdWithRelationships(@Param("id") Long id);

  /**
   * Aggregates total delivered order revenue per merchant.
   *
   * <p>Returns each merchant’s ID and the sum of total amounts for orders with {@link
   * OrderStatus#DELIVERED}.
   *
   * @return a list of object arrays where each element contains {@code [merchantId,
   *     totalDeliveredAmount]}
   */
  @Query(
      "SELECT o.merchant.id, SUM(o.totalAmount) FROM Order o WHERE o.status = com.boozebuddies.model.OrderStatus.DELIVERED GROUP BY o.merchant.id")
  List<Object[]> sumDeliveredTotalsByMerchant();
}
