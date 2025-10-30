package com.boozebuddies.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.boozebuddies.dto.DeliveryDTO;
import com.boozebuddies.entity.Delivery;
import com.boozebuddies.entity.Driver;
import com.boozebuddies.entity.Order;
import com.boozebuddies.mapper.DeliveryMapper;
import com.boozebuddies.model.DeliveryStatus;
import com.boozebuddies.service.DeliveryService;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import com.boozebuddies.security.JwtAuthenticationFilter;
import com.boozebuddies.config.TestSecurityConfig;



@WebMvcTest(
    controllers = DeliveryController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = JwtAuthenticationFilter.class
    )
)
@AutoConfigureMockMvc(addFilters = false) // ⛔ disables all Spring Security filters
@Import(TestSecurityConfig.class)         // ✅ imports your test security config
@DisplayName("DeliveryController Tests")
public class DeliveryControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private DeliveryService deliveryService;

  @MockBean private DeliveryMapper deliveryMapper;

  private Delivery testDelivery;
  private DeliveryDTO testDeliveryDTO;

  @BeforeEach
  void setUp() {
    Order testOrder = new Order();
    testOrder.setId(100L);

    Driver testDriver = Driver.builder().id(10L).name("John Driver").phone("555-1234").build();

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

  @Test
  @DisplayName("POST /api/deliveries/assign returns 200 on success")
  void assignDriverToOrder_success() throws Exception {
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
  @DisplayName("POST /api/deliveries/assign returns 400 on exception")
  void assignDriverToOrder_exception_returnsBadRequest() throws Exception {
    when(deliveryService.assignDriverToOrder(any(Order.class), any(Driver.class)))
        .thenThrow(new RuntimeException("no driver"));

    mockMvc
        .perform(post("/api/deliveries/assign?orderId=200&driverId=20"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.startsWith("Failed to assign driver:")));
  }

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
  @DisplayName("PUT /api/deliveries/{id}/status returns 404 when not found")
  void updateDeliveryStatus_notFound() throws Exception {
    when(deliveryService.updateDeliveryStatus(999L, DeliveryStatus.DELIVERED)).thenReturn(null);

    mockMvc
        .perform(put("/api/deliveries/999/status?status=DELIVERED"))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("PUT /api/deliveries/{id}/status returns 400 on exception")
  void updateDeliveryStatus_exception_returnsBadRequest() throws Exception {
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

    when(deliveryService.cancelDelivery(eq(1L), eq("Customer%20requested")))
        .thenReturn(testDelivery);
    when(deliveryMapper.toDTO(testDelivery)).thenReturn(mapped);

    mockMvc
        .perform(post("/api/deliveries/1/cancel?reason=Customer%20requested"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Delivery cancelled successfully"))
        .andExpect(jsonPath("$.data.cancellationReason").value("Customer requested"));
  }

  @Test
  @DisplayName("POST /api/deliveries/{id}/cancel returns 404 when not found")
  void cancelDelivery_notFound() throws Exception {
    when(deliveryService.cancelDelivery(999L, "x")).thenReturn(null);

    mockMvc.perform(post("/api/deliveries/999/cancel?reason=x")).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("POST /api/deliveries/{id}/cancel returns 400 on exception")
  void cancelDelivery_exception_returnsBadRequest() throws Exception {
    when(deliveryService.cancelDelivery(1L, "reason")).thenThrow(new RuntimeException("e"));

    mockMvc
        .perform(post("/api/deliveries/1/cancel?reason=reason"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.startsWith("Failed to cancel delivery:")));
  }

  @Test
  @DisplayName("GET /api/deliveries/driver/{driverId} returns 200 with list")
  void getDeliveriesByDriver_success() throws Exception {
    when(deliveryService.getDeliveriesByDriver(10L)).thenReturn(List.of(testDelivery));
    when(deliveryMapper.toDTO(testDelivery)).thenReturn(testDeliveryDTO);

    mockMvc
        .perform(get("/api/deliveries/driver/10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Deliveries retrieved successfully"))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data[0].orderId").value(100));
  }

  @Test
  @DisplayName("GET /api/deliveries/{id} returns 200 with delivery")
  void getDeliveryById_success() throws Exception {
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
  @DisplayName("GET /api/deliveries/{id} returns 404 when not found")
  void getDeliveryById_notFound() throws Exception {
    when(deliveryService.getDeliveryById(404L)).thenReturn(null);

    mockMvc.perform(get("/api/deliveries/404")).andExpect(status().isNotFound());
  }

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

  @Test
  @DisplayName("GET /api/deliveries/{id} returns 400 on exception")
  void getDeliveryById_exception_returnsBadRequest() throws Exception {
    when(deliveryService.getDeliveryById(123L)).thenThrow(new RuntimeException("err"));

    mockMvc
        .perform(get("/api/deliveries/123"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.startsWith("Failed to retrieve delivery:")));
  }
}
