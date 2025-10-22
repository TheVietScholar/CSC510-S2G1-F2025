package com.boozebuddies.controller;

import com.boozebuddies.dto.LoginRequest;
import com.boozebuddies.dto.RegisterUserRequest;
import com.boozebuddies.dto.UserDTO;
import com.boozebuddies.entity.User;
import com.boozebuddies.mapper.UserMapper;
import com.boozebuddies.service.UserService;
import com.boozebuddies.service.ValidationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@DisplayName("UserController Tests")
class UserControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private UserService userService;

  @MockBean private ValidationService validationService;

  @MockBean private UserMapper userMapper;

  private User testUser;
  private UserDTO testUserDTO;
  private RegisterUserRequest registerRequest;
  private LoginRequest loginRequest;

  @BeforeEach
  void setUp() {
    testUser =
        User.builder()
            .id(1L)
            .name("John Doe")
            .email("john@example.com")
            .phone("555-123-4567")
            .dateOfBirth(LocalDate.of(1990, 1, 1))
            .ageVerified(true)
            .build();

    testUserDTO = new UserDTO();
    testUserDTO.setId(1L);
    testUserDTO.setEmail("john@example.com");
    testUserDTO.setName("John Doe");

    registerRequest = new RegisterUserRequest();
    registerRequest.setName("John Doe");
    registerRequest.setEmail("john@example.com");
    registerRequest.setPhone("555-123-4567");
    registerRequest.setDateOfBirth(LocalDate.of(1990, 1, 1));

    loginRequest = new LoginRequest("john@example.com", "Password123");
  }

  // ==================== REGISTER TESTS ====================

  @Test
  @DisplayName("POST /api/users/register should return 201 on successful registration")
  void testRegisterSuccess() throws Exception {
    when(userService.registerUser(any())).thenReturn(testUser);
    when(userMapper.toDTO(testUser)).thenReturn(testUserDTO);

    mockMvc
        .perform(
            post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("User registered successfully"))
        .andExpect(jsonPath("$.data.email").value("john@example.com"));

    verify(userService, times(1)).registerUser(any());
    verify(userMapper, times(1)).toDTO(testUser);
  }

  @Test
  @DisplayName("POST /api/users/register should return 400 on invalid input")
  void testRegisterInvalidInput() throws Exception {
    when(userService.registerUser(any()))
        .thenThrow(new IllegalArgumentException("Invalid email format"));

    mockMvc
        .perform(
            post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Invalid email format"));

    verify(userService, times(1)).registerUser(any());
  }

  @Test
  @DisplayName("POST /api/users/register should handle unexpected exceptions")
  void testRegisterUnexpectedException() throws Exception {
    when(userService.registerUser(any()))
        .thenThrow(new RuntimeException("Database error"));

    mockMvc
        .perform(
            post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("An error occurred during registration"));
  }

  // ==================== LOGIN TESTS ====================

  @Test
  @DisplayName("POST /api/users/login should return 200 on successful login")
  void testLoginSuccess() throws Exception {
    when(userService.login("john@example.com", "Password123")).thenReturn(testUser);
    when(userMapper.toDTO(testUser)).thenReturn(testUserDTO);

    mockMvc
        .perform(
            post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Login successful"))
        .andExpect(jsonPath("$.data.email").value("john@example.com"));

    verify(userService, times(1)).login("john@example.com", "Password123");
    verify(userMapper, times(1)).toDTO(testUser);
  }

  @Test
  @DisplayName("POST /api/users/login should return 400 on invalid credentials")
  void testLoginInvalidCredentials() throws Exception {
    when(userService.login("john@example.com", "wrongpassword")).thenReturn(null);

    mockMvc
        .perform(
            post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new LoginRequest("john@example.com", "wrongpassword"))))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Invalid email or password"));

    verify(userService, times(1)).login("john@example.com", "wrongpassword");
  }

  @Test
  @DisplayName("POST /api/users/login should return 400 when email is null")
  void testLoginNullEmail() throws Exception {
    LoginRequest nullEmailRequest = new LoginRequest(null, "Password123");

    mockMvc
        .perform(
            post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nullEmailRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Email and password are required"));

    verify(userService, never()).login(any(), any());
  }

  @Test
  @DisplayName("POST /api/users/login should return 400 when password is null")
  void testLoginNullPassword() throws Exception {
    LoginRequest nullPasswordRequest = new LoginRequest("john@example.com", null);

    mockMvc
        .perform(
            post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nullPasswordRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Email and password are required"));

    verify(userService, never()).login(any(), any());
  }

  @Test
  @DisplayName("POST /api/users/login should handle unexpected exceptions")
  void testLoginUnexpectedException() throws Exception {
    when(userService.login("john@example.com", "Password123"))
        .thenThrow(new RuntimeException("Database error"));

    mockMvc
        .perform(
            post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("An error occurred during login"));
  }

  // ==================== GET USER TESTS ====================

  @Test
  @DisplayName("GET /api/users/{id} should return 200 with user data")
  void testGetUserByIdSuccess() throws Exception {
    when(userService.getUserById(1L)).thenReturn(java.util.Optional.of(testUser));
    when(userMapper.toDTO(testUser)).thenReturn(testUserDTO);

    mockMvc
        .perform(get("/api/users/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("User retrieved successfully"))
        .andExpect(jsonPath("$.data.email").value("john@example.com"));

    verify(userService, times(1)).getUserById(1L);
    verify(userMapper, times(1)).toDTO(testUser);
  }

  @Test
  @DisplayName("GET /api/users/{id} should return 404 when user not found")
  void testGetUserByIdNotFound() throws Exception {
    when(userService.getUserById(999L)).thenReturn(java.util.Optional.empty());

    mockMvc.perform(get("/api/users/999")).andExpect(status().isNotFound());

    verify(userService, times(1)).getUserById(999L);
  }

  @Test
  @DisplayName("GET /api/users/{id} should return 400 when ID is invalid")
  void testGetUserByIdInvalidId() throws Exception {
    mockMvc
        .perform(get("/api/users/0"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Invalid user ID"));

    verify(userService, never()).getUserById(any());
  }

  @Test
  @DisplayName("GET /api/users/{id} should return 400 when ID is negative")
  void testGetUserByIdNegativeId() throws Exception {
    mockMvc
        .perform(get("/api/users/-1"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Invalid user ID"));

    verify(userService, never()).getUserById(any());
  }

  @Test
  @DisplayName("GET /api/users/{id} should handle service exceptions")
  void testGetUserByIdServiceException() throws Exception {
    when(userService.getUserById(1L))
        .thenThrow(new RuntimeException("Database error"));

    mockMvc
        .perform(get("/api/users/1"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("An error occurred retrieving user"));
  }

  // ==================== GET ALL USERS TESTS ====================

  @Test
  @DisplayName("GET /api/users should return 200 with list of users")
  void testGetAllUsersSuccess() throws Exception {
    UserDTO userDTO2 = new UserDTO();
    userDTO2.setId(2L);
    userDTO2.setEmail("jane@example.com");

    User user2 =
        User.builder()
            .id(2L)
            .name("Jane Doe")
            .email("jane@example.com")
            .build();

    when(userService.getAllUsers()).thenReturn(List.of(testUser, user2));
    when(userMapper.toDTO(testUser)).thenReturn(testUserDTO);
    when(userMapper.toDTO(user2)).thenReturn(userDTO2);

    mockMvc
        .perform(get("/api/users"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Users retrieved successfully"))
        .andExpect(jsonPath("$.data[0].email").value("john@example.com"))
        .andExpect(jsonPath("$.data[1].email").value("jane@example.com"));

    verify(userService, times(1)).getAllUsers();
  }

  @Test
  @DisplayName("GET /api/users should return 200 with empty list when no users exist")
  void testGetAllUsersEmpty() throws Exception {
    when(userService.getAllUsers()).thenReturn(List.of());

    mockMvc
        .perform(get("/api/users"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Users retrieved successfully"))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data.length()").value(0));

    verify(userService, times(1)).getAllUsers();
  }

  @Test
  @DisplayName("GET /api/users should handle service exceptions")
  void testGetAllUsersServiceException() throws Exception {
    when(userService.getAllUsers()).thenThrow(new RuntimeException("Database error"));

    mockMvc
        .perform(get("/api/users"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("An error occurred retrieving users"));
  }

  // ==================== UPDATE USER TESTS ====================

  @Test
  @DisplayName("PUT /api/users/{id} should return 200 on successful update")
  void testUpdateUserSuccess() throws Exception {
    UserDTO updateDTO = new UserDTO();
    updateDTO.setEmail("newemail@example.com");

    User updatedUser =
        User.builder()
            .id(1L)
            .email("newemail@example.com")
            .build();

    UserDTO updatedDTO = new UserDTO();
    updatedDTO.setId(1L);
    updatedDTO.setEmail("newemail@example.com");

    when(userMapper.toEntity(updateDTO)).thenReturn(updatedUser);
    when(userService.updateUser(1L, updatedUser)).thenReturn(updatedUser);
    when(userMapper.toDTO(updatedUser)).thenReturn(updatedDTO);

    mockMvc
        .perform(
            put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("User updated successfully"))
        .andExpect(jsonPath("$.data.email").value("newemail@example.com"));

    verify(userService, times(1)).updateUser(1L, updatedUser);
    verify(userMapper, times(1)).toDTO(updatedUser);
  }

  @Test
  @DisplayName("PUT /api/users/{id} should return 404 when user not found")
  void testUpdateUserNotFound() throws Exception {
    UserDTO updateDTO = new UserDTO();
    updateDTO.setEmail("newemail@example.com");

    when(userMapper.toEntity(updateDTO)).thenReturn(testUser);
    when(userService.updateUser(999L, testUser)).thenReturn(null);

    mockMvc
        .perform(
            put("/api/users/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
        .andExpect(status().isNotFound());

    verify(userService, times(1)).updateUser(999L, testUser);
  }

  @Test
  @DisplayName("PUT /api/users/{id} should return 400 when ID is invalid")
  void testUpdateUserInvalidId() throws Exception {
    UserDTO updateDTO = new UserDTO();
    updateDTO.setEmail("newemail@example.com");

    mockMvc
        .perform(
            put("/api/users/0")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Invalid user ID"));

    verify(userService, never()).updateUser(any(), any());
  }

  @Test
  @DisplayName("PUT /api/users/{id} should return 400 on IllegalArgumentException")
  void testUpdateUserIllegalArgumentException() throws Exception {
    UserDTO updateDTO = new UserDTO();
    updateDTO.setEmail("invalid-email");

    when(userMapper.toEntity(updateDTO)).thenReturn(testUser);
    when(userService.updateUser(1L, testUser))
        .thenThrow(new IllegalArgumentException("Invalid email format"));

    mockMvc
        .perform(
            put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Invalid email format"));
  }

  @Test
  @DisplayName("PUT /api/users/{id} should handle unexpected exceptions")
  void testUpdateUserUnexpectedException() throws Exception {
    UserDTO updateDTO = new UserDTO();
    updateDTO.setEmail("newemail@example.com");

    when(userMapper.toEntity(updateDTO)).thenReturn(testUser);
    when(userService.updateUser(1L, testUser))
        .thenThrow(new RuntimeException("Database error"));

    mockMvc
        .perform(
            put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("An error occurred updating user"));
  }

  // ==================== VERIFY AGE TESTS ====================

  @Test
  @DisplayName("POST /api/users/{id}/verify-age should return 200 when user is of legal age")
  void testVerifyAgeSuccess() throws Exception {
    when(userService.getUserById(1L)).thenReturn(java.util.Optional.of(testUser));
    when(validationService.validateAge(testUser)).thenReturn(true);
    when(userService.updateUser(1L, testUser)).thenReturn(testUser);
    when(userMapper.toDTO(testUser)).thenReturn(testUserDTO);

    mockMvc
        .perform(post("/api/users/1/verify-age"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Age verification successful"));

    verify(validationService, times(1)).validateAge(testUser);
    verify(userService, times(1)).updateUser(1L, testUser);
  }

  @Test
  @DisplayName("POST /api/users/{id}/verify-age should return 200 and set ageVerified to true")
  void testVerifyAgeSetsFlagSuccess() throws Exception {
    User unverifiedUser =
        User.builder()
            .id(1L)
            .dateOfBirth(LocalDate.of(1990, 1, 1))
            .ageVerified(false)
            .build();

    when(userService.getUserById(1L)).thenReturn(java.util.Optional.of(unverifiedUser));
    when(validationService.validateAge(unverifiedUser)).thenReturn(true);
    when(userService.updateUser(1L, unverifiedUser)).thenReturn(unverifiedUser);
    when(userMapper.toDTO(unverifiedUser)).thenReturn(testUserDTO);

    mockMvc
        .perform(post("/api/users/1/verify-age"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    verify(userService, times(1)).updateUser(1L, unverifiedUser);
  }

  @Test
  @DisplayName("POST /api/users/{id}/verify-age should return 400 when user is too young")
  void testVerifyAgeFailed() throws Exception {
    User youngUser =
        User.builder()
            .id(1L)
            .dateOfBirth(LocalDate.of(2015, 1, 1))
            .build();

    when(userService.getUserById(1L)).thenReturn(java.util.Optional.of(youngUser));
    when(validationService.validateAge(youngUser)).thenReturn(false);

    mockMvc
        .perform(post("/api/users/1/verify-age"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Age verification failed"));

    verify(validationService, times(1)).validateAge(youngUser);
    verify(userService, never()).updateUser(any(), any());
  }

  @Test
  @DisplayName("POST /api/users/{id}/verify-age should return 400 when user not found")
  void testVerifyAgeUserNotFound() throws Exception {
    when(userService.getUserById(999L)).thenReturn(java.util.Optional.empty());

    mockMvc
        .perform(post("/api/users/999/verify-age"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false));

    verify(userService, times(1)).getUserById(999L);
    verify(validationService, never()).validateAge(any());
  }

  @Test
  @DisplayName("POST /api/users/{id}/verify-age should return 400 when ID is invalid")
  void testVerifyAgeInvalidId() throws Exception {
    mockMvc
        .perform(post("/api/users/0/verify-age"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Invalid user ID"));

    verify(userService, never()).getUserById(any());
  }

  @Test
  @DisplayName("POST /api/users/{id}/verify-age should return 400 when ID is negative")
  void testVerifyAgeNegativeId() throws Exception {
    mockMvc
        .perform(post("/api/users/-5/verify-age"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Invalid user ID"));

    verify(userService, never()).getUserById(any());
  }

  @Test
  @DisplayName("POST /api/users/{id}/verify-age should handle unexpected exceptions")
  void testVerifyAgeUnexpectedException() throws Exception {
    when(userService.getUserById(1L)).thenReturn(java.util.Optional.of(testUser));
    when(validationService.validateAge(testUser))
        .thenThrow(new RuntimeException("Validation service error"));

    mockMvc
        .perform(post("/api/users/1/verify-age"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("An error occurred during age verification"));
  }

  // ==================== DELETE USER TESTS ====================

  @Test
  @DisplayName("DELETE /api/users/{id} should return 200 on successful deletion")
  void testDeleteUserSuccess() throws Exception {
    when(userService.deleteUser(1L)).thenReturn(true);

    mockMvc
        .perform(delete("/api/users/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("User deleted successfully"));

    verify(userService, times(1)).deleteUser(1L);
  }

  @Test
  @DisplayName("DELETE /api/users/{id} should return 404 when user not found")
  void testDeleteUserNotFound() throws Exception {
    when(userService.deleteUser(999L)).thenReturn(false);

    mockMvc
        .perform(delete("/api/users/999"))
        .andExpect(status().isNotFound());

    verify(userService, times(1)).deleteUser(999L);
  }

  @Test
  @DisplayName("DELETE /api/users/{id} should return 400 when ID is invalid")
  void testDeleteUserInvalidId() throws Exception {
    mockMvc
        .perform(delete("/api/users/0"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Invalid user ID"));

    verify(userService, never()).deleteUser(any());
  }

  @Test
  @DisplayName("DELETE /api/users/{id} should return 400 when ID is negative")
  void testDeleteUserNegativeId() throws Exception {
    mockMvc
        .perform(delete("/api/users/-1"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Invalid user ID"));

    verify(userService, never()).deleteUser(any());
  }

  @Test
  @DisplayName("DELETE /api/users/{id} should handle unexpected exceptions")
  void testDeleteUserUnexpectedException() throws Exception {
    when(userService.deleteUser(1L)).thenThrow(new RuntimeException("Database error"));

    mockMvc
        .perform(delete("/api/users/1"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("An error occurred deleting user"));
  }
}