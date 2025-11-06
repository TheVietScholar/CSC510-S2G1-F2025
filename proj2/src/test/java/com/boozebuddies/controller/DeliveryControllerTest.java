package com.boozebuddies.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.boozebuddies.config.TestSecurityConfig;
import com.boozebuddies.dto.DeliveryDTO;
import com.boozebuddies.entity.Delivery;
import com.boozebuddies.entity.Driver;
import com.boozebuddies.entity.Order;
import com.boozebuddies.entity.User;
import com.boozebuddies.mapper.DeliveryMapper;
import com.boozebuddies.model.DeliveryStatus;
import com.boozebuddies.model.Role;
import com.boozebuddies.security.JwtAuthenticationFilter;
import com.boozebuddies.service.DeliveryService;
import com.boozebuddies.service.DriverService;
import com.boozebuddies.service.OrderService;
import com.boozebuddies.service.PermissionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
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
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    controllers = DeliveryController.class,
    excludeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = JwtAuthenticationFilter.class))
@AutoConfigureMockMvc(addFilters = false)
@Import(TestSecurityConfig.class)
@DisplayName("DeliveryController Tests")
public class DeliveryControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private DeliveryService deliveryService;

  @MockBean private DeliveryMapper deliveryMapper;

  @MockBean private PermissionService permissionService;

  @MockBean private DriverService driverService;

  @MockBean private OrderService orderService;

  private Delivery testDelivery;
  private DeliveryDTO testDeliveryDTO;
  private User testDriverUser;
  private Driver testDriver;
  private User adminUser;
  private User otherDriverUser;
  private Driver otherDriver;
  private User orderOwner;

  @BeforeEach
  void setUp() {
    Order testOrder = new Order();
    testOrder.setId(100L);

    testDriverUser = User.builder().id(10L).name("John Driver").phone("555-1234").build();
    testDriver =
        Driver.builder().user(testDriverUser).id(10L).name("John Driver").phone("555-1234").build();
    testDriverUser.setDriver(testDriver);
    testDriverUser.addRole(Role.DRIVER);

    otherDriverUser = User.builder().id(20L).name("Jane Driver").phone("555-5678").build();
    otherDriver =
        Driver.builder()
            .user(otherDriverUser)
            .id(20L)
            .name("Jane Driver")
            .phone("555-5678")
            .build();
    otherDriverUser.setDriver(otherDriver);
    otherDriverUser.addRole(Role.DRIVER);

    adminUser = User.builder().id(99L).name("Admin User").build();
    adminUser.setRoles(Set.of(Role.ADMIN));

    orderOwner = User.builder().id(50L).name("Order Owner").build();
    orderOwner.addRole(Role.USER);
    testOrder.setUser(orderOwner);

    testDelivery =
        Delivery.builder()
            .id(1L)
            .order(testOrder)
            .driver(testDriver)
            .status(DeliveryStatus.PENDING)
            .deliveryAddress("123 Main St")
            .build();

    testDeliveryDTO =
        DeliveryDTO.builder()
            .id(1L)
            .orderId(100L)
            .driverId(10L)
            .status(DeliveryStatus.PENDING.name())
            .deliveryAddress("123 Main St")
            .driverName("John Driver")
            .driverPhone("555-1234")
            .build();
  }

  // ==================== ASSIGN DRIVER TESTS ====================

  @Test
  @DisplayName("POST /api/deliveries/assign returns 200 on success")
  void assignDriverToOrder_success() throws Exception {
    when(orderService.getOrderById(100L)).thenReturn(Optional.of(testDelivery.getOrder()));
    when(driverService.getDriverById(10L)).thenReturn(Optional.of(testDelivery.getDriver()));
    when(deliveryService.assignDriverToOrder(any(Order.class), any(Driver.class)))
        .thenReturn(testDelivery);
    when(deliveryMapper.toDTO(testDelivery)).thenReturn(testDeliveryDTO);

    mockMvc
        .perform(post("/api/deliveries/assign?orderId=100&driverId=10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Driver assigned successfully"))
        .andExpect(jsonPath("$.data.orderId").value(100))
        .andExpect(jsonPath("$.data.driverId").value(10))
        .andExpect(jsonPath("$.data.status").value(DeliveryStatus.PENDING.name()));
  }

  @Test
  @DisplayName("POST /api/deliveries/assign returns 400 when order not found")
  void assignDriverToOrder_orderNotFound() throws Exception {
    when(orderService.getOrderById(999L)).thenReturn(Optional.empty());

    mockMvc
        .perform(post("/api/deliveries/assign?orderId=999&driverId=10"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Order not found")));
  }

  @Test
  @DisplayName("POST /api/deliveries/assign returns 400 when driver not found")
  void assignDriverToOrder_driverNotFound() throws Exception {
    when(orderService.getOrderById(100L)).thenReturn(Optional.of(testDelivery.getOrder()));
    when(driverService.getDriverById(999L)).thenReturn(Optional.empty());

    mockMvc
        .perform(post("/api/deliveries/assign?orderId=100&driverId=999"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Driver not found")));
  }

  @Test
  @DisplayName("POST /api/deliveries/assign returns 400 on exception")
  void assignDriverToOrder_exception_returnsBadRequest() throws Exception {
    when(orderService.getOrderById(100L)).thenReturn(Optional.of(testDelivery.getOrder()));
    when(driverService.getDriverById(10L)).thenReturn(Optional.of(testDriver));
    when(deliveryService.assignDriverToOrder(any(Order.class), any(Driver.class)))
        .thenThrow(new RuntimeException("no driver"));

    mockMvc
        .perform(post("/api/deliveries/assign?orderId=100&driverId=10"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.startsWith("Failed to assign driver:")));
  }

  // ==================== GET ALL DELIVERIES TESTS ====================

  @Test
  @DisplayName("GET /api/deliveries returns 200 with all deliveries")
  void getAllDeliveries_success() throws Exception {
    when(deliveryService.getAllDeliveries()).thenReturn(List.of(testDelivery));
    when(deliveryMapper.toDTO(testDelivery)).thenReturn(testDeliveryDTO);

    mockMvc
        .perform(get("/api/deliveries"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("All deliveries retrieved successfully"))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data[0].id").value(1));
  }

  @Test
  @DisplayName("GET /api/deliveries returns 400 on exception")
  void getAllDeliveries_exception_returnsBadRequest() throws Exception {
    when(deliveryService.getAllDeliveries()).thenThrow(new RuntimeException("db error"));

    mockMvc
        .perform(get("/api/deliveries"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.containsString("Failed to retrieve deliveries")));
  }

  // ==================== GET ACTIVE DELIVERIES TESTS ====================

  @Test
  @DisplayName("GET /api/deliveries/active returns 200 with list")
  void getActiveDeliveries_success() throws Exception {
    when(deliveryService.getActiveDeliveries()).thenReturn(Collections.singletonList(testDelivery));
    when(deliveryMapper.toDTO(testDelivery)).thenReturn(testDeliveryDTO);

    mockMvc
        .perform(get("/api/deliveries/active"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Active deliveries retrieved successfully"))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data.length()").value(1));
  }

  @Test
  @DisplayName("GET /api/deliveries/active returns 400 on exception")
  void getActiveDeliveries_exception_returnsBadRequest() throws Exception {
    when(deliveryService.getActiveDeliveries()).thenThrow(new RuntimeException("db down"));

    mockMvc
        .perform(get("/api/deliveries/active"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.startsWith("Failed to retrieve active deliveries:")));
  }

  // ==================== GET DELIVERIES BY DRIVER TESTS ====================

  @Test
  @DisplayName("GET /api/deliveries/driver/{driverId} returns 200 with list")
  void getDeliveriesByDriver_success() throws Exception {
    when(deliveryService.getDeliveriesByDriver(10L)).thenReturn(List.of(testDelivery));
    when(deliveryMapper.toDTO(testDelivery)).thenReturn(testDeliveryDTO);

    mockMvc
        .perform(get("/api/deliveries/driver/10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Driver deliveries retrieved successfully"))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data[0].orderId").value(100));
  }

  @Test
  @DisplayName("GET /api/deliveries/driver/{driverId} returns 400 on exception")
  void getDeliveriesByDriver_exception_returnsBadRequest() throws Exception {
    when(deliveryService.getDeliveriesByDriver(77L)).thenThrow(new RuntimeException("boom"));

    mockMvc
        .perform(get("/api/deliveries/driver/77"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.startsWith("Failed to retrieve deliveries:")));
  }

  // ==================== GET DELIVERY BY ID TESTS ====================

  @Test
  @DisplayName("GET /api/deliveries/{id} returns 200 with delivery for driver")
  void getDeliveryById_success() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(testDriverUser);
    when(deliveryService.getDeliveryById(1L)).thenReturn(testDelivery);
    when(deliveryMapper.toDTO(testDelivery)).thenReturn(testDeliveryDTO);

    mockMvc
        .perform(get("/api/deliveries/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Delivery retrieved successfully"))
        .andExpect(jsonPath("$.data.id").value(1));
  }

  @Test
  @DisplayName("GET /api/deliveries/{id} returns 200 for admin")
  void getDeliveryById_adminAccess_success() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(adminUser);
    when(deliveryService.getDeliveryById(1L)).thenReturn(testDelivery);
    when(deliveryMapper.toDTO(testDelivery)).thenReturn(testDeliveryDTO);

    mockMvc
        .perform(get("/api/deliveries/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.id").value(1));
  }

  @Test
  @DisplayName(
      "GET /api/deliveries/{id} returns 403 when driver tries to access another's delivery")
  void getDeliveryById_wrongDriver_accessDenied() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(otherDriverUser);
    when(deliveryService.getDeliveryById(1L)).thenReturn(testDelivery);

    mockMvc
        .perform(get("/api/deliveries/1"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.containsString("don't have permission")));
  }

  @Test
  @DisplayName("GET /api/deliveries/{id} returns 404 when not found")
  void getDeliveryById_notFound() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(adminUser);
    when(deliveryService.getDeliveryById(404L)).thenReturn(null);

    mockMvc.perform(get("/api/deliveries/404")).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("GET /api/deliveries/{id} returns 400 on exception")
  void getDeliveryById_exception_returnsBadRequest() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(adminUser);
    when(deliveryService.getDeliveryById(123L)).thenThrow(new RuntimeException("err"));

    mockMvc
        .perform(get("/api/deliveries/123"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.startsWith("Failed to retrieve delivery:")));
  }

  // ==================== GET MY DELIVERIES TESTS ====================

  @Test
  @DisplayName("GET /api/deliveries/driver/my-deliveries returns 200 with driver's deliveries")
  void getMyDeliveries_success() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(testDriverUser);
    when(deliveryService.getDeliveriesByDriver(10L)).thenReturn(List.of(testDelivery));
    when(deliveryMapper.toDTO(testDelivery)).thenReturn(testDeliveryDTO);

    mockMvc
        .perform(get("/api/deliveries/driver/my-deliveries"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Your deliveries retrieved successfully"))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data[0].driverId").value(10));
  }

  @Test
  @DisplayName("GET /api/deliveries/driver/my-deliveries returns 400 when no driver profile")
  void getMyDeliveries_noDriverProfile() throws Exception {
    User userWithoutDriver = User.builder().id(30L).name("No Driver").build();
    userWithoutDriver.addRole(Role.DRIVER);
    when(permissionService.getAuthenticatedUser(any())).thenReturn(userWithoutDriver);

    mockMvc
        .perform(get("/api/deliveries/driver/my-deliveries"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("No driver profile found for this user"));
  }

  @Test
  @DisplayName("GET /api/deliveries/driver/my-deliveries returns 400 on exception")
  void getMyDeliveries_exception_returnsBadRequest() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(testDriverUser);
    when(deliveryService.getDeliveriesByDriver(10L)).thenThrow(new RuntimeException("db error"));

    mockMvc
        .perform(get("/api/deliveries/driver/my-deliveries"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.containsString("Failed to retrieve deliveries")));
  }

  // ==================== UPDATE DELIVERY STATUS TESTS ====================

  @Test
  @DisplayName("PUT /api/deliveries/{id}/status returns 200 on success")
  void updateDeliveryStatus_success() throws Exception {
    testDelivery.setStatus(DeliveryStatus.IN_TRANSIT);
    DeliveryDTO mapped =
        DeliveryDTO.builder()
            .id(1L)
            .orderId(100L)
            .driverId(10L)
            .status(DeliveryStatus.IN_TRANSIT.name())
            .build();

    when(permissionService.getAuthenticatedUser(any())).thenReturn(testDriverUser);
    when(deliveryService.getDeliveryById(1L)).thenReturn(testDelivery);
    when(deliveryService.updateDeliveryStatus(1L, DeliveryStatus.IN_TRANSIT))
        .thenReturn(testDelivery);
    when(deliveryMapper.toDTO(testDelivery)).thenReturn(mapped);

    mockMvc
        .perform(put("/api/deliveries/1/status?status=IN_TRANSIT"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Delivery status updated successfully"))
        .andExpect(jsonPath("$.data.status").value(DeliveryStatus.IN_TRANSIT.name()));
  }

  @Test
  @DisplayName("PUT /api/deliveries/{id}/status returns 200 for admin")
  void updateDeliveryStatus_adminAccess_success() throws Exception {
    testDelivery.setStatus(DeliveryStatus.DELIVERED);
    DeliveryDTO mapped =
        DeliveryDTO.builder().id(1L).status(DeliveryStatus.DELIVERED.name()).build();

    when(permissionService.getAuthenticatedUser(any())).thenReturn(adminUser);
    when(deliveryService.getDeliveryById(1L)).thenReturn(testDelivery);
    when(deliveryService.updateDeliveryStatus(1L, DeliveryStatus.DELIVERED))
        .thenReturn(testDelivery);
    when(deliveryMapper.toDTO(testDelivery)).thenReturn(mapped);

    mockMvc
        .perform(put("/api/deliveries/1/status?status=DELIVERED"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));
  }

  @Test
  @DisplayName("PUT /api/deliveries/{id}/status returns 403 when wrong driver")
  void updateDeliveryStatus_wrongDriver_accessDenied() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(otherDriverUser);
    when(deliveryService.getDeliveryById(1L)).thenReturn(testDelivery);

    mockMvc
        .perform(put("/api/deliveries/1/status?status=DELIVERED"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.containsString("your own deliveries")));
  }

  @Test
  @DisplayName("PUT /api/deliveries/{id}/status returns 404 when not found")
  void updateDeliveryStatus_notFound() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(adminUser);
    when(deliveryService.getDeliveryById(999L)).thenReturn(null);

    mockMvc
        .perform(put("/api/deliveries/999/status?status=DELIVERED"))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("PUT /api/deliveries/{id}/status returns 400 on exception")
  void updateDeliveryStatus_exception_returnsBadRequest() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(testDriverUser);
    when(deliveryService.getDeliveryById(1L)).thenReturn(testDelivery);
    when(deliveryService.updateDeliveryStatus(1L, DeliveryStatus.FAILED))
        .thenThrow(new RuntimeException("transition invalid"));

    mockMvc
        .perform(put("/api/deliveries/1/status?status=FAILED"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.startsWith("Failed to update delivery status:")));
  }

  // ==================== MARK AS PICKED UP TESTS ====================

  @Test
  @DisplayName("POST /api/deliveries/{id}/pickup returns 200 on success")
  void markAsPickedUp_success() throws Exception {
    testDelivery.setStatus(DeliveryStatus.PICKED_UP);
    DeliveryDTO mapped =
        DeliveryDTO.builder().id(1L).status(DeliveryStatus.PICKED_UP.name()).build();

    when(permissionService.getAuthenticatedUser(any())).thenReturn(testDriverUser);
    when(deliveryService.getDeliveryById(1L)).thenReturn(testDelivery);
    when(deliveryService.updateDeliveryStatus(1L, DeliveryStatus.PICKED_UP))
        .thenReturn(testDelivery);
    when(deliveryMapper.toDTO(testDelivery)).thenReturn(mapped);

    mockMvc
        .perform(post("/api/deliveries/1/pickup"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Order marked as picked up successfully"))
        .andExpect(jsonPath("$.data.status").value(DeliveryStatus.PICKED_UP.name()));
  }

  @Test
  @DisplayName("POST /api/deliveries/{id}/pickup returns 400 when no driver profile")
  void markAsPickedUp_noDriverProfile() throws Exception {
    User userWithoutDriver = User.builder().id(30L).name("No Driver").build();
    userWithoutDriver.addRole(Role.DRIVER);
    when(permissionService.getAuthenticatedUser(any())).thenReturn(userWithoutDriver);

    mockMvc
        .perform(post("/api/deliveries/1/pickup"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("No driver profile found for this user"));
  }

  @Test
  @DisplayName("POST /api/deliveries/{id}/pickup returns 403 when wrong driver")
  void markAsPickedUp_wrongDriver_accessDenied() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(otherDriverUser);
    when(deliveryService.getDeliveryById(1L)).thenReturn(testDelivery);

    mockMvc
        .perform(post("/api/deliveries/1/pickup"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.containsString("your own deliveries")));
  }

  @Test
  @DisplayName("POST /api/deliveries/{id}/pickup returns 404 when not found")
  void markAsPickedUp_notFound() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(testDriverUser);
    when(deliveryService.getDeliveryById(999L)).thenReturn(null);

    mockMvc.perform(post("/api/deliveries/999/pickup")).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("POST /api/deliveries/{id}/pickup returns 400 on exception")
  void markAsPickedUp_exception_returnsBadRequest() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(testDriverUser);
    when(deliveryService.getDeliveryById(1L)).thenReturn(testDelivery);
    when(deliveryService.updateDeliveryStatus(1L, DeliveryStatus.PICKED_UP))
        .thenThrow(new RuntimeException("invalid state"));

    mockMvc
        .perform(post("/api/deliveries/1/pickup"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.containsString("Failed to mark as picked up")));
  }

  // ==================== MARK AS DELIVERED TESTS ====================

  @Test
  @DisplayName("POST /api/deliveries/{id}/deliver returns 200 on success")
  void markAsDelivered_success() throws Exception {
    testDelivery.setStatus(DeliveryStatus.DELIVERED);
    DeliveryDTO mapped =
        DeliveryDTO.builder().id(1L).status(DeliveryStatus.DELIVERED.name()).build();

    when(permissionService.getAuthenticatedUser(any())).thenReturn(testDriverUser);
    when(deliveryService.getDeliveryById(1L)).thenReturn(testDelivery);
    when(deliveryService.updateDeliveryStatus(1L, DeliveryStatus.DELIVERED))
        .thenReturn(testDelivery);
    when(deliveryMapper.toDTO(testDelivery)).thenReturn(mapped);

    mockMvc
        .perform(post("/api/deliveries/1/deliver"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Order marked as delivered successfully"))
        .andExpect(jsonPath("$.data.status").value(DeliveryStatus.DELIVERED.name()));
  }

  @Test
  @DisplayName("POST /api/deliveries/{id}/deliver returns 400 when no driver profile")
  void markAsDelivered_noDriverProfile() throws Exception {
    User userWithoutDriver = User.builder().id(30L).name("No Driver").build();
    userWithoutDriver.addRole(Role.DRIVER);
    when(permissionService.getAuthenticatedUser(any())).thenReturn(userWithoutDriver);

    mockMvc
        .perform(post("/api/deliveries/1/deliver"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("No driver profile found for this user"));
  }

  @Test
  @DisplayName("POST /api/deliveries/{id}/deliver returns 403 when wrong driver")
  void markAsDelivered_wrongDriver_accessDenied() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(otherDriverUser);
    when(deliveryService.getDeliveryById(1L)).thenReturn(testDelivery);

    mockMvc
        .perform(post("/api/deliveries/1/deliver"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message").value(org.hamcrest.Matchers.containsString("your own orders")));
  }

  @Test
  @DisplayName("POST /api/deliveries/{id}/deliver returns 404 when not found")
  void markAsDelivered_notFound() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(testDriverUser);
    when(deliveryService.getDeliveryById(999L)).thenReturn(null);

    mockMvc.perform(post("/api/deliveries/999/deliver")).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("POST /api/deliveries/{id}/deliver returns 400 on exception")
  void markAsDelivered_exception_returnsBadRequest() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(testDriverUser);
    when(deliveryService.getDeliveryById(1L)).thenReturn(testDelivery);
    when(deliveryService.updateDeliveryStatus(1L, DeliveryStatus.DELIVERED))
        .thenThrow(new RuntimeException("cannot deliver"));

    mockMvc
        .perform(post("/api/deliveries/1/deliver"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.containsString("Failed to mark as delivered")));
  }

  // ==================== VERIFY AGE TESTS ====================

  @Test
  @DisplayName("POST /api/deliveries/{id}/verify-age returns 200 when age verified")
  void verifyCustomerAge_verified_success() throws Exception {
    testDelivery.setAgeVerified(true);
    testDelivery.setIdType("Driver License");
    testDelivery.setIdNumber("1234");
    DeliveryDTO mapped = DeliveryDTO.builder().id(1L).ageVerified(true).build();

    when(permissionService.getAuthenticatedUser(any())).thenReturn(testDriverUser);
    when(deliveryService.getDeliveryById(1L)).thenReturn(testDelivery);
    when(deliveryService.updateDeliveryWithAgeVerification(1L, true, "Driver License", "1234"))
        .thenReturn(testDelivery);
    when(deliveryMapper.toDTO(testDelivery)).thenReturn(mapped);

    mockMvc
        .perform(
            post(
                "/api/deliveries/1/verify-age?ageVerified=true&idType=Driver License&idNumber=1234"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Customer age verified successfully"));
  }

  @Test
  @DisplayName("POST /api/deliveries/{id}/verify-age returns 200 when age not verified")
  void verifyCustomerAge_notVerified_success() throws Exception {
    testDelivery.setAgeVerified(false);
    DeliveryDTO mapped = DeliveryDTO.builder().id(1L).ageVerified(false).build();

    when(permissionService.getAuthenticatedUser(any())).thenReturn(testDriverUser);
    when(deliveryService.getDeliveryById(1L)).thenReturn(testDelivery);
    when(deliveryService.updateDeliveryWithAgeVerification(1L, false, null, null))
        .thenReturn(testDelivery);
    when(deliveryMapper.toDTO(testDelivery)).thenReturn(mapped);

    mockMvc
        .perform(post("/api/deliveries/1/verify-age?ageVerified=false"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.containsString("verification failed")));
  }

  @Test
  @DisplayName("POST /api/deliveries/{id}/verify-age returns 400 when no driver profile")
  void verifyCustomerAge_noDriverProfile() throws Exception {
    User userWithoutDriver = User.builder().id(30L).name("No Driver").build();
    userWithoutDriver.addRole(Role.DRIVER);
    when(permissionService.getAuthenticatedUser(any())).thenReturn(userWithoutDriver);

    mockMvc
        .perform(post("/api/deliveries/1/verify-age?ageVerified=true"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("No driver profile found for this user"));
  }

  @Test
  @DisplayName("POST /api/deliveries/{id}/verify-age returns 403 when wrong driver")
  void verifyCustomerAge_wrongDriver_accessDenied() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(otherDriverUser);
    when(deliveryService.getDeliveryById(1L)).thenReturn(testDelivery);

    mockMvc
        .perform(post("/api/deliveries/1/verify-age?ageVerified=true"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.containsString("your own deliveries")));
  }

  @Test
  @DisplayName("POST /api/deliveries/{id}/verify-age returns 404 when not found")
  void verifyCustomerAge_notFound() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(testDriverUser);
    when(deliveryService.getDeliveryById(999L)).thenReturn(null);

    mockMvc
        .perform(post("/api/deliveries/999/verify-age?ageVerified=true"))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("POST /api/deliveries/{id}/verify-age returns 400 on exception")
  void verifyCustomerAge_exception_returnsBadRequest() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(testDriverUser);
    when(deliveryService.getDeliveryById(1L)).thenReturn(testDelivery);
    when(deliveryService.updateDeliveryWithAgeVerification(eq(1L), eq(true), any(), any()))
        .thenThrow(new RuntimeException("update failed"));

    mockMvc
        .perform(post("/api/deliveries/1/verify-age?ageVerified=true"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.containsString("Failed to verify age")));
  }

  // ==================== CANCEL DELIVERY TESTS ====================

  @Test
  @DisplayName("POST /api/deliveries/{id}/cancel returns 200 on success")
  void cancelDelivery_success() throws Exception {
    testDelivery.setStatus(DeliveryStatus.CANCELLED);
    testDelivery.setCancellationReason("Customer requested");
    DeliveryDTO mapped =
        DeliveryDTO.builder()
            .id(1L)
            .orderId(100L)
            .driverId(10L)
            .status(DeliveryStatus.CANCELLED.name())
            .cancellationReason("Customer requested")
            .build();
    when(permissionService.getAuthenticatedUser(any())).thenReturn(testDriverUser);
    when(deliveryService.getDeliveryById(1L)).thenReturn(testDelivery);
    when(deliveryService.cancelDelivery(eq(1L), eq("Customer requested"))).thenReturn(testDelivery);
    when(deliveryMapper.toDTO(testDelivery)).thenReturn(mapped);

    mockMvc
        .perform(post("/api/deliveries/1/cancel?reason=Customer requested"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Delivery cancelled successfully"))
        .andExpect(jsonPath("$.data.cancellationReason").value("Customer requested"));
  }

  @Test
  @DisplayName("POST /api/deliveries/{id}/cancel returns 400 when no driver profile")
  void cancelDelivery_noDriverProfile() throws Exception {
    User userWithoutDriver = User.builder().id(30L).name("No Driver").build();
    userWithoutDriver.addRole(Role.DRIVER);
    when(permissionService.getAuthenticatedUser(any())).thenReturn(userWithoutDriver);

    mockMvc
        .perform(post("/api/deliveries/1/cancel?reason=test"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("No driver profile found for this user"));
  }

  @Test
  @DisplayName("POST /api/deliveries/{id}/cancel returns 403 when wrong driver")
  void cancelDelivery_wrongDriver_accessDenied() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(otherDriverUser);
    when(deliveryService.getDeliveryById(1L)).thenReturn(testDelivery);

    mockMvc
        .perform(post("/api/deliveries/1/cancel?reason=test"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.containsString("your own deliveries")));
  }

  @Test
  @DisplayName("POST /api/deliveries/{id}/cancel returns 404 when not found")
  void cancelDelivery_notFound() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(testDriverUser);
    when(deliveryService.getDeliveryById(999L)).thenReturn(null);

    mockMvc.perform(post("/api/deliveries/999/cancel?reason=x")).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("POST /api/deliveries/{id}/cancel returns 400 on exception")
  void cancelDelivery_exception_returnsBadRequest() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(testDriverUser);
    when(deliveryService.getDeliveryById(1L)).thenReturn(testDelivery);
    when(deliveryService.cancelDelivery(1L, "reason")).thenThrow(new RuntimeException("e"));

    mockMvc
        .perform(post("/api/deliveries/1/cancel?reason=reason"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.startsWith("Failed to cancel delivery:")));
  }

  // ==================== UPDATE LOCATION TESTS ====================

  @Test
  @DisplayName("PUT /api/deliveries/{id}/location returns 200 on success")
  void updateDeliveryLocation_success() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(testDriverUser);
    when(deliveryService.getDeliveryById(1L)).thenReturn(testDelivery);
    doNothing().when(deliveryService).updateDeliveryLocation(1L, 40.7128, -74.0060);

    mockMvc
        .perform(put("/api/deliveries/1/location?latitude=40.7128&longitude=-74.0060"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Location updated successfully"));

    verify(deliveryService).updateDeliveryLocation(1L, 40.7128, -74.0060);
  }

  @Test
  @DisplayName("PUT /api/deliveries/{id}/location returns 400 when no driver profile")
  void updateDeliveryLocation_noDriverProfile() throws Exception {
    User userWithoutDriver = User.builder().id(30L).name("No Driver").build();
    userWithoutDriver.addRole(Role.DRIVER);
    when(permissionService.getAuthenticatedUser(any())).thenReturn(userWithoutDriver);

    mockMvc
        .perform(put("/api/deliveries/1/location?latitude=40.7128&longitude=-74.0060"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("No driver profile found for this user"));
  }

  @Test
  @DisplayName("PUT /api/deliveries/{id}/location returns 403 when wrong driver")
  void updateDeliveryLocation_wrongDriver_accessDenied() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(otherDriverUser);
    when(deliveryService.getDeliveryById(1L)).thenReturn(testDelivery);

    mockMvc
        .perform(put("/api/deliveries/1/location?latitude=40.7128&longitude=-74.0060"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.containsString("your own deliveries")));
  }

  @Test
  @DisplayName("PUT /api/deliveries/{id}/location returns 404 when not found")
  void updateDeliveryLocation_notFound() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(testDriverUser);
    when(deliveryService.getDeliveryById(999L)).thenReturn(null);

    mockMvc
        .perform(put("/api/deliveries/999/location?latitude=40.7128&longitude=-74.0060"))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("PUT /api/deliveries/{id}/location returns 400 on exception")
  void updateDeliveryLocation_exception_returnsBadRequest() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(testDriverUser);
    when(deliveryService.getDeliveryById(1L)).thenReturn(testDelivery);
    doThrow(new RuntimeException("location update failed"))
        .when(deliveryService)
        .updateDeliveryLocation(eq(1L), any(), any());

    mockMvc
        .perform(put("/api/deliveries/1/location?latitude=40.7128&longitude=-74.0060"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.containsString("Failed to update location")));
  }

  // ==================== GET DELIVERY BY ORDER ID TESTS ====================

  @Test
  @DisplayName("GET /api/deliveries/order/{orderId} returns 200 with delivery for order owner")
  void getDeliveryByOrderId_orderOwner_success() throws Exception {
    User orderOwner = User.builder().id(50L).name("Order Owner").build();
    orderOwner.addRole(Role.USER);
    testDelivery.getOrder().setUser(orderOwner);

    when(permissionService.getAuthenticatedUser(any())).thenReturn(orderOwner);
    when(deliveryService.getDeliveryByOrderId(100L)).thenReturn(testDelivery);
    when(deliveryMapper.toDTO(testDelivery)).thenReturn(testDeliveryDTO);

    mockMvc
        .perform(get("/api/deliveries/order/100"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Delivery retrieved successfully"))
        .andExpect(jsonPath("$.data.orderId").value(100));
  }

  @Test
  @DisplayName("GET /api/deliveries/order/{orderId} returns 200 for admin")
  void getDeliveryByOrderId_adminAccess_success() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(adminUser);
    when(deliveryService.getDeliveryByOrderId(100L)).thenReturn(testDelivery);
    when(deliveryMapper.toDTO(testDelivery)).thenReturn(testDeliveryDTO);

    mockMvc
        .perform(get("/api/deliveries/order/100"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.orderId").value(100));
  }

  @Test
  @DisplayName("GET /api/deliveries/order/{orderId} returns 200 for driver")
  void getDeliveryByOrderId_driverAccess_success() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(testDriverUser);
    when(deliveryService.getDeliveryByOrderId(100L)).thenReturn(testDelivery);
    when(deliveryMapper.toDTO(testDelivery)).thenReturn(testDeliveryDTO);

    mockMvc
        .perform(get("/api/deliveries/order/100"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.orderId").value(100));
  }

  @Test
  @DisplayName("GET /api/deliveries/order/{orderId} returns 403 when user cannot access")
  void getDeliveryByOrderId_accessDenied() throws Exception {
    User otherUser = User.builder().id(99L).name("Other User").build();
    otherUser.addRole(Role.USER);
    testDelivery.getOrder().setUser(orderOwner);

    when(permissionService.getAuthenticatedUser(any())).thenReturn(otherUser);
    when(deliveryService.getDeliveryByOrderId(100L)).thenReturn(testDelivery);

    mockMvc
        .perform(get("/api/deliveries/order/100"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.containsString("don't have permission")));
  }

  @Test
  @DisplayName("GET /api/deliveries/order/{orderId} returns 404 when not found")
  void getDeliveryByOrderId_notFound() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(adminUser);
    when(deliveryService.getDeliveryByOrderId(999L)).thenReturn(null);

    mockMvc.perform(get("/api/deliveries/order/999")).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("GET /api/deliveries/order/{orderId} returns 400 on exception")
  void getDeliveryByOrderId_exception_returnsBadRequest() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(adminUser);
    when(deliveryService.getDeliveryByOrderId(100L)).thenThrow(new RuntimeException("error"));

    mockMvc
        .perform(get("/api/deliveries/order/100"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.startsWith("Failed to retrieve delivery:")));
  }

  @Test
  @DisplayName("GET /api/deliveries/order/{orderId} returns 403 when order has no user")
  void getDeliveryByOrderId_orderWithNoUser_accessDenied() throws Exception {
    User otherUser = User.builder().id(99L).name("Other User").build();
    otherUser.addRole(Role.USER);
    testDelivery.getOrder().setUser(null); // Order has no user

    when(permissionService.getAuthenticatedUser(any())).thenReturn(otherUser);
    when(deliveryService.getDeliveryByOrderId(100L)).thenReturn(testDelivery);

    mockMvc
        .perform(get("/api/deliveries/order/100"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.containsString("don't have permission")));
  }

  @Test
  @DisplayName("GET /api/deliveries/{id} returns 403 when order has no user")
  void getDeliveryById_orderWithNoUser_accessDenied() throws Exception {
    User nonDriverUser = User.builder().id(99L).name("Non Driver").build();
    nonDriverUser.addRole(Role.USER);
    testDelivery.getOrder().setUser(null); // Order has no user

    when(permissionService.getAuthenticatedUser(any())).thenReturn(nonDriverUser);
    when(deliveryService.getDeliveryById(1L)).thenReturn(testDelivery);

    mockMvc
        .perform(get("/api/deliveries/1"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.containsString("don't have permission")));
  }
}
