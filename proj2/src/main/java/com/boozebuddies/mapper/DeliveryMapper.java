package com.boozebuddies.mapper;

import com.boozebuddies.dto.DeliveryDTO;
import com.boozebuddies.entity.Delivery;
import org.springframework.stereotype.Component;

@Component
public class DeliveryMapper {

  public DeliveryDTO toDTO(Delivery delivery) {
    if (delivery == null) return null;

    return DeliveryDTO.builder()
        .id(delivery.getId())
        .orderId(delivery.getOrder() != null ? delivery.getOrder().getId() : null)
        .driverId(delivery.getDriver() != null ? delivery.getDriver().getId() : null)
        .status(delivery.getStatus().name())
        .deliveryAddress(delivery.getDeliveryAddress())
        .deliveryLatitude(delivery.getDeliveryLatitude())
        .deliveryLongitude(delivery.getDeliveryLongitude())
        .cancellationReason(delivery.getCancellationReason())
        .pickupTime(delivery.getPickupTime())
        .deliveredTime(delivery.getDeliveredTime())
        .estimatedDeliveryTime(delivery.getEstimatedDeliveryTime())
        .driverName(delivery.getDriver() != null ? delivery.getDriver().getName() : null)
        .driverPhone(delivery.getDriver() != null ? delivery.getDriver().getPhone() : null)
        // .trackingUrl(delivery.getTrackingUrl())
        .build();
  }
}
