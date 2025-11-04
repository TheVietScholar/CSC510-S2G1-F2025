package com.boozebuddies.service;

import com.boozebuddies.dto.RegisterUserRequest;
import com.boozebuddies.entity.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserService {

  // ==================== User Registration Methods ====================
  /**
   * Registers a new user in the system.
   *
   * @param request The registration request containing user details.
   * @return The registered user entity.
   */
  public User registerUser(RegisterUserRequest request);

  // ==================== User Lookup Methods ====================
  /**
   * Finds a user by their email address.
   *
   * @param email The user's email address.
   * @return An Optional containing the user if found, otherwise empty.
   */
  Optional<User> findByEmail(String email);

  /**
   * /** Retrieves a user by their unique ID.
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

  /**
   * Updates the user's last login timestamp.
   *
   * @param userId
   */
  public void updateLastLogin(Long userId);

  // ==================== Token Management Methods ====================

  /**
   * Saves a refresh token for the user.
   *
   * @param userId The ID of the user.
   * @param refreshToken The refresh token to save.
   * @param expiryDate The expiry date of the refresh token.
   */
  void saveRefreshToken(Long userId, String refreshToken, LocalDateTime expiryDate);

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
   * Revokes a user's refresh token (logout).
   *
   * @param userId The ID of the user.
   */
  void revokeRefreshToken(Long userId);

  // ==================== Business Logic Methods ====================

  /**
   * Checks if a user can place orders. User must be active and age verified.
   *
   * @param user The user to check.
   * @return true if the user can place orders, false otherwise.
   */
  boolean canPlaceOrders(User user);
}
