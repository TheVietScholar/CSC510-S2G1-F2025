package com.boozebuddies.service.implementation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.boozebuddies.entity.Delivery;
import com.boozebuddies.entity.Driver;
import com.boozebuddies.entity.User;
import com.boozebuddies.model.Role;
import com.boozebuddies.service.DeliveryService;
import com.boozebuddies.service.OrderService;
import com.boozebuddies.service.UserService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class PermissionServiceImplTest {

  @Mock private UserService userService;
  @Mock private OrderService orderService;
  @Mock private DeliveryService deliveryService;

  @InjectMocks private PermissionServiceImpl permissionService;

  private Authentication auth;

  @BeforeEach
  void setUp() {
    auth = mock(Authentication.class);
  }

  @Test
  void driverCanAccessDelivery_true() {
    when(auth.getName()).thenReturn("driver@example.com");

    User user = new User();
    user.setEmail("driver@example.com");
    user.addRole(Role.DRIVER);
    Driver driver = new Driver();
    driver.setId(5L);
    user.setDriver(driver);

    when(userService.findByEmail("driver@example.com")).thenReturn(Optional.of(user));

    Delivery delivery = new Delivery();
    Driver assigned = new Driver();
    assigned.setId(5L);
    delivery.setDriver(assigned);

    when(deliveryService.getDeliveryById(10L)).thenReturn(delivery);

    assertTrue(permissionService.driverCanAccessDelivery(auth, 10L));
  }

  @Test
  void driverCanAccessDelivery_false_otherDriver() {
    when(auth.getName()).thenReturn("driver2@example.com");

    User user = new User();
    user.setEmail("driver2@example.com");
    user.addRole(Role.DRIVER);
    Driver driver = new Driver();
    driver.setId(7L);
    user.setDriver(driver);

    when(userService.findByEmail("driver2@example.com")).thenReturn(Optional.of(user));

    Delivery delivery = new Delivery();
    Driver assigned = new Driver();
    assigned.setId(5L);
    delivery.setDriver(assigned);

    when(deliveryService.getDeliveryById(11L)).thenReturn(delivery);

    assertFalse(permissionService.driverCanAccessDelivery(auth, 11L));
  }

  @Test
  void driverCanAccessDelivery_false_notDriver() {
    when(auth.getName()).thenReturn("user@example.com");

    User user = new User();
    user.setEmail("user@example.com");
    // no driver role or driver profile
    when(userService.findByEmail("user@example.com")).thenReturn(Optional.of(user));

    Delivery delivery = new Delivery();
    Driver assigned = new Driver();
    assigned.setId(5L);
    delivery.setDriver(assigned);

    when(deliveryService.getDeliveryById(12L)).thenReturn(delivery);

    assertFalse(permissionService.driverCanAccessDelivery(auth, 12L));
  }
}
