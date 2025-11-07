package com.boozebuddies.service.implementation;

import com.boozebuddies.entity.User;
import com.boozebuddies.model.Role;
import com.boozebuddies.service.DeliveryService;
import com.boozebuddies.service.OrderService;
import com.boozebuddies.service.PermissionService;
import com.boozebuddies.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * Implementation of the {@link PermissionService} interface responsible for checking access control
 * and ownership across users, merchants, orders, and deliveries.
 *
 * <p>This service validates whether a user has the necessary permissions or ownership to perform
 * certain actions, based on authentication context and system roles.
 *
 * <p>It integrates with {@link UserService}, {@link OrderService}, and {@link DeliveryService} to
 * verify relationships between authenticated users and resources (e.g., merchants, orders,
 * deliveries).
 */
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

  /** Service for user management and lookup. */
  private final UserService userService;

  /** Service for retrieving and verifying order-related permissions. */
  private final OrderService orderService;

  /** Service for retrieving and verifying delivery-related permissions. */
  private final DeliveryService deliveryService;

  /**
   * Checks if the authenticated user is the same as the target user.
   *
   * @param authentication the current authentication object containing the user's identity
   * @param userId the ID of the user to compare against
   * @return true if the authenticated user's ID matches the provided ID, false otherwise
   */
  @Override
  public boolean isSelf(Authentication authentication, Long userId) {
    if (authentication == null || userId == null) {
      return false;
    }

    // Try to get User from authentication principal first (set by JwtAuthenticationFilter)
    Object principal = authentication.getPrincipal();
    if (principal instanceof User) {
      User user = (User) principal;
      return user.getId().equals(userId);
    }

    // Fallback: look up by email if principal is not a User object
    String email = authentication.getName();
    return userService.findByEmail(email).map(user -> user.getId().equals(userId)).orElse(false);
  }

  /**
   * Checks if the authenticated user owns a given merchant.
   *
   * @param authentication the current authentication object
   * @param merchantId the ID of the merchant to check ownership for
   * @return true if the authenticated user owns the merchant, false otherwise
   */
  @Override
  public boolean ownsMerchant(Authentication authentication, Long merchantId) {
    if (authentication == null || merchantId == null) {
      return false;
    }
    String email = authentication.getName();
    return userService.findByEmail(email).map(user -> user.ownsMerchant(merchantId)).orElse(false);
  }

  /**
   * Verifies if the authenticated user has a specified system role.
   *
   * @param authentication the current authentication object
   * @param role the role to check (e.g., ADMIN, DRIVER, MERCHANT_ADMIN)
   * @return true if the user has the specified role, false otherwise
   */
  @Override
  public boolean hasRole(Authentication authentication, Role role) {
    if (authentication == null || role == null) {
      return false;
    }
    String email = authentication.getName();
    return userService.findByEmail(email).map(user -> user.hasRole(role)).orElse(false);
  }

  /**
   * Retrieves the currently authenticated {@link User} object.
   *
   * @param authentication the current authentication object
   * @return the corresponding {@link User}, or null if the user could not be found
   */
  @Override
  public User getAuthenticatedUser(Authentication authentication) {
    if (authentication == null) {
      return null;
    }

    // Check if principal is already a User object (set by JwtAuthenticationFilter)
    Object principal = authentication.getPrincipal();
    if (principal instanceof User) {
      return (User) principal;
    }

    // Fallback: look up by email (for other authentication types)
    String email = authentication.getName();
    return userService.findByEmail(email).orElse(null);
  }

  /**
   * Checks if the authenticated user has access to their own driver profile.
   *
   * @param authentication the current authentication object
   * @param driverId the ID of the driver profile
   * @return true if the authenticated user is a driver and owns the profile, false otherwise
   */
  @Override
  public boolean isDriverProfile(Authentication authentication, Long driverId) {
    if (authentication == null || driverId == null) {
      return false;
    }
    String email = authentication.getName();
    return userService
        .findByEmail(email)
        .filter(user -> user.hasRole(Role.DRIVER))
        .map(user -> user.getDriver() != null && user.getDriver().getId().equals(driverId))
        .orElse(false);
  }

  /**
   * Checks if the authenticated user owns a specific order.
   *
   * @param authentication the current authentication object
   * @param orderId the ID of the order to check
   * @return true if the order belongs to the authenticated user, false otherwise
   */
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
    return orderService
        .getOrderById(orderId)
        .map(order -> order.getUser() != null && order.getUser().getId().equals(user.getId()))
        .orElse(false);
  }

  /**
   * Checks if a merchant-admin user can access a given order.
   *
   * @param authentication the current authentication object
   * @param orderId the ID of the order to verify
   * @return true if the merchant-admin owns the merchant associated with the order, false otherwise
   */
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
    return orderService
        .getOrderById(orderId)
        .map(order -> order.getMerchant() != null && user.ownsMerchant(order.getMerchant().getId()))
        .orElse(false);
  }

  /**
   * Verifies if a driver can access a specific delivery.
   *
   * @param authentication the current authentication object
   * @param deliveryId the ID of the delivery to check
   * @return true if the delivery belongs to the driver, false otherwise
   */
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
      var delivery = deliveryService.getDeliveryById(deliveryId);
      if (delivery == null || delivery.getDriver() == null) {
        return false;
      }
      return delivery.getDriver().getId().equals(user.getDriver().getId());
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Checks if a driver can access a specific order.
   *
   * @param authentication the current authentication object
   * @param orderId the ID of the order to verify
   * @return true if the order is assigned to the driver, false otherwise
   */
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
      return orderService
          .getOrderById(orderId)
          .map(
              order ->
                  order.getDriver() != null
                      && order.getDriver().getId().equals(user.getDriver().getId()))
          .orElse(false);
    } catch (Exception e) {
      return false;
    }
  }
}
