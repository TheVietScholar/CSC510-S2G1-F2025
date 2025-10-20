package com.boozebuddies.controller;

import com.boozebuddies.dto.LoginRequest;
import com.boozebuddies.dto.RegisterUserRequest;
import com.boozebuddies.dto.UserDTO;
import com.boozebuddies.entity.User;
import com.boozebuddies.mapper.UserMapper;
import com.boozebuddies.service.UserService;
import com.boozebuddies.service.ValidationService;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@DisplayName("UserController Tests")
class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private UserService userService;

  @MockBean
  private ValidationService validationService;

  @MockBean
  private UserMapper userMapper;

  // ==================== REGISTER TESTS ====================

  @Test
  @DisplayName("POST /api/users/register should return 201 on successful registration")
  void testRegisterSuccess() throws Exception {
    RegisterUserRequest request = new RegisterUserRequest();
    request.setName("John Doe");
    request.setEmail("john@example.com");
    request.setPassword("Password123");
    request.setPhone("555-123-4567");
    request.setDateOfBirth(LocalDate.of(1990, 1, 1));

    User user = User.builder()
        .id(1L)
        .name("John Doe")
        .email("john@example.com")
        .phone("555-123-4567")
        .dateOfBirth(LocalDate.of(1990, 1, 1))
        .ageVerified(true)
        .build();

    UserDTO userDTO = new UserDTO();
    userDTO.setId(1L);
    userDTO.setEmail("john@example.com");

    when(userService.registerUser(any())).thenReturn(user);
    when(userMapper.toDTO(user)).thenReturn(userDTO);

    mockMvc.perform(post("/api/users/register")
        .contentType("application/json")
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.email").value("john@example.com"));
  }

  @Test
  @DisplayName("POST /api/users/register should return 400 on invalid input")
  void testRegisterInvalidInput() throws Exception {
    RegisterUserRequest request = new RegisterUserRequest();
    request.setEmail("invalid");
    request.setPassword("weak");

    when(userService.registerUser(any())).thenThrow(new IllegalArgumentException("Invalid email"));

    mockMvc.perform(post("/api/users/register")
        .contentType("application/json")
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  // ==================== LOGIN TESTS ====================

  @Test
  @DisplayName("POST /api/users/login should return 200 on successful login")
  void testLoginSuccess() throws Exception {
    LoginRequest request = new LoginRequest("john@example.com", "Password123");

    User user = User.builder()
        .id(1L)
        .email("john@example.com")
        .build();

    UserDTO userDTO = new UserDTO();
    userDTO.setId(1L);
    userDTO.setEmail("john@example.com");

    when(userService.login("john@example.com", "Password123")).thenReturn(user);
    when(userMapper.toDTO(user)).thenReturn(userDTO);

    mockMvc.perform(post("/api/users/login")
        .contentType("application/json")
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("john@example.com"));
  }

  @Test
  @DisplayName("POST /api/users/login should return 400 on invalid credentials")
  void testLoginInvalidCredentials() throws Exception {
    LoginRequest request = new LoginRequest("john@example.com", "wrongpassword");

    when(userService.login("john@example.com", "wrongpassword")).thenReturn(null);

    mockMvc.perform(post("/api/users/login")
        .contentType("application/json")
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("POST /api/users/login should return 400 when email or password is null")
  void testLoginMissingCredentials() throws Exception {
    LoginRequest request = new LoginRequest(null, null);

    mockMvc.perform(post("/api/users/login")
        .contentType("application/json")
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  // ==================== GET USER TESTS ====================

  @Test
  @DisplayName("GET /api/users/{id} should return 200 with user data")
  void testGetUserByIdSuccess() throws Exception {
    User user = User.builder()
        .id(1L)
        .email("john@example.com")
        .build();

    UserDTO userDTO = new UserDTO();
    userDTO.setId(1L);
    userDTO.setEmail("john@example.com");

    when(userService.getUserById(1L)).thenReturn(java.util.Optional.of(user));
    when(userMapper.toDTO(user)).thenReturn(userDTO);

    mockMvc.perform(get("/api/users/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("john@example.com"));
  }

  @Test
  @DisplayName("GET /api/users/{id} should return 404 for non-existent user")
  void testGetUserByIdNotFound() throws Exception {
    when(userService.getUserById(999L)).thenReturn(java.util.Optional.empty());

    mockMvc.perform(get("/api/users/999"))
        .andExpect(status().isNotFound());
  }

  // ==================== DELETE USER TESTS ====================

  @Test
  @DisplayName("DELETE /api/users/{id} should return 200 on successful deletion")
  void testDeleteUserSuccess() throws Exception {
    when(userService.deleteUser(1L)).thenReturn(true);

    mockMvc.perform(delete("/api/users/1"))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("DELETE /api/users/{id} should return 404 for non-existent user")
  void testDeleteUserNotFound() throws Exception {
    when(userService.deleteUser(999L)).thenReturn(false);

    mockMvc.perform(delete("/api/users/999"))
        .andExpect(status().isNotFound());
  }


  @Test
  @DisplayName("GET /api/users should return 200 with list of users")
  void testGetAllUsersSuccess() throws Exception {
    UserDTO userDTO1 = new UserDTO();
    userDTO1.setId(1L);
    userDTO1.setEmail("john@example.com");

    UserDTO userDTO2 = new UserDTO();
    userDTO2.setId(2L);
    userDTO2.setEmail("jane@example.com");

    User user1 = User.builder().id(1L).email("john@example.com").build();
    User user2 = User.builder().id(2L).email("jane@example.com").build();

    when(userService.getAllUsers()).thenReturn(List.of(user1, user2));
    when(userMapper.toDTO(user1)).thenReturn(userDTO1);
    when(userMapper.toDTO(user2)).thenReturn(userDTO2);

    mockMvc.perform(get("/api/users"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].email").value("john@example.com"))
        .andExpect(jsonPath("$[1].email").value("jane@example.com"));
  }

  @Test
  @DisplayName("PUT /api/users/{id} should return 200 on successful update")
  void testUpdateUserSuccess() throws Exception {
    UserDTO updateDTO = new UserDTO();
    updateDTO.setEmail("newemail@example.com");

    User originalUser = User.builder().id(1L).email("john@example.com").build();
    User updatedUser = User.builder().id(1L).email("newemail@example.com").build();

    when(userMapper.toEntity(updateDTO)).thenReturn(originalUser);
    when(userService.updateUser(1L, originalUser)).thenReturn(updatedUser);
    when(userMapper.toDTO(updatedUser)).thenReturn(updateDTO);

    mockMvc.perform(put("/api/users/1")
        .contentType("application/json")
        .content(objectMapper.writeValueAsString(updateDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("newemail@example.com"));
  }

  @Test
  @DisplayName("POST /api/users/{id}/verify-age should return 200 when user is of legal age")
  void testVerifyAgeSuccess() throws Exception {
    User user = User.builder()
        .id(1L)
        .dateOfBirth(LocalDate.of(1990, 1, 1))
        .build();

    UserDTO userDTO = new UserDTO();
    userDTO.setId(1L);

    when(userService.getUserById(1L)).thenReturn(java.util.Optional.of(user));
    when(validationService.validateAge(user)).thenReturn(true);
    when(userService.updateUser(1L, user)).thenReturn(user);

    mockMvc.perform(post("/api/users/1/verify-age"))
        .andExpect(status().isOk())
        .andExpect(content().string("Age verification successful"));
  }

  @Test
  @DisplayName("POST /api/users/{id}/verify-age should return 400 when user is too young")
  void testVerifyAgeFailed() throws Exception {
    User user = User.builder()
        .id(1L)
        .dateOfBirth(LocalDate.of(2015, 1, 1))
        .build();

    when(userService.getUserById(1L)).thenReturn(java.util.Optional.of(user));
    when(validationService.validateAge(user)).thenReturn(false);

    mockMvc.perform(post("/api/users/1/verify-age"))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Age verification failed"));
  }
}