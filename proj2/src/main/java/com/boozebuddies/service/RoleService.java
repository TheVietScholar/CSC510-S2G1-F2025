package com.boozebuddies.service;

import com.boozebuddies.entity.User;
import com.boozebuddies.model.Role;
import java.util.Set;

/** Service for managing user roles. */
public interface RoleService {

  /**
   * Assign a role to a user.
   *
   * @param userId The user ID
   * @param role The role to assign
   * @return The updated user
   */
  User assignRole(Long userId, Role role);

  /**
   * Assign a role to a user with merchant association (for MERCHANT_ADMIN).
   *
   * @param userId The user ID
   * @param role The role to assign
   * @param merchantId The merchant ID (required for MERCHANT_ADMIN)
   * @return The updated user
   */
  User assignRoleWithMerchant(Long userId, Role role, Long merchantId);

  /**
   * Remove a role from a user.
   *
   * @param userId The user ID
   * @param role The role to remove
   * @return The updated user
   */
  User removeRole(Long userId, Role role);

  /**
   * Replace all roles for a user.
   *
   * @param userId The user ID
   * @param roles The new set of roles
   * @return The updated user
   */
  User setRoles(Long userId, Set<Role> roles);

  /**
   * Associate a merchant with a user (for MERCHANT_ADMIN role).
   *
   * @param userId The user ID
   * @param merchantId The merchant ID
   * @return The updated user
   */
  User assignMerchantToUser(Long userId, Long merchantId);

  /**
   * Remove merchant association from a user.
   *
   * @param userId The user ID
   * @return The updated user
   */
  User removeMerchantFromUser(Long userId);

  /**
   * Check if a user can perform an action on a merchant.
   *
   * @param user The user
   * @param merchantId The merchant ID
   * @return true if user can perform the action
   */
  boolean canAccessMerchant(User user, Long merchantId);

  /**
   * Get the primary role for a user (for display purposes). Priority: ADMIN > MERCHANT_ADMIN >
   * DRIVER > USER
   *
   * @param user The user
   * @return The primary role
   */
  Role getPrimaryRole(User user);
}
