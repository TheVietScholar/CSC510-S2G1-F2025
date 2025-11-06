package com.boozebuddies.mapper;

import com.boozebuddies.dto.DeliveryDTO;
import com.boozebuddies.entity.Delivery;
import com.boozebuddies.entity.Driver;
import com.boozebuddies.entity.Order;
import com.boozebuddies.model.DeliveryStatus;
import org.springframework.stereotype.Component;

/** Mapper for converting between Delivery entities and DeliveryDTO objects. */
@Component
public class DeliveryMapper {

  /**
   * Converts a Delivery entity to a DeliveryDTO.
   *
   * @param delivery the delivery entity to convert
   * @return the DeliveryDTO, or null if the input is null
   */
  public DeliveryDTO toDTO(Delivery delivery) {
    if (delivery == null) return null;

    return DeliveryDTO.builder()
        .id(delivery.getId())
        .orderId(delivery.getOrder() != null ? delivery.getOrder().getId() : null)
        .driverId(delivery.getDriver() != null ? delivery.getDriver().getId() : null)
        .status(delivery.getStatus().name())

        // Delivery location
        .deliveryAddress(delivery.getDeliveryAddress())
        .deliveryLatitude(delivery.getDeliveryLatitude())
        .deliveryLongitude(delivery.getDeliveryLongitude())

        // Lifecycle timestamps
        .pickupTime(delivery.getPickupTime())
        .deliveredTime(delivery.getDeliveredTime())
        .estimatedDeliveryTime(delivery.getEstimatedDeliveryTime())

        // Driver info
        .driverName(delivery.getDriver() != null ? delivery.getDriver().getName() : null)
        .driverPhone(delivery.getDriver() != null ? delivery.getDriver().getPhone() : null)

        // Age verification (critical for alcohol delivery compliance)
        .ageVerified(delivery.getAgeVerified())
        .idType(delivery.getIdType())
        .idNumber(delivery.getIdNumber()) // Already last 4 digits from service layer
        .ageVerifiedAt(delivery.getAgeVerifiedAt())

        // Real-time tracking
        .currentLatitude(delivery.getCurrentLatitude())
        .currentLongitude(delivery.getCurrentLongitude())
        .lastLocationUpdate(delivery.getLastLocationUpdate())

        // Cancellation
        .cancellationReason(delivery.getCancellationReason())

        // Audit timestamps
        .createdAt(delivery.getCreatedAt())
        .updatedAt(delivery.getUpdatedAt())

        // Optional: tracking URL (implement later if needed)
        // .trackingUrl(delivery.getTrackingUrl())
        .build();
  }

  /**
   * Converts a DeliveryDTO to a Delivery entity.
   *
   * @param dto the DeliveryDTO to convert
   * @return the Delivery entity, or null if the input is null
   */
  public Delivery toEntity(DeliveryDTO dto) {
    if (dto == null) return null;

    Delivery delivery = new Delivery();
    delivery.setId(dto.getId());
    delivery.setStatus(DeliveryStatus.valueOf(dto.getStatus()));
    delivery.setDeliveryAddress(dto.getDeliveryAddress());
    delivery.setDeliveryLatitude(dto.getDeliveryLatitude());
    delivery.setDeliveryLongitude(dto.getDeliveryLongitude());
    delivery.setPickupTime(dto.getPickupTime());
    delivery.setDeliveredTime(dto.getDeliveredTime());
    delivery.setEstimatedDeliveryTime(dto.getEstimatedDeliveryTime());
    delivery.setCancellationReason(dto.getCancellationReason());
    delivery.setUpdatedAt(dto.getUpdatedAt());

    if (dto.getOrderId() != null) {
      Order order = new Order();
      order.setId(dto.getOrderId());
      delivery.setOrder(order);
    }

    if (dto.getDriverId() != null) {
      Driver driver = new Driver();
      driver.setId(dto.getDriverId());
      driver.setName(dto.getDriverName());
      driver.setPhone(dto.getDriverPhone());
      delivery.setDriver(driver);
    }

    return delivery;
  }
}
