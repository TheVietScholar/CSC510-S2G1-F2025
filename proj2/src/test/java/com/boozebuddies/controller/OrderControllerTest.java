package com.boozebuddies.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.boozebuddies.dto.*;
import com.boozebuddies.entity.*;
import com.boozebuddies.model.OrderStatus;
import com.boozebuddies.model.Role;
import com.boozebuddies.service.OrderService;
import com.boozebuddies.service.PermissionService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
public class OrderControllerTest {

  @Mock private OrderService orderService;
  @Mock private PermissionService permissionService;
  @Mock private Authentication authentication;

  @InjectMocks private OrderController orderController;

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
  void createOrder_Success() {
    when(permissionService.getAuthenticatedUser(authentication)).thenReturn(testUser);
    when(orderService.createOrder(any(Order.class))).thenReturn(testOrder);

    ResponseEntity<ApiResponse<OrderDTO>> response =
        orderController.createOrder(testCreateRequest, authentication);

    ApiResponse<OrderDTO> body = response.getBody();
    assertNotNull(body);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(body.isSuccess());
    assertEquals("Order created successfully", body.getMessage());
  }

  @Test
  void createOrder_AccessDenied() {
    // User tries to create order for someone else
    CreateOrderRequest invalidRequest =
        CreateOrderRequest.builder()
            .userId(99L)
            .merchantId(1L)
            .items(testCreateRequest.getItems())
            .build();

    when(permissionService.getAuthenticatedUser(authentication)).thenReturn(testUser);

    ResponseEntity<ApiResponse<OrderDTO>> response =
        orderController.createOrder(invalidRequest, authentication);

    ApiResponse<OrderDTO> body = response.getBody();
    assertNotNull(body);
    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    assertTrue(body.getMessage().contains("You can only create orders for yourself"));
  }

  // ==================== GET ORDER ====================
  @Test
  void getOrderById_Success() {
    when(orderService.getOrderById(1L)).thenReturn(Optional.of(testOrder));

    ResponseEntity<ApiResponse<OrderDTO>> response =
        orderController.getOrderById(1L, authentication);

    ApiResponse<OrderDTO> body = response.getBody();
    assertNotNull(body);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(body.isSuccess());
    assertEquals("Order retrieved successfully", body.getMessage());
  }

  @Test
  void getOrderById_NotFound() {
    when(orderService.getOrderById(99L)).thenReturn(Optional.empty());

    ResponseEntity<ApiResponse<OrderDTO>> response =
        orderController.getOrderById(99L, authentication);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void getMyOrders_Success() {
    when(permissionService.getAuthenticatedUser(authentication)).thenReturn(testUser);
    when(orderService.getOrdersByUser(testUser.getId())).thenReturn(List.of(testOrder));

    ResponseEntity<ApiResponse<List<OrderDTO>>> response =
        orderController.getMyOrders(authentication);

    ApiResponse<List<OrderDTO>> body = response.getBody();
    assertNotNull(body);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(body.isSuccess());
    assertEquals(1, body.getData().size());
  }

  // ==================== CANCEL ORDER ====================
  @Test
  void cancelOrder_Success() {
    when(permissionService.getAuthenticatedUser(authentication)).thenReturn(testUser);
    when(orderService.getOrderById(1L)).thenReturn(Optional.of(testOrder));
    when(orderService.cancelOrder(1L)).thenReturn(testOrder);

    ResponseEntity<ApiResponse<OrderDTO>> response =
        orderController.cancelOrder(1L, authentication);

    ApiResponse<OrderDTO> body = response.getBody();
    assertNotNull(body);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(body.isSuccess());
    assertEquals("Order cancelled successfully", body.getMessage());
  }

  @Test
  void cancelOrder_NotOwner() {
    when(permissionService.getAuthenticatedUser(authentication)).thenReturn(testUser);
    User otherUser = User.builder().id(2L).build();
    Order orderForOther = Order.builder().id(1L).user(otherUser).build();
    when(orderService.getOrderById(1L)).thenReturn(Optional.of(orderForOther));

    ResponseEntity<ApiResponse<OrderDTO>> response =
        orderController.cancelOrder(1L, authentication);

    ApiResponse<OrderDTO> body = response.getBody();
    assertNotNull(body);
    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    assertTrue(body.getMessage().contains("You can only cancel your own orders"));
  }

  // ==================== UPDATE ORDER STATUS ====================
  @Test
  void updateOrderStatus_Success() {
    when(orderService.getOrderById(1L)).thenReturn(Optional.of(testOrder));
    when(orderService.updateOrderStatus(1L, "CONFIRMED")).thenReturn(testOrder);

    ResponseEntity<ApiResponse<OrderDTO>> response =
        orderController.updateOrderStatus(1L, "CONFIRMED", authentication);

    ApiResponse<OrderDTO> body = response.getBody();
    assertNotNull(body);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(body.isSuccess());
    assertEquals("Order status updated successfully", body.getMessage());
  }

  @Test
  void updateOrderStatus_NotFound() {
    when(orderService.getOrderById(99L)).thenReturn(Optional.empty());

    ResponseEntity<ApiResponse<OrderDTO>> response =
        orderController.updateOrderStatus(99L, "CONFIRMED", authentication);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }
}
