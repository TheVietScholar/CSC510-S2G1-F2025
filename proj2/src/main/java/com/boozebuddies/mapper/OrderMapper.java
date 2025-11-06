package com.boozebuddies.mapper;

import com.boozebuddies.dto.CreateOrderRequest;
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

@Component
public class OrderMapper {

  public OrderDTO toDTO(Order order) {
    if (order == null)
      return null;

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

  public Order toEntity(CreateOrderRequest request) {
    if (request == null)
      return null;

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

  private OrderItemDTO orderItemToDTO(OrderItem orderItem) {
    if (orderItem == null)
      return null;

    return OrderItemDTO.builder()
        .id(orderItem.getId())
        .productId(orderItem.getProduct() != null ? orderItem.getProduct().getId() : null)
        .productName(orderItem.getProduct() != null ? orderItem.getProduct().getName() : null)
        .quantity(orderItem.getQuantity())
        .unitPrice(orderItem.getUnitPrice())
        .subtotal(orderItem.getSubtotal())
        .build();
  }

  private OrderItem orderItemRequestToEntity(OrderItemRequest request) {
    if (request == null)
      return null;

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
