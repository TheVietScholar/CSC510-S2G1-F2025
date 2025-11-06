package com.boozebuddies.service;

import com.boozebuddies.entity.Delivery;
import com.boozebuddies.entity.Driver;
import com.boozebuddies.entity.Merchant;
import com.boozebuddies.entity.User;

/**
 * Service responsible for managing and sending notifications to users, drivers, and merchants via
 * various channels such as email, SMS, or in-app messages.
 */
public interface NotificationService {

  /**
   * Sends a general notification to a user (email, SMS, or in-app).
   *
   * @param user The recipient of the notification.
   * @param message The message content.
   */
  void notifyUser(User user, String message);

  /**
   * Sends a notification to a driver regarding a delivery assignment or status update.
   *
   * @param driver The driver receiving the notification.
   * @param delivery The delivery related to the notification.
   * @param message The message content.
   */
  void notifyDriver(Driver driver, Delivery delivery, String message);

  /**
   * Notifies a merchant about a new or updated order.
   *
   * @param merchant The merchant receiving the notification.
   * @param message The message content.
   */
  void notifyMerchant(Merchant merchant, String message);

  /**
   * Sends a delivery status update to the customer.
   *
   * @param user The customer receiving the update.
   * @param delivery The delivery whose status changed.
   */
  void sendDeliveryStatusUpdate(User user, Delivery delivery);

  /**
   * Broadcasts a system-wide announcement (e.g., maintenance notice or promotional offer) to all
   * relevant users on the platform.
   *
   * @param message The announcement message.
   */
  void broadcastSystemMessage(String message);

  /**
   * Sends an order confirmation notification to the customer after the order has been placed
   * successfully.
   *
   * @param delivery The delivery associated with the confirmed order.
   */
  void sendOrderConfirmation(Delivery delivery);

  /**
   * Sends an order cancellation notification to the customer and updates all relevant parties.
   *
   * @param delivery The delivery associated with the canceled order.
   */
  void sendOrderCancellation(Delivery delivery);
}
