package com.boozebuddies.service.implementation;

import com.boozebuddies.entity.Delivery;
import com.boozebuddies.entity.Driver;
import com.boozebuddies.entity.Merchant;
import com.boozebuddies.entity.User;
import com.boozebuddies.service.NotificationService;
import org.springframework.stereotype.Service;

/**
 * Implementation of the {@link NotificationService} that handles sending notifications to users,
 * drivers, and merchants. In this implementation, notifications are simulated through console
 * output, but it can be extended to integrate with real systems such as email, SMS, or push
 * notifications.
 *
 * <p>This service supports:
 *
 * <ul>
 *   <li>Notifying individual users, drivers, and merchants
 *   <li>Sending delivery status updates
 *   <li>Broadcasting global system messages
 *   <li>Convenience methods for order confirmation and cancellation
 * </ul>
 */
@Service
public class NotificationServiceImpl implements NotificationService {

  /**
   * Sends a general notification message to a user.
   *
   * @param user the {@link User} recipient of the notification
   * @param message the message content to send
   */
  @Override
  public void notifyUser(User user, String message) {
    if (user != null) {
      System.out.println("[USER NOTIFICATION] To: " + user.getEmail() + " | Message: " + message);
    }
  }

  /**
   * Sends a notification message to a driver related to a specific delivery.
   *
   * @param driver the {@link Driver} recipient
   * @param delivery the {@link Delivery} associated with the notification, may be {@code null}
   * @param message the notification message
   */
  @Override
  public void notifyDriver(Driver driver, Delivery delivery, String message) {
    if (driver != null) {
      System.out.println(
          "[DRIVER NOTIFICATION] To Driver ID: "
              + driver.getId()
              + " | Delivery ID: "
              + (delivery != null ? delivery.getId() : "N/A")
              + " | Message: "
              + message);
    }
  }

  /**
   * Sends a notification message to a merchant.
   *
   * @param merchant the {@link Merchant} recipient
   * @param message the message content
   */
  @Override
  public void notifyMerchant(Merchant merchant, String message) {
    if (merchant != null) {
      System.out.println(
          "[MERCHANT NOTIFICATION] To Merchant ID: " + merchant.getId() + " | Message: " + message);
    }
  }

  /**
   * Sends a delivery status update to a user, indicating the current state of their delivery.
   *
   * @param user the {@link User} receiving the update
   * @param delivery the {@link Delivery} whose status has changed
   */
  @Override
  public void sendDeliveryStatusUpdate(User user, Delivery delivery) {
    if (user != null && delivery != null) {
      String statusMessage =
          "[DELIVERY STATUS UPDATE] To: "
              + user.getEmail()
              + " | Delivery ID: "
              + delivery.getId()
              + " | Status: "
              + delivery.getStatus();
      System.out.println(statusMessage);
    }
  }

  /**
   * Broadcasts a system-wide message to all users or listeners.
   *
   * <p>This is typically used for administrative or platform-wide announcements.
   *
   * @param message the broadcast message
   */
  @Override
  public void broadcastSystemMessage(String message) {
    System.out.println("[SYSTEM BROADCAST] " + message);
  }

  /**
   * Sends confirmation notifications when a new order has been placed.
   *
   * <p>Notifies both the user who placed the order and the merchant receiving it.
   *
   * @param delivery the {@link Delivery} associated with the confirmed order
   */
  public void sendOrderConfirmation(Delivery delivery) {
    if (delivery != null && delivery.getOrder() != null) {
      notifyUser(delivery.getOrder().getUser(), "Your order has been confirmed!");
      notifyMerchant(delivery.getOrder().getMerchant(), "A new order has been placed.");
    }
  }

  /**
   * Sends cancellation notifications when an order has been cancelled.
   *
   * <p>Notifies both the user and the merchant involved in the order.
   *
   * @param delivery the {@link Delivery} associated with the cancelled order
   */
  public void sendOrderCancellation(Delivery delivery) {
    if (delivery != null && delivery.getOrder() != null) {
      notifyUser(delivery.getOrder().getUser(), "Your order has been cancelled.");
      notifyMerchant(delivery.getOrder().getMerchant(), "An order has been cancelled.");
    }
  }
}
