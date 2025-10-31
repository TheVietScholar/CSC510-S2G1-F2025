package com.boozebuddies.service.implementation;

import com.boozebuddies.entity.User;
import com.boozebuddies.exception.UnauthorizedException;
import com.boozebuddies.exception.ValidationException;
import com.boozebuddies.model.Role;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.boozebuddies.service.RoleService;
import com.boozebuddies.service.UserService;

/**
 * Implementation of role management service.
 */
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

  private final UserService userService;

  @Override
  @Transactional
  public User assignRole(Long userId, Role role) {
    User user = userService.findById(userId);
    
    // Validate role assignment
    validateRoleAssignment(user, role);
    
    user.addRole(role);
    return userService.updateUser(userId, user);
  }

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
    // Note: Driver entity cleanup should be handled separately if needed
    
    return userService.updateUser(userId, user);
  }

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

  @Override
  @Transactional
  public User assignMerchantToUser(Long userId, Long merchantId) {
    User user = userService.findById(userId);
    
    if (!user.hasRole(Role.MERCHANT_ADMIN)) {
      throw new UnauthorizedException("User must have MERCHANT_ADMIN role to be assigned a merchant");
    }
    
    if (merchantId == null) {
      throw new ValidationException("Merchant ID cannot be null");
    }
    
    // TODO: Validate that merchant exists using MerchantService when available
    
    user.setMerchantId(merchantId);
    return userService.updateUser(userId, user);
  }

  @Override
  @Transactional
  public User removeMerchantFromUser(Long userId) {
    User user = userService.findById(userId);
    user.setMerchantId(null);
    return userService.updateUser(userId, user);
  }

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
   * Validate role assignment rules.
   */
  private void validateRoleAssignment(User user, Role role) {
    // Add business rules for role assignment
    // For example: a user can't be both DRIVER and MERCHANT_ADMIN
    
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
