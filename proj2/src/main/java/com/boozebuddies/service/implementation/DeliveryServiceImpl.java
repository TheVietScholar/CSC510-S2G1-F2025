package com.boozebuddies.service.implementation;

import com.boozebuddies.entity.Delivery;
import com.boozebuddies.entity.Driver;
import com.boozebuddies.entity.Order;
import com.boozebuddies.model.DeliveryStatus;
import com.boozebuddies.repository.DeliveryRepository;
import com.boozebuddies.service.DeliveryService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {

  private final DeliveryRepository deliveryRepository;

  /** Assigns a driver to a specific order and creates a new delivery record. */
  @Override
  public Delivery assignDriverToOrder(Order order, Driver driver) {
    Delivery delivery = new Delivery();
    delivery.setOrder(order);
    delivery.setDriver(driver);
    delivery.setStatus(DeliveryStatus.PENDING);
    return deliveryRepository.save(delivery);
  }

  /** Updates the delivery status. */
  @Override
  public Delivery updateDeliveryStatus(Long deliveryId, DeliveryStatus status) {
    Optional<Delivery> deliveryOpt = deliveryRepository.findById(deliveryId);
    if (deliveryOpt.isEmpty()) {
      return null;
    }
    Delivery delivery = deliveryOpt.get();
    delivery.setStatus(status);
    return deliveryRepository.save(delivery);
  }

  /** Cancels a delivery and provides a reason. */
  @Override
  public Delivery cancelDelivery(Long deliveryId, String reason) {
    Optional<Delivery> deliveryOpt = deliveryRepository.findById(deliveryId);
    if (deliveryOpt.isEmpty()) {
      return null;
    }
    Delivery delivery = deliveryOpt.get();
    delivery.setStatus(DeliveryStatus.CANCELLED);
    delivery.setCancellationReason(reason);
    return deliveryRepository.save(delivery);
  }

  /** Retrieves all deliveries assigned to a specific driver. */
  @Override
  public List<Delivery> getDeliveriesByDriver(Long driverId) {
    return deliveryRepository.findByDriverId(driverId);
  }

  /** Finds a delivery by its unique ID. */
  @Override
  public Delivery getDeliveryById(Long deliveryId) {
    return deliveryRepository.findById(deliveryId).orElse(null);
  }

  /** Gets all active (non-completed and non-cancelled) deliveries. */
  @Override
  public List<Delivery> getActiveDeliveries() {
    List<Delivery> active = new ArrayList<>();
    active.addAll(deliveryRepository.findByStatus(DeliveryStatus.PENDING));
    active.addAll(deliveryRepository.findByStatus(DeliveryStatus.ASSIGNED));
    active.addAll(deliveryRepository.findByStatus(DeliveryStatus.PICKED_UP));
    active.addAll(deliveryRepository.findByStatus(DeliveryStatus.IN_TRANSIT));
    active.addAll(deliveryRepository.findByStatus(DeliveryStatus.FAILED));
    return active;
  }
}
