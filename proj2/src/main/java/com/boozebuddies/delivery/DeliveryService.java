//Need to figure out the package and imports for the new classes
// Need to figure out driver repository and tracking services classes
package com.boozebuddies.delivery;

import com.boozebuddies.model.*;
import com.boozebuddies.repository.DriverRepository;

public class DeliveryService {

  private DriverRepository driverRepository;
  private TrackingService trackingService;

  // Constructor
  public DeliveryService(DriverRepository driverRepository, TrackingService trackingService) {
    this.driverRepository = driverRepository;
    this.trackingService = trackingService;
  }

  /**
   * Assigns an available driver to the given order.
   *
   * @param order The order to assign a driver for.
   * @return The assigned Driver.
   */
  public Driver assignDriver(Order order) {
    Driver availableDriver = driverRepository.findAvailableDriver();

    if (availableDriver == null) {
      throw new IllegalStateException("No available drivers at the moment.");
    }

    // Mark driver as unavailable and create delivery
    availableDriver.setAvailable(false);

    Delivery delivery = new Delivery(System.currentTimeMillis(), order);
    delivery.assignDriver(availableDriver);

    // Optionally persist delivery info
    // deliveryRepository.save(delivery);

    return availableDriver;
  }

  /**
   * Updates the delivery status for a given order.
   *
   * @param order     The order whose status needs updating.
   * @param newStatus The new status to apply.
   */
  public void updateDeliveryStatus(Order order, DeliveryStatus newStatus) {
    Delivery delivery = order.getDelivery();

    if (delivery == null) {
      throw new IllegalStateException("No delivery found for this order.");
    }

    delivery.setStatus(newStatus);

    // Optional: Update tracking info as well
    trackingService.updateTracking(order, newStatus);
  }

  /**
   * Returns current tracking information for the order.
   *
   * @param order The order to track.
   * @return A TrackingInfo object with latest delivery details.
   */
  public TrackingInfo trackOrder(Order order) {
    return trackingService.getTrackingInfo(order);
  }
}
