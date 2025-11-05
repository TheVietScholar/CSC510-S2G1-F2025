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

/**
 * Implementation of the {@link DeliveryService} interface.
 *
 * <p>Provides business logic for managing deliveries, including creation, updates, status
 * transitions, location tracking, and verification processes. This service interacts with the
 * {@link DeliveryRepository} to persist delivery data.
 */
@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {

  private final DeliveryRepository deliveryRepository;

  /**
   * Assigns a driver to a specific order and creates a new delivery record.
   *
   * <p>Sets the delivery status to {@link DeliveryStatus#ASSIGNED} and initializes timestamps for
   * creation and update.
   *
   * @param order the {@link Order} associated with the delivery
   * @param driver the {@link Driver} assigned to handle the delivery
   * @return the newly created {@link Delivery} entity
   */
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

  /**
   * Updates the status of a delivery record.
   *
   * <p>Automatically updates timestamps for pickup or delivery events when applicable.
   *
   * @param deliveryId the ID of the delivery to update
   * @param status the new {@link DeliveryStatus}
   * @return the updated {@link Delivery} entity
   * @throws RuntimeException if the delivery is not found
   */
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

    if (status == DeliveryStatus.PICKED_UP && delivery.getPickupTime() == null) {
      delivery.setPickupTime(LocalDateTime.now());
    } else if (status == DeliveryStatus.DELIVERED && delivery.getDeliveredTime() == null) {
      delivery.setDeliveredTime(LocalDateTime.now());
    }

    return deliveryRepository.save(delivery);
  }

  /**
   * Cancels a delivery and records the provided reason.
   *
   * @param deliveryId the ID of the delivery to cancel
   * @param reason the cancellation reason
   * @return the updated {@link Delivery} entity
   * @throws RuntimeException if the delivery is not found
   */
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

  /**
   * Retrieves all deliveries assigned to a specific driver.
   *
   * @param driverId the ID of the driver
   * @return a list of {@link Delivery} entities assigned to the driver
   */
  @Override
  public List<Delivery> getDeliveriesByDriver(Long driverId) {
    return deliveryRepository.findByDriverId(driverId);
  }

  /**
   * Retrieves a delivery by its unique ID.
   *
   * @param deliveryId the ID of the delivery
   * @return the corresponding {@link Delivery}, or {@code null} if not found
   */
  @Override
  public Delivery getDeliveryById(Long deliveryId) {
    return deliveryRepository.findById(deliveryId).orElse(null);
  }

  /** Finds a delivery by order ID. */
  @Override
  public Delivery getDeliveryByOrderId(Long orderId) {
    return deliveryRepository.findByOrderId(orderId).orElse(null);
  }

  /**
   * Retrieves all deliveries that are currently active.
   *
   * <p>Active deliveries include those in statuses such as PENDING, ASSIGNED, PICKED_UP,
   * IN_TRANSIT, or FAILED.
   *
   * @return a list of active {@link Delivery} entities
   */
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

  /**
   * Retrieves all deliveries in the system.
   *
   * @return a list of all {@link Delivery} entities
   */
  @Override
  public List<Delivery> getAllDeliveries() {
    return deliveryRepository.findAll();
  }

  /**
   * Updates a delivery record with age verification details.
   *
   * <p>For privacy, only the last 4 digits of the ID number are stored.
   *
   * @param deliveryId the ID of the delivery
   * @param ageVerified whether the recipient’s age was verified
   * @param idType the type of identification used (e.g., "Driver’s License")
   * @param idNumber the identification number (only last 4 digits are retained)
   * @return the updated {@link Delivery} entity
   * @throws RuntimeException if the delivery is not found
   */
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

    if (idNumber != null && idNumber.length() > 4) {
      delivery.setIdNumber(idNumber.substring(idNumber.length() - 4));
    } else {
      delivery.setIdNumber(idNumber);
    }

    delivery.setAgeVerifiedAt(LocalDateTime.now());
    delivery.setUpdatedAt(LocalDateTime.now());

    return deliveryRepository.save(delivery);
  }

  /**
   * Updates the real-time geographic location of a delivery.
   *
   * <p>Used for tracking the driver’s progress and updating live maps or customer views.
   *
   * @param deliveryId the ID of the delivery
   * @param latitude the current latitude
   * @param longitude the current longitude
   * @throws RuntimeException if the delivery is not found
   */
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
