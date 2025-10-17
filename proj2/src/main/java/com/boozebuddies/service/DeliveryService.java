package com.boozebuddies.service;

import com.boozebuddies.model.Delivery;
import com.boozebuddies.model.Driver;
import com.boozebuddies.model.Order;
import java.util.List;

public interface DeliveryService {

    /**
     * Assigns a driver to a specific order and creates a new delivery record.
     * 
     * @param order The order to be delivered.
     * @param driver The driver assigned to the delivery.
     * @return The created Delivery object.
     */
    Delivery assignDriverToOrder(Order order, Driver driver);

    /**
     * Updates the delivery status (e.g., PENDING, IN_PROGRESS, COMPLETED, CANCELLED).
     * 
     * @param deliveryId The unique ID of the delivery.
     * @param status The new status to set.
     * @return The updated Delivery object.
     */
    Delivery updateDeliveryStatus(Long deliveryId, DeliveryStatus status);

    /**
     * Cancels a delivery and provides a reason.
     * 
     * @param deliveryId The ID of the delivery to cancel.
     * @param reason The reason for cancellation.
     * @return The updated Delivery object reflecting the cancellation.
     */
    Delivery cancelDelivery(Long deliveryId, String reason);

    /**
     * Retrieves all deliveries assigned to a specific driver.
     * 
     * @param driverId The ID of the driver.
     * @return A list of deliveries associated with the driver.
     */
    List<Delivery> getDeliveriesByDriver(Long driverId);

    /**
     * Finds a delivery by its unique ID.
     * 
     * @param deliveryId The ID of the delivery.
     * @return The corresponding Delivery object, or null if not found.
     */
    Delivery getDeliveryById(Long deliveryId);

    /**
     * Gets all active (non-completed and non-cancelled) deliveries.
     * 
     * @return A list of active deliveries.
     */
    List<Delivery> getActiveDeliveries();
}