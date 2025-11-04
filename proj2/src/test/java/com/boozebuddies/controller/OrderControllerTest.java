package com.boozebuddies.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.boozebuddies.config.TestSecurityConfig;
import com.boozebuddies.dto.*;
import com.boozebuddies.entity.*;
import com.boozebuddies.mapper.OrderMapper;
import com.boozebuddies.model.OrderStatus;
import com.boozebuddies.model.Role;
import com.boozebuddies.security.JwtAuthenticationFilter;
import com.boozebuddies.service.OrderService;
import com.boozebuddies.service.PermissionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
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
    controllers = OrderController.class,
    excludeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = JwtAuthenticationFilter.class))
@AutoConfigureMockMvc(addFilters = false) // ⛔ disables all Spring Security filters
@Import(TestSecurityConfig.class)
@DisplayName("OrderController Tests")
public class OrderControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private OrderService orderService;
  @MockBean private OrderMapper orderMapper;
  @MockBean private PermissionService permissionService;

  private User testUser;
  private Merchant testMerchant;
  private Order testOrder;
  private OrderItem testOrderItem;
  private Product testProduct;
  private CreateOrderRequest testCreateRequest;

  @BeforeEach
  void setUp() {
    // Test user
    testUser = User.builder().id(1L).name("John Doe").roles(Set.of(Role.USER)).build();

    // Merchant
    testMerchant = Merchant.builder().id(1L).name("Test Liquor Store").build();

    // Product
    testProduct =
        Product.builder()
            .id(1L)
            .name("Test Beer")
            .price(new BigDecimal("19.99"))
            .merchant(testMerchant)
            .build();

    // Order Item
    testOrderItem =
        OrderItem.builder()
            .id(1L)
            .product(testProduct)
            .quantity(2)
            .unitPrice(new BigDecimal("19.99"))
            .build();

    // Order
    testOrder =
        Order.builder()
            .id(1L)
            .user(testUser)
            .merchant(testMerchant)
            .items(List.of(testOrderItem))
            .totalAmount(new BigDecimal("39.98"))
            .status(OrderStatus.PENDING)
            .createdAt(LocalDateTime.now())
            .build();

    // Create Order Request
    testCreateRequest =
        CreateOrderRequest.builder()
            .userId(1L)
            .merchantId(1L)
            .items(
                List.of(
                    OrderItemRequest.builder()
                        .productId(1L)
                        .quantity(2)
                        .unitPrice(new BigDecimal("19.99"))
                        .build()))
            .build();
  }

  // ==================== CREATE ORDER ====================
  @Test
  void createOrder_Success() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(testUser);
    when(orderService.createOrder(any(Order.class))).thenReturn(testOrder);

    mockMvc
        .perform(
            post("/api/orders")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(testCreateRequest)))
        .andExpect(status().isOk());
  }

  @Test
  void createOrder_AccessDenied() throws Exception {
    // User tries to create order for someone else
    CreateOrderRequest invalidRequest =
        CreateOrderRequest.builder()
            .userId(99L)
            .merchantId(1L)
            .items(testCreateRequest.getItems())
            .build();

    when(permissionService.getAuthenticatedUser(any())).thenReturn(testUser);

    mockMvc
        .perform(
            post("/api/orders")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isForbidden());
  }

  // ==================== GET ORDER ====================
  @Test
  void getOrderById_Success() throws Exception {
    when(orderService.getOrderById(1L)).thenReturn(Optional.of(testOrder));
    when(permissionService.getAuthenticatedUser(any())).thenReturn(testUser);

    mockMvc.perform(get("/api/orders/1")).andExpect(status().isOk());
  }

  @Test
  void getOrderById_NotFound() throws Exception {
    when(orderService.getOrderById(99L)).thenReturn(Optional.empty());

    mockMvc.perform(get("/api/orders/99")).andExpect(status().isNotFound());
  }

  @Test
  void getMyOrders_Success() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(testUser);
    when(orderService.getOrdersByUser(testUser.getId())).thenReturn(List.of(testOrder));

    mockMvc.perform(get("/api/orders/my-orders")).andExpect(status().isOk());
  }

  // ==================== CANCEL ORDER ====================
  @Test
  void cancelOrder_Success() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(testUser);
    when(orderService.getOrderById(1L)).thenReturn(Optional.of(testOrder));
    when(orderService.cancelOrder(1L)).thenReturn(testOrder);

    mockMvc.perform(post("/api/orders/1/cancel")).andExpect(status().isOk());
  }

  @Test
  void cancelOrder_NotOwner() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(testUser);
    User otherUser = User.builder().id(2L).build();
    Order orderForOther = Order.builder().id(1L).user(otherUser).build();
    when(orderService.getOrderById(1L)).thenReturn(Optional.of(orderForOther));

    mockMvc.perform(post("/api/orders/1/cancel")).andExpect(status().isForbidden());
  }

  // ==================== UPDATE ORDER STATUS ====================
  @Test
  void updateOrderStatus_Success() throws Exception {
    when(orderService.getOrderById(1L)).thenReturn(Optional.of(testOrder));
    when(orderService.updateOrderStatus(1L, "CONFIRMED")).thenReturn(testOrder);

    mockMvc
        .perform(put("/api/orders/1/status").param("status", "CONFIRMED"))
        .andExpect(status().isOk());
  }

  @Test
  void updateOrderStatus_NotFound() throws Exception {
    when(orderService.getOrderById(99L)).thenReturn(Optional.empty());

    mockMvc
        .perform(put("/api/orders/99/status").param("status", "CONFIRMED"))
        .andExpect(status().isNotFound());
  }
}
