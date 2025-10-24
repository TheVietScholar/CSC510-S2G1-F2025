package com.boozebuddies.service.implementation;

import static org.junit.jupiter.api.Assertions.*;

import com.boozebuddies.entity.Delivery;
import com.boozebuddies.entity.Driver;
import com.boozebuddies.entity.Merchant;
import com.boozebuddies.entity.Order;
import com.boozebuddies.entity.User;
import com.boozebuddies.model.DeliveryStatus;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class NotificationServiceImplTest {

  private NotificationServiceImpl notificationService;
  private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

  @BeforeEach
  void setUp() {
    notificationService = new NotificationServiceImpl();
    System.setOut(new PrintStream(outputStream));
  }

  @Test
  void testNotifyUser_ValidUser_PrintsUserNotification() {
    User user = User.builder().email("customer@example.com").build();

    notificationService.notifyUser(user, "Test message");

    String output = outputStream.toString();
    assertTrue(output.contains("[USER NOTIFICATION]"));
    assertTrue(output.contains("customer@example.com"));
    assertTrue(output.contains("Test message"));
  }

  @Test
  void testNotifyUser_NullUser_NoOutput() {
    notificationService.notifyUser(null, "Test message");

    String output = outputStream.toString();
    assertEquals("", output);
  }

  @Test
  void testNotifyDriver_ValidDriver_PrintsDriverNotification() {
    Driver driver = Driver.builder().id(1L).build();

    Delivery delivery = Delivery.builder().id(100L).build();

    notificationService.notifyDriver(driver, delivery, "Delivery assigned");

    String output = outputStream.toString();
    assertTrue(output.contains("[DRIVER NOTIFICATION]"));
    assertTrue(output.contains("Driver ID: 1"));
    assertTrue(output.contains("Delivery ID: 100"));
    assertTrue(output.contains("Delivery assigned"));
  }

  @Test
  void testNotifyDriver_NullDelivery_PrintsWithN_A() {
    Driver driver = Driver.builder().id(1L).build();

    notificationService.notifyDriver(driver, null, "Test message");

    String output = outputStream.toString();
    assertTrue(output.contains("Delivery ID: N/A"));
  }

  @Test
  void testNotifyDriver_NullDriver_NoOutput() {
    Delivery delivery = Delivery.builder().id(100L).build();

    notificationService.notifyDriver(null, delivery, "Test message");

    String output = outputStream.toString();
    assertEquals("", output);
  }

  @Test
  void testNotifyMerchant_ValidMerchant_PrintsMerchantNotification() {
    Merchant merchant = Merchant.builder().id(5L).build();

    notificationService.notifyMerchant(merchant, "New order received");

    String output = outputStream.toString();
    assertTrue(output.contains("[MERCHANT NOTIFICATION]"));
    assertTrue(output.contains("Merchant ID: 5"));
    assertTrue(output.contains("New order received"));
  }

  @Test
  void testNotifyMerchant_NullMerchant_NoOutput() {
    notificationService.notifyMerchant(null, "Test message");

    String output = outputStream.toString();
    assertEquals("", output);
  }

  @Test
  void testSendDeliveryStatusUpdate_ValidUserAndDelivery_PrintsStatusUpdate() {
    User user = User.builder().email("customer@example.com").build();

    Delivery delivery = Delivery.builder().id(200L).status(DeliveryStatus.IN_TRANSIT).build();

    notificationService.sendDeliveryStatusUpdate(user, delivery);

    String output = outputStream.toString();
    assertTrue(output.contains("[DELIVERY STATUS UPDATE]"));
    assertTrue(output.contains("customer@example.com"));
    assertTrue(output.contains("Delivery ID: 200"));
    assertTrue(output.contains("Status: IN_TRANSIT"));
  }

  @Test
  void testSendDeliveryStatusUpdate_NullUser_NoOutput() {
    Delivery delivery = Delivery.builder().id(200L).status(DeliveryStatus.IN_TRANSIT).build();

    notificationService.sendDeliveryStatusUpdate(null, delivery);

    String output = outputStream.toString();
    assertEquals("", output);
  }

  @Test
  void testSendDeliveryStatusUpdate_NullDelivery_NoOutput() {
    User user = User.builder().email("customer@example.com").build();

    notificationService.sendDeliveryStatusUpdate(user, null);

    String output = outputStream.toString();
    assertEquals("", output);
  }

  @Test
  void testBroadcastSystemMessage_ValidMessage_PrintsSystemBroadcast() {
    notificationService.broadcastSystemMessage("System maintenance scheduled");

    String output = outputStream.toString();
    assertTrue(output.contains("[SYSTEM BROADCAST]"));
    assertTrue(output.contains("System maintenance scheduled"));
  }

  @Test
  void testSendOrderConfirmation_ValidDelivery_SendsUserAndMerchantNotifications() {
    User user = User.builder().email("customer@example.com").build();

    Merchant merchant = Merchant.builder().id(5L).build();

    Order order = Order.builder().user(user).merchant(merchant).build();

    Delivery delivery = Delivery.builder().order(order).build();

    notificationService.sendOrderConfirmation(delivery);

    String output = outputStream.toString();
    assertTrue(output.contains("[USER NOTIFICATION]"));
    assertTrue(output.contains("customer@example.com"));
    assertTrue(output.contains("Your order has been confirmed!"));
    assertTrue(output.contains("[MERCHANT NOTIFICATION]"));
    assertTrue(output.contains("Merchant ID: 5"));
    assertTrue(output.contains("A new order has been placed."));
  }

  @Test
  void testSendOrderConfirmation_NullDelivery_NoOutput() {
    notificationService.sendOrderConfirmation(null);

    String output = outputStream.toString();
    assertEquals("", output);
  }

  @Test
  void testSendOrderConfirmation_DeliveryWithNullOrder_NoOutput() {
    Delivery delivery = Delivery.builder().order(null).build();

    notificationService.sendOrderConfirmation(delivery);

    String output = outputStream.toString();
    assertEquals("", output);
  }

  @Test
  void testSendOrderCancellation_ValidDelivery_SendsCancellationNotifications() {
    User user = User.builder().email("customer@example.com").build();

    Merchant merchant = Merchant.builder().id(5L).build();

    Order order = Order.builder().user(user).merchant(merchant).build();

    Delivery delivery = Delivery.builder().order(order).build();

    notificationService.sendOrderCancellation(delivery);

    String output = outputStream.toString();
    assertTrue(output.contains("[USER NOTIFICATION]"));
    assertTrue(output.contains("customer@example.com"));
    assertTrue(output.contains("Your order has been cancelled."));
    assertTrue(output.contains("[MERCHANT NOTIFICATION]"));
    assertTrue(output.contains("Merchant ID: 5"));
    assertTrue(output.contains("An order has been cancelled."));
  }

  @Test
  void testSendOrderCancellation_NullDelivery_NoOutput() {
    notificationService.sendOrderCancellation(null);

    String output = outputStream.toString();
    assertEquals("", output);
  }
}
