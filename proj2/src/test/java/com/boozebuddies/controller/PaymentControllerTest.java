package com.boozebuddies.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.boozebuddies.dto.ApiResponse;
import com.boozebuddies.dto.PaymentDTO;
import com.boozebuddies.entity.Order;
import com.boozebuddies.entity.Payment;
import com.boozebuddies.entity.User;
import com.boozebuddies.mapper.PaymentMapper;
import com.boozebuddies.model.PaymentStatus;
import com.boozebuddies.service.OrderService;
import com.boozebuddies.service.PaymentService;
import com.boozebuddies.service.PermissionService;
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
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
public class PaymentControllerTest {

    @Mock private PaymentService paymentService;
    @Mock private PaymentMapper paymentMapper;
    @Mock private PermissionService permissionService;
    @Mock private OrderService orderService;
    @Mock private Authentication authentication;

    @InjectMocks private PaymentController paymentController;

    private User testUser;
    private Order testOrder;
    private Payment testPayment;
    private PaymentDTO testPaymentDTO;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).name("John Doe").build();
        testOrder = Order.builder().id(1L).user(testUser).totalAmount(new BigDecimal("99.99")).build();
        testPayment = Payment.builder()
                .id(1L)
                .order(testOrder)
                .user(testUser)
                .amount(new BigDecimal("99.99"))
                .status(PaymentStatus.AUTHORIZED)
                .paymentMethod("credit_card")
                .createdAt(LocalDateTime.now())
                .build();
        testPaymentDTO = PaymentDTO.builder()
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
        when(permissionService.getAuthenticatedUser(authentication)).thenReturn(testUser);
        when(orderService.getOrderById(1L)).thenReturn(Optional.of(testOrder));
        when(paymentService.processPayment(testOrder, "credit_card")).thenReturn(testPayment);
        when(paymentMapper.toDTO(testPayment)).thenReturn(testPaymentDTO);

        ResponseEntity<ApiResponse<PaymentDTO>> response =
                paymentController.processPayment(1L, "credit_card", authentication);

        ApiResponse<PaymentDTO> body = response.getBody();
        assertNotNull(body);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(body.isSuccess());
        assertEquals(testPaymentDTO, body.getData());
    }

    @Test
    void refundPayment_Success() {
        Payment refundedPayment = Payment.builder()
                .id(2L)
                .order(testOrder)
                .user(testUser)
                .amount(new BigDecimal("99.99"))
                .status(PaymentStatus.REFUNDED)
                .build();
        PaymentDTO refundedDTO = PaymentDTO.builder()
                .id(2L)
                .orderId(1L)
                .userId(1L)
                .amount(new BigDecimal("99.99"))
                .status(PaymentStatus.REFUNDED.name())
                .build();

        when(orderService.getOrderById(1L)).thenReturn(Optional.of(testOrder));
        when(paymentService.refundPayment(testOrder, "customer_request")).thenReturn(refundedPayment);
        when(paymentMapper.toDTO(refundedPayment)).thenReturn(refundedDTO);

        ResponseEntity<ApiResponse<PaymentDTO>> response =
                paymentController.refundPayment(1L, "customer_request");

        ApiResponse<PaymentDTO> body = response.getBody();
        assertNotNull(body);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(body.isSuccess());
        assertEquals(refundedDTO, body.getData());
    }

    @Test
    void getMyPayments_Success() {
        when(permissionService.getAuthenticatedUser(authentication)).thenReturn(testUser);
        Page<Payment> pagedPayments = new PageImpl<>(List.of(testPayment));
        when(paymentService.getPaymentsByUser(testUser, Pageable.unpaged())).thenReturn(pagedPayments);
        when(paymentMapper.toDTO(testPayment)).thenReturn(testPaymentDTO);

        ResponseEntity<ApiResponse<Page<PaymentDTO>>> response =
                paymentController.getMyPayments(authentication, Pageable.unpaged());

        ApiResponse<Page<PaymentDTO>> body = response.getBody();
        assertNotNull(body);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(body.isSuccess());
        assertEquals(1, body.getData().getTotalElements());
    }

    @Test
    void getPaymentsByUser_Success() {
        when(permissionService.getAuthenticatedUser(authentication)).thenReturn(testUser);
        Page<Payment> pagedPayments = new PageImpl<>(List.of(testPayment));
        when(paymentService.getPaymentsByUser(any(User.class), any(Pageable.class))).thenReturn(pagedPayments);
        when(paymentMapper.toDTO(testPayment)).thenReturn(testPaymentDTO);

        ResponseEntity<ApiResponse<Page<PaymentDTO>>> response =
                paymentController.getPaymentsByUser(1L, authentication, Pageable.unpaged());

        ApiResponse<Page<PaymentDTO>> body = response.getBody();
        assertNotNull(body);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(body.isSuccess());
        assertEquals(1, body.getData().getTotalElements());
    }

    @Test
    void getPaymentByOrderId_Found() {
        when(permissionService.getAuthenticatedUser(authentication)).thenReturn(testUser);
        when(orderService.getOrderById(1L)).thenReturn(Optional.of(testOrder));
        when(paymentService.getPaymentByOrderId(1L)).thenReturn(Optional.of(testPayment));
        when(paymentMapper.toDTO(testPayment)).thenReturn(testPaymentDTO);

        ResponseEntity<ApiResponse<PaymentDTO>> response =
                paymentController.getPaymentByOrderId(1L, authentication);

        ApiResponse<PaymentDTO> body = response.getBody();
        assertNotNull(body);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(body.isSuccess());
        assertEquals(testPaymentDTO, body.getData());
    }

    @Test
    void getPaymentByOrderId_NotFound() {
        when(permissionService.getAuthenticatedUser(authentication)).thenReturn(testUser);
        when(orderService.getOrderById(99L)).thenReturn(Optional.empty());

        ResponseEntity<ApiResponse<PaymentDTO>> response =
                paymentController.getPaymentByOrderId(99L, authentication);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void validatePaymentMethod_Valid() {
        when(permissionService.getAuthenticatedUser(authentication)).thenReturn(testUser);
        when(paymentService.validatePaymentMethod(any(User.class), eq("credit_card"))).thenReturn(true);

        ResponseEntity<ApiResponse<Boolean>> response =
                paymentController.validatePaymentMethod(1L, "credit_card", authentication);

        ApiResponse<Boolean> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.isSuccess());
        assertTrue(body.getData());
    }

    @Test
    void validatePaymentMethod_Invalid() {
        when(permissionService.getAuthenticatedUser(authentication)).thenReturn(testUser);
        when(paymentService.validatePaymentMethod(any(User.class), eq("invalid"))).thenReturn(false);

        ResponseEntity<ApiResponse<Boolean>> response =
                paymentController.validatePaymentMethod(1L, "invalid", authentication);

        ApiResponse<Boolean> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.isSuccess());
        assertFalse(body.getData());
    }
}
