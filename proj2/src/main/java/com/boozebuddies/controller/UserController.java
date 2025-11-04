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

  /** Get user by ID. Users can view their own profile, admins can view any profile. */
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

  /** Get all users (Admin only). */
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

  /** Get current user's profile. */
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

  /** Update user. Users can update their own profile, admins can update any profile. */
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

  /** Verify user's age (can be self or admin). */
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

  /** Delete user (Admin only). */
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

  /** Assign a role to a user. */
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

  /** Remove a role from a user. */
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

  /** Set all roles for a user (replaces existing roles). */
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

  /** Assign a merchant to a user (for MERCHANT_ADMIN role). */
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

  @lombok.Data
  public static class RoleRequest {
    private Role role;
    private Long merchantId; // Optional, only for MERCHANT_ADMIN
  }

  @lombok.Data
  public static class SetRolesRequest {
    private Set<Role> roles;
  }

  @lombok.Data
  public static class MerchantAssignmentRequest {
    private Long merchantId;
  }
}
