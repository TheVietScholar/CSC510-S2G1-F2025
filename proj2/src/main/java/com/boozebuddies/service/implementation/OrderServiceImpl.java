package com.boozebuddies.service.implementation;

import com.boozebuddies.entity.Delivery;
import com.boozebuddies.entity.Order;
import com.boozebuddies.model.OrderStatus;
import com.boozebuddies.repository.DeliveryRepository;
import com.boozebuddies.repository.OrderRepository;
import com.boozebuddies.service.NotificationService;
import com.boozebuddies.service.OrderService;
import com.boozebuddies.service.PaymentService;
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

  @Transactional
  public Order createOrder(Order order) {
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

    // Process payment
    paymentService.processPayment(savedOrder, null);

    // Create delivery record
    Delivery delivery = createDeliveryRecord(savedOrder);

    // Notify merchant and user
    notificationService.sendOrderConfirmation(delivery);

    return savedOrder;
  }

  public Optional<Order> getOrderById(Long id) {
    return orderRepository.findById(id);
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

    if (hasAlcohol && !order.getUser().isAgeVerified()) {
      throw new RuntimeException("User must be age verified for alcohol orders");
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
}
