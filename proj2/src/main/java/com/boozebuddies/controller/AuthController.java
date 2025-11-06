package com.boozebuddies.controller;

import com.boozebuddies.dto.AuthenticationRequest;
import com.boozebuddies.dto.AuthenticationResponse;
import com.boozebuddies.dto.RefreshTokenRequest;
import com.boozebuddies.dto.RegisterUserRequest;
import com.boozebuddies.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** REST controller for handling authentication operations. */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthenticationService authenticationService;

  /**
   * Constructor injection for the authentication service.
   *
   * @param authenticationService the authentication service
   */
  @Autowired
  public AuthController(AuthenticationService authenticationService) {
    this.authenticationService = authenticationService;
  }

  /**
   * Registers a new user.
   *
   * @param request the registration request containing user details
   * @return the authentication response with JWT tokens
   */
  @PostMapping("/register")
  public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterUserRequest request) {
    AuthenticationResponse resp = authenticationService.register(request);
    return ResponseEntity.ok(resp);
  }

  /**
   * Authenticates a user and returns JWT tokens.
   *
   * @param request the authentication request with email and password
   * @return the authentication response with JWT tokens
   */
  @PostMapping("/login")
  public ResponseEntity<AuthenticationResponse> login(@RequestBody AuthenticationRequest request) {
    // Removed unreachable "request == null" check - Spring handles this via
    // HttpMessageNotReadableException
    if (request.getEmail() == null || request.getPassword() == null) {
      throw new IllegalArgumentException("Email and password are required");
    }

    AuthenticationResponse resp = authenticationService.login(request);
    return ResponseEntity.ok(resp);
  }

  @PostMapping("/driver/login")
  public ResponseEntity<AuthenticationResponse> driverLogin(
      @RequestBody AuthenticationRequest request) {
    if (request.getEmail() == null || request.getPassword() == null) {
      throw new IllegalArgumentException("Email and password are required");
    }

    AuthenticationResponse resp = authenticationService.driverLogin(request);
    return ResponseEntity.ok(resp);
  }

  @PostMapping("/refresh")
  public ResponseEntity<AuthenticationResponse> refresh(@RequestBody RefreshTokenRequest request) {
    AuthenticationResponse resp = authenticationService.refreshToken(request);
    return ResponseEntity.ok(resp);
  }

  /**
   * Logs out a user by invalidating their tokens.
   *
   * @param userId the ID of the user to log out
   * @return a no content response
   */
  @PostMapping("/logout/{userId}")
  public ResponseEntity<Void> logout(@PathVariable Long userId) {
    authenticationService.logout(userId);
    return ResponseEntity.noContent().build();
  }
}
