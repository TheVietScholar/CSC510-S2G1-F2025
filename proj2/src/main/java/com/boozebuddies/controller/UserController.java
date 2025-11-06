package com.boozebuddies.controller;

import com.boozebuddies.dto.ApiResponse;
import com.boozebuddies.dto.UserDTO;
import com.boozebuddies.entity.User;
import com.boozebuddies.mapper.UserMapper;
import com.boozebuddies.model.Role;
import com.boozebuddies.security.annotation.RoleAnnotations.*;
import com.boozebuddies.service.PermissionService;
import com.boozebuddies.service.RoleService;
import com.boozebuddies.service.UserService;
import com.boozebuddies.service.ValidationService;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/** REST controller for managing users and user operations. */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;
  private final ValidationService validationService;
  private final UserMapper userMapper;
  private final PermissionService permissionService;
  private final RoleService roleService;

  // ==================== REGISTER ====================
  // Authentication is handled by AuthController at /api/auth

  // ==================== RETRIEVE ====================

  /**
   * Retrieves a user by ID. Users can view their own profile, admins can view any profile.
   *
   * @param id the user ID
   * @param authentication the authentication object
   * @return the user with the specified ID
   */
  @GetMapping("/{id}")
  @IsAuthenticated
  public ResponseEntity<?> getUserById(@PathVariable Long id, Authentication authentication) {
    try {
      if (id == null || id <= 0) {
        return ResponseEntity.badRequest().body(ApiResponse.error("Invalid user ID"));
      }

      User authenticatedUser = permissionService.getAuthenticatedUser(authentication);

      // Check if user is accessing their own profile or is an admin
      if (!authenticatedUser.getId().equals(id) && !authenticatedUser.isAdmin()) {
        throw new AccessDeniedException("You can only view your own profile");
      }

      return userService
          .getUserById(id)
          .map(
              user ->
                  ResponseEntity.ok(
                      ApiResponse.success(userMapper.toDTO(user), "User retrieved successfully")))
          .orElse(ResponseEntity.notFound().build());
    } catch (AccessDeniedException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("An error occurred retrieving user"));
    }
  }

  /**
   * Retrieves all users. Admin only.
   *
   * @return a list of all users
   */
  @GetMapping
  @IsAdmin
  public ResponseEntity<?> getAllUsers() {
    try {
      List<UserDTO> users =
          userService.getAllUsers().stream().map(userMapper::toDTO).collect(Collectors.toList());
      return ResponseEntity.ok(ApiResponse.success(users, "Users retrieved successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("An error occurred retrieving users"));
    }
  }

  /**
   * Retrieves the authenticated user's profile.
   *
   * @param authentication the authentication object
   * @return the current user's profile
   */
  @GetMapping("/me")
  @IsAuthenticated
  public ResponseEntity<?> getCurrentUser(Authentication authentication) {
    try {
      User user = permissionService.getAuthenticatedUser(authentication);
      return ResponseEntity.ok(
          ApiResponse.success(userMapper.toDTO(user), "Your profile retrieved successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(ApiResponse.error("Error retrieving your profile"));
    }
  }

  // ==================== UPDATE ====================

  /**
   * Updates a user. Users can update their own profile, admins can update any profile.
   *
   * @param id the user ID
   * @param userDTO the updated user data
   * @param authentication the authentication object
   * @return the updated user
   */
  @PutMapping("/{id}")
  @IsAuthenticated
  public ResponseEntity<?> updateUser(
      @PathVariable Long id, @RequestBody UserDTO userDTO, Authentication authentication) {
    try {
      if (id == null || id <= 0) {
        return ResponseEntity.badRequest().body(ApiResponse.error("Invalid user ID"));
      }

      User authenticatedUser = permissionService.getAuthenticatedUser(authentication);

      // Check if user is updating their own profile or is an admin
      if (!authenticatedUser.getId().equals(id) && !authenticatedUser.isAdmin()) {
        throw new AccessDeniedException("You can only update your own profile");
      }

      User updatedUser = userService.updateUser(id, userMapper.toEntity(userDTO));
      if (updatedUser == null) {
        return ResponseEntity.notFound().build();
      }
      return ResponseEntity.ok(
          ApiResponse.success(userMapper.toDTO(updatedUser), "User updated successfully"));
    } catch (AccessDeniedException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(e.getMessage()));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(ApiResponse.error("An error occurred updating user"));
    }
  }

  // ==================== VERIFY AGE ====================

  /**
   * Verifies a user's age. Users can verify their own age, admins can verify any user's age.
   *
   * @param id the user ID
   * @param authentication the authentication object
   * @return the user with updated age verification status
   */
  @PostMapping("/{id}/verify-age")
  @IsAuthenticated
  public ResponseEntity<?> verifyAge(@PathVariable Long id, Authentication authentication) {
    try {
      if (id == null || id <= 0) {
        return ResponseEntity.badRequest().body(ApiResponse.error("Invalid user ID"));
      }

      User authenticatedUser = permissionService.getAuthenticatedUser(authentication);

      // Check if user is verifying their own age or is an admin
      if (!authenticatedUser.getId().equals(id) && !authenticatedUser.isAdmin()) {
        throw new AccessDeniedException("You can only verify your own age");
      }

      User user =
          userService.getUserById(id).orElseThrow(() -> new RuntimeException("User not found"));

      // In real app, this would integrate with external age verification service
      boolean isVerified = validationService.validateAge(user);

      if (isVerified) {
        user.setAgeVerified(true);
        userService.updateUser(id, user);
        return ResponseEntity.ok(
            ApiResponse.success(userMapper.toDTO(user), "Age verification successful"));
      } else {
        return ResponseEntity.badRequest().body(ApiResponse.error("Age verification failed"));
      }
    } catch (AccessDeniedException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("An error occurred during age verification"));
    }
  }

  // ==================== DELETE ====================

  /**
   * Deletes a user. Admin only.
   *
   * @param id the user ID
   * @return a success message
   */
  @DeleteMapping("/{id}")
  @IsAdmin
  public ResponseEntity<?> deleteUser(@PathVariable Long id) {
    try {
      if (id == null || id <= 0) {
        return ResponseEntity.badRequest().body(ApiResponse.error("Invalid user ID"));
      }

      boolean deleted = userService.deleteUser(id);
      if (deleted) {
        return ResponseEntity.ok(ApiResponse.success(null, "User deleted successfully"));
      } else {
        return ResponseEntity.notFound().build();
      }
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(ApiResponse.error("An error occurred deleting user"));
    }
  }

  // ==================== ROLE MANAGEMENT (Admin only) ====================

  /**
   * Assigns a role to a user. Admin only.
   *
   * @param id the user ID
   * @param request the role assignment request
   * @return the updated user
   */
  @PostMapping("/{id}/roles")
  @IsAdmin
  public ResponseEntity<?> assignRole(@PathVariable Long id, @RequestBody RoleRequest request) {
    try {
      User updatedUser;

      if (request.getRole() == Role.MERCHANT_ADMIN && request.getMerchantId() != null) {
        updatedUser =
            roleService.assignRoleWithMerchant(id, request.getRole(), request.getMerchantId());
      } else {
        updatedUser = roleService.assignRole(id, request.getRole());
      }

      return ResponseEntity.ok(
          ApiResponse.success(userMapper.toDTO(updatedUser), "Role assigned successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Error assigning role: " + e.getMessage()));
    }
  }

  /**
   * Removes a role from a user. Admin only.
   *
   * @param id the user ID
   * @param role the role to remove
   * @return the updated user
   */
  @DeleteMapping("/{id}/roles/{role}")
  @IsAdmin
  public ResponseEntity<?> removeRole(@PathVariable Long id, @PathVariable Role role) {
    try {
      User updatedUser = roleService.removeRole(id, role);
      return ResponseEntity.ok(
          ApiResponse.success(userMapper.toDTO(updatedUser), "Role removed successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Error removing role: " + e.getMessage()));
    }
  }

  /**
   * Sets all roles for a user, replacing existing roles. Admin only.
   *
   * @param id the user ID
   * @param request the set roles request
   * @return the updated user
   */
  @PutMapping("/{id}/roles")
  @IsAdmin
  public ResponseEntity<?> setRoles(@PathVariable Long id, @RequestBody SetRolesRequest request) {
    try {
      User updatedUser = roleService.setRoles(id, request.getRoles());
      return ResponseEntity.ok(
          ApiResponse.success(userMapper.toDTO(updatedUser), "Roles updated successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Error updating roles: " + e.getMessage()));
    }
  }

  /**
   * Assigns a merchant to a user for MERCHANT_ADMIN role. Admin only.
   *
   * @param id the user ID
   * @param request the merchant assignment request
   * @return the updated user
   */
  @PostMapping("/{id}/merchant")
  @IsAdmin
  public ResponseEntity<?> assignMerchant(
      @PathVariable Long id, @RequestBody MerchantAssignmentRequest request) {
    try {
      User updatedUser = roleService.assignMerchantToUser(id, request.getMerchantId());
      return ResponseEntity.ok(
          ApiResponse.success(userMapper.toDTO(updatedUser), "Merchant assigned successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Error assigning merchant: " + e.getMessage()));
    }
  }

  // ==================== REQUEST DTOs ====================

  /** Request DTO for assigning a role to a user. */
  @lombok.Data
  public static class RoleRequest {
    private Role role;
    private Long merchantId; // Optional, only for MERCHANT_ADMIN
  }

  /** Request DTO for setting all roles for a user. */
  @lombok.Data
  public static class SetRolesRequest {
    private Set<Role> roles;
  }

  /** Request DTO for assigning a merchant to a user. */
  @lombok.Data
  public static class MerchantAssignmentRequest {
    private Long merchantId;
  }
}
