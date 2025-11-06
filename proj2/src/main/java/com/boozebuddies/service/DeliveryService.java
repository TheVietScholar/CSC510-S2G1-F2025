package com.boozebuddies.service;

import com.boozebuddies.entity.Delivery;
import com.boozebuddies.entity.Driver;
import com.boozebuddies.entity.Order;
import com.boozebuddies.model.DeliveryStatus;
import java.util.List;

public interface DeliveryService {

  /**
   * Assigns a driver to a specific order and creates a new delivery record.
   *
   * @param order The order to assign.
   * @param driver The driver to assign to the order.
   * @return The created Delivery object.
   */
  Delivery assignDriverToOrder(Order order, Driver driver);

  /**
   * Updates the delivery status.
   *
   * @param deliveryId The ID of the delivery to update.
   * @param status The new delivery status.
   * @return The updated Delivery object.
   */
  Delivery updateDeliveryStatus(Long deliveryId, DeliveryStatus status);

  /**
   * Cancels a delivery and provides a reason.
   *
   * @param deliveryId The ID of the delivery to cancel.
   * @param reason The cancellation reason.
   * @return The cancelled Delivery object.
   */
  Delivery cancelDelivery(Long deliveryId, String reason);

  /**
   * Retrieves all deliveries assigned to a specific driver.
   *
   * @param driverId The ID of the driver.
   * @return A list of deliveries assigned to the driver.
   */
  List<Delivery> getDeliveriesByDriver(Long driverId);

  /**
   * Finds a delivery by its unique ID.
   *
   * @param deliveryId The delivery ID.
   * @return The Delivery object, or null if not found.
   */
  Delivery getDeliveryById(Long deliveryId);

  /**
   * Gets all active (non-completed and non-cancelled) deliveries.
   *
   * @return A list of active deliveries.
   */
  List<Delivery> getActiveDeliveries();

  /**
   * Gets all deliveries in the system. Admin only - for monitoring and reporting.
   *
   * @return A list of all deliveries.
   */
  List<Delivery> getAllDeliveries();

  /**
   * Updates a delivery with age verification information.
   *
   * @param deliveryId The delivery ID.
   * @param ageVerified Whether age was verified.
   * @param idType Type of ID checked (e.g., "DRIVER_LICENSE", "PASSPORT").
   * @param idNumber ID number (should be last 4 digits only for security).
   * @return The updated Delivery object.
   */
  Delivery updateDeliveryWithAgeVerification(
      Long deliveryId, boolean ageVerified, String idType, String idNumber);

  /**
   * Updates the delivery location for real-time tracking.
   *
   * @param deliveryId The delivery ID.
   * @param latitude The current latitude.
   * @param longitude The current longitude.
   */
  void updateDeliveryLocation(Long deliveryId, Double latitude, Double longitude);

  /**
   * Finds a delivery by order ID.
   *
   * @param orderId The order ID.
   * @return The Delivery object, or null if not found.
   */
  Delivery getDeliveryByOrderId(Long orderId);
}
