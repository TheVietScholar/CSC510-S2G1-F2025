package com.boozebuddies.service.implementation;

import com.boozebuddies.entity.User;
import com.boozebuddies.model.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import com.boozebuddies.service.PermissionService;
import com.boozebuddies.service.OrderService;
import com.boozebuddies.service.DeliveryService;
import com.boozebuddies.service.UserService;

/**
 * Implementation of permission checking service.
 */
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

  private final UserService userService;
  private final OrderService orderService;
  private final DeliveryService deliveryService;

  @Override
  public boolean isSelf(Authentication authentication, Long userId) {
    if (authentication == null || userId == null) {
      return false;
    }

    String email = authentication.getName();
    return userService.findByEmail(email)
        .map(user -> user.getId().equals(userId))
        .orElse(false);
  }

  @Override
  public boolean ownsMerchant(Authentication authentication, Long merchantId) {
    if (authentication == null || merchantId == null) {
      return false;
    }

    String email = authentication.getName();
    return userService.findByEmail(email)
        .map(user -> user.ownsMerchant(merchantId))
        .orElse(false);
  }

  @Override
  public boolean hasRole(Authentication authentication, Role role) {
    if (authentication == null || role == null) {
      return false;
    }

    String email = authentication.getName();
    return userService.findByEmail(email)
        .map(user -> user.hasRole(role))
        .orElse(false);
  }

  @Override
  public User getAuthenticatedUser(Authentication authentication) {
    if (authentication == null) {
      return null;
    }

    String email = authentication.getName();
    return userService.findByEmail(email).orElse(null);
  }

  @Override
  public boolean isDriverProfile(Authentication authentication, Long driverId) {
    if (authentication == null || driverId == null) {
      return false;
    }

    String email = authentication.getName();
    return userService.findByEmail(email)
        .filter(user -> user.hasRole(Role.DRIVER))
        .map(user -> user.getDriver() != null && user.getDriver().getId().equals(driverId))
        .orElse(false);
  }

  @Override
  public boolean ownsOrder(Authentication authentication, Long orderId) {
    if (authentication == null || orderId == null) {
      return false;
    }

    String email = authentication.getName();
    User user = userService.findByEmail(email).orElse(null);
    if (user == null) {
      return false;
    }

    return orderService.getOrderById(orderId)
        .map(order -> order.getUser() != null && order.getUser().getId().equals(user.getId()))
        .orElse(false);
  }

  @Override
  public boolean merchantCanAccessOrder(Authentication authentication, Long orderId) {
    if (authentication == null || orderId == null) {
      return false;
    }

    String email = authentication.getName();
    User user = userService.findByEmail(email).orElse(null);
    if (user == null || !user.hasRole(Role.MERCHANT_ADMIN)) {
      return false;
    }

    return orderService.getOrderById(orderId)
        .map(order -> 
            order.getMerchant() != null && user.ownsMerchant(order.getMerchant().getId()))
        .orElse(false);
  }

  @Override
  public boolean driverCanAccessDelivery(Authentication authentication, Long deliveryId) {
    if (authentication == null || deliveryId == null) {
      return false;
    }

    String email = authentication.getName();
    User user = userService.findByEmail(email).orElse(null);
    if (user == null || !user.hasRole(Role.DRIVER) || user.getDriver() == null) {
      return false;
    }

    try {
      // DeliveryService returns null if not found (consistent with other services)
      var delivery = deliveryService.getDeliveryById(deliveryId);
      if (delivery == null || delivery.getDriver() == null) {
        return false;
      }
      return delivery.getDriver().getId().equals(user.getDriver().getId());
    } catch (Exception e) {
      return false;
    }
  }

  @Override
  public boolean driverCanAccessOrder(Authentication authentication, Long orderId) {
    if (authentication == null || orderId == null) {
      return false;
    }

    String email = authentication.getName();
    User user = userService.findByEmail(email).orElse(null);
    if (user == null || !user.hasRole(Role.DRIVER) || user.getDriver() == null) {
      return false;
    }

    try {
      return orderService.getOrderById(orderId)
          .map(order -> order.getDriver() != null && order.getDriver().getId().equals(user.getDriver().getId()))
          .orElse(false);
    } catch (Exception e) {
      return false;
    }
  }
}