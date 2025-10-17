package com.boozebuddies.service.implementation;

import com.boozebuddies.entity.Delivery;
import com.boozebuddies.entity.Driver;
import com.boozebuddies.entity.Order;
import com.boozebuddies.model.DeliveryStatus;
import com.boozebuddies.service.DeliveryService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DeliveryServiceImpl implements DeliveryService {

    private final List<Delivery> deliveries = new ArrayList<>();
    private long nextDeliveryId = 1;

    /**
     * Assigns a driver to a specific order and creates a new delivery record.
     */
    @Override
    public Delivery assignDriverToOrder(Order order, Driver driver) {
        Delivery delivery = new Delivery();
        delivery.setDeliveryId(nextDeliveryId++);
        delivery.setOrder(order);
        delivery.setDriver(driver);
        delivery.setStatus(DeliveryStatus.PENDING);
        deliveries.add(delivery);
        return delivery;
    }

    /**
     * Updates the delivery status.
     */
    @Override
    public Delivery updateDeliveryStatus(Long deliveryId, DeliveryStatus status) {
        Delivery delivery = getDeliveryById(deliveryId);
        if (delivery != null) {
            delivery.setStatus(status);
        }
        return delivery;
    }

    /**
     * Cancels a delivery and provides a reason.
     */
    @Override
    public Delivery cancelDelivery(Long deliveryId, String reason) {
        Delivery delivery = getDeliveryById(deliveryId);
        if (delivery != null) {
            delivery.setStatus(DeliveryStatus.CANCELLED);
            delivery.setCancellationReason(reason);
        }
        return delivery;
    }

    /**
     * Retrieves all deliveries assigned to a specific driver.
     */
    @Override
    public List<Delivery> getDeliveriesByDriver(Long driverId) {
        return deliveries.stream()
                .filter(d -> d.getDriver() != null && d.getDriver().getDriverId().equals(driverId))
                .collect(Collectors.toList());
    }

    /**
     * Finds a delivery by its unique ID.
     */
    @Override
    public Delivery getDeliveryById(Long deliveryId) {
        return deliveries.stream()
                .filter(d -> d.getDeliveryId().equals(deliveryId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Gets all active (non-completed and non-cancelled) deliveries.
     */
    @Override
    public List<Delivery> getActiveDeliveries() {
        return deliveries.stream()
                .filter(d -> d.getStatus() != DeliveryStatus.COMPLETED && d.getStatus() != DeliveryStatus.CANCELLED)
                .collect(Collectors.toList());
    }
}
