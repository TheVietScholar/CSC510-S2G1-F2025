package com.boozebuddies.service.implementation;

import com.boozebuddies.entity.Delivery;
import com.boozebuddies.entity.Driver;
import com.boozebuddies.entity.Merchant;
import com.boozebuddies.entity.User;
import com.boozebuddies.service.NotificationService;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {

  @Override
  public void notifyUser(User user, String message) {
    if (user != null) {
      System.out.println("[USER NOTIFICATION] To: " + user.getEmail() + " | Message: " + message);
    }
  }

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

  @Override
  public void notifyMerchant(Merchant merchant, String message) {
    if (merchant != null) {
      System.out.println(
          "[MERCHANT NOTIFICATION] To Merchant ID: " + merchant.getId() + " | Message: " + message);
    }
  }

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

  @Override
  public void broadcastSystemMessage(String message) {
    System.out.println("[SYSTEM BROADCAST] " + message);
  }

  /** Convenience methods for common order notifications. */
  public void sendOrderConfirmation(Delivery delivery) {
    if (delivery != null && delivery.getOrder() != null) {
      notifyUser(delivery.getOrder().getUser(), "Your order has been confirmed!");
      notifyMerchant(delivery.getOrder().getMerchant(), "A new order has been placed.");
    }
  }

  public void sendOrderCancellation(Delivery delivery) {
    if (delivery != null && delivery.getOrder() != null) {
      notifyUser(delivery.getOrder().getUser(), "Your order has been cancelled.");
      notifyMerchant(delivery.getOrder().getMerchant(), "An order has been cancelled.");
    }
  }
}
