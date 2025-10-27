package com.boozebuddies.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.boozebuddies.dto.ApiResponse;
import com.boozebuddies.dto.PaymentDTO;
import com.boozebuddies.entity.Order;
import com.boozebuddies.entity.Payment;
import com.boozebuddies.entity.User;
import com.boozebuddies.mapper.PaymentMapper;
import com.boozebuddies.model.PaymentStatus;
import com.boozebuddies.service.PaymentService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
public class PaymentControllerTest {

  @Mock private PaymentService paymentService;

  @Mock private PaymentMapper paymentMapper;

  @InjectMocks private PaymentController paymentController;

  private Payment testPayment;
  private PaymentDTO testPaymentDTO;
  private Order testOrder;
  private User testUser;

  @BeforeEach
  void setUp() {
    testUser = User.builder().id(1L).name("John Doe").build();

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

  @Test
  void processPayment_Success() {
    when(paymentService.processPayment(any(Order.class), eq("credit_card")))
        .thenReturn(testPayment);
    when(paymentMapper.toDTO(testPayment)).thenReturn(testPaymentDTO);

    ResponseEntity<ApiResponse<PaymentDTO>> response =
        paymentController.processPayment(1L, "credit_card");

    assertEquals(HttpStatus.OK, response.getStatusCode());
    ApiResponse<PaymentDTO> body = response.getBody();
    assertNotNull(body);
    assertTrue(body.isSuccess());
    assertEquals(testPaymentDTO, body.getData());
    verify(paymentService).processPayment(any(Order.class), eq("credit_card"));
  }

  @Test
  void refundPayment_Success() {
    Payment refundedPayment =
        Payment.builder()
            .id(2L)
            .order(testOrder)
            .user(testUser)
            .amount(new BigDecimal("99.99"))
            .status(PaymentStatus.REFUNDED)
            .refundReason("customer_request")
            .build();

    PaymentDTO refundedDTO =
        PaymentDTO.builder()
            .id(2L)
            .orderId(1L)
            .userId(1L)
            .amount(new BigDecimal("99.99"))
            .status(PaymentStatus.REFUNDED.name())
            .build();

    when(paymentService.refundPayment(any(Order.class), eq("customer_request")))
        .thenReturn(refundedPayment);
    when(paymentMapper.toDTO(refundedPayment)).thenReturn(refundedDTO);

    ResponseEntity<ApiResponse<PaymentDTO>> response =
        paymentController.refundPayment(1L, "customer_request");

    assertEquals(HttpStatus.OK, response.getStatusCode());
    ApiResponse<PaymentDTO> body = response.getBody();
    assertNotNull(body);
    assertTrue(body.isSuccess());
    assertEquals(refundedDTO, body.getData());
    verify(paymentService).refundPayment(any(Order.class), eq("customer_request"));
  }

  @Test
  @SuppressWarnings("unchecked")
  void getPaymentsByUser_Success() {
    Page<Payment> pagedPayments = new PageImpl<>(List.of(testPayment));
    when(paymentService.getPaymentsByUser(any(User.class), any(Pageable.class)))
        .thenReturn(pagedPayments);

    ResponseEntity<?> response = paymentController.getPaymentsByUser(1L, Pageable.unpaged());

    assertEquals(HttpStatus.OK, response.getStatusCode());
    ApiResponse<Page<Payment>> body = (ApiResponse<Page<Payment>>) response.getBody();
    assertNotNull(body);
    assertTrue(body.isSuccess());
    assertEquals(pagedPayments, body.getData());
  }

  @Test
  @SuppressWarnings("unchecked")
  void getPaymentByOrderId_Found() {
    when(paymentService.getPaymentByOrderId(1L)).thenReturn(Optional.of(testPayment));
    when(paymentMapper.toDTO(testPayment)).thenReturn(testPaymentDTO);

    ResponseEntity<?> response = paymentController.getPaymentByOrderId(1L);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    ApiResponse<PaymentDTO> body = (ApiResponse<PaymentDTO>) response.getBody();
    assertNotNull(body);
    assertTrue(body.isSuccess());
    assertEquals(testPaymentDTO, body.getData());
  }

  @Test
  void getPaymentByOrderId_NotFound() {
    when(paymentService.getPaymentByOrderId(99L)).thenReturn(Optional.empty());

    ResponseEntity<?> response = paymentController.getPaymentByOrderId(99L);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  @SuppressWarnings("unchecked")
  void calculateTotalRevenue_Success() {
    LocalDateTime start = LocalDateTime.now().minusDays(7);
    LocalDateTime end = LocalDateTime.now();
    BigDecimal expectedRevenue = new BigDecimal("299.97");

    when(paymentService.calculateTotalRevenue(any(), any())).thenReturn(expectedRevenue);

    ResponseEntity<?> response = paymentController.calculateTotalRevenue(start, end);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    ApiResponse<BigDecimal> body = (ApiResponse<BigDecimal>) response.getBody();
    assertNotNull(body);
    assertTrue(body.isSuccess());
    assertEquals(expectedRevenue, body.getData());
  }

  @Test
  @SuppressWarnings("unchecked")
  void validatePaymentMethod_Valid() {
    when(paymentService.validatePaymentMethod(any(User.class), eq("credit_card"))).thenReturn(true);

    ResponseEntity<?> response = paymentController.validatePaymentMethod(1L, "credit_card");

    assertEquals(HttpStatus.OK, response.getStatusCode());
    ApiResponse<Boolean> body = (ApiResponse<Boolean>) response.getBody();
    assertNotNull(body);
    assertTrue(body.isSuccess());
    assertTrue(body.getData());
  }

  @Test
  @SuppressWarnings("unchecked")
  void validatePaymentMethod_Invalid() {
    when(paymentService.validatePaymentMethod(any(User.class), eq("invalid_method")))
        .thenReturn(false);

    ResponseEntity<?> response = paymentController.validatePaymentMethod(1L, "invalid_method");

    assertEquals(HttpStatus.OK, response.getStatusCode());
    ApiResponse<Boolean> body = (ApiResponse<Boolean>) response.getBody();
    assertNotNull(body);
    assertTrue(body.isSuccess());
    assertFalse(body.getData());
  }
}
