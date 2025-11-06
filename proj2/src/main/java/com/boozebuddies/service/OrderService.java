package com.boozebuddies.service;

import com.boozebuddies.entity.Order;
import java.util.List;
import java.util.Optional;

public interface OrderService {

  /**
   * Creates a new order, processes payment, creates a delivery record, and sends notifications.
   *
   * @param order The order to create.
   * @return The created order.
   */
  Order createOrder(Order order);

  /**
   * Retrieves an order by its unique ID.
   *
   * @param id The order ID.
   * @return An Optional containing the order if found, otherwise empty.
   */
  Optional<Order> getOrderById(Long id);

  /**
   * Retrieves all orders placed by a specific user.
   *
   * @param userId The user's ID.
   * @return A list of orders associated with the user.
   */
  List<Order> getOrdersByUser(Long userId);

  /**
   * Retrieves all orders for a specific merchant.
   *
   * @param merchantId The merchant's ID.
   * @return A list of orders associated with the merchant.
   */
  List<Order> getOrdersByMerchant(Long merchantId);

  /**
   * Retrieves all orders assigned to a specific driver.
   *
   * @param driverId The driver's ID.
   * @return A list of orders assigned to the driver.
   */
  List<Order> getOrdersByDriver(Long driverId);

  /**
   * Retrieves all orders in the system.
   *
   * @return A list of all orders.
   */
  List<Order> getAllOrders();

  /**
   * Cancels an order, processes refund, and sends notifications.
   *
   * @param orderId The ID of the order to cancel.
   * @return The cancelled order.
   */
  Order cancelOrder(Long orderId);

  /**
   * Updates the status of an order (e.g., CONFIRMED, PREPARING, COMPLETED).
   *
   * @param orderId The ID of the order to update.
   * @param status The new status as a string.
   * @return The updated order.
   */
  Order updateOrderStatus(Long orderId, String status);

  /**
   * Retrieves a list of orders within a certain distance from given coordinates.
   *
   * @param latitude The latitude of the reference point.
   * @param longitude The longitude of the reference point.
   * @param distanceKm The distance in kilometers.
   * @return A list of orders within the specified distance.
   */
  List<Order> getOrdersWithinDistance(double latitude, double longitude, double distanceKm);

  /**
   * Calculate distance between two points using Haversine formula.
   *
   * @param lat1 Latitude of first point
   * @param lon1 Longitude of first point
   * @param lat2 Latitude of second point
   * @param lon2 Longitude of second point
   * @return Distance in kilometers
   */
  double calculateDistance(double lat1, double lon1, double lat2, double lon2);

  /**
   * Update the estimated delivery time for an order.
   *
   * @param orderId The order ID
   * @param estimatedDeliveryTime The estimated delivery time
   * @return The updated order
   */
  Order updateEstimatedDeliveryTime(Long orderId, java.time.LocalDateTime estimatedDeliveryTime);
}
