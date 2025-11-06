package com.boozebuddies.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.boozebuddies.config.TestSecurityConfig;
import com.boozebuddies.controller.UserController.MerchantAssignmentRequest;
import com.boozebuddies.controller.UserController.SetRolesRequest;
import com.boozebuddies.dto.UserDTO;
import com.boozebuddies.entity.User;
import com.boozebuddies.mapper.UserMapper;
import com.boozebuddies.model.Role;
import com.boozebuddies.security.JwtAuthenticationFilter;
import com.boozebuddies.service.PermissionService;
import com.boozebuddies.service.RoleService;
import com.boozebuddies.service.UserService;
import com.boozebuddies.service.ValidationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    controllers = UserController.class,
    excludeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = JwtAuthenticationFilter.class))
@AutoConfigureMockMvc(addFilters = false)
@Import(TestSecurityConfig.class)
@DisplayName("UserController Tests")
class UserControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockBean private UserService userService;
  @MockBean private ValidationService validationService;
  @MockBean private UserMapper userMapper;
  @MockBean private PermissionService permissionService;
  @MockBean private RoleService roleService;

  private User testUser;
  private UserDTO testUserDTO;
  private Authentication mockAuth;

  @BeforeEach
  void setUp() {
    testUser =
        User.builder()
            .id(1L)
            .name("John Doe")
            .email("john@example.com")
            .dateOfBirth(LocalDate.of(1990, 1, 1))
            .ageVerified(true)
            .build();
    testUserDTO = new UserDTO();
    testUserDTO.setId(1L);
    testUserDTO.setEmail("john@example.com");
    testUserDTO.setName("John Doe");
    mockAuth = mock(Authentication.class);
  }

  // ==================== GET BY ID ====================
  @Test
  @DisplayName("GET /api/users/{id} returns 200 for self or admin")
  void testGetUserByIdSuccess() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(testUser);
    when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
    when(userMapper.toDTO(testUser)).thenReturn(testUserDTO);

    mockMvc
        .perform(get("/api/users/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.email").value("john@example.com"));

    verify(userService).getUserById(1L);
  }

  @Test
  @DisplayName("GET /api/users/{id} returns 403 if user accesses another profile")
  void testGetUserByIdAccessDenied() throws Exception {
    User otherUser = User.builder().id(2L).build();
    when(permissionService.getAuthenticatedUser(any())).thenReturn(otherUser);

    mockMvc
        .perform(get("/api/users/1"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.message").value("You can only view your own profile"));
  }

  // ==================== GET CURRENT USER ====================
  @Test
  @DisplayName("GET /api/users/me returns 200 with current user data")
  void testGetCurrentUser() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(testUser);
    when(userMapper.toDTO(testUser)).thenReturn(testUserDTO);

    mockMvc
        .perform(get("/api/users/me"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Your profile retrieved successfully"))
        .andExpect(jsonPath("$.data.email").value("john@example.com"));
  }

  // ==================== UPDATE ====================
  @Test
  @DisplayName("PUT /api/users/{id} returns 200 for successful update")
  void testUpdateUserSuccess() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(testUser);
    when(userMapper.toEntity(any(UserDTO.class))).thenReturn(testUser);
    when(userService.updateUser(eq(1L), any(User.class))).thenReturn(testUser);
    when(userMapper.toDTO(testUser)).thenReturn(testUserDTO);

    mockMvc
        .perform(
            put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUserDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("User updated successfully"));
  }

  // ==================== VERIFY AGE ====================
  @Test
  @DisplayName("POST /api/users/{id}/verify-age returns 200 when age valid")
  void testVerifyAgeSuccess() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(testUser);
    when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
    when(validationService.validateAge(testUser)).thenReturn(true);
    when(userService.updateUser(1L, testUser)).thenReturn(testUser);
    when(userMapper.toDTO(testUser)).thenReturn(testUserDTO);

    mockMvc
        .perform(post("/api/users/1/verify-age"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Age verification successful"));
  }

  @Test
  @DisplayName("POST /api/users/{id}/verify-age returns 400 if too young")
  void testVerifyAgeFailed() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(testUser);
    when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
    when(validationService.validateAge(testUser)).thenReturn(false);

    mockMvc
        .perform(post("/api/users/1/verify-age"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Age verification failed"));
  }

  @Test
  @DisplayName("POST /api/users/{id}/verify-age returns 400 when service throws exception")
  void verifyAge_GenericException() throws Exception {
    // Authenticated user is admin so passes access check
    User adminUser = User.builder().id(99L).name("Admin").build();
    adminUser.setRoles(Set.of(Role.ADMIN));

    when(permissionService.getAuthenticatedUser(any(Authentication.class))).thenReturn(adminUser);
    when(userService.getUserById(1L)).thenThrow(new RuntimeException("Database error"));

    mockMvc
        .perform(post("/api/users/1/verify-age").with(user("admin").roles("ADMIN")))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("An error occurred during age verification"));
  }

  // ==================== DELETE ====================
  @Test
  @DisplayName("DELETE /api/users/{id} returns 200 when deleted")
  void testDeleteUserSuccess() throws Exception {
    when(userService.deleteUser(1L)).thenReturn(true);

    mockMvc
        .perform(delete("/api/users/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("User deleted successfully"));
  }

  @Test
  @DisplayName("DELETE /api/users/{id} returns 404 when not found")
  void testDeleteUserNotFound() throws Exception {
    when(userService.deleteUser(999L)).thenReturn(false);

    mockMvc.perform(delete("/api/users/999")).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("DELETE /api/users/{id}/roles/{role} returns 400 on exception")
  void testRemoveRole_Exception() throws Exception {
    Long userId = 1L;
    Role role = Role.ADMIN;

    // Mock roleService to throw an exception
    when(roleService.removeRole(userId, role)).thenThrow(new RuntimeException("Database error"));

    mockMvc
        .perform(delete("/api/users/{id}/roles/{role}", userId, role))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.containsString("Error removing role")));
  }

  // ==================== ROLE MANAGEMENT ====================
  @Test
  @DisplayName("POST /api/users/{id}/roles assigns a role successfully")
  void testAssignRole() throws Exception {
    UserController.RoleRequest request = new UserController.RoleRequest();
    request.setRole(Role.ADMIN);

    when(roleService.assignRole(1L, Role.ADMIN)).thenReturn(testUser);
    when(userMapper.toDTO(testUser)).thenReturn(testUserDTO);

    mockMvc
        .perform(
            post("/api/users/1/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Role assigned successfully"));
  }

  @Test
  @DisplayName("DELETE /api/users/{id}/roles/{role} removes a role successfully")
  void testRemoveRole() throws Exception {
    when(roleService.removeRole(1L, Role.ADMIN)).thenReturn(testUser);
    when(userMapper.toDTO(testUser)).thenReturn(testUserDTO);

    mockMvc
        .perform(delete("/api/users/1/roles/ADMIN"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Role removed successfully"));
  }

  @Test
  @DisplayName("PUT /api/users/{id}/roles sets all roles successfully")
  void testSetRoles() throws Exception {
    UserController.SetRolesRequest request = new UserController.SetRolesRequest();
    request.setRoles(Set.of(Role.ADMIN, Role.USER));

    when(roleService.setRoles(1L, request.getRoles())).thenReturn(testUser);
    when(userMapper.toDTO(testUser)).thenReturn(testUserDTO);

    mockMvc
        .perform(
            put("/api/users/1/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Roles updated successfully"));
  }

  @Test
  @DisplayName("POST /api/users/{id}/merchant assigns merchant successfully")
  void testAssignMerchant() throws Exception {
    UserController.MerchantAssignmentRequest request =
        new UserController.MerchantAssignmentRequest();
    request.setMerchantId(10L);

    when(roleService.assignMerchantToUser(1L, 10L)).thenReturn(testUser);
    when(userMapper.toDTO(testUser)).thenReturn(testUserDTO);

    mockMvc
        .perform(
            post("/api/users/1/merchant")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Merchant assigned successfully"));
  }

  @Test
  @DisplayName("PUT /api/users/{id} returns 403 if not self or admin")
  void testUpdateUserForbidden() throws Exception {
    User anotherUser = User.builder().id(2L).build();
    when(permissionService.getAuthenticatedUser(any())).thenReturn(anotherUser);

    mockMvc
        .perform(
            put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUserDTO)))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.message").value("You can only update your own profile"));
  }

  @Test
  @DisplayName("POST /api/users/{id}/roles returns 400 when invalid role")
  void testAssignRoleInvalid() throws Exception {
    UserController.RoleRequest request = new UserController.RoleRequest();
    request.setRole(Role.ADMIN);

    when(roleService.assignRole(1L, Role.ADMIN))
        .thenThrow(new com.boozebuddies.exception.ValidationException("Invalid role"));

    mockMvc
        .perform(
            post("/api/users/1/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Error assigning role: Invalid role"));
  }

  @Test
  @DisplayName("PUT /api/users/{id}/roles returns 400 on exception")
  void testSetRoles_Exception() throws Exception {
    Long userId = 1L;

    // Create request body
    SetRolesRequest request = new SetRolesRequest();
    request.setRoles(Set.of(Role.ADMIN, Role.USER));

    // Mock roleService to throw an exception
    when(roleService.setRoles(eq(userId), any())).thenThrow(new RuntimeException("Database error"));

    mockMvc
        .perform(
            put("/api/users/{id}/roles", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.containsString("Error updating roles")));
  }

  @Test
  @DisplayName("POST /api/users/{id}/merchant handles null merchantId")
  void testAssignMerchantNullId() throws Exception {
    UserController.MerchantAssignmentRequest request =
        new UserController.MerchantAssignmentRequest();
    request.setMerchantId(null); // legitimate null

    User updatedUser = testUser; // whatever you want the service to return
    when(roleService.assignMerchantToUser(1L, null)).thenReturn(updatedUser);
    when(userMapper.toDTO(updatedUser)).thenReturn(testUserDTO);

    mockMvc
        .perform(
            post("/api/users/1/merchant")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Merchant assigned successfully"));

    verify(roleService, times(1)).assignMerchantToUser(1L, null);
  }

  @Test
  @DisplayName("POST /api/users/{id}/merchant returns 400 on exception")
  void testAssignMerchant_Exception() throws Exception {
    Long userId = 1L;

    // Create request body
    MerchantAssignmentRequest request = new MerchantAssignmentRequest();
    request.setMerchantId(10L);

    // Mock roleService to throw an exception
    when(roleService.assignMerchantToUser(eq(userId), eq(10L)))
        .thenThrow(new RuntimeException("Database error"));

    mockMvc
        .perform(
            post("/api/users/{id}/merchant", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.containsString("Error assigning merchant")));
  }

  @Test
  @DisplayName("GET /api/users returns 200 with list of users")
  void getAllUsers_Success() throws Exception {
    // Mock some users
    List<User> userList = List.of(testUser);
    List<UserDTO> userDTOList = List.of(testUserDTO);

    when(userService.getAllUsers()).thenReturn(userList);
    when(userMapper.toDTO(testUser)).thenReturn(testUserDTO);

    mockMvc
        .perform(get("/api/users").with(user("admin").roles("ADMIN")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Users retrieved successfully"))
        .andExpect(jsonPath("$.data[0].id").value(testUserDTO.getId()));
  }

  @Test
  @DisplayName("GET /api/users returns 400 when service throws exception")
  void getAllUsers_Exception() throws Exception {
    when(userService.getAllUsers()).thenThrow(new RuntimeException("DB error"));

    mockMvc
        .perform(get("/api/users").with(user("admin").roles("ADMIN")))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("An error occurred retrieving users"));
  }

  // ==================== GET BY ID - Additional Coverage ====================

  @Test
  @DisplayName("GET /api/users/{id} returns 404 when user not found")
  void testGetUserById_NotFound() throws Exception {
    // Make the authenticated user an admin so they pass the permission check
    User adminUser = User.builder().id(99L).name("Admin").build();
    adminUser.setRoles(Set.of(Role.ADMIN));

    when(permissionService.getAuthenticatedUser(any())).thenReturn(adminUser);
    when(userService.getUserById(999L)).thenReturn(Optional.empty());

    mockMvc.perform(get("/api/users/999")).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("GET /api/users/{id} returns 400 for invalid ID")
  void testGetUserById_InvalidId() throws Exception {
    mockMvc
        .perform(get("/api/users/0"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Invalid user ID"));
  }

  @Test
  @DisplayName("GET /api/users/{id} returns 400 on generic exception")
  void testGetUserById_Exception() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(testUser);
    when(userService.getUserById(1L)).thenThrow(new RuntimeException("Database error"));

    mockMvc
        .perform(get("/api/users/1"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("An error occurred retrieving user"));
  }

  // ==================== GET CURRENT USER - Additional Coverage ====================

  @Test
  @DisplayName("GET /api/users/me returns 400 on exception")
  void testGetCurrentUser_Exception() throws Exception {
    when(permissionService.getAuthenticatedUser(any()))
        .thenThrow(new RuntimeException("Auth error"));

    mockMvc
        .perform(get("/api/users/me"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Error retrieving your profile"));
  }

  // ==================== UPDATE - Additional Coverage ====================

  @Test
  @DisplayName("PUT /api/users/{id} returns 400 for invalid ID")
  void testUpdateUser_InvalidId() throws Exception {
    mockMvc
        .perform(
            put("/api/users/0")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUserDTO)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Invalid user ID"));
  }

  @Test
  @DisplayName("PUT /api/users/{id} returns 404 when user not found")
  void testUpdateUser_NotFound() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(testUser);
    when(userMapper.toEntity(any(UserDTO.class))).thenReturn(testUser);
    when(userService.updateUser(eq(1L), any(User.class))).thenReturn(null);

    mockMvc
        .perform(
            put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUserDTO)))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("PUT /api/users/{id} returns 400 on IllegalArgumentException")
  void testUpdateUser_IllegalArgumentException() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(testUser);
    when(userMapper.toEntity(any(UserDTO.class))).thenReturn(testUser);
    when(userService.updateUser(eq(1L), any(User.class)))
        .thenThrow(new IllegalArgumentException("Invalid data"));

    mockMvc
        .perform(
            put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUserDTO)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Invalid data"));
  }

  @Test
  @DisplayName("PUT /api/users/{id} returns 400 on generic exception")
  void testUpdateUser_Exception() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(testUser);
    when(userMapper.toEntity(any(UserDTO.class))).thenReturn(testUser);
    when(userService.updateUser(eq(1L), any(User.class)))
        .thenThrow(new RuntimeException("Database error"));

    mockMvc
        .perform(
            put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUserDTO)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("An error occurred updating user"));
  }

  // ==================== VERIFY AGE - Additional Coverage ====================

  @Test
  @DisplayName("POST /api/users/{id}/verify-age returns 400 for invalid ID")
  void testVerifyAge_InvalidId() throws Exception {
    mockMvc
        .perform(post("/api/users/0/verify-age"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Invalid user ID"));
  }

  @Test
  @DisplayName("POST /api/users/{id}/verify-age returns 403 for access denied")
  void testVerifyAge_AccessDenied() throws Exception {
    User otherUser = User.builder().id(2L).build();
    when(permissionService.getAuthenticatedUser(any())).thenReturn(otherUser);

    mockMvc
        .perform(post("/api/users/1/verify-age"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.message").value("You can only verify your own age"));
  }

  // ==================== DELETE - Additional Coverage ====================

  @Test
  @DisplayName("DELETE /api/users/{id} returns 400 for invalid ID")
  void testDeleteUser_InvalidId() throws Exception {
    mockMvc
        .perform(delete("/api/users/-1"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Invalid user ID"));
  }

  @Test
  @DisplayName("DELETE /api/users/{id} returns 400 on exception")
  void testDeleteUser_Exception() throws Exception {
    when(userService.deleteUser(1L)).thenThrow(new RuntimeException("Database error"));

    mockMvc
        .perform(delete("/api/users/1"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("An error occurred deleting user"));
  }

  // ==================== ROLE ASSIGNMENT WITH MERCHANT ====================

  @Test
  @DisplayName("POST /api/users/{id}/roles assigns MERCHANT_ADMIN with merchantId")
  void testAssignRole_WithMerchantId() throws Exception {
    UserController.RoleRequest request = new UserController.RoleRequest();
    request.setRole(Role.MERCHANT_ADMIN);
    request.setMerchantId(5L);

    when(roleService.assignRoleWithMerchant(1L, Role.MERCHANT_ADMIN, 5L)).thenReturn(testUser);
    when(userMapper.toDTO(testUser)).thenReturn(testUserDTO);

    mockMvc
        .perform(
            post("/api/users/1/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Role assigned successfully"));

    verify(roleService).assignRoleWithMerchant(1L, Role.MERCHANT_ADMIN, 5L);
  }
}
