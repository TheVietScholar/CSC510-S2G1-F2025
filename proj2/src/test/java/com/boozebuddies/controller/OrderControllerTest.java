package com.boozebuddies.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.boozebuddies.dto.ApiResponse;
import com.boozebuddies.dto.CreateOrderRequest;
import com.boozebuddies.dto.OrderDTO;
import com.boozebuddies.dto.OrderItemRequest;
import com.boozebuddies.entity.Order;
import com.boozebuddies.entity.OrderItem;
import com.boozebuddies.entity.Product;
import com.boozebuddies.entity.User;
import com.boozebuddies.entity.Merchant;
import com.boozebuddies.model.OrderStatus;
import com.boozebuddies.service.OrderService;

@ExtendWith(MockitoExtension.class)
public class OrderControllerTest {

  @Mock
  private OrderService orderService;

  @InjectMocks
  private OrderController orderController;

  private Order testOrder;
  private User testUser;
  private Merchant testMerchant;
  private OrderItem testOrderItem;
  private Product testProduct;
  private CreateOrderRequest testCreateRequest;

  @BeforeEach
  void setUp() {
    testUser = User.builder()
        .id(1L)
        .name("John Doe")
        .build();

    testMerchant = Merchant.builder()
        .id(1L)
        .name("Test Liquor Store")
        .build();

    testProduct = Product.builder()
        .id(1L)
        .name("Test Beer")
        .price(new BigDecimal("19.99"))
        .merchant(testMerchant)
        .build();

    testOrderItem = OrderItem.builder()
        .id(1L)
        .product(testProduct)
        .quantity(2)
        .unitPrice(new BigDecimal("19.99"))
        .build();

    testOrder = Order.builder()
        .id(1L)
        .user(testUser)
        .merchant(testMerchant)
        .items(List.of(testOrderItem))
        .totalAmount(new BigDecimal("39.98"))
        .status(OrderStatus.PENDING)
        .createdAt(LocalDateTime.now())
        .build();

    testCreateRequest = CreateOrderRequest.builder()
        .userId(1L)
        .merchantId(1L)
        .items(Arrays.asList(
            OrderItemRequest.builder()
                .productId(1L)
                .quantity(2)
                .unitPrice(new BigDecimal("10.00"))
                .build()))
        .build();
  }

  @Test
  void createOrder_Success() {
    when(orderService.createOrder(any(Order.class))).thenReturn(testOrder);

    ResponseEntity<ApiResponse<OrderDTO>> response = orderController.createOrder(testCreateRequest);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    ApiResponse<OrderDTO> body = response.getBody();
    assertNotNull(body);
    assertTrue(body.isSuccess());
    assertEquals("Order created successfully", body.getMessage());
  }

  @Test
  void getOrderById_Found() {
    when(orderService.getOrderById(1L)).thenReturn(Optional.of(testOrder));

    ResponseEntity<ApiResponse<OrderDTO>> response = orderController.getOrderById(1L);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    ApiResponse<OrderDTO> body = response.getBody();
    assertNotNull(body);
    assertTrue(body.isSuccess());
    assertEquals("Order retrieved successfully", body.getMessage());
  }

  @Test
  void getOrderById_NotFound() {
    when(orderService.getOrderById(99L)).thenReturn(Optional.empty());

    ResponseEntity<ApiResponse<OrderDTO>> response = orderController.getOrderById(99L);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void getOrdersByUser_Success() {
    when(orderService.getOrdersByUser(1L))
        .thenReturn(List.of(testOrder));

    ResponseEntity<ApiResponse<List<OrderDTO>>> response = orderController.getOrdersByUser(1L);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    ApiResponse<List<OrderDTO>> body = response.getBody();
    assertNotNull(body);
    assertTrue(body.isSuccess());
    assertEquals("User orders retrieved successfully", body.getMessage());
  }

  @Test
  void updateOrderStatus_Success() {
    when(orderService.updateOrderStatus(eq(1L), eq("CONFIRMED")))
        .thenReturn(testOrder);

    ResponseEntity<ApiResponse<OrderDTO>> response = orderController.updateOrderStatus(1L, "CONFIRMED");

    assertEquals(HttpStatus.OK, response.getStatusCode());
    ApiResponse<OrderDTO> body = response.getBody();
    assertNotNull(body);
    assertTrue(body.isSuccess());
    assertEquals("Order status updated successfully", body.getMessage());
  }

  @Test
  void updateOrderStatus_NotFound() {
    when(orderService.updateOrderStatus(eq(99L), anyString()))
        .thenThrow(new IllegalArgumentException("Order not found"));

    ResponseEntity<ApiResponse<OrderDTO>> response = orderController.updateOrderStatus(99L, "CONFIRMED");

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    ApiResponse<OrderDTO> body = response.getBody();
    assertNotNull(body);
    assertFalse(body.isSuccess());
    assertEquals("Failed to update order status: Order not found", body.getMessage());
  }

  @Test
  void cancelOrder_Success() {
    Order cancelledOrder = Order.builder()
        .id(1L)
        .status(OrderStatus.CANCELLED)
        .build();

    when(orderService.cancelOrder(1L)).thenReturn(cancelledOrder);

    ResponseEntity<ApiResponse<OrderDTO>> response = orderController.cancelOrder(1L);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    ApiResponse<OrderDTO> body = response.getBody();
    assertNotNull(body);
    assertTrue(body.isSuccess());
    assertEquals("Order cancelled successfully", body.getMessage());
  }

  @Test
  void cancelOrder_NotFound() {
    when(orderService.cancelOrder(99L))
        .thenThrow(new IllegalArgumentException("Order not found"));

    ResponseEntity<ApiResponse<OrderDTO>> response = orderController.cancelOrder(99L);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    ApiResponse<OrderDTO> body = response.getBody();
    assertNotNull(body);
    assertFalse(body.isSuccess());
    assertEquals("Failed to cancel order: Order not found", body.getMessage());
  }

  @Test
  void createOrder_ValidationError() {
    CreateOrderRequest invalidRequest = CreateOrderRequest.builder().build();
    when(orderService.createOrder(any(Order.class)))
        .thenThrow(new IllegalArgumentException("Invalid order data"));

    ResponseEntity<ApiResponse<OrderDTO>> response = orderController.createOrder(invalidRequest);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    ApiResponse<OrderDTO> body = response.getBody();
    assertNotNull(body);
    assertFalse(body.isSuccess());
    assertEquals("Failed to create order: Invalid order data", body.getMessage());
  }

  @Test
  void updateOrderStatus_InvalidStatus() {
    ResponseEntity<ApiResponse<OrderDTO>> response = orderController.updateOrderStatus(1L, "INVALID_STATUS");

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    ApiResponse<OrderDTO> body = response.getBody();
    assertNotNull(body);
    assertFalse(body.isSuccess());
    assertTrue(body.getMessage().contains("Failed to update order status"));
  }
}
