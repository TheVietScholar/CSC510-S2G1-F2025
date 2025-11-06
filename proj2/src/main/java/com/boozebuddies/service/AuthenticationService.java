package com.boozebuddies.service;

import com.boozebuddies.dto.AuthenticationRequest;
import com.boozebuddies.dto.AuthenticationResponse;
import com.boozebuddies.dto.RefreshTokenRequest;
import com.boozebuddies.dto.RegisterUserRequest;

/**
 * Service interface for handling authentication and authorization operations within the
 * BoozeBuddies application. Provides methods for user registration, login, token refresh, and
 * logout management.
 */
public interface AuthenticationService {

  /**
   * Registers a new user in the system and issues authentication tokens upon successful
   * registration.
   *
   * @param request The registration request containing user details such as email, password, and
   *     name.
   * @return An {@link AuthenticationResponse} containing the generated access and refresh tokens.
   */
  AuthenticationResponse register(RegisterUserRequest request);

  /**
   * Authenticates a user using their credentials (e.g., email and password) and issues JWT tokens.
   *
   * @param request The authentication request containing login credentials.
   * @return An {@link AuthenticationResponse} containing access and refresh tokens if
   *     authentication is successful.
   */
  AuthenticationResponse login(AuthenticationRequest request);

  /**
   * Generates a new access token using a valid refresh token.
   *
   * @param request The refresh token request containing the user's refresh token.
   * @return A new {@link AuthenticationResponse} containing refreshed authentication tokens.
   */
  AuthenticationResponse refreshToken(RefreshTokenRequest request);

  /**
   * Logs out a user by revoking or invalidating their active refresh token, preventing further use.
   *
   * @param userId The ID of the user to log out.
   */
  void logout(Long userId);

  /** Authenticate driver and return JWT tokens. */
  AuthenticationResponse driverLogin(AuthenticationRequest request);
}
