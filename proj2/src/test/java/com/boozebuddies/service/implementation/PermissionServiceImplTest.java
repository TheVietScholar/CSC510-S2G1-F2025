package com.boozebuddies.service.implementation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.boozebuddies.entity.*;
import com.boozebuddies.model.Role;
import com.boozebuddies.service.*;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

class PermissionServiceImplTest {

  private UserService userService;
  private OrderService orderService;
  private DeliveryService deliveryService;
  private PermissionServiceImpl permissionService;
  private Authentication auth;

  private User testUser;

  @BeforeEach
  void setup() {
    userService = mock(UserService.class);
    orderService = mock(OrderService.class);
    deliveryService = mock(DeliveryService.class);
    permissionService = new PermissionServiceImpl(userService, orderService, deliveryService);

    auth = mock(Authentication.class);
    when(auth.getName()).thenReturn("test@example.com");

    testUser = new User();
    testUser.setId(1L);
    testUser.setEmail("test@example.com");
  }

  // ---------- isSelf ----------

  @Test
  @DisplayName("isSelf returns true if authentication user matches ID")
  void testIsSelf_True() {
    when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
    assertTrue(permissionService.isSelf(auth, 1L));
  }

  @Test
  @DisplayName("isSelf returns false for nulls or mismatches")
  void testIsSelf_FalseCases() {
    when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
    assertFalse(permissionService.isSelf(null, 1L));
    assertFalse(permissionService.isSelf(auth, null));
    assertFalse(permissionService.isSelf(auth, 999L));
  }

  // ---------- ownsMerchant ----------

  @Test
  void testOwnsMerchant_True() {
    when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
    testUser.setMerchantId(10L);
    testUser.addRole(Role.MERCHANT_ADMIN); // ← make the user a merchant admin
    assertTrue(permissionService.ownsMerchant(auth, 10L));
  }

  @Test
  void testOwnsMerchant_FalseWhenNotOwnerOrNull() {
    when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
    testUser.setMerchantId(5L);
    assertFalse(permissionService.ownsMerchant(auth, 10L));
    assertFalse(permissionService.ownsMerchant(null, 10L));
    assertFalse(permissionService.ownsMerchant(auth, null));
  }

  // ---------- hasRole ----------

  @Test
  void testHasRole_True() {
    testUser.addRole(Role.ADMIN);
    when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
    assertTrue(permissionService.hasRole(auth, Role.ADMIN));
  }

  @Test
  void testHasRole_False() {
    when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
    assertFalse(permissionService.hasRole(auth, Role.DRIVER));
    assertFalse(permissionService.hasRole(null, Role.ADMIN));
    assertFalse(permissionService.hasRole(auth, null));
  }

  // ---------- getAuthenticatedUser ----------

  @Test
  void testGetAuthenticatedUser_ReturnsUser() {
    when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
    assertEquals(testUser, permissionService.getAuthenticatedUser(auth));
  }

  @Test
  void testGetAuthenticatedUser_NullCases() {
    when(userService.findByEmail("test@example.com")).thenReturn(Optional.empty());
    assertNull(permissionService.getAuthenticatedUser(auth));
    assertNull(permissionService.getAuthenticatedUser(null));
  }

  // ---------- isDriverProfile ----------

  @Test
  void testIsDriverProfile_True() {
    Driver driver = new Driver();
    driver.setId(5L);
    testUser.addRole(Role.DRIVER);
    testUser.setDriver(driver);

    when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
    assertTrue(permissionService.isDriverProfile(auth, 5L));
  }

  @Test
  void testIsDriverProfile_FalseCases() {
    when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
    assertFalse(permissionService.isDriverProfile(auth, 5L));
    assertFalse(permissionService.isDriverProfile(null, 5L));
    assertFalse(permissionService.isDriverProfile(auth, null));
  }

  // ---------- ownsOrder ----------

  @Test
  void testOwnsOrder_True() {
    Order order = new Order();
    order.setId(20L);
    order.setUser(testUser);

    when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
    when(orderService.getOrderById(20L)).thenReturn(Optional.of(order));

    assertTrue(permissionService.ownsOrder(auth, 20L));
  }

  @Test
  void testOwnsOrder_FalseCases() {
    when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
    when(orderService.getOrderById(99L)).thenReturn(Optional.empty());
    assertFalse(permissionService.ownsOrder(auth, 99L));
    assertFalse(permissionService.ownsOrder(null, 1L));
    assertFalse(permissionService.ownsOrder(auth, null));
  }

  // ---------- merchantCanAccessOrder ----------

  @Test
  void testMerchantCanAccessOrder_True() {
    Merchant merchant = new Merchant();
    merchant.setId(10L);
    Order order = new Order();
    order.setMerchant(merchant);

    testUser.addRole(Role.MERCHANT_ADMIN);
    testUser.setMerchantId(10L);

    when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
    when(orderService.getOrderById(99L)).thenReturn(Optional.of(order));

    assertTrue(permissionService.merchantCanAccessOrder(auth, 99L));
  }

  @Test
  void testMerchantCanAccessOrder_FalseCases() {
    when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
    assertFalse(permissionService.merchantCanAccessOrder(auth, 99L));
    assertFalse(permissionService.merchantCanAccessOrder(null, 99L));
    assertFalse(permissionService.merchantCanAccessOrder(auth, null));
  }

  // ---------- driverCanAccessDelivery ----------

  @Test
  void testDriverCanAccessDelivery_True() {
    Driver driver = new Driver();
    driver.setId(7L);
    testUser.addRole(Role.DRIVER);
    testUser.setDriver(driver);

    Delivery delivery = new Delivery();
    delivery.setId(50L);
    delivery.setDriver(driver);

    when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
    when(deliveryService.getDeliveryById(50L)).thenReturn(delivery);

    assertTrue(permissionService.driverCanAccessDelivery(auth, 50L));
  }

  @Test
  void testDriverCanAccessDelivery_FalseCases() {
    when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
    when(deliveryService.getDeliveryById(50L)).thenReturn(null);
    assertFalse(permissionService.driverCanAccessDelivery(auth, 50L));
    assertFalse(permissionService.driverCanAccessDelivery(null, 50L));
    assertFalse(permissionService.driverCanAccessDelivery(auth, null));
  }

  // ---------- driverCanAccessOrder ----------

  @Test
  void testDriverCanAccessOrder_True() {
    Driver driver = new Driver();
    driver.setId(7L);
    testUser.addRole(Role.DRIVER);
    testUser.setDriver(driver);

    Order order = new Order();
    order.setDriver(driver);

    when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
    when(orderService.getOrderById(55L)).thenReturn(Optional.of(order));

    assertTrue(permissionService.driverCanAccessOrder(auth, 55L));
  }

  @Test
  void testDriverCanAccessOrder_FalseCases() {
    when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
    when(orderService.getOrderById(55L)).thenReturn(Optional.empty());
    assertFalse(permissionService.driverCanAccessOrder(auth, 55L));
    assertFalse(permissionService.driverCanAccessOrder(null, 55L));
    assertFalse(permissionService.driverCanAccessOrder(auth, null));
  }
}
