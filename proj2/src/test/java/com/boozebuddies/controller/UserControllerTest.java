package com.boozebuddies.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.boozebuddies.config.TestSecurityConfig;
import com.boozebuddies.dto.ApiResponse;
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
    testUser = User.builder()
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

    mockMvc.perform(get("/api/users/1"))
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

    mockMvc.perform(get("/api/users/1"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.message").value("You can only view your own profile"));
  }

  // ==================== GET CURRENT USER ====================
  @Test
  @DisplayName("GET /api/users/me returns 200 with current user data")
  void testGetCurrentUser() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(testUser);
    when(userMapper.toDTO(testUser)).thenReturn(testUserDTO);

    mockMvc.perform(get("/api/users/me"))
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

    mockMvc.perform(put("/api/users/1")
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

    mockMvc.perform(post("/api/users/1/verify-age"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Age verification successful"));
  }

  @Test
  @DisplayName("POST /api/users/{id}/verify-age returns 400 if too young")
  void testVerifyAgeFailed() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(testUser);
    when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
    when(validationService.validateAge(testUser)).thenReturn(false);

    mockMvc.perform(post("/api/users/1/verify-age"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Age verification failed"));
  }

  // ==================== DELETE ====================
  @Test
  @DisplayName("DELETE /api/users/{id} returns 200 when deleted")
  void testDeleteUserSuccess() throws Exception {
    when(userService.deleteUser(1L)).thenReturn(true);

    mockMvc.perform(delete("/api/users/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("User deleted successfully"));
  }

  @Test
  @DisplayName("DELETE /api/users/{id} returns 404 when not found")
  void testDeleteUserNotFound() throws Exception {
    when(userService.deleteUser(999L)).thenReturn(false);

    mockMvc.perform(delete("/api/users/999"))
        .andExpect(status().isNotFound());
  }

  // ==================== ROLE MANAGEMENT ====================
  @Test
  @DisplayName("POST /api/users/{id}/roles assigns a role successfully")
  void testAssignRole() throws Exception {
    UserController.RoleRequest request = new UserController.RoleRequest();
    request.setRole(Role.ADMIN);

    when(roleService.assignRole(1L, Role.ADMIN)).thenReturn(testUser);
    when(userMapper.toDTO(testUser)).thenReturn(testUserDTO);

    mockMvc.perform(post("/api/users/1/roles")
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

    mockMvc.perform(delete("/api/users/1/roles/ADMIN"))
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

    mockMvc.perform(put("/api/users/1/roles")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Roles updated successfully"));
  }

  @Test
  @DisplayName("POST /api/users/{id}/merchant assigns merchant successfully")
  void testAssignMerchant() throws Exception {
    UserController.MerchantAssignmentRequest request = new UserController.MerchantAssignmentRequest();
    request.setMerchantId(10L);

    when(roleService.assignMerchantToUser(1L, 10L)).thenReturn(testUser);
    when(userMapper.toDTO(testUser)).thenReturn(testUserDTO);

    mockMvc.perform(post("/api/users/1/merchant")
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

    mockMvc.perform(put("/api/users/1")
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

    mockMvc.perform(post("/api/users/1/roles")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Error assigning role: Invalid role"));
  }

  @Test
  @DisplayName("POST /api/users/{id}/merchant handles null merchantId")
  void testAssignMerchantNullId() throws Exception {
      UserController.MerchantAssignmentRequest request = new UserController.MerchantAssignmentRequest();
      request.setMerchantId(null); // legitimate null

      User updatedUser = testUser; // whatever you want the service to return
      when(roleService.assignMerchantToUser(1L, null)).thenReturn(updatedUser);
      when(userMapper.toDTO(updatedUser)).thenReturn(testUserDTO);

      mockMvc.perform(post("/api/users/1/merchant")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.message").value("Merchant assigned successfully"));

      verify(roleService, times(1)).assignMerchantToUser(1L, null);
  }


  
}
