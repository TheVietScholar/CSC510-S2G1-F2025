package com.boozebuddies.controller;

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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    controllers = OrderController.class,
    excludeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = JwtAuthenticationFilter.class))
@AutoConfigureMockMvc(addFilters = false)
@Import(TestSecurityConfig.class)
@DisplayName("OrderController Tests")
public class OrderControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private OrderService orderService;
  @MockBean private OrderMapper orderMapper;
  @MockBean private PermissionService permissionService;

  private User testUser;
  private User adminUser;
  private User merchantAdminUser;
  private User driverUser;
  private Merchant testMerchant;
  private Order testOrder;
  private OrderDTO testOrderDTO;
  private DriverOrderDTO testDriverOrderDTO;
  private OrderItem testOrderItem;
  private Product testProduct;
  private CreateOrderRequest testCreateRequest;
  private Driver testDriver;

  @BeforeEach
  void setUp() {
    // Test user
    testUser = User.builder().id(1L).name("John Doe").build();
    testUser.addRole(Role.USER);

    // Admin user
    adminUser = User.builder().id(99L).name("Admin").build();
    adminUser.setRoles(Set.of(Role.ADMIN));

    // Merchant admin user
    merchantAdminUser = User.builder().id(20L).name("Merchant Admin").merchantId(1L).build();
    merchantAdminUser.addRole(Role.MERCHANT_ADMIN);

    // Driver user
    testDriver = Driver.builder().id(5L).name("Driver Joe").build();
    driverUser = User.builder().id(30L).name("Driver User").build();
    driverUser.addRole(Role.DRIVER);
    driverUser.setDriver(testDriver);
    testDriver.setUser(driverUser);

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

    // Order DTO
    testOrderDTO =
        OrderDTO.builder()
            .id(1L)
            .userId(1L)
            .merchantId(1L)
            .totalAmount(new BigDecimal("39.98"))
            .status("PENDING")
            .build();

    // Driver Order DTO
    testDriverOrderDTO =
        DriverOrderDTO.builder()
            .id(1L)
            .userId(1L)
            .merchantId(1L)
            .merchantName("Test Merchant")
            .customerName("John Doe")
            .totalAmount(new BigDecimal("39.98"))
            .status("PENDING")
            .distanceKm(5.5)
            .etaMin(16)
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

  // ==================== CREATE ORDER TESTS ====================

  @Test
  @DisplayName("POST /api/orders should return 200 on success")
  void createOrder_Success() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(testUser);
    when(orderMapper.toEntity(any(CreateOrderRequest.class))).thenReturn(testOrder);
    when(orderService.createOrder(any(Order.class))).thenReturn(testOrder);
    when(orderMapper.toDTO(testOrder)).thenReturn(testOrderDTO);

    mockMvc
        .perform(
            post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCreateRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Order created successfully"))
        .andExpect(jsonPath("$.data.id").value(1));
  }

  @Test
  @DisplayName(
      "POST /api/orders should return 403 when user tries to create order for someone else")
  void createOrder_AccessDenied() throws Exception {
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
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.containsString("only create orders for yourself")));
  }

  @Test
  @DisplayName("POST /api/orders should set userId from authenticated user when null")
  void createOrder_NullUserId_Success() throws Exception {
    CreateOrderRequest requestWithNullUserId =
        CreateOrderRequest.builder()
            .userId(null)
            .merchantId(1L)
            .items(testCreateRequest.getItems())
            .build();

    when(permissionService.getAuthenticatedUser(any())).thenReturn(testUser);
    when(orderMapper.toEntity(any(CreateOrderRequest.class))).thenReturn(testOrder);
    when(orderService.createOrder(any(Order.class))).thenReturn(testOrder);
    when(orderMapper.toDTO(testOrder)).thenReturn(testOrderDTO);

    mockMvc
        .perform(
            post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestWithNullUserId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));
  }

  @Test
  @DisplayName("POST /api/orders should return 400 on exception")
  void createOrder_Exception() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(testUser);
    when(orderMapper.toEntity(any(CreateOrderRequest.class))).thenReturn(testOrder);
    when(orderService.createOrder(any(Order.class)))
        .thenThrow(new RuntimeException("Order creation failed"));

    mockMvc
        .perform(
            post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCreateRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.containsString("Failed to create order")));
  }

  // ==================== GET ORDER BY ID TESTS ====================

  @Test
  @DisplayName("GET /api/orders/{id} should return 200 with order data")
  void getOrderById_Success() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(testUser);
    when(orderService.getOrderById(1L)).thenReturn(Optional.of(testOrder));
    when(orderMapper.toDTO(testOrder)).thenReturn(testOrderDTO);

    mockMvc
        .perform(get("/api/orders/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Order retrieved successfully"))
        .andExpect(jsonPath("$.data.id").value(1));
  }

  @Test
  @DisplayName("Regular user can access their own order")
  void getOrderById_UserOwnsOrder() throws Exception {
    testOrder.setUser(testUser); // testUser owns the order
    when(permissionService.getAuthenticatedUser(any())).thenReturn(testUser);
    when(orderService.getOrderById(1L)).thenReturn(Optional.of(testOrder));
    when(orderMapper.toDTO(testOrder)).thenReturn(testOrderDTO);

    mockMvc
        .perform(get("/api/orders/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.id").value(1));
  }

  @Test
  @DisplayName("GET /api/orders/{id} should return 404 when order not found")
  void getOrderById_NotFound() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(testUser);
    when(orderService.getOrderById(99L)).thenReturn(Optional.empty());

    mockMvc.perform(get("/api/orders/99")).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("GET /api/orders/{id} should return 400 with invalid ID")
  void getOrderById_InvalidId() throws Exception {
    mockMvc
        .perform(get("/api/orders/0"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Invalid order ID"));

    verify(orderService, never()).getOrderById(any());
  }

  @Test
  @DisplayName("GET /api/orders/{id} should return 400 with negative ID")
  void getOrderById_NegativeId() throws Exception {
    mockMvc
        .perform(get("/api/orders/-1"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Invalid order ID"));

    verify(orderService, never()).getOrderById(any());
  }

  @Test
  @DisplayName("GET /api/orders/{id} should return 400 on exception")
  void getOrderById_Exception() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(testUser);
    when(orderService.getOrderById(1L)).thenThrow(new RuntimeException("Database error"));

    mockMvc
        .perform(get("/api/orders/1"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.containsString("Failed to retrieve order")));
  }

  @Test
  @DisplayName("GET /api/orders/{id} should return 200 for admin viewing any order")
  void getOrderById_AdminSuccess() throws Exception {
    Order otherUserOrder = Order.builder().id(1L).user(testUser).merchant(testMerchant).build();

    when(permissionService.getAuthenticatedUser(any())).thenReturn(adminUser);
    when(orderService.getOrderById(1L)).thenReturn(Optional.of(otherUserOrder));
    when(orderMapper.toDTO(otherUserOrder)).thenReturn(testOrderDTO);

    mockMvc
        .perform(get("/api/orders/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Order retrieved successfully"));
  }

  @Test
  @DisplayName("GET /api/orders/{id} should return 403 when user doesn't own order")
  void getOrderById_NotOwner() throws Exception {
    User otherUser = User.builder().id(2L).name("Other User").build();
    otherUser.addRole(Role.USER);
    Order orderForOther = Order.builder().id(1L).user(otherUser).merchant(testMerchant).build();

    when(permissionService.getAuthenticatedUser(any())).thenReturn(testUser);
    when(orderService.getOrderById(1L)).thenReturn(Optional.of(orderForOther));

    mockMvc
        .perform(get("/api/orders/1"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(
                    org.hamcrest.Matchers.containsString(
                        "don't have permission to view this order")));
  }

  @Test
  @DisplayName(
      "GET /api/orders/{id} should return 200 for merchant admin viewing own merchant's order")
  void getOrderById_MerchantAdminSuccess() throws Exception {
    Order merchantOrder = Order.builder().id(1L).user(testUser).merchant(testMerchant).build();

    when(permissionService.getAuthenticatedUser(any())).thenReturn(merchantAdminUser);
    when(orderService.getOrderById(1L)).thenReturn(Optional.of(merchantOrder));
    mockMvc
        .perform(get("/api/orders/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Order retrieved successfully"));
  }

  @Test
  @DisplayName("GET /api/orders/{id} should return 200 for driver viewing assigned order")
  void getOrderById_DriverSuccess() throws Exception {
    Order driverOrder =
        Order.builder().id(1L).user(testUser).merchant(testMerchant).driver(testDriver).build();

    when(permissionService.getAuthenticatedUser(any())).thenReturn(driverUser);
    when(orderService.getOrderById(1L)).thenReturn(Optional.of(driverOrder));
    when(orderMapper.toDTO(driverOrder)).thenReturn(testOrderDTO);

    mockMvc
        .perform(get("/api/orders/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Order retrieved successfully"));
  }

  @Test
  @DisplayName("GET /api/orders/{id} should return 401 when user is null")
  void getOrderById_Unauthorized() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(null);

    mockMvc
        .perform(get("/api/orders/1"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Authentication required"));
  }

  // ==================== GET MY ORDERS TESTS ====================

  @Test
  @DisplayName("GET /api/orders/my-orders should return 200 with user's orders")
  void getMyOrders_Success() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(testUser);
    when(orderService.getOrdersByUser(1L)).thenReturn(List.of(testOrder));
    when(orderMapper.toDTO(testOrder)).thenReturn(testOrderDTO);

    mockMvc
        .perform(get("/api/orders/my-orders"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Your orders retrieved successfully"))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data[0].id").value(1));
  }

  @Test
  @DisplayName("GET /api/orders/my-orders should return 200 with empty list")
  void getMyOrders_EmptyList() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(testUser);
    when(orderService.getOrdersByUser(1L)).thenReturn(List.of());

    mockMvc
        .perform(get("/api/orders/my-orders"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data.length()").value(0));
  }

  @Test
  @DisplayName("GET /api/orders/my-orders should return 400 on exception")
  void getMyOrders_Exception() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(testUser);
    when(orderService.getOrdersByUser(1L)).thenThrow(new RuntimeException("Database error"));

    mockMvc
        .perform(get("/api/orders/my-orders"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.containsString("Failed to retrieve your orders")));
  }

  // ==================== GET ORDERS BY USER (ADMIN) TESTS ====================

  @Test
  @DisplayName("GET /api/orders/user/{userId} should return 200 with user's orders")
  void getOrdersByUser_Success() throws Exception {
    when(orderService.getOrdersByUser(1L)).thenReturn(List.of(testOrder));
    when(orderMapper.toDTO(testOrder)).thenReturn(testOrderDTO);

    mockMvc
        .perform(get("/api/orders/user/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("User orders retrieved successfully"))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data[0].id").value(1));
  }

  @Test
  @DisplayName("GET /api/orders/user/{userId} should return 400 with invalid ID")
  void getOrdersByUser_InvalidId() throws Exception {
    mockMvc
        .perform(get("/api/orders/user/0"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Invalid user ID"));

    verify(orderService, never()).getOrdersByUser(any());
  }

  @Test
  @DisplayName("GET /api/orders/user/{userId} should return 400 with negative ID")
  void getOrdersByUser_NegativeId() throws Exception {
    mockMvc
        .perform(get("/api/orders/user/-1"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Invalid user ID"));

    verify(orderService, never()).getOrdersByUser(any());
  }

  @Test
  @DisplayName("GET /api/orders/user/{userId} should return 400 on exception")
  void getOrdersByUser_Exception() throws Exception {
    when(orderService.getOrdersByUser(1L)).thenThrow(new RuntimeException("Database error"));

    mockMvc
        .perform(get("/api/orders/user/1"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.containsString("Failed to retrieve user orders")));
  }

  // ==================== GET ALL ORDERS (ADMIN) TESTS ====================

  @Test
  @DisplayName("GET /api/orders should return 200 with all orders")
  void getAllOrders_Success() throws Exception {
    Order order2 = Order.builder().id(2L).user(testUser).merchant(testMerchant).build();
    OrderDTO orderDTO2 = OrderDTO.builder().id(2L).userId(1L).merchantId(1L).build();

    when(orderService.getAllOrders()).thenReturn(List.of(testOrder, order2));
    when(orderMapper.toDTO(testOrder)).thenReturn(testOrderDTO);
    when(orderMapper.toDTO(order2)).thenReturn(orderDTO2);

    mockMvc
        .perform(get("/api/orders"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("All orders retrieved successfully"))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data.length()").value(2));
  }

  @Test
  @DisplayName("GET /api/orders should return 200 with empty list")
  void getAllOrders_EmptyList() throws Exception {
    when(orderService.getAllOrders()).thenReturn(List.of());

    mockMvc
        .perform(get("/api/orders"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data.length()").value(0));
  }

  @Test
  @DisplayName("GET /api/orders should return 400 on exception")
  void getAllOrders_Exception() throws Exception {
    when(orderService.getAllOrders()).thenThrow(new RuntimeException("Database error"));

    mockMvc
        .perform(get("/api/orders"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.containsString("Failed to retrieve orders")));
  }

  // ==================== GET MY MERCHANT ORDERS TESTS ====================

  @Test
  @DisplayName("GET /api/orders/merchant/my-orders should return 200 with merchant's orders")
  void getMyMerchantOrders_Success() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(merchantAdminUser);
    when(orderService.getOrdersByMerchant(1L)).thenReturn(List.of(testOrder));
    when(orderMapper.toDTO(testOrder)).thenReturn(testOrderDTO);

    mockMvc
        .perform(get("/api/orders/merchant/my-orders"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Your merchant orders retrieved successfully"))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data[0].id").value(1));
  }

  @Test
  @DisplayName("GET /api/orders/merchant/my-orders should return 400 when no merchant assigned")
  void getMyMerchantOrders_NoMerchantAssigned() throws Exception {
    User adminWithoutMerchant =
        User.builder().id(20L).name("Admin Without Merchant").merchantId(null).build();
    adminWithoutMerchant.addRole(Role.MERCHANT_ADMIN);

    when(permissionService.getAuthenticatedUser(any())).thenReturn(adminWithoutMerchant);

    mockMvc
        .perform(get("/api/orders/merchant/my-orders"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("No merchant assigned to this admin"));

    verify(orderService, never()).getOrdersByMerchant(any());
  }

  @Test
  @DisplayName("GET /api/orders/merchant/my-orders should return 400 on exception")
  void getMyMerchantOrders_Exception() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(merchantAdminUser);
    when(orderService.getOrdersByMerchant(1L)).thenThrow(new RuntimeException("Database error"));

    mockMvc
        .perform(get("/api/orders/merchant/my-orders"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.containsString("Failed to retrieve merchant orders")));
  }

  // ==================== GET ORDERS BY MERCHANT TESTS ====================

  @Test
  @DisplayName("GET /api/orders/merchant/{merchantId} should return 200 for admin")
  void getOrdersByMerchant_AdminSuccess() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(adminUser);
    when(orderService.getOrdersByMerchant(1L)).thenReturn(List.of(testOrder));
    when(orderMapper.toDTO(testOrder)).thenReturn(testOrderDTO);

    mockMvc
        .perform(get("/api/orders/merchant/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Merchant orders retrieved successfully"))
        .andExpect(jsonPath("$.data").isArray());
  }

  @Test
  @DisplayName(
      "GET /api/orders/merchant/{merchantId} should return 200 for merchant admin viewing own merchant")
  void getOrdersByMerchant_MerchantAdminSuccess() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(merchantAdminUser);
    when(orderService.getOrdersByMerchant(1L)).thenReturn(List.of(testOrder));
    when(orderMapper.toDTO(testOrder)).thenReturn(testOrderDTO);

    mockMvc
        .perform(get("/api/orders/merchant/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));
  }

  @Test
  @DisplayName(
      "GET /api/orders/merchant/{merchantId} should return 403 when merchant admin tries to view other merchant")
  void getOrdersByMerchant_AccessDenied() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(merchantAdminUser);

    mockMvc
        .perform(get("/api/orders/merchant/999"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(
                    org.hamcrest.Matchers.containsString(
                        "only view orders for your own merchant")));

    verify(orderService, never()).getOrdersByMerchant(999L);
  }

  @Test
  @DisplayName("GET /api/orders/merchant/{merchantId} should return 400 with invalid ID")
  void getOrdersByMerchant_InvalidId() throws Exception {
    mockMvc
        .perform(get("/api/orders/merchant/0"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Invalid merchant ID"));

    verify(orderService, never()).getOrdersByMerchant(any());
  }

  @Test
  @DisplayName("GET /api/orders/merchant/{merchantId} should return 400 with negative ID")
  void getOrdersByMerchant_NegativeId() throws Exception {
    mockMvc
        .perform(get("/api/orders/merchant/-1"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Invalid merchant ID"));

    verify(orderService, never()).getOrdersByMerchant(any());
  }

  @Test
  @DisplayName("GET /api/orders/merchant/{merchantId} should return 400 on exception")
  void getOrdersByMerchant_Exception() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(adminUser);
    when(orderService.getOrdersByMerchant(1L)).thenThrow(new RuntimeException("Database error"));

    mockMvc
        .perform(get("/api/orders/merchant/1"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.containsString("Failed to retrieve merchant orders")));
  }

  // ==================== GET DRIVER ORDERS TESTS ====================

  @Test
  @DisplayName("GET /api/orders/driver/assigned should return 200 with driver's orders")
  void getDriverOrders_Success() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(driverUser);
    when(orderService.getOrdersByDriver(5L)).thenReturn(List.of(testOrder));
    when(orderMapper.toDTO(testOrder)).thenReturn(testOrderDTO);

    mockMvc
        .perform(get("/api/orders/driver/assigned"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Your assigned orders retrieved successfully"))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data[0].id").value(1));
  }

  @Test
  @DisplayName("GET /api/orders/driver/assigned should return 400 when no driver profile")
  void getDriverOrders_NoDriverProfile() throws Exception {
    User userWithoutDriver = User.builder().id(30L).name("User Without Driver").build();
    userWithoutDriver.addRole(Role.DRIVER);

    when(permissionService.getAuthenticatedUser(any())).thenReturn(userWithoutDriver);

    mockMvc
        .perform(get("/api/orders/driver/assigned"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("No driver profile found for this user"));

    verify(orderService, never()).getOrdersByDriver(any());
  }

  @Test
  @DisplayName("GET /api/orders/driver/assigned should return 400 on exception")
  void getDriverOrders_Exception() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(driverUser);
    when(orderService.getOrdersByDriver(5L)).thenThrow(new RuntimeException("Database error"));

    mockMvc
        .perform(get("/api/orders/driver/assigned"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.containsString("Failed to retrieve driver orders")));
  }

  // ==================== CANCEL ORDER TESTS ====================

  @Test
  @DisplayName("POST /api/orders/{id}/cancel should return 200 on success")
  void cancelOrder_Success() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(testUser);
    when(orderService.getOrderById(1L)).thenReturn(Optional.of(testOrder));
    when(orderService.cancelOrder(1L)).thenReturn(testOrder);
    when(orderMapper.toDTO(testOrder)).thenReturn(testOrderDTO);

    mockMvc
        .perform(post("/api/orders/1/cancel"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Order cancelled successfully"));
  }

  @Test
  @DisplayName("POST /api/orders/{id}/cancel should return 403 when user doesn't own order")
  void cancelOrder_NotOwner() throws Exception {
    User otherUser = User.builder().id(2L).build();
    Order orderForOther = Order.builder().id(1L).user(otherUser).build();

    when(permissionService.getAuthenticatedUser(any())).thenReturn(testUser);
    when(orderService.getOrderById(1L)).thenReturn(Optional.of(orderForOther));

    mockMvc
        .perform(post("/api/orders/1/cancel"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.containsString("only cancel your own orders")));
  }

  @Test
  @DisplayName("POST /api/orders/{id}/cancel should return 400 with invalid ID")
  void cancelOrder_InvalidId() throws Exception {
    mockMvc
        .perform(post("/api/orders/0/cancel"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Invalid order ID"));

    verify(orderService, never()).getOrderById(any());
  }

  @Test
  @DisplayName("POST /api/orders/{id}/cancel should return 404 when order not found")
  void cancelOrder_NotFound() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(testUser);
    when(orderService.getOrderById(99L)).thenReturn(Optional.empty());

    mockMvc.perform(post("/api/orders/99/cancel")).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("POST /api/orders/{id}/cancel should return 403 when order has null user")
  void cancelOrder_NullUser() throws Exception {
    Order orderWithNullUser = Order.builder().id(1L).user(null).build();

    when(permissionService.getAuthenticatedUser(any())).thenReturn(testUser);
    when(orderService.getOrderById(1L)).thenReturn(Optional.of(orderWithNullUser));

    mockMvc
        .perform(post("/api/orders/1/cancel"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.success").value(false));
  }

  @Test
  @DisplayName("POST /api/orders/{id}/cancel should return 400 on exception")
  void cancelOrder_Exception() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(testUser);
    when(orderService.getOrderById(1L)).thenReturn(Optional.of(testOrder));
    when(orderService.cancelOrder(1L)).thenThrow(new RuntimeException("Cancellation failed"));

    mockMvc
        .perform(post("/api/orders/1/cancel"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.containsString("Failed to cancel order")));
  }

  // ==================== UPDATE ORDER STATUS TESTS ====================

  @Test
  @DisplayName("PUT /api/orders/{id}/status should return 200 on success")
  void updateOrderStatus_Success() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(adminUser);
    when(orderService.getOrderById(1L)).thenReturn(Optional.of(testOrder));
    when(orderService.updateOrderStatus(1L, "CONFIRMED")).thenReturn(testOrder);
    when(orderMapper.toDTO(testOrder)).thenReturn(testOrderDTO);

    mockMvc
        .perform(put("/api/orders/1/status").param("status", "CONFIRMED"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Order status updated successfully"));
  }

  @Test
  @DisplayName("PUT /api/orders/{id}/status should return 404 when order not found")
  void updateOrderStatus_NotFound() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(adminUser);
    when(orderService.getOrderById(99L)).thenReturn(Optional.empty());

    mockMvc
        .perform(put("/api/orders/99/status").param("status", "CONFIRMED"))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("PUT /api/orders/{id}/status should return 400 with negative ID")
  void updateOrderStatus_NegativeId() throws Exception {
    mockMvc
        .perform(put("/api/orders/-1/status").param("status", "CONFIRMED"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Invalid order ID"));

    verify(orderService, never()).getOrderById(any());
  }

  @Test
  @DisplayName("PUT /api/orders/{id}/status should return 400 on exception")
  void updateOrderStatus_Exception() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(adminUser);
    when(orderService.getOrderById(1L)).thenReturn(Optional.of(testOrder));
    when(orderService.updateOrderStatus(1L, "CONFIRMED"))
        .thenThrow(new RuntimeException("Update failed"));

    mockMvc
        .perform(put("/api/orders/1/status").param("status", "CONFIRMED"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.containsString("Failed to update order status")));
  }

  // ==================== ADDITIONAL GET ORDER BY ID PERMISSION TESTS
  // ====================

  @Test
  @DisplayName("GET /api/orders/{id} should return 200 when user owns the order")
  void getOrderById_UserOwnsOrder_Success() throws Exception {
    testOrder.setUser(testUser);
    testOrder.setMerchant(testMerchant);

    when(permissionService.getAuthenticatedUser(any())).thenReturn(testUser);
    when(orderService.getOrderById(1L)).thenReturn(Optional.of(testOrder));
    when(orderMapper.toDTO(testOrder)).thenReturn(testOrderDTO);

    mockMvc
        .perform(get("/api/orders/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.id").value(1));
  }

  @Test
  @DisplayName("GET /api/orders/{id} should return 200 when merchant admin owns merchant")
  void getOrderById_MerchantAdminOwnsOrderMerchant_Success() throws Exception {
    testOrder.setUser(testUser);
    testOrder.setMerchant(testMerchant);

    when(permissionService.getAuthenticatedUser(any())).thenReturn(merchantAdminUser);
    when(orderService.getOrderById(1L)).thenReturn(Optional.of(testOrder));
    when(orderMapper.toDTO(testOrder)).thenReturn(testOrderDTO);

    mockMvc
        .perform(get("/api/orders/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.id").value(1));
  }

  @Test
  @DisplayName("GET /api/orders/{id} should return 403 when merchant admin doesn't own merchant")
  void getOrderById_MerchantAdminDoesNotOwnMerchant_Forbidden() throws Exception {
    Merchant otherMerchant = Merchant.builder().id(99L).name("Other Store").build();
    testOrder.setUser(testUser);
    testOrder.setMerchant(otherMerchant);

    when(permissionService.getAuthenticatedUser(any())).thenReturn(merchantAdminUser);
    when(orderService.getOrderById(1L)).thenReturn(Optional.of(testOrder));

    mockMvc
        .perform(get("/api/orders/1"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("You don't have permission to view this order"));
  }

  @Test
  @DisplayName("GET /api/orders/{id} should return 200 when driver is assigned to order")
  void getOrderById_DriverAssignedToOrder_Success() throws Exception {
    testOrder.setUser(testUser);
    testOrder.setMerchant(testMerchant);
    testOrder.setDriver(testDriver);

    when(permissionService.getAuthenticatedUser(any())).thenReturn(driverUser);
    when(orderService.getOrderById(1L)).thenReturn(Optional.of(testOrder));
    when(orderMapper.toDTO(testOrder)).thenReturn(testOrderDTO);

    mockMvc
        .perform(get("/api/orders/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.id").value(1));
  }

  @Test
  @DisplayName("GET /api/orders/{id} should return 403 when driver is not assigned to order")
  void getOrderById_DriverNotAssignedToOrder_Forbidden() throws Exception {
    Driver otherDriver = Driver.builder().id(99L).name("Other Driver").build();
    testOrder.setUser(testUser);
    testOrder.setMerchant(testMerchant);
    testOrder.setDriver(otherDriver);

    when(permissionService.getAuthenticatedUser(any())).thenReturn(driverUser);
    when(orderService.getOrderById(1L)).thenReturn(Optional.of(testOrder));

    mockMvc
        .perform(get("/api/orders/1"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("You don't have permission to view this order"));
  }

  @Test
  @DisplayName("GET /api/orders/{id} should return 403 when user doesn't own order")
  void getOrderById_UserDoesNotOwnOrder_Forbidden() throws Exception {
    User otherUser = User.builder().id(99L).name("Other User").build();
    otherUser.addRole(Role.USER);
    testOrder.setUser(otherUser);
    testOrder.setMerchant(testMerchant);

    when(permissionService.getAuthenticatedUser(any())).thenReturn(testUser);
    when(orderService.getOrderById(1L)).thenReturn(Optional.of(testOrder));

    mockMvc
        .perform(get("/api/orders/1"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("You don't have permission to view this order"));
  }

  @Test
  @DisplayName("GET /api/orders/{id} should handle order with null user")
  void getOrderById_OrderWithNullUser_PermissionCheck() throws Exception {
    testOrder.setUser(null);
    testOrder.setMerchant(testMerchant);

    when(permissionService.getAuthenticatedUser(any())).thenReturn(testUser);
    when(orderService.getOrderById(1L)).thenReturn(Optional.of(testOrder));

    mockMvc
        .perform(get("/api/orders/1"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.success").value(false));
  }

  @Test
  @DisplayName("GET /api/orders/{id} should handle order with null merchant")
  void getOrderById_OrderWithNullMerchant_PermissionCheck() throws Exception {
    testOrder.setUser(testUser);
    testOrder.setMerchant(null);

    when(permissionService.getAuthenticatedUser(any())).thenReturn(testUser);
    when(orderService.getOrderById(1L)).thenReturn(Optional.of(testOrder));
    when(orderMapper.toDTO(testOrder)).thenReturn(testOrderDTO);

    mockMvc
        .perform(get("/api/orders/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));
  }

  @Test
  @DisplayName("GET /api/orders/{id} should handle order with null driver")
  void getOrderById_OrderWithNullDriver_PermissionCheck() throws Exception {
    testOrder.setUser(testUser);
    testOrder.setMerchant(testMerchant);
    testOrder.setDriver(null);

    when(permissionService.getAuthenticatedUser(any())).thenReturn(testUser);
    when(orderService.getOrderById(1L)).thenReturn(Optional.of(testOrder));
    when(orderMapper.toDTO(testOrder)).thenReturn(testOrderDTO);

    mockMvc
        .perform(get("/api/orders/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));
  }

  @Test
  @DisplayName("GET /api/orders/{id} should handle driver user with null driver profile")
  void getOrderById_DriverUserWithNullDriverProfile() throws Exception {
    User driverUserNullProfile = User.builder().id(30L).name("Driver User").driver(null).build();
    driverUserNullProfile.addRole(Role.DRIVER);

    testOrder.setUser(testUser);
    testOrder.setMerchant(testMerchant);
    testOrder.setDriver(testDriver);

    when(permissionService.getAuthenticatedUser(any())).thenReturn(driverUserNullProfile);
    when(orderService.getOrderById(1L)).thenReturn(Optional.of(testOrder));

    mockMvc
        .perform(get("/api/orders/1"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.success").value(false));
  }

  @Test
  @DisplayName("GET /api/orders/{id} should handle merchant admin with null merchantId")
  void getOrderById_MerchantAdminWithNullMerchantId() throws Exception {
    User merchantAdminNullId =
        User.builder().id(20L).name("Merchant Admin").merchantId(null).build();
    merchantAdminNullId.addRole(Role.MERCHANT_ADMIN);

    testOrder.setUser(testUser);
    testOrder.setMerchant(testMerchant);

    when(permissionService.getAuthenticatedUser(any())).thenReturn(merchantAdminNullId);
    when(orderService.getOrderById(1L)).thenReturn(Optional.of(testOrder));

    mockMvc
        .perform(get("/api/orders/1"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.success").value(false));
  }

  // ==================== GET ORDERS BY DISTANCE TESTS ====================

  @Test
  @DisplayName("GET /api/orders/by-distance should return 200 with orders within radius")
  void getOrdersByDistance_Success() throws Exception {
    double latitude = 35.5;
    double longitude = -78.9;
    double radiusKm = 10.0;

    // Set merchant coordinates for distance calculation
    testMerchant.setLatitude(35.51);
    testMerchant.setLongitude(-78.91);
    testOrder.setMerchant(testMerchant);

    when(orderService.getOrdersWithinDistance(latitude, longitude, radiusKm))
        .thenReturn(List.of(testOrder));
    when(orderService.calculateDistance(eq(latitude), eq(longitude), eq(35.51), eq(-78.91)))
        .thenReturn(5.5);
    when(orderService.updateEstimatedDeliveryTime(eq(1L), any(java.time.LocalDateTime.class)))
        .thenReturn(testOrder);
    when(orderMapper.toDriverDTO(eq(testOrder), eq(5.5))).thenReturn(testDriverOrderDTO);

    mockMvc
        .perform(
            get("/api/orders/by-distance")
                .param("latitude", String.valueOf(latitude))
                .param("longitude", String.valueOf(longitude))
                .param("radiusKm", String.valueOf(radiusKm)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Orders within distance retrieved successfully"))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data[0].id").value(1))
        .andExpect(jsonPath("$.data[0].distanceKm").value(5.5))
        .andExpect(jsonPath("$.data[0].etaMin").exists());
  }

  @Test
  @DisplayName("GET /api/orders/by-distance should return 200 with empty list when no orders")
  void getOrdersByDistance_EmptyList() throws Exception {
    double latitude = 35.5;
    double longitude = -78.9;
    double radiusKm = 10.0;

    when(orderService.getOrdersWithinDistance(latitude, longitude, radiusKm)).thenReturn(List.of());

    mockMvc
        .perform(
            get("/api/orders/by-distance")
                .param("latitude", String.valueOf(latitude))
                .param("longitude", String.valueOf(longitude))
                .param("radiusKm", String.valueOf(radiusKm)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data.length()").value(0));
  }

  @Test
  @DisplayName("GET /api/orders/by-distance should return 400 when radiusKm is zero")
  void getOrdersByDistance_ZeroRadius() throws Exception {
    mockMvc
        .perform(
            get("/api/orders/by-distance")
                .param("latitude", "35.5")
                .param("longitude", "-78.9")
                .param("radiusKm", "0"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Invalid location or radius"));

    verify(orderService, never()).getOrdersWithinDistance(anyDouble(), anyDouble(), anyDouble());
  }

  @Test
  @DisplayName("GET /api/orders/by-distance should return 400 when radiusKm is negative")
  void getOrdersByDistance_NegativeRadius() throws Exception {
    mockMvc
        .perform(
            get("/api/orders/by-distance")
                .param("latitude", "35.5")
                .param("longitude", "-78.9")
                .param("radiusKm", "-5.0"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Invalid location or radius"));

    verify(orderService, never()).getOrdersWithinDistance(anyDouble(), anyDouble(), anyDouble());
  }

  @Test
  @DisplayName("GET /api/orders/by-distance should return 400 when latitude < -90")
  void getOrdersByDistance_LatitudeTooLow() throws Exception {
    mockMvc
        .perform(
            get("/api/orders/by-distance")
                .param("latitude", "-91")
                .param("longitude", "-78.9")
                .param("radiusKm", "10.0"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Invalid location or radius"));

    verify(orderService, never()).getOrdersWithinDistance(anyDouble(), anyDouble(), anyDouble());
  }

  @Test
  @DisplayName("GET /api/orders/by-distance should return 400 when latitude > 90")
  void getOrdersByDistance_LatitudeTooHigh() throws Exception {
    mockMvc
        .perform(
            get("/api/orders/by-distance")
                .param("latitude", "91")
                .param("longitude", "-78.9")
                .param("radiusKm", "10.0"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Invalid location or radius"));

    verify(orderService, never()).getOrdersWithinDistance(anyDouble(), anyDouble(), anyDouble());
  }

  @Test
  @DisplayName("GET /api/orders/by-distance should return 400 when longitude < -180")
  void getOrdersByDistance_LongitudeTooLow() throws Exception {
    mockMvc
        .perform(
            get("/api/orders/by-distance")
                .param("latitude", "35.5")
                .param("longitude", "-181")
                .param("radiusKm", "10.0"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Invalid location or radius"));

    verify(orderService, never()).getOrdersWithinDistance(anyDouble(), anyDouble(), anyDouble());
  }

  @Test
  @DisplayName("GET /api/orders/by-distance should return 400 when longitude > 180")
  void getOrdersByDistance_LongitudeTooHigh() throws Exception {
    mockMvc
        .perform(
            get("/api/orders/by-distance")
                .param("latitude", "35.5")
                .param("longitude", "181")
                .param("radiusKm", "10.0"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Invalid location or radius"));

    verify(orderService, never()).getOrdersWithinDistance(anyDouble(), anyDouble(), anyDouble());
  }

  @Test
  @DisplayName("GET /api/orders/by-distance should accept boundary latitude values")
  void getOrdersByDistance_BoundaryLatitude() throws Exception {
    when(orderService.getOrdersWithinDistance(-90.0, -78.9, 10.0)).thenReturn(List.of(testOrder));
    when(orderMapper.toDTO(testOrder)).thenReturn(testOrderDTO);

    mockMvc
        .perform(
            get("/api/orders/by-distance")
                .param("latitude", "-90")
                .param("longitude", "-78.9")
                .param("radiusKm", "10.0"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    when(orderService.getOrdersWithinDistance(90.0, -78.9, 10.0)).thenReturn(List.of(testOrder));

    mockMvc
        .perform(
            get("/api/orders/by-distance")
                .param("latitude", "90")
                .param("longitude", "-78.9")
                .param("radiusKm", "10.0"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));
  }

  @Test
  @DisplayName("GET /api/orders/by-distance should accept boundary longitude values")
  void getOrdersByDistance_BoundaryLongitude() throws Exception {
    when(orderService.getOrdersWithinDistance(35.5, -180.0, 10.0)).thenReturn(List.of(testOrder));
    when(orderMapper.toDTO(testOrder)).thenReturn(testOrderDTO);

    mockMvc
        .perform(
            get("/api/orders/by-distance")
                .param("latitude", "35.5")
                .param("longitude", "-180")
                .param("radiusKm", "10.0"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    when(orderService.getOrdersWithinDistance(35.5, 180.0, 10.0)).thenReturn(List.of(testOrder));

    mockMvc
        .perform(
            get("/api/orders/by-distance")
                .param("latitude", "35.5")
                .param("longitude", "180")
                .param("radiusKm", "10.0"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));
  }

  @Test
  @DisplayName("GET /api/orders/by-distance should return 400 on exception")
  void getOrdersByDistance_Exception() throws Exception {
    when(orderService.getOrdersWithinDistance(35.5, -78.9, 10.0))
        .thenThrow(new RuntimeException("Database error"));

    mockMvc
        .perform(
            get("/api/orders/by-distance")
                .param("latitude", "35.5")
                .param("longitude", "-78.9")
                .param("radiusKm", "10.0"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.containsString("Failed to retrieve orders")));
  }

  // ==================== ADDITIONAL EDGE CASE TESTS ====================

  @Test
  @DisplayName("POST /api/orders should handle empty items list")
  void createOrder_EmptyItemsList() throws Exception {
    CreateOrderRequest emptyRequest =
        CreateOrderRequest.builder().userId(1L).merchantId(1L).items(List.of()).build();

    when(permissionService.getAuthenticatedUser(any())).thenReturn(testUser);
    when(orderMapper.toEntity(any(CreateOrderRequest.class))).thenReturn(testOrder);
    when(orderService.createOrder(any(Order.class))).thenReturn(testOrder);
    when(orderMapper.toDTO(testOrder)).thenReturn(testOrderDTO);

    mockMvc
        .perform(
            post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyRequest)))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("PUT /api/orders/{id}/status should handle various status values")
  void updateOrderStatus_VariousStatuses() throws Exception {
    String[] statuses = {
      "PENDING", "CONFIRMED", "PREPARING", "READY", "OUT_FOR_DELIVERY", "DELIVERED", "CANCELLED"
    };

    for (String status : statuses) {
      when(orderService.getOrderById(1L)).thenReturn(Optional.of(testOrder));
      when(orderService.updateOrderStatus(1L, status)).thenReturn(testOrder);
      when(orderMapper.toDTO(testOrder)).thenReturn(testOrderDTO);

      mockMvc
          .perform(put("/api/orders/1/status").param("status", status))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true));
    }
  }

  @Test
  @DisplayName("GET endpoints should handle AccessDeniedException from PermissionService")
  void getOrderById_PermissionServiceThrowsAccessDenied() throws Exception {
    when(permissionService.getAuthenticatedUser(any()))
        .thenThrow(new org.springframework.security.access.AccessDeniedException("Access denied"));

    mockMvc
        .perform(get("/api/orders/1"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Access denied"));
  }

  @Test
  @DisplayName(
      "POST /api/orders/{id}/cancel should handle order cancellation business logic exception")
  void cancelOrder_BusinessLogicException() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(testUser);
    when(orderService.getOrderById(1L)).thenReturn(Optional.of(testOrder));
    when(orderService.cancelOrder(1L))
        .thenThrow(new IllegalStateException("Cannot cancel order in current status"));

    mockMvc
        .perform(post("/api/orders/1/cancel"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.containsString("Failed to cancel order")));
  }
}
