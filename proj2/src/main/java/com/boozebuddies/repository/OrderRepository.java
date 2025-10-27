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

  @Query("SELECT o FROM Order o WHERE o.merchant.id = :merchantId")
  Page<Order> findByMerchantId(@Param("merchantId") Long merchantId, Pageable pageable);

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

  @Query(
      "SELECT o FROM Order o WHERE o.status = com.boozebuddies.model.OrderStatus.DELIVERED AND o.updatedAt BETWEEN :from AND :to")
  List<Order> findDeliveredBetween(
      @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

  // Promo usage
  @Query("SELECT o FROM Order o WHERE o.promoCode = :promoCode ORDER BY o.createdAt DESC")
  List<Order> findByPromoCodeOrderByCreatedAtDesc(@Param("promoCode") String promoCode);

  @Query("SELECT o FROM Order o WHERE o.promoCode = :promoCode AND o.status = :status")
  List<Order> findByPromoCodeAndStatus(
      @Param("promoCode") String promoCode, @Param("status") OrderStatus status);

  @Query("SELECT o FROM Order o WHERE o.promoCode = :promoCode AND o.user.id = :customerId")
  List<Order> findByPromoCodeAndCustomerId(
      @Param("promoCode") String promoCode, @Param("customerId") Long customerId);

  @Query("SELECT COUNT(o) FROM Order o WHERE o.promoCode = :promoCode")
  long countByPromoCode(@Param("promoCode") String promoCode);

  @Query(
      "SELECT o FROM Order o WHERE o.promoCode = :promoCode AND o.createdAt BETWEEN :from AND :to")
  List<Order> findByPromoCodeAndCreatedAtBetween(
      @Param("promoCode") String promoCode,
      @Param("from") LocalDateTime from,
      @Param("to") LocalDateTime to);

  @Query("SELECT o FROM Order o WHERE o.user.id = :customerId AND o.status IN :statuses")
  List<Order> findCancellableOrders(
      @Param("customerId") Long customerId, @Param("statuses") List<OrderStatus> statuses);

  // Shortcut to check existence for a user
  boolean existsByUser_Id(Long customerId);
}
