package com.boozebuddies.mapper;

import com.boozebuddies.dto.CreateOrderRequest;
import com.boozebuddies.dto.DriverOrderDTO;
import com.boozebuddies.dto.OrderDTO;
import com.boozebuddies.dto.OrderItemDTO;
import com.boozebuddies.dto.OrderItemRequest;
import com.boozebuddies.entity.Merchant;
import com.boozebuddies.entity.Order;
import com.boozebuddies.entity.OrderItem;
import com.boozebuddies.entity.Product;
import com.boozebuddies.entity.User;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/** Mapper for converting between Order entities and Order-related DTO objects. */
@Component
public class OrderMapper {

  /**
   * Converts an Order entity to an OrderDTO.
   *
   * @param order the order entity to convert
   * @return the OrderDTO, or null if the input is null
   */
  public OrderDTO toDTO(Order order) {
    if (order == null) return null;

    return OrderDTO.builder()
        .id(order.getId())
        .userId(order.getUser() != null ? order.getUser().getId() : null)
        .merchantId(order.getMerchant() != null ? order.getMerchant().getId() : null)
        .driverId(order.getDriver() != null ? order.getDriver().getId() : null)
        .totalAmount(order.getTotalAmount())
        .status(order.getStatus().name())
        .deliveryAddress(order.getDeliveryAddress())
        .items(
            order.getItems() != null
                ? order.getItems().stream().map(this::orderItemToDTO).collect(Collectors.toList())
                : null)
        .createdAt(order.getCreatedAt())
        .updatedAt(order.getUpdatedAt())
        .estimatedDeliveryTime(order.getEstimatedDeliveryTime())
        .build();
  }

  /**
   * Converts a CreateOrderRequest to an Order entity.
   *
   * @param request the CreateOrderRequest to convert
   * @return the Order entity, or null if the input is null
   */
  public Order toEntity(CreateOrderRequest request) {
    if (request == null) return null;

    Order order = new Order();

    // Set basic fields
    order.setDeliveryAddress(request.getDeliveryAddress());
    order.setSpecialInstructions(request.getSpecialInstructions());

    // Set user (just ID - service will fetch full entity)
    if (request.getUserId() != null) {
      User user = new User();
      user.setId(request.getUserId());
      order.setUser(user);
    }

    // Set merchant (just ID - service will fetch full entity)
    if (request.getMerchantId() != null) {
      Merchant merchant = new Merchant();
      merchant.setId(request.getMerchantId());
      order.setMerchant(merchant);
    }

    // Convert order items
    if (request.getItems() != null) {
      order.setItems(
          request.getItems().stream()
              .map(this::orderItemRequestToEntity)
              .collect(Collectors.toList()));
    }

    return order;
  }

  /**
   * Convert Order to DriverOrderDTO with distance and ETA calculations.
   *
   * @param order The order entity
   * @param distanceKm Distance from driver to merchant in kilometers (calculated externally)
   * @return DriverOrderDTO with distance and ETA
   */
  public DriverOrderDTO toDriverDTO(Order order, Double distanceKm) {
    if (order == null) return null;

    // Calculate ETA: assume average speed of 30 km/h in urban areas
    // ETA = distance / speed * 60 (convert to minutes)
    // Add 5 minutes for pickup time
    Integer etaMin = null;
    if (distanceKm != null) {
      etaMin = (int) Math.ceil((distanceKm / 30.0) * 60) + 5;
    }

    return DriverOrderDTO.builder()
        .id(order.getId())
        .userId(order.getUser() != null ? order.getUser().getId() : null)
        .merchantId(order.getMerchant() != null ? order.getMerchant().getId() : null)
        .merchantName(order.getMerchant() != null ? order.getMerchant().getName() : null)
        .customerName(order.getUser() != null ? order.getUser().getName() : null)
        .deliveryAddress(order.getDeliveryAddress())
        .totalAmount(order.getTotalAmount())
        .status(order.getStatus().name())
        .items(
            order.getItems() != null
                ? order.getItems().stream().map(this::orderItemToDTO).collect(Collectors.toList())
                : null)
        .distanceKm(distanceKm)
        .etaMin(etaMin)
        .createdAt(order.getCreatedAt())
        .updatedAt(order.getUpdatedAt())
        .estimatedDeliveryTime(order.getEstimatedDeliveryTime())
        .build();
  }

  private OrderItemDTO orderItemToDTO(OrderItem orderItem) {
    if (orderItem == null) return null;

    // Use snapshot name if available, otherwise try to get from product
    String productName = orderItem.getName();
    if (productName == null && orderItem.getProduct() != null) {
      productName = orderItem.getProduct().getName();
    }

    return OrderItemDTO.builder()
        .id(orderItem.getId())
        .orderId(orderItem.getOrder() != null ? orderItem.getOrder().getId() : null)
        .productId(orderItem.getProduct() != null ? orderItem.getProduct().getId() : null)
        .productName(productName)
        .quantity(orderItem.getQuantity())
        .unitPrice(orderItem.getUnitPrice())
        .subtotal(orderItem.getSubtotal())
        .build();
  }

  /**
   * Converts an OrderItemRequest to an OrderItem entity.
   *
   * @param request the OrderItemRequest to convert
   * @return the OrderItem entity, or null if the input is null
   */
  private OrderItem orderItemRequestToEntity(OrderItemRequest request) {
    if (request == null) return null;

    OrderItem orderItem = new OrderItem();
    orderItem.setQuantity(request.getQuantity());
    orderItem.setUnitPrice(request.getUnitPrice());

    // Set product reference (just ID - service will fetch full entity)
    if (request.getProductId() != null) {
      Product product = new Product();
      product.setId(request.getProductId());
      orderItem.setProduct(product);
    }

    return orderItem;
  }
}
