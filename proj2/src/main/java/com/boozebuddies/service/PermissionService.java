package com.boozebuddies.service;

import com.boozebuddies.entity.User;
import com.boozebuddies.model.Role;
import org.springframework.security.core.Authentication;

/**
 * Service for checking permissions and ownership. Used by custom security annotations and
 * controllers.
 */
public interface PermissionService {

  /**
   * Check if the authenticated user is accessing their own resource.
   *
   * @param authentication The authentication object
   * @param userId The user ID being accessed
   * @return true if the user is accessing their own resource
   */
  boolean isSelf(Authentication authentication, Long userId);

  /**
   * Check if the authenticated merchant admin owns the merchant.
   *
   * @param authentication The authentication object
   * @param merchantId The merchant ID being accessed
   * @return true if the merchant admin owns this merchant
   */
  boolean ownsMerchant(Authentication authentication, Long merchantId);

  /**
   * Check if user has a specific role.
   *
   * @param authentication The authentication object
   * @param role The role to check
   * @return true if user has the role
   */
  boolean hasRole(Authentication authentication, Role role);

  /**
   * Get the authenticated user.
   *
   * @param authentication The authentication object
   * @return The authenticated user, or null if not found
   */
  User getAuthenticatedUser(Authentication authentication);

  /**
   * Check if the authenticated user is a driver and owns the driver profile.
   *
   * @param authentication The authentication object
   * @param driverId The driver ID being accessed
   * @return true if the user owns this driver profile
   */
  boolean isDriverProfile(Authentication authentication, Long driverId);

  /**
   * Check if user owns an order.
   *
   * @param authentication The authentication object
   * @param orderId The order ID
   * @return true if the user owns the order
   */
  boolean ownsOrder(Authentication authentication, Long orderId);

  /**
   * Check if merchant admin can access an order (order is for their merchant).
   *
   * @param authentication The authentication object
   * @param orderId The order ID
   * @return true if the merchant admin can access the order
   */
  boolean merchantCanAccessOrder(Authentication authentication, Long orderId);

  /**
   * Check if driver can access a delivery (delivery is assigned to them).
   *
   * @param authentication The authentication object
   * @param deliveryId The delivery ID
   * @return true if the driver can access the delivery
   */
  boolean driverCanAccessDelivery(Authentication authentication, Long deliveryId);

  /**
   * Check if driver can access an order (order is assigned to them).
   *
   * @param authentication The authentication object
   * @param orderId The order ID
   * @return true if the driver can access the order
   */
  boolean driverCanAccessOrder(Authentication authentication, Long orderId);
}
