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

/**
 * Implementation of the {@link OrderService} that handles business logic for order creation,
 * retrieval, updates, and cancellation. This service integrates with payment, delivery, and
 * notification components to ensure end-to-end order management.
 *
 * <p>Responsibilities include:
 * <ul>
 *   <li>Validating order creation and updating timestamps</li>
 *   <li>Managing order lifecycle transitions</li>
 *   <li>Handling payment processing and refunds</li>
 *   <li>Creating and updating delivery records</li>
 *   <li>Sending relevant notifications to users and merchants</li>
 * </ul>
 */
@Service
public class OrderServiceImpl implements OrderService {

  @Autowired private OrderRepository orderRepository;
  @Autowired private DeliveryRepository deliveryRepository;
  @Autowired private PaymentService paymentService;
  @Autowired private NotificationService notificationService;

  /**
   * Creates a new order, processes payment, generates a delivery record, and sends confirmation
   * notifications.
   *
   * @param order the order to create
   * @return the persisted order with associated delivery details
   * @throws RuntimeException if validation fails (e.g., missing user, merchant, or items)
   */
  @Transactional
  public Order createOrder(Order order) {
    validateOrderCreation(order);
    order.setStatus(OrderStatus.PENDING);
    order.setCreatedAt(LocalDateTime.now());
    order.setUpdatedAt(LocalDateTime.now());

    if (order.getTotalAmount() == null) {
      order.calculateTotal();
    }

    Order savedOrder = orderRepository.save(order);
    paymentService.processPayment(savedOrder, null);
    Delivery delivery = createDeliveryRecord(savedOrder);
    notificationService.sendOrderConfirmation(delivery);
    return savedOrder;
  }

  /**
   * Retrieves an order by its unique identifier.
   *
   * @param id the order ID
   * @return an {@link Optional} containing the order if found
   */
  public Optional<Order> getOrderById(Long id) {
    return orderRepository.findById(id);
  }

  /**
   * Retrieves all orders placed by a specific user.
   *
   * @param userId the user's ID
   * @return list of orders associated with the user
   */
  public List<Order> getOrdersByUser(Long userId) {
    return orderRepository.findByCustomerId(userId);
  }

  /**
   * Retrieves all orders belonging to a merchant.
   *
   * @param merchantId the merchant's ID
   * @return list of orders associated with the merchant
   */
  public List<Order> getOrdersByMerchant(Long merchantId) {
    return orderRepository.findByMerchantId(merchantId);
  }

  /**
   * Retrieves all orders assigned to a specific delivery driver.
   *
   * @param driverId the driver’s ID
   * @return list of orders handled by the driver
   */
  public List<Order> getOrdersByDriver(Long driverId) {
    return orderRepository.findByDriverId(driverId);
  }

  /**
   * Retrieves all orders in the system.
   *
   * @return list of all orders
   */
  public List<Order> getAllOrders() {
    return orderRepository.findAll();
  }

  /**
   * Cancels an existing order, issues a refund if applicable, and sends cancellation notifications.
   *
   * @param orderId the ID of the order to cancel
   * @return the updated order with {@link OrderStatus#CANCELLED} status
   * @throws RuntimeException if the order cannot be cancelled or does not exist
   */
  @Transactional
  public Order cancelOrder(Long orderId) {
    Order order =
        orderRepository
            .findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));

    if (!order.canBeCancelled()) {
      throw new RuntimeException(
          "Order cannot be cancelled in current status: " + order.getStatus());
    }

    order.setStatus(OrderStatus.CANCELLED);
    order.setUpdatedAt(LocalDateTime.now());

    Order cancelledOrder = orderRepository.save(order);
    paymentService.refundPayment(cancelledOrder, "Order cancelled by user");
    notificationService.sendOrderCancellation(cancelledOrder.getDelivery());

    return cancelledOrder;
  }

  /**
   * Updates the status of an existing order and performs any necessary side-effects such as
   * notifications or payment actions.
   *
   * @param orderId the ID of the order to update
   * @param status the new order status (as a string)
   * @return the updated order
   * @throws RuntimeException if the order is not found or status transition is invalid
   */
  @Transactional
  public Order updateOrderStatus(Long orderId, String status) {
    Order order =
        orderRepository
            .findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));

    OrderStatus newStatus = OrderStatus.valueOf(status.toUpperCase());

    if (!order.isValidStatusTransition(newStatus)) {
      throw new RuntimeException(
          "Invalid status transition from " + order.getStatus() + " to " + newStatus);
    }

    order.setStatus(newStatus);
    order.setUpdatedAt(LocalDateTime.now());

    Order updatedOrder = orderRepository.save(order);
    handleStatusChange(updatedOrder, newStatus);
    return updatedOrder;
  }

  /**
   * Validates business rules before creating a new order.
   *
   * @param order the order to validate
   * @throws RuntimeException if required fields are missing or validation fails
   */
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

    boolean hasAlcohol =
        order.getItems().stream()
            .anyMatch(item -> item.getProduct() != null && item.getProduct().isAlcohol());

    if (hasAlcohol && !order.getUser().isAgeVerified()) {
      throw new RuntimeException("User must be age verified for alcohol orders");
    }
  }

  /**
   * Creates a corresponding delivery record for a newly placed order.
   *
   * @param order the order for which delivery will be created
   * @return the created {@link Delivery} entity
   */
  private Delivery createDeliveryRecord(Order order) {
    Delivery delivery = new Delivery();
    delivery.setOrder(order);
    delivery.setStatus(com.boozebuddies.model.DeliveryStatus.PENDING);
    delivery.setDeliveryAddress(order.getDeliveryAddress());
    delivery.setCreatedAt(LocalDateTime.now());
    deliveryRepository.save(delivery);
    return delivery;
  }

  /**
   * Handles business logic and notifications based on a change in order status.
   *
   * @param order the order whose status has changed
   * @param newStatus the new {@link OrderStatus}
   */
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
