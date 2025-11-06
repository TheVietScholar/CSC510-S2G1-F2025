package com.boozebuddies.controller;

import com.boozebuddies.dto.AuthenticationRequest;
import com.boozebuddies.dto.AuthenticationResponse;
import com.boozebuddies.dto.RefreshTokenRequest;
import com.boozebuddies.dto.RegisterUserRequest;
import com.boozebuddies.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthenticationService authenticationService;

  @Autowired
  public AuthController(AuthenticationService authenticationService) {
    this.authenticationService = authenticationService;
  }

  @PostMapping("/register")
  public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterUserRequest request) {
    AuthenticationResponse resp = authenticationService.register(request);
    return ResponseEntity.ok(resp);
  }

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

  @PostMapping("/logout/{userId}")
  public ResponseEntity<Void> logout(@PathVariable Long userId) {
    authenticationService.logout(userId);
    return ResponseEntity.noContent().build();
  }
}
