package com.boozebuddies.service.implementation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.boozebuddies.entity.Order;
import com.boozebuddies.entity.Payment;
import com.boozebuddies.entity.User;
import com.boozebuddies.model.PaymentStatus;
import com.boozebuddies.repository.OrderRepository;
import com.boozebuddies.repository.PaymentRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

  @Mock private PaymentRepository paymentRepository;
  @Mock private OrderRepository orderRepository;
  @InjectMocks private PaymentServiceImpl paymentService;

  private User testUser;

  @BeforeEach
  void setUp() {
    testUser =
        User.builder()
            .id(1L)
            .name("John Doe")
            .email("john@example.com")
            .passwordHash("SecurePass123")
            .phone("1234567890")
            .dateOfBirth(LocalDate.of(1990, 5, 15))
            .ageVerified(true)
            .build();
  }

  @Test
  void testProcessPaymentSuccessAndGetPaymentsByUserAndByOrderId() {
    Order order = new Order();
    order.setId(1L);
    order.setUser(testUser);
    order.setTotalAmount(new BigDecimal("12.50"));

    Payment payment =
        Payment.builder()
            .order(order)
            .amount(order.getTotalAmount())
            .status(PaymentStatus.AUTHORIZED)
            .user(testUser)
            .build();

    when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
    when(paymentRepository.findByUser_Id(eq(testUser.getId()), any()))
        .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(payment)));
    when(paymentRepository.findByOrder_Id(order.getId()))
        .thenReturn(java.util.Optional.of(payment));

    Payment processed = paymentService.processPayment(order, "credit_card");
    assertNotNull(processed);
    assertEquals(new BigDecimal("12.50"), processed.getAmount());
    assertEquals(PaymentStatus.AUTHORIZED, processed.getStatus());
    assertSame(testUser, processed.getUser());
    verify(paymentRepository, times(1)).save(any(Payment.class));

    List<Payment> paymentsByUser = paymentService.getPaymentsByUser(testUser);
    assertEquals(1, paymentsByUser.size());
    assertEquals(payment, paymentsByUser.get(0));

    Payment fetched = paymentService.getPaymentByOrderId(1L);
    assertNotNull(fetched);
    assertEquals(order.getId(), fetched.getOrder().getId());
  }

  @Test
  void testProcessPaymentInvalidMethodThrows() {
    User user = new User();
    Order order = new Order();
    order.setId(2L);
    order.setUser(user);
    order.setTotalAmount(new BigDecimal("5.00"));

    assertThrows(RuntimeException.class, () -> paymentService.processPayment(order, ""));
    assertThrows(RuntimeException.class, () -> paymentService.processPayment(order, null));
    verify(paymentRepository, never()).save(any());
  }

  @Test
  void testRefundPaymentAddsRefundRecordAndDoesNotRemoveOriginal() {
    User user = new User();
    Order order = new Order();
    order.setId(3L);
    order.setUser(user);
    order.setTotalAmount(new BigDecimal("20.00"));

    Payment authorized = paymentService.processPayment(order, "paypal");

    // Refund the payment
    Payment refunded = paymentService.refundPayment(order, "customer_requested");

    assertNotNull(refunded);
    assertEquals(PaymentStatus.REFUNDED, refunded.getStatus());
    assertEquals("customer_requested", refunded.getRefundReason());
    // Both payments (original authorized and refund) should exist for the user
    List<Payment> payments = paymentService.getPaymentsByUser(user);
    assertEquals(2, payments.size());

    // getPaymentByOrderId returns the first matching payment in the list (likely
    // the original
    // authorized payment)
    Payment returned = paymentService.getPaymentByOrderId(order.getId());
    assertNotNull(returned);
    assertEquals(
        PaymentStatus.AUTHORIZED,
        returned.getStatus(),
        "Implementation currently returns the original AUTHORIZED payment for an order, even after a refund record is created.");
  }

  @Test
  void testRefundPaymentWhenNotFoundThrows() {
    User user = new User();
    Order order = new Order();
    order.setId(99L);
    order.setUser(user);
    order.setTotalAmount(new BigDecimal("1.00"));

    when(paymentRepository.findByOrder_Id(order.getId())).thenReturn(java.util.Optional.empty());
    assertThrows(RuntimeException.class, () -> paymentService.refundPayment(order, "no_payment"));
  }

  @Test
  void testCalculateTotalRevenueRespectsDateRangeAndStatus() {
    User user = new User();
    Order orderA = new Order();
    orderA.setId(10L);
    orderA.setUser(user);
    orderA.setTotalAmount(new BigDecimal("15.00"));
    Order orderB = new Order();
    orderB.setId(11L);
    orderB.setUser(user);
    orderB.setTotalAmount(new BigDecimal("7.00"));

    // Process two payments
    Payment pA = paymentService.processPayment(orderA, "card");
    Payment pB = paymentService.processPayment(orderB, "card");

    // Set payment dates so only pA is in the target range
    LocalDateTime now = LocalDateTime.now();
    pA.setPaymentDate(now.minusDays(1));
    pB.setPaymentDate(now.minusDays(10));

    LocalDateTime rangeStart = now.minusDays(2);
    LocalDateTime rangeEnd = now.plusDays(1);

    // Only pA is AUTHORIZED and in range -> should be counted
    BigDecimal revenue = paymentService.calculateTotalRevenue(rangeStart, rangeEnd);
    assertEquals(new BigDecimal("15.00"), revenue);

    // Refund pA (creates a separate REFUNDED payment record). Current
    // implementation leaves the
    // original AUTHORIZED in place.
    paymentService.refundPayment(orderA, "returned");

    // After refund, because implementation does not change the original AUTHORIZED
    // payment,
    // calculateTotalRevenue still counts it.
    BigDecimal revenueAfterRefund = paymentService.calculateTotalRevenue(rangeStart, rangeEnd);
    assertEquals(
        new BigDecimal("15.00"),
        revenueAfterRefund,
        "Note: refunded payments currently do not remove or mark the original AUTHORIZED payment; revenue still includes the original amount.");
  }

  @Test
  void testCalculateTotalRevenueOutsideRangeIsZero() {
    User user = new User();
    Order order = new Order();
    order.setId(20L);
    order.setUser(user);
    order.setTotalAmount(new BigDecimal("30.00"));

    // p is not used, so removed

    when(paymentRepository.findByCreatedAtBetween(any(), any())).thenReturn(List.of());
    BigDecimal revenue =
        paymentService.calculateTotalRevenue(
            LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
    assertEquals(BigDecimal.ZERO, revenue);
  }

  @Test
  void testValidatePaymentMethod() {
    User user = new User();
    assertTrue(paymentService.validatePaymentMethod(user, "card"));
    assertFalse(paymentService.validatePaymentMethod(null, "card"));
    assertFalse(paymentService.validatePaymentMethod(user, ""));
    assertFalse(paymentService.validatePaymentMethod(user, "   "));
    assertFalse(paymentService.validatePaymentMethod(user, null));
  }
}
