package com.boozebuddies.repository;

import com.boozebuddies.entity.Order;
import com.boozebuddies.model.OrderStatus;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

  // Basic filters
  List<Order> findByStatus(OrderStatus status);

  Page<Order> findByStatus(OrderStatus status, Pageable pageable);

  @Query("SELECT o FROM Order o WHERE o.user.id = :customerId ORDER BY o.createdAt DESC")
  List<Order> findByCustomerId(@Param("customerId") Long customerId);

  // Pageable version for MerchantController
  @Query("SELECT o FROM Order o WHERE o.merchant.id = :merchantId")
  Page<Order> findByMerchantId(@Param("merchantId") Long merchantId, Pageable pageable);

  // ⭐ NEW - Non-pageable version for OrderService
  @Query("SELECT o FROM Order o WHERE o.merchant.id = :merchantId ORDER BY o.createdAt DESC")
  List<Order> findByMerchantId(@Param("merchantId") Long merchantId);

  // ⭐ NEW - Find orders assigned to a driver
  @Query("SELECT o FROM Order o WHERE o.driver.id = :driverId ORDER BY o.createdAt DESC")
  List<Order> findByDriverId(@Param("driverId") Long driverId);

  // Ready for dispatch (kitchen/dispatch boards)
  @Query(
      "SELECT o FROM Order o WHERE o.merchant.id = :merchantId AND o.status IN :statuses ORDER BY o.createdAt ASC")
  List<Order> findActiveByMerchant(
      @Param("merchantId") Long merchantId, @Param("statuses") List<OrderStatus> statuses);

  // Search (by item name snapshot or merchant name)
  @Query(
      """
                        SELECT DISTINCT o FROM Order o
                        JOIN o.items i
                        WHERE LOWER(i.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                           OR LOWER(o.merchant.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                        """)
  Page<Order> searchOrders(@Param("keyword") String keyword, Pageable pageable);

  /*
   * keep result sets bounded (one order at a time) to avoid Cartesian explosions.
   * For large histories, prefer
   * OrderItemRepository.findByOrder_IdOrderByLineNoAsc(...) and return a DTO of
   * items to the client.
   */
  @Query(
      """
                          SELECT o FROM Order o
                          LEFT JOIN FETCH o.items i
                          LEFT JOIN FETCH i.product
                          WHERE o.id = :id
                        """)
  Optional<Order> findWithItems(@Param("id") Long id);

  // Totals & reporting
  @Query(
      "SELECT o.merchant.id, SUM(o.totalAmount) FROM Order o WHERE o.status = com.boozebuddies.model.OrderStatus.DELIVERED GROUP BY o.merchant.id")
  List<Object[]> sumDeliveredTotalsByMerchant();
}