package com.boozebuddies.service.implementation;

import com.boozebuddies.entity.Delivery;
import com.boozebuddies.entity.Driver;
import com.boozebuddies.entity.Order;
import com.boozebuddies.model.DeliveryStatus;
import com.boozebuddies.repository.DeliveryRepository;
import com.boozebuddies.service.DeliveryService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {

  private final DeliveryRepository deliveryRepository;

  /** Assigns a driver to a specific order and creates a new delivery record. */
  @Override
  @Transactional
  public Delivery assignDriverToOrder(Order order, Driver driver) {
    Delivery delivery = new Delivery();
    delivery.setOrder(order);
    delivery.setDriver(driver);
    delivery.setStatus(DeliveryStatus.ASSIGNED);
    delivery.setCreatedAt(LocalDateTime.now());
    delivery.setUpdatedAt(LocalDateTime.now());
    return deliveryRepository.save(delivery);
  }

  /** Updates the delivery status. */
  @Override
  @Transactional
  public Delivery updateDeliveryStatus(Long deliveryId, DeliveryStatus status) {
    Optional<Delivery> deliveryOpt = deliveryRepository.findById(deliveryId);
    if (deliveryOpt.isEmpty()) {
      throw new RuntimeException("Delivery not found");
    }
    Delivery delivery = deliveryOpt.get();
    delivery.setStatus(status);
    delivery.setUpdatedAt(LocalDateTime.now());

    // Update specific timestamps based on status (using your field names)
    if (status == DeliveryStatus.PICKED_UP && delivery.getPickupTime() == null) {
      delivery.setPickupTime(LocalDateTime.now());
    } else if (status == DeliveryStatus.DELIVERED && delivery.getDeliveredTime() == null) {
      delivery.setDeliveredTime(LocalDateTime.now());
    }

    return deliveryRepository.save(delivery);
  }

  /** Cancels a delivery and provides a reason. */
  @Override
  @Transactional
  public Delivery cancelDelivery(Long deliveryId, String reason) {
    Optional<Delivery> deliveryOpt = deliveryRepository.findById(deliveryId);
    if (deliveryOpt.isEmpty()) {
      throw new RuntimeException("Delivery not found");
    }
    Delivery delivery = deliveryOpt.get();
    delivery.setStatus(DeliveryStatus.CANCELLED);
    delivery.setCancellationReason(reason);
    delivery.setUpdatedAt(LocalDateTime.now());
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

  /** Finds a delivery by order ID. */
  @Override
  public Delivery getDeliveryByOrderId(Long orderId) {
    return deliveryRepository.findByOrderId(orderId).orElse(null);
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

  /** Gets all deliveries in the system. */
  @Override
  public List<Delivery> getAllDeliveries() {
    return deliveryRepository.findAll();
  }

  /** Updates a delivery with age verification information. */
  @Override
  @Transactional
  public Delivery updateDeliveryWithAgeVerification(
      Long deliveryId, boolean ageVerified, String idType, String idNumber) {
    Optional<Delivery> deliveryOpt = deliveryRepository.findById(deliveryId);
    if (deliveryOpt.isEmpty()) {
      throw new RuntimeException("Delivery not found");
    }

    Delivery delivery = deliveryOpt.get();
    delivery.setAgeVerified(ageVerified);
    delivery.setIdType(idType);

    // SECURITY: In production, only store last 4 digits of ID number
    if (idNumber != null && idNumber.length() > 4) {
      delivery.setIdNumber(idNumber.substring(idNumber.length() - 4));
    } else {
      delivery.setIdNumber(idNumber);
    }

    delivery.setAgeVerifiedAt(LocalDateTime.now());
    delivery.setUpdatedAt(LocalDateTime.now());

    return deliveryRepository.save(delivery);
  }

  /** Updates the delivery location for real-time tracking. */
  @Override
  @Transactional
  public void updateDeliveryLocation(Long deliveryId, Double latitude, Double longitude) {
    Optional<Delivery> deliveryOpt = deliveryRepository.findById(deliveryId);
    if (deliveryOpt.isEmpty()) {
      throw new RuntimeException("Delivery not found");
    }

    Delivery delivery = deliveryOpt.get();
    delivery.setCurrentLatitude(latitude);
    delivery.setCurrentLongitude(longitude);
    delivery.setLastLocationUpdate(LocalDateTime.now());
    delivery.setUpdatedAt(LocalDateTime.now());

    deliveryRepository.save(delivery);
  }
}
