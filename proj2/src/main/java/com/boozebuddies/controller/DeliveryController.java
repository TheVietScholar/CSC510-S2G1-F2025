package com.boozebuddies.controller;

import com.boozebuddies.dto.ApiResponse;
import com.boozebuddies.dto.DeliveryDTO;
import com.boozebuddies.entity.Delivery;
import com.boozebuddies.entity.Driver;
import com.boozebuddies.entity.Order;
import com.boozebuddies.mapper.DeliveryMapper;
import com.boozebuddies.model.DeliveryStatus;
import com.boozebuddies.service.DeliveryService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

  private final DeliveryService deliveryService;
  private final DeliveryMapper deliveryMapper;

  /** Assign a driver to an order and create delivery record */
  @PostMapping("/assign")
  public ResponseEntity<ApiResponse<DeliveryDTO>> assignDriverToOrder(
      @RequestParam Long orderId, @RequestParam Long driverId) {
    try {
      // In a real implementation, you'd fetch Order and Driver entities from
      // repositories
      Order order = new Order();
      order.setId(orderId);

      Driver driver = new Driver();
      driver.setId(driverId);

      Delivery delivery = deliveryService.assignDriverToOrder(order, driver);
      DeliveryDTO deliveryDTO = deliveryMapper.toDTO(delivery);

      return ResponseEntity.ok(ApiResponse.success(deliveryDTO, "Driver assigned successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to assign driver: " + e.getMessage()));
    }
  }

  /** Update delivery status */
  @PutMapping("/{deliveryId}/status")
  public ResponseEntity<ApiResponse<DeliveryDTO>> updateDeliveryStatus(
      @PathVariable Long deliveryId, @RequestParam DeliveryStatus status) {
    try {
      Delivery delivery = deliveryService.updateDeliveryStatus(deliveryId, status);
      if (delivery == null) {
        return ResponseEntity.notFound().build();
      }
      DeliveryDTO deliveryDTO = deliveryMapper.toDTO(delivery);
      return ResponseEntity.ok(
          ApiResponse.success(deliveryDTO, "Delivery status updated successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to update delivery status: " + e.getMessage()));
    }
  }

  /** Cancel a delivery with reason */
  @PostMapping("/{deliveryId}/cancel")
  public ResponseEntity<ApiResponse<DeliveryDTO>> cancelDelivery(
      @PathVariable Long deliveryId, @RequestParam String reason) {
    try {
      Delivery delivery = deliveryService.cancelDelivery(deliveryId, reason);
      if (delivery == null) {
        return ResponseEntity.notFound().build();
      }
      DeliveryDTO deliveryDTO = deliveryMapper.toDTO(delivery);
      return ResponseEntity.ok(ApiResponse.success(deliveryDTO, "Delivery cancelled successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to cancel delivery: " + e.getMessage()));
    }
  }

  /** Get all deliveries for a specific driver */
  @GetMapping("/driver/{driverId}")
  public ResponseEntity<ApiResponse<List<DeliveryDTO>>> getDeliveriesByDriver(
      @PathVariable Long driverId) {
    try {
      List<Delivery> deliveries = deliveryService.getDeliveriesByDriver(driverId);
      List<DeliveryDTO> deliveryDTOs =
          deliveries.stream().map(deliveryMapper::toDTO).collect(Collectors.toList());
      return ResponseEntity.ok(
          ApiResponse.success(deliveryDTOs, "Deliveries retrieved successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to retrieve deliveries: " + e.getMessage()));
    }
  }

  /** Get delivery by ID */
  @GetMapping("/{deliveryId}")
  public ResponseEntity<ApiResponse<DeliveryDTO>> getDeliveryById(@PathVariable Long deliveryId) {
    try {
      Delivery delivery = deliveryService.getDeliveryById(deliveryId);
      if (delivery == null) {
        return ResponseEntity.notFound().build();
      }
      DeliveryDTO deliveryDTO = deliveryMapper.toDTO(delivery);
      return ResponseEntity.ok(ApiResponse.success(deliveryDTO, "Delivery retrieved successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to retrieve delivery: " + e.getMessage()));
    }
  }

  /** Get all active deliveries */
  @GetMapping("/active")
  public ResponseEntity<ApiResponse<List<DeliveryDTO>>> getActiveDeliveries() {
    try {
      List<Delivery> activeDeliveries = deliveryService.getActiveDeliveries();
      List<DeliveryDTO> deliveryDTOs =
          activeDeliveries.stream().map(deliveryMapper::toDTO).collect(Collectors.toList());
      return ResponseEntity.ok(
          ApiResponse.success(deliveryDTOs, "Active deliveries retrieved successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to retrieve active deliveries: " + e.getMessage()));
    }
  }
}
