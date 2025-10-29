package com.boozebuddies.service;

import com.boozebuddies.dto.RegisterUserRequest;
import com.boozebuddies.entity.User;
import com.boozebuddies.model.Role;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserService {

  // ==================== Registration Methods ====================

  /**
   * Registers a new user in the system.
   *
   * @param user The user to register.
   * @return The registered user with generated ID.
   */
  User register(User user);

  /**
   * Registers a new user from registration request DTO.
   *
   * @param request The registration request containing user details.
   * @return The registered user with generated ID.
   */
  User registerUser(RegisterUserRequest request);

  // ==================== Authentication Methods ====================

  /**
   * Authenticates a user with email and password.
   *
   * @deprecated Use AuthenticationService for proper JWT-based authentication
   * @param email The user's email.
   * @param password The user's password.
   * @return The authenticated user if credentials are correct, otherwise null.
   */
  @Deprecated
  User login(String email, String password);

  /**
   * Finds a user by their email address.
   *
   * @param email The user's email address.
   * @return An Optional containing the user if found, otherwise empty.
   */
  Optional<User> findByEmail(String email);

  /**
   * Updates the user's last login timestamp.
   *
   * @param userId The ID of the user.
   */
  void updateLastLogin(Long userId);

  // ==================== User Retrieval Methods ====================

  /**
   * Retrieves a user by their unique ID.
   *
   * @param userId The ID of the user.
   * @return An Optional containing the user if found, otherwise empty.
   */
  Optional<User> getUserById(Long userId);

  /**
   * Retrieves a user by their unique ID. Throws exception if not found.
   *
   * @param userId The ID of the user.
   * @return The user entity.
   * @throws com.boozebuddies.exception.UserNotFoundException if user is not found.
   */
  User findById(Long userId);

  /**
   * Retrieves all users in the system.
   *
   * @return A list of all users.
   */
  List<User> getAllUsers();

  // ==================== User Management Methods ====================

  /**
   * Updates a user's information.
   *
   * @param userId The ID of the user to update.
   * @param user The user object containing updated information.
   * @return The updated user.
   */
  User updateUser(Long userId, User user);

  /**
   * Deletes a user from the system.
   *
   * @param userId The ID of the user to delete.
   * @return true if user was deleted, false if user was not found.
   */
  boolean deleteUser(Long userId);

  /**
   * Deactivates a user account. Also revokes any active refresh tokens.
   *
   * @param userId The ID of the user to deactivate.
   */
  void deactivateUser(Long userId);

  /**
   * Activates a previously deactivated user account.
   *
   * @param userId The ID of the user to activate.
   */
  void activateUser(Long userId);

  // ==================== Token Management Methods ====================

  /**
   * Saves a refresh token for the user.
   *
   * @param userId The ID of the user.
   * @param refreshToken The refresh token to save (raw token will be hashed by implementation).
   * @param expiryDate The expiry date of the refresh token.
   */
  void saveRefreshToken(
      Long userId, String refreshTokenId, String refreshToken, LocalDateTime expiryDate);

  /**
   * Validates if a refresh token is still valid.
   *
   * @param refreshToken The refresh token to validate.
   * @return true if the token is valid and not expired, false otherwise.
   */
  boolean isRefreshTokenValid(String refreshToken);

  /**
   * Finds a user by their refresh token.
   *
   * @param refreshToken The refresh token.
   * @return An Optional containing the user if found, otherwise empty.
   */
  Optional<User> findByRefreshToken(String refreshToken);

  /**
   * Finds a user by refresh token id.
   *
   * @param refreshTokenId the token id portion
   * @return optional user
   */
  Optional<User> findByRefreshTokenId(String refreshTokenId);

  /**
   * Revokes a user's refresh token (logout).
   *
   * @param userId The ID of the user.
   */
  void revokeRefreshToken(Long userId);

  // ==================== Email Verification Methods ====================

  /**
   * Marks a user's email as verified.
   *
   * @param userId The ID of the user.
   */
  void verifyEmail(Long userId);

  // ==================== Role Management Methods ====================

  /**
   * Checks if a user has a specific role.
   *
   * @param user The user to check.
   * @param role The role to check for.
   * @return true if the user has the role, false otherwise.
   */
  boolean hasRole(User user, Role role);

  /**
   * Assigns a role to a user.
   *
   * @param userId The ID of the user.
   * @param role The role to assign.
   */
  void assignRole(Long userId, Role role);

  /**
   * Removes a role from a user.
   *
   * @param userId The ID of the user.
   * @param role The role to remove.
   */
  void removeRole(Long userId, Role role);

  // ==================== Password Management Methods ====================

  /**
   * Changes a user's password. Validates the old password before setting the new one.
   *
   * @param userId The ID of the user.
   * @param oldPassword The user's current password.
   * @param newPassword The new password to set.
   * @throws IllegalArgumentException if old password is incorrect or new password is invalid.
   */
  void changePassword(Long userId, String oldPassword, String newPassword);

  /**
   * Resets a user's password (used with password reset tokens). Does not validate old password.
   *
   * @param userId The ID of the user.
   * @param newPassword The new password to set.
   * @throws IllegalArgumentException if new password is invalid.
   */
  void resetPassword(Long userId, String newPassword);

  // ==================== Business Logic Methods ====================

  /**
   * Checks if a user can place orders. User must be active, email verified, and age verified.
   *
   * @param user The user to check.
   * @return true if the user can place orders, false otherwise.
   */
  boolean canPlaceOrders(User user);
}
