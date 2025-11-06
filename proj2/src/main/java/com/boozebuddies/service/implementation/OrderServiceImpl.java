package com.boozebuddies.service.implementation;

import com.boozebuddies.entity.Delivery;
import com.boozebuddies.entity.Order;
import com.boozebuddies.entity.OrderItem;
import com.boozebuddies.entity.Product;
import com.boozebuddies.entity.User;
import com.boozebuddies.model.OrderStatus;
import com.boozebuddies.repository.DeliveryRepository;
import com.boozebuddies.repository.OrderRepository;
import com.boozebuddies.service.NotificationService;
import com.boozebuddies.service.OrderService;
import com.boozebuddies.service.PaymentService;
import com.boozebuddies.service.ProductService;
import com.boozebuddies.service.UserService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderServiceImpl implements OrderService {

  @Autowired private OrderRepository orderRepository;

  @Autowired private DeliveryRepository deliveryRepository;

  @Autowired private PaymentService paymentService;

  @Autowired private NotificationService notificationService;

  @Autowired private ProductService productService;

  @Autowired private UserService userService;

  @Transactional
  public Order createOrder(Order order) {
    // Initialize order items: fetch products, set names, link to order, calculate
    // subtotals
    initializeOrderItems(order);

    // Validate business rules
    validateOrderCreation(order);

    // Set initial status and timestamps
    order.setStatus(OrderStatus.PENDING);
    order.setCreatedAt(LocalDateTime.now());
    order.setUpdatedAt(LocalDateTime.now());

    // Calculate total if not set
    if (order.getTotalAmount() == null) {
      order.calculateTotal();
    }

    // Save order
    Order savedOrder = orderRepository.save(order);

    // Process payment with test payment method (for testing purposes)
    paymentService.processPayment(savedOrder, "test_payment");

    // Create delivery record
    Delivery delivery = createDeliveryRecord(savedOrder);

    // Notify merchant and user
    notificationService.sendOrderConfirmation(delivery);

    return savedOrder;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Order> getOrderById(Long id) {
    // Use query that eagerly loads relationships for permission checks
    Optional<Order> orderOpt = orderRepository.findByIdWithRelationships(id);
    // Fallback to standard findById if the custom query doesn't work
    return orderOpt.isPresent() ? orderOpt : orderRepository.findById(id);
  }

  public List<Order> getOrdersByUser(Long userId) {
    return orderRepository.findByCustomerId(userId);
  }

  public List<Order> getOrdersByMerchant(Long merchantId) {
    return orderRepository.findByMerchantId(merchantId);
  }

  public List<Order> getOrdersByDriver(Long driverId) {
    return orderRepository.findByDriverId(driverId);
  }

  public List<Order> getAllOrders() {
    return orderRepository.findAll();
  }

  @Transactional
  public Order cancelOrder(Long orderId) {
    Order order =
        orderRepository
            .findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));

    // Check if order can be cancelled
    if (!order.canBeCancelled()) {
      throw new RuntimeException(
          "Order cannot be cancelled in current status: " + order.getStatus());
    }

    // Update status
    order.setStatus(OrderStatus.CANCELLED);
    order.setUpdatedAt(LocalDateTime.now());

    Order cancelledOrder = orderRepository.save(order);

    // Process refund if payment was made
    paymentService.refundPayment(cancelledOrder, "Order cancelled by user");

    // Notify user and merchant
    notificationService.sendOrderCancellation(cancelledOrder.getDelivery());

    return cancelledOrder;
  }

  @Transactional
  public Order updateOrderStatus(Long orderId, String status) {
    Order order =
        orderRepository
            .findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));

    OrderStatus newStatus = OrderStatus.valueOf(status.toUpperCase());

    // Validate status transition
    if (!order.isValidStatusTransition(newStatus)) {
      throw new RuntimeException(
          "Invalid status transition from " + order.getStatus() + " to " + newStatus);
    }

    order.setStatus(newStatus);
    order.setUpdatedAt(LocalDateTime.now());

    Order updatedOrder = orderRepository.save(order);

    // Handle status-specific logic
    handleStatusChange(updatedOrder, newStatus);

    return updatedOrder;
  }

  private void initializeOrderItems(Order order) {
    if (order.getItems() == null || order.getItems().isEmpty()) {
      return;
    }

    // Initialize each order item with product details
    for (int i = 0; i < order.getItems().size(); i++) {
      OrderItem item = order.getItems().get(i);

      // Set line number
      item.setLineNo(i + 1);

      // Link item to order
      item.setOrder(order);

      // Fetch and set product if productId is available
      if (item.getProduct() != null && item.getProduct().getId() != null) {
        Product product = productService.getProductById(item.getProduct().getId());
        if (product != null) {
          item.setProduct(product);
          // Set name from product (required field)
          if (item.getName() == null) {
            item.setName(product.getName());
          }
        } else {
          throw new RuntimeException("Product not found with id: " + item.getProduct().getId());
        }
      }

      // Ensure unitPrice is set (from request or product)
      if (item.getUnitPrice() == null && item.getProduct() != null) {
        item.setUnitPrice(item.getProduct().getPrice());
      }

      // Calculate subtotal explicitly (before @PrePersist runs)
      if (item.getUnitPrice() != null && item.getQuantity() != null) {
        item.setSubtotal(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
      } else {
        throw new RuntimeException(
            "Order item missing required fields: unitPrice="
                + item.getUnitPrice()
                + ", quantity="
                + item.getQuantity());
      }
    }
  }

  private void validateOrderCreation(Order order) {
    if (order.getUser() == null) {
      throw new RuntimeException("User is required");
    }

    if (order.getMerchant() == null) {
      throw new RuntimeException("Merchant is required");
    }

    if (order.getItems() == null || order.getItems().isEmpty()) {
      throw new RuntimeException("Order must contain at least one item");
    }

    // Check if user is age verified for alcohol products
    boolean hasAlcohol =
        order.getItems().stream()
            .anyMatch(item -> item.getProduct() != null && item.getProduct().isAlcohol());

    if (hasAlcohol) {
      // Fetch fresh user data from database to ensure we have latest age verification
      // status
      User user = userService.findById(order.getUser().getId());
      if (!user.isAgeVerified()) {
        throw new RuntimeException("User must be age verified for alcohol orders");
      }
    }
  }

  private Delivery createDeliveryRecord(Order order) {
    Delivery delivery = new Delivery();
    delivery.setOrder(order);
    delivery.setStatus(com.boozebuddies.model.DeliveryStatus.PENDING);
    delivery.setDeliveryAddress(order.getDeliveryAddress());
    delivery.setCreatedAt(LocalDateTime.now());
    deliveryRepository.save(delivery);
    return delivery;
  }

  private void handleStatusChange(Order order, OrderStatus newStatus) {
    switch (newStatus) {
      case CONFIRMED:
        notificationService.sendDeliveryStatusUpdate(order.getUser(), order.getDelivery());
        break;
      case PREPARING:
        // notificationService.sendOrderPreparing(order);
        break;
      case READY_FOR_PICKUP:
        // notificationService.sendOrderReady(order);
        break;
      case COMPLETED:
        // paymentService.capturePayment(order);
        // notificationService.sendOrderCompleted(order);
        break;
      case CANCELLED:
        // notificationService.sendOrderCancellation(order);
        break;
    }
  }

  @Override
  public List<Order> getOrdersWithinDistance(double latitude, double longitude, double distanceKm) {
    List<Order> availOrders = orderRepository.findAvailableForAssignment();

    for (Order order : availOrders) {
      if (calculateDistance(
              latitude,
              longitude,
              order.getMerchant().getLatitude(),
              order.getMerchant().getLongitude())
          > distanceKm) {
        availOrders.remove(order);
      }
    }

    return availOrders;
  }

  /**
   * Calculate distance between two points using Haversine formula. Returns distance in kilometers.
   */
  private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
    final int EARTH_RADIUS_KM = 6371;

    double dLat = Math.toRadians(lat2 - lat1);
    double dLon = Math.toRadians(lon2 - lon1);

    double a =
        Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);

    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    return EARTH_RADIUS_KM * c;
  }

  
}
