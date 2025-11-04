package com.boozebuddies.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.boozebuddies.config.TestSecurityConfig;
import com.boozebuddies.dto.PaymentDTO;
import com.boozebuddies.entity.Order;
import com.boozebuddies.entity.Payment;
import com.boozebuddies.entity.User;
import com.boozebuddies.mapper.PaymentMapper;
import com.boozebuddies.model.PaymentStatus;
import com.boozebuddies.model.Role;
import com.boozebuddies.security.JwtAuthenticationFilter;
import com.boozebuddies.service.OrderService;
import com.boozebuddies.service.PaymentService;
import com.boozebuddies.service.PermissionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    controllers = PaymentController.class,
    excludeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = JwtAuthenticationFilter.class))
@AutoConfigureMockMvc(addFilters = false)
@Import(TestSecurityConfig.class)
@DisplayName("PaymentController Tests")
public class PaymentControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private PaymentService paymentService;
  @MockBean private PaymentMapper paymentMapper;
  @MockBean private PermissionService permissionService;
  @MockBean private OrderService orderService;

  private User testUser;
  private User adminUser;
  private User otherUser;
  private Order testOrder;
  private Payment testPayment;
  private PaymentDTO testPaymentDTO;

  @BeforeEach
  void setUp() {
    testUser = User.builder().id(1L).name("John Doe").build();
    testUser.addRole(Role.USER);

    adminUser = User.builder().id(99L).name("Admin").build();
    adminUser.setRoles(java.util.Set.of(Role.ADMIN));

    otherUser = User.builder().id(2L).name("Jane Doe").build();
    otherUser.addRole(Role.USER);

    testOrder = Order.builder().id(1L).user(testUser).totalAmount(new BigDecimal("99.99")).build();

    testPayment =
        Payment.builder()
            .id(1L)
            .order(testOrder)
            .user(testUser)
            .amount(new BigDecimal("99.99"))
            .status(PaymentStatus.AUTHORIZED)
            .paymentMethod("credit_card")
            .createdAt(LocalDateTime.now())
            .build();

    testPaymentDTO =
        PaymentDTO.builder()
            .id(1L)
            .orderId(1L)
            .userId(1L)
            .amount(new BigDecimal("99.99"))
            .status(PaymentStatus.AUTHORIZED.name())
            .paymentMethod("credit_card")
            .build();
  }

  // ==================== PROCESS PAYMENT TESTS ====================

  @Test
  @DisplayName("POST /api/payments/process should return 200 on success")
  void processPayment_Success() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(testUser);
    when(orderService.getOrderById(1L)).thenReturn(Optional.of(testOrder));
    when(paymentService.processPayment(testOrder, "credit_card")).thenReturn(testPayment);
    when(paymentMapper.toDTO(testPayment)).thenReturn(testPaymentDTO);

    mockMvc
        .perform(post("/api/payments/process?orderId=1&paymentMethod=credit_card"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Payment processed successfully"))
        .andExpect(jsonPath("$.data.id").value(1));
  }

  @Test
  @DisplayName("POST /api/payments/process should return 400 when order not found")
  void processPayment_OrderNotFound() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(testUser);
    when(orderService.getOrderById(999L)).thenReturn(Optional.empty());

    mockMvc
        .perform(post("/api/payments/process?orderId=999&paymentMethod=credit_card"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Order not found")));
  }

  @Test
  @DisplayName("POST /api/payments/process should return 403 when order doesn't belong to user")
  void processPayment_AccessDenied() throws Exception {
    Order otherUserOrder = Order.builder().id(2L).user(otherUser).build();

    when(permissionService.getAuthenticatedUser(any())).thenReturn(testUser);
    when(orderService.getOrderById(2L)).thenReturn(Optional.of(otherUserOrder));

    mockMvc
        .perform(post("/api/payments/process?orderId=2&paymentMethod=credit_card"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.containsString("only pay for your own orders")));
  }

  @Test
  @DisplayName("POST /api/payments/process should return 403 when order has null user")
  void processPayment_OrderNullUser() throws Exception {
    Order orderWithNullUser = Order.builder().id(3L).user(null).build();

    when(permissionService.getAuthenticatedUser(any())).thenReturn(testUser);
    when(orderService.getOrderById(3L)).thenReturn(Optional.of(orderWithNullUser));

    mockMvc
        .perform(post("/api/payments/process?orderId=3&paymentMethod=credit_card"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.success").value(false));
  }

  @Test
  @DisplayName("POST /api/payments/process should return 400 on exception")
  void processPayment_Exception() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(testUser);
    when(orderService.getOrderById(1L)).thenReturn(Optional.of(testOrder));
    when(paymentService.processPayment(testOrder, "credit_card"))
        .thenThrow(new RuntimeException("Payment gateway error"));

    mockMvc
        .perform(post("/api/payments/process?orderId=1&paymentMethod=credit_card"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.containsString("Failed to process payment")));
  }

  // ==================== REFUND PAYMENT TESTS ====================

  @Test
  @DisplayName("POST /api/payments/refund should return 200 on success")
  void refundPayment_Success() throws Exception {
    Payment refundedPayment =
        Payment.builder()
            .id(2L)
            .order(testOrder)
            .user(testUser)
            .amount(new BigDecimal("99.99"))
            .status(PaymentStatus.REFUNDED)
            .build();
    PaymentDTO refundedDTO =
        PaymentDTO.builder().id(2L).orderId(1L).status(PaymentStatus.REFUNDED.name()).build();

    when(orderService.getOrderById(1L)).thenReturn(Optional.of(testOrder));
    when(paymentService.refundPayment(testOrder, "customer_request")).thenReturn(refundedPayment);
    when(paymentMapper.toDTO(refundedPayment)).thenReturn(refundedDTO);

    mockMvc
        .perform(post("/api/payments/refund?orderId=1&reason=customer_request"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Refund processed successfully"))
        .andExpect(jsonPath("$.data.status").value("REFUNDED"));
  }

  @Test
  @DisplayName("POST /api/payments/refund should return 400 when order not found")
  void refundPayment_OrderNotFound() throws Exception {
    when(orderService.getOrderById(999L)).thenReturn(Optional.empty());

    mockMvc
        .perform(post("/api/payments/refund?orderId=999&reason=test"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Order not found")));
  }

  @Test
  @DisplayName("POST /api/payments/refund should return 400 on exception")
  void refundPayment_Exception() throws Exception {
    when(orderService.getOrderById(1L)).thenReturn(Optional.of(testOrder));
    when(paymentService.refundPayment(testOrder, "reason"))
        .thenThrow(new RuntimeException("Refund failed"));

    mockMvc
        .perform(post("/api/payments/refund?orderId=1&reason=reason"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.containsString("Failed to process refund")));
  }

  // ==================== GET MY PAYMENTS TESTS ====================

  @Test
  @DisplayName("GET /api/payments/my-payments should return 200 with user's payments")
  void getMyPayments_Success() throws Exception {
    Pageable pageable = PageRequest.of(0, 10);
    Page<Payment> pagedPayments = new PageImpl<>(List.of(testPayment), pageable, 1);

    when(permissionService.getAuthenticatedUser(any())).thenReturn(testUser);
    when(paymentService.getPaymentsByUser(testUser, pageable)).thenReturn(pagedPayments);
    when(paymentMapper.toDTO(testPayment)).thenReturn(testPaymentDTO);

    mockMvc
        .perform(get("/api/payments/my-payments?page=0&size=10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Your payments retrieved successfully"))
        .andExpect(jsonPath("$.data.content").isArray())
        .andExpect(jsonPath("$.data.totalElements").value(1));
  }

  @Test
  @DisplayName("GET /api/payments/my-payments should return 200 with empty page")
  void getMyPayments_EmptyPage() throws Exception {
    Pageable pageable = PageRequest.of(0, 10);
    Page<Payment> emptyPage = new PageImpl<>(List.of(), pageable, 0);

    when(permissionService.getAuthenticatedUser(any())).thenReturn(testUser);
    when(paymentService.getPaymentsByUser(testUser, pageable)).thenReturn(emptyPage);

    mockMvc
        .perform(get("/api/payments/my-payments?page=0&size=10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.totalElements").value(0));
  }

  @Test
  @DisplayName("GET /api/payments/my-payments should return 400 on exception")
  void getMyPayments_Exception() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(testUser);
    when(paymentService.getPaymentsByUser(any(), any()))
        .thenThrow(new RuntimeException("Database error"));

    mockMvc
        .perform(get("/api/payments/my-payments"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.containsString("Failed to retrieve payments")));
  }

  // ==================== GET PAYMENTS BY USER TESTS ====================

  @Test
  @DisplayName("GET /api/payments/user/{userId} should return 200 when user views own payments")
  void getPaymentsByUser_OwnPayments() throws Exception {
    Pageable pageable = PageRequest.of(0, 10);
    Page<Payment> pagedPayments = new PageImpl<>(List.of(testPayment), pageable, 1);

    when(permissionService.getAuthenticatedUser(any())).thenReturn(testUser);
    when(paymentService.getPaymentsByUser(any(User.class), eq(pageable))).thenReturn(pagedPayments);
    when(paymentMapper.toDTO(testPayment)).thenReturn(testPaymentDTO);

    mockMvc
        .perform(get("/api/payments/user/1?page=0&size=10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Payments retrieved successfully"))
        .andExpect(jsonPath("$.data.totalElements").value(1));
  }

  @Test
  @DisplayName(
      "GET /api/payments/user/{userId} should return 200 when admin views any user's payments")
  void getPaymentsByUser_AdminAccess() throws Exception {
    Pageable pageable = PageRequest.of(0, 10);
    Page<Payment> pagedPayments = new PageImpl<>(List.of(testPayment), pageable, 1);

    when(permissionService.getAuthenticatedUser(any())).thenReturn(adminUser);
    when(paymentService.getPaymentsByUser(any(User.class), eq(pageable))).thenReturn(pagedPayments);
    when(paymentMapper.toDTO(testPayment)).thenReturn(testPaymentDTO);

    mockMvc
        .perform(get("/api/payments/user/1?page=0&size=10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));
  }

  @Test
  @DisplayName(
      "GET /api/payments/user/{userId} should return 403 when non-admin views other user's payments")
  void getPaymentsByUser_AccessDenied() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(testUser);

    mockMvc
        .perform(get("/api/payments/user/2?page=0&size=10"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.containsString("only view your own payment history")));
  }

  @Test
  @DisplayName("GET /api/payments/user/{userId} should return 400 on exception")
  void getPaymentsByUser_Exception() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(testUser);
    when(paymentService.getPaymentsByUser(any(), any()))
        .thenThrow(new RuntimeException("Database error"));

    mockMvc
        .perform(get("/api/payments/user/1"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.containsString("Failed to retrieve payments")));
  }

  // ==================== GET PAYMENT BY ORDER ID TESTS ====================

  @Test
  @DisplayName(
      "GET /api/payments/order/{orderId} should return 200 when user views own order payment")
  void getPaymentByOrderId_Success() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(testUser);
    when(orderService.getOrderById(1L)).thenReturn(Optional.of(testOrder));
    when(paymentService.getPaymentByOrderId(1L)).thenReturn(Optional.of(testPayment));
    when(paymentMapper.toDTO(testPayment)).thenReturn(testPaymentDTO);

    mockMvc
        .perform(get("/api/payments/order/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Payment retrieved successfully"))
        .andExpect(jsonPath("$.data.id").value(1));
  }

  @Test
  @DisplayName(
      "GET /api/payments/order/{orderId} should return 200 when admin views any order payment")
  void getPaymentByOrderId_AdminAccess() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(adminUser);
    when(orderService.getOrderById(1L)).thenReturn(Optional.of(testOrder));
    when(paymentService.getPaymentByOrderId(1L)).thenReturn(Optional.of(testPayment));
    when(paymentMapper.toDTO(testPayment)).thenReturn(testPaymentDTO);

    mockMvc
        .perform(get("/api/payments/order/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));
  }

  @Test
  @DisplayName("GET /api/payments/order/{orderId} should return 400 when order not found")
  void getPaymentByOrderId_OrderNotFound() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(testUser);
    when(orderService.getOrderById(999L)).thenReturn(Optional.empty());

    mockMvc
        .perform(get("/api/payments/order/999"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Order not found")));
  }

  @Test
  @DisplayName("GET /api/payments/order/{orderId} should return 404 when payment not found")
  void getPaymentByOrderId_PaymentNotFound() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(testUser);
    when(orderService.getOrderById(1L)).thenReturn(Optional.of(testOrder));
    when(paymentService.getPaymentByOrderId(1L)).thenReturn(Optional.empty());

    mockMvc.perform(get("/api/payments/order/1")).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName(
      "GET /api/payments/order/{orderId} should return 403 when order doesn't belong to user")
  void getPaymentByOrderId_AccessDenied() throws Exception {
    Order otherUserOrder = Order.builder().id(2L).user(otherUser).build();

    when(permissionService.getAuthenticatedUser(any())).thenReturn(testUser);
    when(orderService.getOrderById(2L)).thenReturn(Optional.of(otherUserOrder));

    mockMvc
        .perform(get("/api/payments/order/2"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(
                    org.hamcrest.Matchers.containsString(
                        "only view payments for your own orders")));
  }

  @Test
  @DisplayName("GET /api/payments/order/{orderId} should return 403 when order has null user")
  void getPaymentByOrderId_OrderNullUser() throws Exception {
    Order orderWithNullUser = Order.builder().id(3L).user(null).build();

    when(permissionService.getAuthenticatedUser(any())).thenReturn(testUser);
    when(orderService.getOrderById(3L)).thenReturn(Optional.of(orderWithNullUser));

    mockMvc
        .perform(get("/api/payments/order/3"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.success").value(false));
  }

  @Test
  @DisplayName("GET /api/payments/order/{orderId} should return 400 on exception")
  void getPaymentByOrderId_Exception() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(testUser);
    when(orderService.getOrderById(1L)).thenReturn(Optional.of(testOrder));
    when(paymentService.getPaymentByOrderId(1L)).thenThrow(new RuntimeException("Database error"));

    mockMvc
        .perform(get("/api/payments/order/1"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.containsString("Failed to retrieve payment")));
  }

  // ==================== CALCULATE REVENUE TESTS ====================

  @Test
  @DisplayName("GET /api/payments/revenue should return 200 with calculated revenue")
  void calculateTotalRevenue_Success() throws Exception {
    BigDecimal revenue = new BigDecimal("5000.00");

    when(paymentService.calculateTotalRevenue(any(LocalDateTime.class), any(LocalDateTime.class)))
        .thenReturn(revenue);

    mockMvc
        .perform(
            get("/api/payments/revenue")
                .param("startDate", "2024-01-01T00:00:00")
                .param("endDate", "2024-12-31T23:59:59"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Revenue calculated successfully"));
  }

  @Test
  @DisplayName("GET /api/payments/revenue should return 400 on exception")
  void calculateTotalRevenue_Exception() throws Exception {
    when(paymentService.calculateTotalRevenue(any(LocalDateTime.class), any(LocalDateTime.class)))
        .thenThrow(new RuntimeException("Calculation error"));

    mockMvc
        .perform(
            get("/api/payments/revenue")
                .param("startDate", "2024-01-01T00:00:00")
                .param("endDate", "2024-12-31T23:59:59"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.containsString("Failed to calculate revenue")));
  }

  // ==================== GET ALL PAYMENTS TESTS ====================

  @Test
  @DisplayName("GET /api/payments should return 200 with all payments")
  void getAllPayments_Success() throws Exception {
    Pageable pageable = PageRequest.of(0, 10);
    Payment payment2 = Payment.builder().id(2L).order(testOrder).user(testUser).build();
    PaymentDTO paymentDTO2 = PaymentDTO.builder().id(2L).orderId(1L).userId(1L).build();

    Page<Payment> pagedPayments = new PageImpl<>(List.of(testPayment, payment2), pageable, 2);

    when(paymentService.getAllPayments(pageable)).thenReturn(pagedPayments);
    when(paymentMapper.toDTO(testPayment)).thenReturn(testPaymentDTO);
    when(paymentMapper.toDTO(payment2)).thenReturn(paymentDTO2);

    mockMvc
        .perform(get("/api/payments?page=0&size=10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("All payments retrieved successfully"))
        .andExpect(jsonPath("$.data.totalElements").value(2));
  }

  @Test
  @DisplayName("GET /api/payments should return 200 with empty page")
  void getAllPayments_EmptyPage() throws Exception {
    Pageable pageable = PageRequest.of(0, 10);
    Page<Payment> emptyPage = new PageImpl<>(List.of(), pageable, 0);

    when(paymentService.getAllPayments(pageable)).thenReturn(emptyPage);

    mockMvc
        .perform(get("/api/payments?page=0&size=10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.totalElements").value(0));
  }

  @Test
  @DisplayName("GET /api/payments should return 400 on exception")
  void getAllPayments_Exception() throws Exception {
    when(paymentService.getAllPayments(any())).thenThrow(new RuntimeException("Database error"));

    mockMvc
        .perform(get("/api/payments"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.containsString("Failed to retrieve payments")));
  }

  // ==================== VALIDATE PAYMENT METHOD TESTS ====================

  @Test
  @DisplayName(
      "POST /api/payments/validate should return 200 when user validates own payment method")
  void validatePaymentMethod_OwnMethod_Valid() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(testUser);
    when(paymentService.validatePaymentMethod(any(User.class), eq("credit_card"))).thenReturn(true);

    mockMvc
        .perform(post("/api/payments/validate?userId=1&paymentMethod=credit_card"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Payment method validated successfully"))
        .andExpect(jsonPath("$.data").value(true));
  }

  @Test
  @DisplayName("POST /api/payments/validate should return 200 when payment method is invalid")
  void validatePaymentMethod_Invalid() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(testUser);
    when(paymentService.validatePaymentMethod(any(User.class), eq("invalid"))).thenReturn(false);

    mockMvc
        .perform(post("/api/payments/validate?userId=1&paymentMethod=invalid"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data").value(false));
  }

  @Test
  @DisplayName(
      "POST /api/payments/validate should return 200 when admin validates for another user")
  void validatePaymentMethod_AdminAccess() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(adminUser);
    when(paymentService.validatePaymentMethod(any(User.class), eq("credit_card"))).thenReturn(true);

    mockMvc
        .perform(post("/api/payments/validate?userId=1&paymentMethod=credit_card"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data").value(true));
  }

  @Test
  @DisplayName(
      "POST /api/payments/validate should return 403 when non-admin validates for another user")
  void validatePaymentMethod_AccessDenied() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(testUser);

    mockMvc
        .perform(post("/api/payments/validate?userId=2&paymentMethod=credit_card"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(
                    org.hamcrest.Matchers.containsString(
                        "only validate your own payment methods")));
  }

  @Test
  @DisplayName("POST /api/payments/validate should return 400 on exception")
  void validatePaymentMethod_Exception() throws Exception {
    when(permissionService.getAuthenticatedUser(any())).thenReturn(testUser);
    when(paymentService.validatePaymentMethod(any(), any()))
        .thenThrow(new RuntimeException("Validation service error"));

    mockMvc
        .perform(post("/api/payments/validate?userId=1&paymentMethod=credit_card"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.containsString("Failed to validate payment method")));
  }
}
