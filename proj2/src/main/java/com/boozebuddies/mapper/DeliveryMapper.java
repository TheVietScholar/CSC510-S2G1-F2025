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
}