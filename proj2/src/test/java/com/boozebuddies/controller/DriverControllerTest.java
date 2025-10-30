package com.boozebuddies.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.boozebuddies.dto.DriverDTO;
import com.boozebuddies.entity.Driver;
import com.boozebuddies.mapper.DriverMapper;
import com.boozebuddies.model.CertificationStatus;
import com.boozebuddies.service.DriverService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import com.boozebuddies.security.JwtAuthenticationFilter;
import com.boozebuddies.config.TestSecurityConfig;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@WebMvcTest(
    controllers = DriverController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = JwtAuthenticationFilter.class
    )
)
@AutoConfigureMockMvc(addFilters = false) // ⛔ disables all Spring Security filters
@Import(TestSecurityConfig.class)         // ✅ imports your test security config
@DisplayName("DriverController Tests")
public class DriverControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private DriverService driverService;

  @MockBean private DriverMapper driverMapper;

  private Driver testDriver;
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

  @Test
  @DisplayName("POST /api/drivers/register returns 200 and ApiResponse on success")
  void registerDriver_success() throws Exception {
    when(driverMapper.toEntity(testDriverDTO)).thenReturn(testDriver);
    when(driverService.registerDriver(testDriver)).thenReturn(testDriver);
    when(driverMapper.toDTO(testDriver)).thenReturn(testDriverDTO);

    mockMvc
        .perform(
            post("/api/drivers/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testDriverDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Driver registered successfully"))
        .andExpect(jsonPath("$.data.id").value(1));
  }

  @Test
  @DisplayName("POST /api/drivers/register returns 400 on exception")
  void registerDriver_exception_returnsBadRequest() throws Exception {
    when(driverMapper.toEntity(testDriverDTO)).thenReturn(testDriver);
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
                .value(org.hamcrest.Matchers.startsWith("Failed to register driver:")));
  }

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
        .thenReturn(null);

    mockMvc
        .perform(put("/api/drivers/999/certification?status=REVOKED"))
        .andExpect(status().isNotFound());
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

  @Test
  @DisplayName("PUT /api/drivers/{id}/availability returns 200 on success")
  void updateAvailability_success() throws Exception {
    testDriver.setAvailable(false);
    when(driverService.updateAvailability(1L, false)).thenReturn(testDriver);
    when(driverMapper.toDTO(testDriver)).thenReturn(testDriverDTO);

    mockMvc
        .perform(put("/api/drivers/1/availability?available=false"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Availability updated successfully"))
        .andExpect(jsonPath("$.data.id").value(1));
  }

  @Test
  @DisplayName("PUT /api/drivers/{id}/availability returns 404 when not found")
  void updateAvailability_notFound() throws Exception {
    when(driverService.updateAvailability(999L, true)).thenReturn(null);

    mockMvc
        .perform(put("/api/drivers/999/availability?available=true"))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("PUT /api/drivers/{id}/availability returns 400 on exception")
  void updateAvailability_exception_returnsBadRequest() throws Exception {
    when(driverService.updateAvailability(1L, true)).thenThrow(new RuntimeException("x"));

    mockMvc
        .perform(put("/api/drivers/1/availability?available=true"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.startsWith("Failed to update availability:")));
  }

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
  @DisplayName("GET /api/drivers/{id} returns 200 with driver")
  void getDriverById_success() throws Exception {
    when(driverService.getDriverById(1L)).thenReturn(testDriver);
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
    when(driverService.getDriverById(999L)).thenReturn(null);

    mockMvc.perform(get("/api/drivers/999")).andExpect(status().isNotFound());
  }

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
}
