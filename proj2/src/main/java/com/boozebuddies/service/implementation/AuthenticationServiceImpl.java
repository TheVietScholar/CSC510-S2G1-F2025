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
import com.boozebuddies.service.UserService;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

  private final UserService userService;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtil jwtUtil;
  private final UserMapper userMapper;
  private final long refreshExpirationMs;

  @Autowired
  public AuthenticationServiceImpl(
      UserService userService,
      PasswordEncoder passwordEncoder,
      JwtUtil jwtUtil,
      UserMapper userMapper,
      @Value("${jwt.refreshExpirationMs:604800000}") long refreshExpirationMs) {
    this.userService = userService;
    this.passwordEncoder = passwordEncoder;
    this.jwtUtil = jwtUtil;
    this.userMapper = userMapper;
    this.refreshExpirationMs = refreshExpirationMs;
  }

  @Override
  @Transactional
  public AuthenticationResponse register(RegisterUserRequest request) {
    User created = userService.registerUser(request);

    // Generate tokens
    String accessToken = jwtUtil.generateToken(created);
    // Use id + secret pattern for refresh tokens so we can store hashed secret in DB
    String refreshTokenId = UUID.randomUUID().toString();
    String refreshTokenSecret = UUID.randomUUID().toString();
    String clientRefreshToken = refreshTokenId + "." + refreshTokenSecret;
    LocalDateTime refreshExpiry =
        Instant.now()
            .plusMillis(refreshExpirationMs)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime();

    userService.saveRefreshToken(
        created.getId(), refreshTokenId, refreshTokenSecret, refreshExpiry);

    return AuthenticationResponse.builder()
        .token(accessToken)
        .refreshToken(clientRefreshToken)
        .user(userMapper.toDTO(created))
        .message("Registered and authenticated")
        .build();
  }

  @Override
  public AuthenticationResponse login(AuthenticationRequest request) {
    if (request == null || request.getEmail() == null || request.getPassword() == null) {
      throw new InvalidCredentialsException("Invalid login request");
    }

    User user =
        userService
            .findByEmail(request.getEmail())
            .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

    if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
      throw new InvalidCredentialsException("Invalid credentials");
    }

    userService.updateLastLogin(user.getId());

    String accessToken = jwtUtil.generateToken(user);
    String refreshTokenId = UUID.randomUUID().toString();
    String refreshTokenSecret = UUID.randomUUID().toString();
    String clientRefreshToken = refreshTokenId + "." + refreshTokenSecret;
    LocalDateTime refreshExpiry =
        Instant.now()
            .plusMillis(refreshExpirationMs)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime();

    userService.saveRefreshToken(user.getId(), refreshTokenId, refreshTokenSecret, refreshExpiry);

    return AuthenticationResponse.builder()
        .token(accessToken)
        .refreshToken(clientRefreshToken)
        .user(userMapper.toDTO(user))
        .message("Authenticated")
        .build();
  }

  @Override
  public AuthenticationResponse refreshToken(RefreshTokenRequest request) {
    if (request == null || request.getRefreshToken() == null) {
      throw new InvalidTokenException("Missing refresh token");
    }

    if (!userService.isRefreshTokenValid(request.getRefreshToken())) {
      throw new InvalidTokenException("Refresh token invalid or expired");
    }

    // Extract id from token and lookup user
    String[] parts = request.getRefreshToken().split("\\.");
    if (parts.length != 2) {
      throw new InvalidTokenException("Invalid refresh token format");
    }
    String id = parts[0];

    User user =
        userService
            .findByRefreshTokenId(id)
            .orElseThrow(
                () -> new InvalidTokenException("Refresh token not associated with a user"));

    // Rotate: issue new refresh token and persist hashed secret
    String newRefreshTokenId = UUID.randomUUID().toString();
    String newRefreshSecret = UUID.randomUUID().toString();
    String clientNewRefresh = newRefreshTokenId + "." + newRefreshSecret;
    LocalDateTime refreshExpiry =
        Instant.now()
            .plusMillis(refreshExpirationMs)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime();
    userService.saveRefreshToken(user.getId(), newRefreshTokenId, newRefreshSecret, refreshExpiry);

    String accessToken = jwtUtil.generateToken(user);

    return AuthenticationResponse.builder()
        .token(accessToken)
        .refreshToken(clientNewRefresh)
        .user(userMapper.toDTO(user))
        .message("Token refreshed")
        .build();
  }

  @Override
  public void logout(Long userId) {
    userService.revokeRefreshToken(userId);
  }
}
