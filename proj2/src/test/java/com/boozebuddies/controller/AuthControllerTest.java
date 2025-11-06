package com.boozebuddies.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.boozebuddies.dto.*;
import com.boozebuddies.exception.GlobalExceptionHandler;
import com.boozebuddies.service.AuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AuthControllerTest {

  private final AuthenticationService authenticationService =
      Mockito.mock(AuthenticationService.class);
  private final AuthController authController = new AuthController(authenticationService);
  private final MockMvc mockMvc =
      MockMvcBuilders.standaloneSetup(authController)
          .setControllerAdvice(new GlobalExceptionHandler())
          .build();
  private final ObjectMapper objectMapper = new ObjectMapper();

  // REGISTER TESTS
  @Test
  void registerSuccessfullyCreatesUser() throws Exception {
    RegisterUserRequest request = new RegisterUserRequest();
    request.setEmail("newuser@example.com");
    request.setPassword("password123");
    request.setName("newuser");

    AuthenticationResponse response = new AuthenticationResponse();
    response.setToken("new-user-jwt-token");

    Mockito.when(authenticationService.register(Mockito.any(RegisterUserRequest.class)))
        .thenReturn(response);

    mockMvc
        .perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").value("new-user-jwt-token"));

    Mockito.verify(authenticationService).register(Mockito.any(RegisterUserRequest.class));
  }

  // LOGIN TESTS
  @Test
  void loginReturnsToken() throws Exception {
    AuthenticationRequest request = new AuthenticationRequest();
    request.setEmail("test@example.com");
    request.setPassword("password");

    AuthenticationResponse response = new AuthenticationResponse();
    response.setToken("fake-jwt-token");

    Mockito.when(authenticationService.login(Mockito.any(AuthenticationRequest.class)))
        .thenReturn(response);

    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").value("fake-jwt-token"));
  }

  @Test
  void loginThrowsExceptionWhenEmailIsNull() throws Exception {
    AuthenticationRequest request = new AuthenticationRequest();
    request.setPassword("password");

    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Email and password are required"));
  }

  @Test
  void loginThrowsExceptionWhenPasswordIsNull() throws Exception {
    AuthenticationRequest request = new AuthenticationRequest();
    request.setEmail("test@example.com");

    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Email and password are required"));
  }

  @Test
  void loginThrowsExceptionWhenBothEmailAndPasswordAreNull() throws Exception {
    AuthenticationRequest request = new AuthenticationRequest();

    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Email and password are required"));
  }

  // REFRESH TOKEN TESTS
  @Test
  void refreshTokenSuccessfullyReturnsNewToken() throws Exception {
    RefreshTokenRequest request = new RefreshTokenRequest();
    request.setRefreshToken("old-refresh-token");

    AuthenticationResponse response = new AuthenticationResponse();
    response.setToken("new-jwt-token");
    response.setRefreshToken("new-refresh-token");

    Mockito.when(authenticationService.refreshToken(Mockito.any(RefreshTokenRequest.class)))
        .thenReturn(response);

    mockMvc
        .perform(
            post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").value("new-jwt-token"))
        .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"));

    Mockito.verify(authenticationService).refreshToken(Mockito.any(RefreshTokenRequest.class));
  }

  // LOGOUT TESTS
  @Test
  void logoutSuccessfullyLogsOutUser() throws Exception {
    Long userId = 123L;

    Mockito.doNothing().when(authenticationService).logout(userId);

    mockMvc.perform(post("/api/auth/logout/" + userId)).andExpect(status().isNoContent());

    Mockito.verify(authenticationService).logout(userId);
  }

  @Test
  void logoutHandlesDifferentUserId() throws Exception {
    Long userId = 999L;

    Mockito.doNothing().when(authenticationService).logout(userId);

    mockMvc.perform(post("/api/auth/logout/" + userId)).andExpect(status().isNoContent());

    Mockito.verify(authenticationService).logout(999L);
  }

  // ==================== DRIVER LOGIN TESTS ====================

  @Test
  void driverLoginReturnsToken() throws Exception {
    AuthenticationRequest request = new AuthenticationRequest();
    request.setEmail("driver@example.com");
    request.setPassword("password");

    AuthenticationResponse response = new AuthenticationResponse();
    response.setToken("driver-jwt-token");
    response.setRefreshToken("driver-refresh-token");

    Mockito.when(authenticationService.driverLogin(Mockito.any(AuthenticationRequest.class)))
        .thenReturn(response);

    mockMvc
        .perform(
            post("/api/auth/driver/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").value("driver-jwt-token"))
        .andExpect(jsonPath("$.refreshToken").value("driver-refresh-token"));

    Mockito.verify(authenticationService).driverLogin(Mockito.any(AuthenticationRequest.class));
  }

  @Test
  void driverLoginThrowsExceptionWhenEmailIsNull() throws Exception {
    AuthenticationRequest request = new AuthenticationRequest();
    request.setPassword("password");

    mockMvc
        .perform(
            post("/api/auth/driver/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Email and password are required"));
  }

  @Test
  void driverLoginThrowsExceptionWhenPasswordIsNull() throws Exception {
    AuthenticationRequest request = new AuthenticationRequest();
    request.setEmail("driver@example.com");

    mockMvc
        .perform(
            post("/api/auth/driver/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Email and password are required"));
  }

  @Test
  void driverLoginThrowsExceptionWhenBothEmailAndPasswordAreNull() throws Exception {
    AuthenticationRequest request = new AuthenticationRequest();

    mockMvc
        .perform(
            post("/api/auth/driver/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Email and password are required"));
  }

  // ==================== REGISTER EXCEPTION TESTS ====================

  @Test
  void registerHandlesException() throws Exception {
    RegisterUserRequest request = new RegisterUserRequest();
    request.setEmail("newuser@example.com");
    request.setPassword("password123");
    request.setName("newuser");

    Mockito.when(authenticationService.register(Mockito.any(RegisterUserRequest.class)))
        .thenThrow(new RuntimeException("Registration failed"));

    mockMvc
        .perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isInternalServerError());
  }

  // ==================== LOGIN EXCEPTION TESTS ====================

  @Test
  void loginHandlesException() throws Exception {
    AuthenticationRequest request = new AuthenticationRequest();
    request.setEmail("test@example.com");
    request.setPassword("password");

    Mockito.when(authenticationService.login(Mockito.any(AuthenticationRequest.class)))
        .thenThrow(new RuntimeException("Login failed"));

    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isInternalServerError());
  }

  @Test
  void driverLoginHandlesException() throws Exception {
    AuthenticationRequest request = new AuthenticationRequest();
    request.setEmail("driver@example.com");
    request.setPassword("password");

    Mockito.when(authenticationService.driverLogin(Mockito.any(AuthenticationRequest.class)))
        .thenThrow(new RuntimeException("Driver login failed"));

    mockMvc
        .perform(
            post("/api/auth/driver/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isInternalServerError());
  }

  // ==================== REFRESH TOKEN EXCEPTION TESTS ====================

  @Test
  void refreshTokenHandlesException() throws Exception {
    RefreshTokenRequest request = new RefreshTokenRequest();
    request.setRefreshToken("old-refresh-token");

    Mockito.when(authenticationService.refreshToken(Mockito.any(RefreshTokenRequest.class)))
        .thenThrow(new RuntimeException("Token refresh failed"));

    mockMvc
        .perform(
            post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isInternalServerError());
  }

  @Test
  void refreshTokenHandlesNullRefreshToken() throws Exception {
    RefreshTokenRequest request = new RefreshTokenRequest();
    request.setRefreshToken(null);

    // GlobalExceptionHandler catches IllegalArgumentException and returns 400
    Mockito.when(authenticationService.refreshToken(Mockito.any(RefreshTokenRequest.class)))
        .thenThrow(new IllegalArgumentException("Refresh token is required"));

    mockMvc
        .perform(
            post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  // ==================== LOGOUT EXCEPTION TESTS ====================

  @Test
  void logoutHandlesException() throws Exception {
    Long userId = 123L;

    Mockito.doThrow(new RuntimeException("Logout failed"))
        .when(authenticationService)
        .logout(userId);

    mockMvc.perform(post("/api/auth/logout/" + userId)).andExpect(status().isInternalServerError());
  }

  @Test
  void logoutHandlesNullUserId() throws Exception {
    // Spring will handle null path variable, but let's test with 0 or negative
    Mockito.doNothing().when(authenticationService).logout(0L);

    mockMvc.perform(post("/api/auth/logout/0")).andExpect(status().isNoContent());
  }

  // ==================== REGISTER EDGE CASES ====================

  @Test
  void registerHandlesEmptyRequest() throws Exception {
    // Spring will handle null request body via HttpMessageNotReadableException
    // But we can test with empty body - this should work if service handles it
    AuthenticationResponse response = new AuthenticationResponse();
    response.setToken("token");

    Mockito.when(authenticationService.register(Mockito.any(RegisterUserRequest.class)))
        .thenReturn(response);

    mockMvc
        .perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isOk());
  }

  @Test
  void registerReturnsResponseWithRefreshToken() throws Exception {
    RegisterUserRequest request = new RegisterUserRequest();
    request.setEmail("newuser@example.com");
    request.setPassword("password123");
    request.setName("newuser");

    AuthenticationResponse response = new AuthenticationResponse();
    response.setToken("new-user-jwt-token");
    response.setRefreshToken("new-refresh-token");

    Mockito.when(authenticationService.register(Mockito.any(RegisterUserRequest.class)))
        .thenReturn(response);

    mockMvc
        .perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").value("new-user-jwt-token"))
        .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"));
  }
}
