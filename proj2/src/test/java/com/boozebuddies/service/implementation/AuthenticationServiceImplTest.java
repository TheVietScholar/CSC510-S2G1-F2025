package com.boozebuddies.service.implementation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.boozebuddies.dto.AuthenticationRequest;
import com.boozebuddies.dto.AuthenticationResponse;
import com.boozebuddies.dto.RefreshTokenRequest;
import com.boozebuddies.dto.RegisterUserRequest;
import com.boozebuddies.dto.UserDTO;
import com.boozebuddies.entity.User;
import com.boozebuddies.exception.InvalidCredentialsException;
import com.boozebuddies.exception.InvalidTokenException;
import com.boozebuddies.mapper.UserMapper;
import com.boozebuddies.security.JwtUtil;
import com.boozebuddies.service.DriverService;
import com.boozebuddies.service.UserService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationService Tests")
class AuthenticationServiceImplTest {

  @Mock private UserService userService;
  @Mock private DriverService driverService;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private JwtUtil jwtUtil;
  @Mock private UserMapper userMapper;

  private AuthenticationServiceImpl authenticationService;

  private User testUser;
  private UserDTO testUserDTO;
  private RegisterUserRequest registerRequest;
  private AuthenticationRequest loginRequest;

  @BeforeEach
  void setUp() {
    // Manually construct the service with mocked dependencies
    authenticationService =
        new AuthenticationServiceImpl(
            userService,
            driverService,
            passwordEncoder,
            jwtUtil,
            userMapper,
            604800000L // refreshExpirationMs value
            );

    testUser =
        User.builder()
            .id(1L)
            .name("John Doe")
            .email("john@example.com")
            .passwordHash("$2a$10$encoded...")
            .phone("555-1234")
            .dateOfBirth(LocalDate.of(1990, 1, 1))
            .isActive(true)
            .ageVerified(true)
            .build();

    testUserDTO = new UserDTO();
    testUserDTO.setId(1L);
    testUserDTO.setEmail("john@example.com");
    testUserDTO.setName("John Doe");

    registerRequest = new RegisterUserRequest();
    registerRequest.setName("John Doe");
    registerRequest.setEmail("john@example.com");
    registerRequest.setPassword("Password123");
    registerRequest.setPhone("555-1234");
    registerRequest.setDateOfBirth(LocalDate.of(1990, 1, 1));

    loginRequest = new AuthenticationRequest();
    loginRequest.setEmail("john@example.com");
    loginRequest.setPassword("Password123");
  }

  // ==================== REGISTER TESTS ====================

  @Test
  @DisplayName("register should successfully register a new user")
  void register_ValidRequest_Success() {
    when(userService.registerUser(registerRequest)).thenReturn(testUser);
    when(jwtUtil.generateToken(testUser)).thenReturn("access-token", "refresh-token");
    when(userMapper.toDTO(testUser)).thenReturn(testUserDTO);
    doNothing()
        .when(userService)
        .saveRefreshToken(anyLong(), anyString(), any(LocalDateTime.class));

    AuthenticationResponse response = authenticationService.register(registerRequest);

    assertNotNull(response);
    assertEquals("access-token", response.getToken());
    assertEquals("refresh-token", response.getRefreshToken());
    assertEquals("john@example.com", response.getUser().getEmail());
    assertEquals("Registration successful", response.getMessage());

    verify(userService, times(1)).registerUser(registerRequest);
    verify(jwtUtil, times(2)).generateToken(testUser);
    verify(userService, times(1))
        .saveRefreshToken(eq(1L), eq("refresh-token"), any(LocalDateTime.class));
    verify(userMapper, times(1)).toDTO(testUser);
  }

  @Test
  @DisplayName("register should save refresh token with correct expiry")
  void register_SavesRefreshTokenWithExpiry() {
    when(userService.registerUser(registerRequest)).thenReturn(testUser);
    when(jwtUtil.generateToken(testUser)).thenReturn("access-token", "refresh-token");
    when(userMapper.toDTO(testUser)).thenReturn(testUserDTO);
    doNothing()
        .when(userService)
        .saveRefreshToken(anyLong(), anyString(), any(LocalDateTime.class));

    authenticationService.register(registerRequest);

    verify(userService)
        .saveRefreshToken(
            eq(1L),
            eq("refresh-token"),
            argThat(expiry -> expiry != null && expiry.isAfter(LocalDateTime.now())));
  }

  // ==================== LOGIN TESTS ====================

  @Test
  @DisplayName("login should successfully authenticate user with valid credentials")
  void login_ValidCredentials_Success() {
    when(userService.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
    when(passwordEncoder.matches("Password123", testUser.getPasswordHash())).thenReturn(true);
    when(jwtUtil.generateToken(testUser)).thenReturn("access-token", "refresh-token");
    when(userMapper.toDTO(testUser)).thenReturn(testUserDTO);
    doNothing().when(userService).updateLastLogin(1L);
    doNothing()
        .when(userService)
        .saveRefreshToken(anyLong(), anyString(), any(LocalDateTime.class));

    AuthenticationResponse response = authenticationService.login(loginRequest);

    assertNotNull(response);
    assertEquals("access-token", response.getToken());
    assertEquals("refresh-token", response.getRefreshToken());
    assertEquals("john@example.com", response.getUser().getEmail());
    assertEquals("Login successful", response.getMessage());

    verify(userService, times(1)).findByEmail("john@example.com");
    verify(passwordEncoder, times(1)).matches("Password123", testUser.getPasswordHash());
    verify(userService, times(1)).updateLastLogin(1L);
    verify(jwtUtil, times(2)).generateToken(testUser);
    verify(userService, times(1)).saveRefreshToken(eq(1L), anyString(), any(LocalDateTime.class));
  }

  @Test
  @DisplayName("login should throw exception when request is null")
  void login_NullRequest_ThrowsException() {
    InvalidCredentialsException exception =
        assertThrows(InvalidCredentialsException.class, () -> authenticationService.login(null));

    assertEquals("Email and password are required", exception.getMessage());
    verify(userService, never()).findByEmail(any());
  }

  @Test
  @DisplayName("login should throw exception when email is null")
  void login_NullEmail_ThrowsException() {
    loginRequest.setEmail(null);

    InvalidCredentialsException exception =
        assertThrows(
            InvalidCredentialsException.class, () -> authenticationService.login(loginRequest));

    assertEquals("Email and password are required", exception.getMessage());
    verify(userService, never()).findByEmail(any());
  }

  @Test
  @DisplayName("login should throw exception when password is null")
  void login_NullPassword_ThrowsException() {
    loginRequest.setPassword(null);

    InvalidCredentialsException exception =
        assertThrows(
            InvalidCredentialsException.class, () -> authenticationService.login(loginRequest));

    assertEquals("Email and password are required", exception.getMessage());
    verify(userService, never()).findByEmail(any());
  }

  @Test
  @DisplayName("login should throw exception when user not found")
  void login_UserNotFound_ThrowsException() {
    when(userService.findByEmail("john@example.com")).thenReturn(Optional.empty());

    InvalidCredentialsException exception =
        assertThrows(
            InvalidCredentialsException.class, () -> authenticationService.login(loginRequest));

    assertEquals("Invalid email or password", exception.getMessage());
    verify(userService, times(1)).findByEmail("john@example.com");
    verify(passwordEncoder, never()).matches(any(), any());
  }

  @Test
  @DisplayName("login should throw exception when password is incorrect")
  void login_IncorrectPassword_ThrowsException() {
    when(userService.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
    when(passwordEncoder.matches("WrongPassword", testUser.getPasswordHash())).thenReturn(false);

    loginRequest.setPassword("WrongPassword");

    InvalidCredentialsException exception =
        assertThrows(
            InvalidCredentialsException.class, () -> authenticationService.login(loginRequest));

    assertEquals("Invalid email or password", exception.getMessage());
    verify(passwordEncoder, times(1)).matches("WrongPassword", testUser.getPasswordHash());
    verify(jwtUtil, never()).generateToken(any());
  }

  @Test
  @DisplayName("login should throw exception when account is deactivated")
  void login_DeactivatedAccount_ThrowsException() {
    testUser.setActive(false);
    when(userService.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
    when(passwordEncoder.matches("Password123", testUser.getPasswordHash())).thenReturn(true);

    InvalidCredentialsException exception =
        assertThrows(
            InvalidCredentialsException.class, () -> authenticationService.login(loginRequest));

    assertEquals("Account is deactivated. Please contact support.", exception.getMessage());
    verify(userService, never()).updateLastLogin(any());
    verify(jwtUtil, never()).generateToken(any());
  }

  @Test
  @DisplayName("login should update last login timestamp")
  void login_UpdatesLastLogin() {
    when(userService.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
    when(passwordEncoder.matches("Password123", testUser.getPasswordHash())).thenReturn(true);
    when(jwtUtil.generateToken(testUser)).thenReturn("access-token", "refresh-token");
    when(userMapper.toDTO(testUser)).thenReturn(testUserDTO);
    doNothing().when(userService).updateLastLogin(1L);
    doNothing()
        .when(userService)
        .saveRefreshToken(anyLong(), anyString(), any(LocalDateTime.class));

    authenticationService.login(loginRequest);

    verify(userService, times(1)).updateLastLogin(1L);
  }

  // ==================== REFRESH TOKEN TESTS ====================

  @Test
  @DisplayName("refreshToken should generate new access token with valid refresh token")
  void refreshToken_ValidToken_Success() {
    RefreshTokenRequest request = new RefreshTokenRequest();
    request.setRefreshToken("valid-refresh-token");

    when(userService.isRefreshTokenValid("valid-refresh-token")).thenReturn(true);
    when(userService.findByRefreshToken("valid-refresh-token")).thenReturn(Optional.of(testUser));
    when(jwtUtil.extractUsername("valid-refresh-token")).thenReturn("john@example.com");
    when(jwtUtil.generateToken(testUser)).thenReturn("new-access-token");
    when(userMapper.toDTO(testUser)).thenReturn(testUserDTO);

    AuthenticationResponse response = authenticationService.refreshToken(request);

    assertNotNull(response);
    assertEquals("new-access-token", response.getToken());
    assertEquals("valid-refresh-token", response.getRefreshToken()); // Same refresh token
    assertEquals("john@example.com", response.getUser().getEmail());
    assertEquals("Token refreshed successfully", response.getMessage());

    verify(userService, times(1)).isRefreshTokenValid("valid-refresh-token");
    verify(userService, times(1)).findByRefreshToken("valid-refresh-token");
    verify(jwtUtil, times(1)).extractUsername("valid-refresh-token");
    verify(jwtUtil, times(1)).generateToken(testUser);
  }

  @Test
  @DisplayName("refreshToken should throw exception when request is null")
  void refreshToken_NullRequest_ThrowsException() {
    InvalidTokenException exception =
        assertThrows(InvalidTokenException.class, () -> authenticationService.refreshToken(null));

    assertEquals("Refresh token is required", exception.getMessage());
    verify(userService, never()).isRefreshTokenValid(any());
  }

  @Test
  @DisplayName("refreshToken should throw exception when token is null")
  void refreshToken_NullToken_ThrowsException() {
    RefreshTokenRequest request = new RefreshTokenRequest();
    request.setRefreshToken(null);

    InvalidTokenException exception =
        assertThrows(
            InvalidTokenException.class, () -> authenticationService.refreshToken(request));

    assertEquals("Refresh token is required", exception.getMessage());
    verify(userService, never()).isRefreshTokenValid(any());
  }

  @Test
  @DisplayName("refreshToken should throw exception when token is invalid")
  void refreshToken_InvalidToken_ThrowsException() {
    RefreshTokenRequest request = new RefreshTokenRequest();
    request.setRefreshToken("invalid-token");

    when(userService.isRefreshTokenValid("invalid-token")).thenReturn(false);

    InvalidTokenException exception =
        assertThrows(
            InvalidTokenException.class, () -> authenticationService.refreshToken(request));

    assertEquals("Invalid or expired refresh token", exception.getMessage());
    verify(userService, times(1)).isRefreshTokenValid("invalid-token");
    verify(userService, never()).findByRefreshToken(any());
  }

  @Test
  @DisplayName("refreshToken should throw exception when user not found")
  void refreshToken_UserNotFound_ThrowsException() {
    RefreshTokenRequest request = new RefreshTokenRequest();
    request.setRefreshToken("valid-token");

    when(userService.isRefreshTokenValid("valid-token")).thenReturn(true);
    when(userService.findByRefreshToken("valid-token")).thenReturn(Optional.empty());

    InvalidTokenException exception =
        assertThrows(
            InvalidTokenException.class, () -> authenticationService.refreshToken(request));

    assertEquals("Refresh token not found", exception.getMessage());
    verify(userService, times(1)).findByRefreshToken("valid-token");
  }

  @Test
  @DisplayName("refreshToken should throw exception when token doesn't match user")
  void refreshToken_TokenMismatch_ThrowsException() {
    RefreshTokenRequest request = new RefreshTokenRequest();
    request.setRefreshToken("valid-token");

    when(userService.isRefreshTokenValid("valid-token")).thenReturn(true);
    when(userService.findByRefreshToken("valid-token")).thenReturn(Optional.of(testUser));
    when(jwtUtil.extractUsername("valid-token")).thenReturn("different@example.com");

    InvalidTokenException exception =
        assertThrows(
            InvalidTokenException.class, () -> authenticationService.refreshToken(request));

    assertEquals("Token does not match user", exception.getMessage());
    verify(jwtUtil, times(1)).extractUsername("valid-token");
    verify(jwtUtil, never()).generateToken(any());
  }

  @Test
  @DisplayName("refreshToken should throw exception when account is deactivated")
  void refreshToken_DeactivatedAccount_ThrowsException() {
    RefreshTokenRequest request = new RefreshTokenRequest();
    request.setRefreshToken("valid-token");

    testUser.setActive(false);
    when(userService.isRefreshTokenValid("valid-token")).thenReturn(true);
    when(userService.findByRefreshToken("valid-token")).thenReturn(Optional.of(testUser));
    when(jwtUtil.extractUsername("valid-token")).thenReturn("john@example.com");

    InvalidCredentialsException exception =
        assertThrows(
            InvalidCredentialsException.class, () -> authenticationService.refreshToken(request));

    assertEquals("Account is deactivated", exception.getMessage());
    verify(jwtUtil, never()).generateToken(any());
  }

  // ==================== LOGOUT TESTS ====================

  @Test
  @DisplayName("logout should revoke refresh token")
  void logout_Success() {
    doNothing().when(userService).revokeRefreshToken(1L);

    authenticationService.logout(1L);

    verify(userService, times(1)).revokeRefreshToken(1L);
  }

  @Test
  @DisplayName("logout should handle null userId gracefully")
  void logout_NullUserId() {
    doThrow(new IllegalArgumentException("User ID cannot be null"))
        .when(userService)
        .revokeRefreshToken(null);

    assertThrows(IllegalArgumentException.class, () -> authenticationService.logout(null));
  }
}
