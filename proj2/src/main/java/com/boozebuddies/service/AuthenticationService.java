// AuthenticationService.java (Interface)
package com.boozebuddies.service;

import com.boozebuddies.dto.AuthenticationRequest;
import com.boozebuddies.dto.AuthenticationResponse;
import com.boozebuddies.dto.RefreshTokenRequest;
import com.boozebuddies.dto.RegisterUserRequest;

public interface AuthenticationService {
  
  /**
   * Register a new user and return authentication tokens.
   */
  AuthenticationResponse register(RegisterUserRequest request);
  
  /**
   * Authenticate user and return JWT tokens.
   */
  AuthenticationResponse login(AuthenticationRequest request);
  
  /**
   * Refresh access token using refresh token.
   */
  AuthenticationResponse refreshToken(RefreshTokenRequest request);
  
  /**
   * Logout user by revoking refresh token.
   */
  void logout(Long userId);
}