package com.boozebuddies.service;

import com.boozebuddies.model.User;
import com.boozebuddies.model.Driver;
import com.boozebuddies.model.Merchant;
import com.boozebuddies.model.Delivery;

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
     * Broadcasts a system-wide announcement (e.g., maintenance notice or promo).
     *
     * @param message The announcement message.
     */
    void broadcastSystemMessage(String message);
}
