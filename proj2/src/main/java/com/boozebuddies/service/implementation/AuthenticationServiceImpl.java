// AuthenticationServiceImpl.java
package com.boozebuddies.service.implementation;

import com.boozebuddies.dto.AuthenticationRequest;
import com.boozebuddies.dto.AuthenticationResponse;
import com.boozebuddies.dto.RefreshTokenRequest;
import com.boozebuddies.dto.RegisterUserRequest;
import com.boozebuddies.entity.User;
import com.boozebuddies.exception.InvalidCredentialsException;
import com.boozebuddies.exception.InvalidTokenException;
import com.boozebuddies.mapper.UserMapper;
import com.boozebuddies.security.JwtUtil;
import com.boozebuddies.service.AuthenticationService;
import com.boozebuddies.service.DriverService;
import com.boozebuddies.service.UserService;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

  private final UserService userService;
  private final DriverService driverService;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtil jwtUtil;
  private final UserMapper userMapper;
  private final long refreshExpirationMs;

  @Autowired
  public AuthenticationServiceImpl(
      UserService userService,
      DriverService driverService,
      PasswordEncoder passwordEncoder,
      JwtUtil jwtUtil,
      UserMapper userMapper,
      @Value("${jwt.refreshExpirationMs:604800000}") long refreshExpirationMs) {
    this.userService = userService;
    this.driverService = driverService;
    this.passwordEncoder = passwordEncoder;
    this.jwtUtil = jwtUtil;
    this.userMapper = userMapper;
    this.refreshExpirationMs = refreshExpirationMs;
  }

  @Override
  @Transactional
  public AuthenticationResponse register(RegisterUserRequest request) {
    // Register user (UserService handles validation and password encryption)
    User user = userService.registerUser(request);

    // Generate access token (short-lived)
    String accessToken = jwtUtil.generateToken(user);

    // Generate refresh token (long-lived)
    String refreshToken = jwtUtil.generateToken(user);
    LocalDateTime refreshExpiry =
        Instant.now()
            .plusMillis(refreshExpirationMs)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime();

    // Save refresh token in database
    userService.saveRefreshToken(user.getId(), refreshToken, refreshExpiry);

    return AuthenticationResponse.builder()
        .token(accessToken)
        .refreshToken(refreshToken)
        .user(userMapper.toDTO(user))
        .message("Registration successful")
        .build();
  }

  @Override
  @Transactional
  public AuthenticationResponse login(AuthenticationRequest request) {
    // Validate request
    if (request == null || request.getEmail() == null || request.getPassword() == null) {
      throw new InvalidCredentialsException("Email and password are required");
    }

    // Find user by email
    User user =
        userService
            .findByEmail(request.getEmail())
            .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

    // Verify password
    if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
      throw new InvalidCredentialsException("Invalid email or password");
    }

    // Check if account is active
    if (!user.isActive()) {
      throw new InvalidCredentialsException("Account is deactivated. Please contact support.");
    }

    // Update last login timestamp
    userService.updateLastLogin(user.getId());

    // Generate tokens
    String accessToken = jwtUtil.generateToken(user);
    String refreshToken = jwtUtil.generateToken(user);
    LocalDateTime refreshExpiry =
        Instant.now()
            .plusMillis(refreshExpirationMs)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime();

    // Save refresh token
    userService.saveRefreshToken(user.getId(), refreshToken, refreshExpiry);

    return AuthenticationResponse.builder()
        .token(accessToken)
        .refreshToken(refreshToken)
        .user(userMapper.toDTO(user))
        .message("Login successful")
        .build();
  }

  @Override
  @Transactional
  public AuthenticationResponse driverLogin(AuthenticationRequest request) {
    // Validate request
    if (request == null || request.getEmail() == null || request.getPassword() == null) {
      throw new InvalidCredentialsException("Email and password are required");
    }

    // Find user by email
    User user =
        userService
            .findByEmail(request.getEmail())
            .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

    // Ensure user is a driver
    driverService
        .getDriverByUserId(user.getId())
        .orElseThrow(() -> new InvalidCredentialsException("User is not a driver"));

    // Verify password
    if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
      throw new InvalidCredentialsException("Invalid email or password");
    }

    // Check if account is active
    if (!user.isActive()) {
      throw new InvalidCredentialsException("Account is deactivated. Please contact support.");
    }

    // Update last login timestamp
    userService.updateLastLogin(user.getId());

    // Generate tokens
    String accessToken = jwtUtil.generateToken(user);
    String refreshToken = jwtUtil.generateToken(user);
    LocalDateTime refreshExpiry =
        Instant.now()
            .plusMillis(refreshExpirationMs)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime();

    // Save refresh token
    userService.saveRefreshToken(user.getId(), refreshToken, refreshExpiry);

    return AuthenticationResponse.builder()
        .token(accessToken)
        .refreshToken(refreshToken)
        .user(userMapper.toDTO(user))
        .message("Driver login successful")
        .build();
  }

  @Override
  @Transactional
  public AuthenticationResponse refreshToken(RefreshTokenRequest request) {
    // Validate request
    if (request == null || request.getRefreshToken() == null) {
      throw new InvalidTokenException("Refresh token is required");
    }

    String refreshToken = request.getRefreshToken();

    // Validate refresh token exists and is not expired
    if (!userService.isRefreshTokenValid(refreshToken)) {
      throw new InvalidTokenException("Invalid or expired refresh token");
    }

    // Find user by refresh token
    User user =
        userService
            .findByRefreshToken(refreshToken)
            .orElseThrow(() -> new InvalidTokenException("Refresh token not found"));

    // Verify token matches user
    String email = jwtUtil.extractUsername(refreshToken);
    if (!user.getEmail().equals(email)) {
      throw new InvalidTokenException("Token does not match user");
    }

    // Check if account is active
    if (!user.isActive()) {
      throw new InvalidCredentialsException("Account is deactivated");
    }

    // Generate new access token (keep same refresh token for simplicity)
    String newAccessToken = jwtUtil.generateToken(user);

    return AuthenticationResponse.builder()
        .token(newAccessToken)
        .refreshToken(refreshToken) // Return same refresh token
        .user(userMapper.toDTO(user))
        .message("Token refreshed successfully")
        .build();
  }

  @Override
  @Transactional
  public void logout(Long userId) {
    userService.revokeRefreshToken(userId);
  }
}
