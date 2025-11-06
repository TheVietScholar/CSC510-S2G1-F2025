package com.boozebuddies.service.implementation;

import com.boozebuddies.entity.Merchant;
import com.boozebuddies.entity.User;
import com.boozebuddies.exception.UnauthorizedException;
import com.boozebuddies.exception.ValidationException;
import com.boozebuddies.model.Role;
import com.boozebuddies.service.MerchantService;
import com.boozebuddies.service.RoleService;
import com.boozebuddies.service.UserService;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the {@link RoleService} interface, responsible for managing user roles and
 * merchant associations.
 *
 * <p>This service provides operations to assign, remove, and validate roles for users, ensuring
 * that business rules such as age restrictions and mutual exclusivity between certain roles (e.g.,
 * DRIVER and MERCHANT_ADMIN) are enforced.
 *
 * <p>It also manages associations between users and merchants for merchant administrators.
 */
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

  private final UserService userService;
  private final MerchantService merchantService;

  /**
   * Assigns a new role to a user after validating eligibility and existing role combinations.
   *
   * @param userId the ID of the user
   * @param role the {@link Role} to assign
   * @return the updated {@link User} with the new role
   * @throws ValidationException if the role violates business rules
   */
  @Override
  @Transactional
  public User assignRole(Long userId, Role role) {
    User user = userService.findById(userId);

    // Validate role assignment
    validateRoleAssignment(user, role);

    user.addRole(role);
    return userService.updateUser(userId, user);
  }

  /**
   * Assigns a role to a user along with a merchant association, used primarily for assigning {@link
   * Role#MERCHANT_ADMIN}.
   *
   * @param userId the ID of the user
   * @param role the role to assign
   * @param merchantId the ID of the merchant associated with the user (required for MERCHANT_ADMIN)
   * @return the updated {@link User} with assigned role and merchant
   * @throws ValidationException if the merchant ID is missing or invalid for the role
   */
  @Override
  @Transactional
  public User assignRoleWithMerchant(Long userId, Role role, Long merchantId) {
    User user = userService.findById(userId);

    if (role == Role.MERCHANT_ADMIN) {
      if (merchantId == null) {
        throw new ValidationException("Merchant ID is required for MERCHANT_ADMIN role");
      }
      user.setMerchantId(merchantId);
    }

    // Validate role assignment
    validateRoleAssignment(user, role);

    user.addRole(role);
    return userService.updateUser(userId, user);
  }

  /**
   * Removes a specific role from a user.
   *
   * @param userId the ID of the user
   * @param role the role to remove
   * @return the updated {@link User} without the removed role
   * @throws ValidationException if attempting to remove the last role from the user
   */
  @Override
  @Transactional
  public User removeRole(Long userId, Role role) {
    User user = userService.findById(userId);

    // Don't allow removing the last role
    if (user.getRoles().size() == 1 && user.hasRole(role)) {
      throw new ValidationException("Cannot remove the last role from a user");
    }

    user.removeRole(role);

    // Clean up role-specific data
    if (role == Role.MERCHANT_ADMIN) {
      user.setMerchantId(null);
    }

    return userService.updateUser(userId, user);
  }

  /**
   * Replaces all roles for a given user, ensuring that at least one role is assigned.
   *
   * @param userId the ID of the user
   * @param roles the new set of roles to assign
   * @return the updated {@link User} with new roles
   * @throws ValidationException if the roles set is null or empty
   */
  @Override
  @Transactional
  public User setRoles(Long userId, Set<Role> roles) {
    if (roles == null || roles.isEmpty()) {
      throw new ValidationException("User must have at least one role");
    }

    User user = userService.findById(userId);
    user.setRoles(roles);

    // If MERCHANT_ADMIN is not in the new roles, clear merchantId
    if (!roles.contains(Role.MERCHANT_ADMIN)) {
      user.setMerchantId(null);
    }

    return userService.updateUser(userId, user);
  }

  /**
   * Assigns a merchant to a user who already holds the {@link Role#MERCHANT_ADMIN} role.
   *
   * @param userId the ID of the user
   * @param merchantId the ID of the merchant to associate
   * @return the updated {@link User} with the merchant assigned
   * @throws UnauthorizedException if the user does not have MERCHANT_ADMIN role
   * @throws ValidationException if the merchant ID is null or does not exist
   */
  @Override
  @Transactional
  public User assignMerchantToUser(Long userId, Long merchantId) {
    User user = userService.findById(userId);

    if (!user.hasRole(Role.MERCHANT_ADMIN)) {
      throw new UnauthorizedException(
          "User must have MERCHANT_ADMIN role to be assigned a merchant");
    }

    if (merchantId == null) {
      throw new ValidationException("Merchant ID cannot be null");
    }

    // Validate that merchant exists
    Merchant merchant = merchantService.getMerchantById(merchantId);
    if (merchant == null) {
      throw new ValidationException("Merchant not found with ID: " + merchantId);
    }

    user.setMerchantId(merchantId);
    return userService.updateUser(userId, user);
  }

  /**
   * Removes a merchant association from a user, typically when demoting or revoking merchant
   * privileges.
   *
   * @param userId the ID of the user
   * @return the updated {@link User} with the merchant removed
   */
  @Override
  @Transactional
  public User removeMerchantFromUser(Long userId) {
    User user = userService.findById(userId);
    user.setMerchantId(null);
    return userService.updateUser(userId, user);
  }

  /**
   * Checks whether a user has permission to access data for a specific merchant.
   *
   * @param user the user attempting to access the merchant
   * @param merchantId the ID of the merchant being accessed
   * @return {@code true} if the user can access the merchant, otherwise {@code false}
   */
  @Override
  public boolean canAccessMerchant(User user, Long merchantId) {
    if (user.hasRole(Role.ADMIN)) {
      return true; // Admins can access all merchants
    }

    if (user.hasRole(Role.MERCHANT_ADMIN)) {
      return user.ownsMerchant(merchantId);
    }

    return false;
  }

  /**
   * Determines the primary (highest-level) role of a user based on role hierarchy: ADMIN >
   * MERCHANT_ADMIN > DRIVER > USER.
   *
   * @param user the user to evaluate
   * @return the highest-priority {@link Role} the user holds
   */
  @Override
  public Role getPrimaryRole(User user) {
    if (user.hasRole(Role.ADMIN)) {
      return Role.ADMIN;
    }
    if (user.hasRole(Role.MERCHANT_ADMIN)) {
      return Role.MERCHANT_ADMIN;
    }
    if (user.hasRole(Role.DRIVER)) {
      return Role.DRIVER;
    }
    return Role.USER;
  }

  /**
   * Validates role assignment rules to prevent conflicts or violations of business constraints.
   *
   * <p>Rules include:
   *
   * <ul>
   *   <li>A user cannot hold both DRIVER and MERCHANT_ADMIN roles simultaneously.
   *   <li>A user must be age-verified to be assigned the DRIVER role.
   * </ul>
   *
   * @param user the user being validated
   * @param role the role being assigned
   * @throws ValidationException if role assignment violates business rules
   */
  private void validateRoleAssignment(User user, Role role) {
    // Prevent conflicting roles
    if (role == Role.MERCHANT_ADMIN && user.hasRole(Role.DRIVER)) {
      throw new ValidationException("A driver cannot also be a merchant admin");
    }

    if (role == Role.DRIVER && user.hasRole(Role.MERCHANT_ADMIN)) {
      throw new ValidationException("A merchant admin cannot also be a driver");
    }

    // Ensure age verification for certain roles
    if (role == Role.DRIVER && !user.isAgeVerified()) {
      throw new ValidationException("User must be age verified to become a driver");
    }
  }
}
