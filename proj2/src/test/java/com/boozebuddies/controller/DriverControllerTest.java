package com.boozebuddies.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.boozebuddies.config.TestSecurityConfig;
import com.boozebuddies.dto.DriverDTO;
import com.boozebuddies.entity.Driver;
import com.boozebuddies.entity.User;
import com.boozebuddies.exception.DriverNotFoundException;
import com.boozebuddies.mapper.DriverMapper;
import com.boozebuddies.model.CertificationStatus;
import com.boozebuddies.model.Role;
import com.boozebuddies.security.JwtAuthenticationFilter;
import com.boozebuddies.service.DriverService;
import com.boozebuddies.service.PermissionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    controllers = DriverController.class,
    excludeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = JwtAuthenticationFilter.class))
@AutoConfigureMockMvc(addFilters = false)
@Import(TestSecurityConfig.class)
@DisplayName("DriverController Tests")
public class DriverControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private DriverService driverService;

  @MockBean private DriverMapper driverMapper;

  @MockBean private PermissionService permissionService;

  @MockBean private com.boozebuddies.service.UserService userService;

  private Driver testDriver;
  private User testDriverUser;
  private DriverDTO testDriverDTO;

  @BeforeEach
  void setUp() {
    testDriver =
        Driver.builder()
            .id(1L)
            .name("John Doe")
            .email("john@example.com")
            .phone("1234567890")
            .vehicleType("Car")
            .licensePlate("ABC123")
            .build();

    testDriverUser =
        User.builder()
            .id(10L)
            .name("John Doe")
            .email("john@example.com")
            .phone("1234567890")
            .build();

    testDriver.setUser(testDriverUser);
    testDriverUser.setDriver(testDriver);
    testDriverUser.addRole(Role.DRIVER);

    testDriverDTO =
        DriverDTO.builder()
            .id(1L)
            .name("John Doe")
            .email("john@example.com")
            .phone("1234567890")
            .vehicleType("Car")
            .licensePlate("ABC123")
            .build();
  }

  // ==================== REGISTER DRIVER TESTS ====================

  @Test
  @DisplayName("POST /api/drivers/register returns 201 and ApiResponse on success")
  void registerDriver_success() throws Exception {
    when(driverMapper.toEntity(any(DriverDTO.class))).thenReturn(testDriver);
    when(driverService.registerDriver(testDriver)).thenReturn(testDriver);
    when(driverMapper.toDTO(testDriver)).thenReturn(testDriverDTO);

    mockMvc
        .perform(
            post("/api/drivers/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testDriverDTO)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Driver registered successfully"))
        .andExpect(jsonPath("$.data.id").value(1));
  }

  @Test
  @DisplayName("POST /api/drivers/register returns 400 on IllegalArgumentException")
  void registerDriver_illegalArgumentException_returnsBadRequest() throws Exception {
    when(driverMapper.toEntity(any(DriverDTO.class))).thenReturn(testDriver);
    when(driverService.registerDriver(testDriver))
        .thenThrow(new IllegalArgumentException("Invalid driver data"));

    mockMvc
        .perform(
            post("/api/drivers/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testDriverDTO)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Driver registration failed: Invalid driver data"));
  }

  @Test
  @DisplayName("POST /api/drivers/register returns 400 on exception")
  void registerDriver_exception_returnsBadRequest() throws Exception {
    when(driverMapper.toEntity(any(DriverDTO.class))).thenReturn(testDriver);
    when(driverService.registerDriver(testDriver)).thenThrow(new RuntimeException("boom"));

    mockMvc
        .perform(
            post("/api/drivers/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testDriverDTO)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.startsWith("Driver registration failed: boom")));
  }

  // ==================== UPDATE CERTIFICATION STATUS TESTS ====================

  @Test
  @DisplayName("PUT /api/drivers/{id}/certification returns 200 on success")
  void updateCertificationStatus_success() throws Exception {
    testDriver.setCertificationStatus(CertificationStatus.APPROVED);
    when(driverService.updateCertificationStatus(1L, CertificationStatus.APPROVED))
        .thenReturn(testDriver);
    when(driverMapper.toDTO(testDriver)).thenReturn(testDriverDTO);

    mockMvc
        .perform(put("/api/drivers/1/certification?status=APPROVED"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Certification status updated successfully"))
        .andExpect(jsonPath("$.data.id").value(1));
  }

  @Test
  @DisplayName("PUT /api/drivers/{id}/certification returns 404 when not found")
  void updateCertificationStatus_notFound() throws Exception {
    when(driverService.updateCertificationStatus(999L, CertificationStatus.REVOKED))
        .thenThrow(DriverNotFoundException.class);

    mockMvc
        .perform(put("/api/drivers/999/certification?status=REVOKED"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Driver not found with ID: 999"));
  }

  @Test
  @DisplayName("PUT /api/drivers/{id}/certification returns 400 on exception")
  void updateCertificationStatus_exception_returnsBadRequest() throws Exception {
    when(driverService.updateCertificationStatus(1L, CertificationStatus.PENDING))
        .thenThrow(new RuntimeException("error"));

    mockMvc
        .perform(put("/api/drivers/1/certification?status=PENDING"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.startsWith("Failed to update certification status:")));
  }

  // ==================== GET AVAILABLE DRIVERS TESTS ====================

  @Test
  @DisplayName("GET /api/drivers/available returns 200 with list")
  void getAvailableDrivers_success() throws Exception {
    List<Driver> drivers = Arrays.asList(testDriver);
    when(driverService.getAvailableDrivers()).thenReturn(drivers);
    when(driverMapper.toDTO(testDriver)).thenReturn(testDriverDTO);

    mockMvc
        .perform(get("/api/drivers/available"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Available drivers retrieved successfully"))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data[0].id").value(1));
  }

  @Test
  @DisplayName("GET /api/drivers/available returns 400 on exception")
  void getAvailableDrivers_exception_returnsBadRequest() throws Exception {
    when(driverService.getAvailableDrivers()).thenThrow(new RuntimeException("x"));

    mockMvc
        .perform(get("/api/drivers/available"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.startsWith("Failed to retrieve available drivers:")));
  }

  // ==================== GET DRIVER BY ID TESTS ====================

  @Test
  @DisplayName("GET /api/drivers/{id} returns 200 with driver")
  void getDriverById_success() throws Exception {
    when(driverService.getDriverById(1L)).thenReturn(Optional.of(testDriver));
    when(driverMapper.toDTO(testDriver)).thenReturn(testDriverDTO);

    mockMvc
        .perform(get("/api/drivers/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Driver retrieved successfully"))
        .andExpect(jsonPath("$.data.id").value(1));
  }

  @Test
  @DisplayName("GET /api/drivers/{id} returns 404 when not found")
  void getDriverById_notFound() throws Exception {
    when(driverService.getDriverById(999L)).thenReturn(Optional.empty());

    mockMvc
        .perform(get("/api/drivers/999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Driver not found with ID: 999"));
  }

  @Test
  @DisplayName("GET /api/drivers/{id} returns 404 when DriverNotFoundException thrown")
  void getDriverById_driverNotFoundException() throws Exception {
    when(driverService.getDriverById(999L))
        .thenThrow(new DriverNotFoundException("Driver not found"));

    mockMvc
        .perform(get("/api/drivers/999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Driver not found with ID: 999"));
  }

  @Test
  @DisplayName("GET /api/drivers/{id} returns 400 on exception")
  void getDriverById_exception_returnsBadRequest() throws Exception {
    when(driverService.getDriverById(5L)).thenThrow(new RuntimeException("boom"));

    mockMvc
        .perform(get("/api/drivers/5"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.startsWith("Failed to retrieve driver:")));
  }

  // ==================== GET ALL DRIVERS TESTS ====================

  @Test
  @DisplayName("GET /api/drivers returns 200 with list")
  void getAllDrivers_success() throws Exception {
    List<Driver> drivers = Collections.singletonList(testDriver);
    when(driverService.getAllDrivers()).thenReturn(drivers);
    when(driverMapper.toDTO(testDriver)).thenReturn(testDriverDTO);

    mockMvc
        .perform(get("/api/drivers"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("All drivers retrieved successfully"))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data[0].id").value(1));
  }

  @Test
  @DisplayName("GET /api/drivers returns 400 on exception")
  void getAllDrivers_exception_returnsBadRequest() throws Exception {
    when(driverService.getAllDrivers()).thenThrow(new RuntimeException("db error"));

    mockMvc
        .perform(get("/api/drivers"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.startsWith("Failed to retrieve drivers:")));
  }

  // ==================== GET MY PROFILE TESTS ====================

  @Test
  @DisplayName("GET /api/drivers/my-profile returns 200 with driver profile")
  void getMyProfile_success() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(testDriverUser);
    when(permissionService.isSelf(any(), eq(10L))).thenReturn(true);
    when(userService.findById(10L)).thenReturn(testDriverUser);
    when(driverService.getDriverProfile(testDriverUser)).thenReturn(testDriver);
    when(driverMapper.toDTO(testDriver)).thenReturn(testDriverDTO);

    mockMvc
        .perform(get("/api/drivers/my-profile").param("id", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Driver profile retrieved successfully"))
        .andExpect(jsonPath("$.data.id").value(1))
        .andExpect(jsonPath("$.data.name").value("John Doe"));
  }

  @Test
  @DisplayName("GET /api/drivers/my-profile returns 401 when user is null")
  void getMyProfile_nullUser_returnsUnauthorized() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(null);

    mockMvc
        .perform(get("/api/drivers/my-profile").param("id", "10"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Authentication required"));
  }

  @Test
  @DisplayName("GET /api/drivers/my-profile returns 403 when user doesn't have DRIVER role")
  void getMyProfile_noDriverRole_returnsForbidden() throws Exception {
    User nonDriverUser = User.builder().id(99L).name("Not Driver").build();
    nonDriverUser.addRole(Role.USER);

    when(permissionService.getAuthenticatedUser(any())).thenReturn(nonDriverUser);
    when(permissionService.isSelf(any(), eq(99L))).thenReturn(true);
    when(userService.findById(99L)).thenReturn(nonDriverUser);

    mockMvc
        .perform(get("/api/drivers/my-profile").param("id", "99"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("User does not have DRIVER role"));
  }

  @Test
  @DisplayName("GET /api/drivers/my-profile returns 404 when driver profile not found")
  void getMyProfile_driverNotFound_returnsNotFound() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(testDriverUser);
    when(permissionService.isSelf(any(), eq(10L))).thenReturn(true);
    when(userService.findById(10L)).thenReturn(testDriverUser);
    when(driverService.getDriverProfile(testDriverUser))
        .thenThrow(new IllegalArgumentException("Driver not found"));

    mockMvc
        .perform(get("/api/drivers/my-profile").param("id", "10"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.containsString("Driver profile not found")));
  }

  @Test
  @DisplayName("GET /api/drivers/my-profile returns 400 on exception")
  void getMyProfile_exception_returnsBadRequest() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(testDriverUser);
    when(permissionService.isSelf(any(), eq(10L))).thenReturn(true);
    when(userService.findById(10L)).thenReturn(testDriverUser);
    when(driverService.getDriverProfile(testDriverUser)).thenThrow(new RuntimeException("error"));

    mockMvc
        .perform(get("/api/drivers/my-profile").param("id", "10"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.startsWith("Failed to retrieve driver profile:")));
  }

  // ==================== UPDATE AVAILABILITY TESTS ====================

  @Test
  @DisplayName("PUT /api/drivers/my-profile/availability returns 200 when setting to false")
  void updateAvailability_availableFalse_success() throws Exception {
    testDriver.setAvailable(false);
    when(permissionService.getAuthenticatedUser(any())).thenReturn(testDriverUser);
    when(driverService.updateAvailability(1L, false)).thenReturn(testDriver);
    when(driverMapper.toDTO(testDriver)).thenReturn(testDriverDTO);

    mockMvc
        .perform(put("/api/drivers/my-profile/availability?available=false"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("You are now unavailable for deliveries"))
        .andExpect(jsonPath("$.data.id").value(1));
  }

  @Test
  @DisplayName("PUT /api/drivers/my-profile/availability returns 200 when setting to true")
  void updateAvailability_availableTrue_success() throws Exception {
    testDriver.setAvailable(true);
    when(permissionService.getAuthenticatedUser(any())).thenReturn(testDriverUser);
    when(driverService.updateAvailability(1L, true)).thenReturn(testDriver);
    when(driverMapper.toDTO(testDriver)).thenReturn(testDriverDTO);

    mockMvc
        .perform(put("/api/drivers/my-profile/availability?available=true"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("You are now available for deliveries"))
        .andExpect(jsonPath("$.data.id").value(1));
  }

  @Test
  @DisplayName("PUT /api/drivers/my-profile/availability returns 404 when not found")
  void updateAvailability_notFound() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(testDriverUser);
    when(driverService.updateAvailability(anyLong(), anyBoolean()))
        .thenThrow(DriverNotFoundException.class);

    mockMvc
        .perform(put("/api/drivers/my-profile/availability?available=true"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Driver not found for the authenticated user"));
  }

  @Test
  @DisplayName("PUT /api/drivers/my-profile/availability returns 400 on exception")
  void updateAvailability_exception_returnsBadRequest() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(testDriverUser);
    when(driverService.updateAvailability(1L, true)).thenThrow(new RuntimeException("x"));

    mockMvc
        .perform(put("/api/drivers/my-profile/availability?available=true"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.startsWith("Failed to update availability:")));
  }

  // ==================== UPDATE LOCATION TESTS ====================

  @Test
  @DisplayName("PUT /api/drivers/my-profile/location returns 200 on success")
  void updateMyLocation_success() throws Exception {
    testDriver.setCurrentLatitude(40.7128);
    testDriver.setCurrentLongitude(-74.0060);

    when(permissionService.getAuthenticatedUser(any())).thenReturn(testDriverUser);
    when(driverService.updateDriverLocation(1L, 40.7128, -74.0060)).thenReturn(testDriver);
    when(driverMapper.toDTO(testDriver)).thenReturn(testDriverDTO);

    mockMvc
        .perform(put("/api/drivers/my-profile/location?latitude=40.7128&longitude=-74.0060"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Location updated successfully"))
        .andExpect(jsonPath("$.data.id").value(1));
  }

  @Test
  @DisplayName("PUT /api/drivers/my-profile/location returns 400 when no driver profile")
  void updateMyLocation_noDriverProfile() throws Exception {
    User userWithoutDriver = User.builder().id(99L).name("No Driver").build();
    userWithoutDriver.addRole(Role.DRIVER);

    when(permissionService.getAuthenticatedUser(any())).thenReturn(userWithoutDriver);

    mockMvc
        .perform(put("/api/drivers/my-profile/location?latitude=40.7128&longitude=-74.0060"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("No driver profile found for this user"));
  }

  @Test
  @DisplayName("PUT /api/drivers/my-profile/location returns 400 on exception")
  void updateMyLocation_exception_returnsBadRequest() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(testDriverUser);
    when(driverService.updateDriverLocation(1L, 40.7128, -74.0060))
        .thenThrow(new RuntimeException("location service down"));

    mockMvc
        .perform(put("/api/drivers/my-profile/location?latitude=40.7128&longitude=-74.0060"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.startsWith("Failed to update location:")));
  }
}
