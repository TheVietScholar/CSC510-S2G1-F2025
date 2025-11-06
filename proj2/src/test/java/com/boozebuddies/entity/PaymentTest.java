package com.boozebuddies.entity;

import static org.junit.jupiter.api.Assertions.*;

import com.boozebuddies.model.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Payment Entity Tests")
class PaymentTest {

  private Payment payment;
  private Order testOrder;
  private User testUser;

  @BeforeEach
  void setUp() {
    testOrder = Order.builder().id(1L).build();
    testUser = User.builder().id(1L).name("John Doe").email("john@example.com").build();

    payment =
        Payment.builder()
            .id(1L)
            .order(testOrder)
            .user(testUser)
            .amount(new BigDecimal("99.99"))
            .status(PaymentStatus.CAPTURED)
            .paymentMethod("CREDIT_CARD")
            .transactionId("TXN123456")
            .build();
  }

  // ==================== BUILDER TESTS ====================

  @Test
  @DisplayName("Builder creates payment with all fields")
  void testBuilder_AllFields() {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime paymentDate = now.minusHours(1);

    Payment testPayment =
        Payment.builder()
            .id(2L)
            .order(testOrder)
            .user(testUser)
            .amount(new BigDecimal("150.00"))
            .status(PaymentStatus.PENDING)
            .paymentMethod("PAYPAL")
            .transactionId("TXN789")
            .failureReason("Card declined")
            .refundReason("Customer request")
            .createdAt(now)
            .updatedAt(now)
            .paymentDate(paymentDate)
            .build();

    assertNotNull(testPayment);
    assertEquals(2L, testPayment.getId());
    assertEquals(testOrder, testPayment.getOrder());
    assertEquals(testUser, testPayment.getUser());
    assertEquals(new BigDecimal("150.00"), testPayment.getAmount());
    assertEquals(PaymentStatus.PENDING, testPayment.getStatus());
    assertEquals("PAYPAL", testPayment.getPaymentMethod());
    assertEquals("TXN789", testPayment.getTransactionId());
    assertEquals("Card declined", testPayment.getFailureReason());
    assertEquals("Customer request", testPayment.getRefundReason());
    assertEquals(now, testPayment.getCreatedAt());
    assertEquals(now, testPayment.getUpdatedAt());
    assertEquals(paymentDate, testPayment.getPaymentDate());
  }

  @Test
  @DisplayName("Builder creates payment with default values")
  void testBuilder_Defaults() {
    Payment testPayment =
        Payment.builder()
            .order(testOrder)
            .user(testUser)
            .amount(new BigDecimal("50.00"))
            .status(PaymentStatus.CAPTURED)
            .build();

    assertNotNull(testPayment);
    assertEquals("", testPayment.getFailureReason());
    assertEquals("", testPayment.getRefundReason());
    assertNotNull(testPayment.getCreatedAt());
    assertNotNull(testPayment.getUpdatedAt());
  }

  // ==================== GETTERS AND SETTERS TESTS ====================

  @Test
  @DisplayName("getId returns correct value")
  void testGetId() {
    assertEquals(1L, payment.getId());
  }

  @Test
  @DisplayName("setId sets correct value")
  void testSetId() {
    payment.setId(999L);
    assertEquals(999L, payment.getId());
  }

  @Test
  @DisplayName("getOrder returns correct value")
  void testGetOrder() {
    assertEquals(testOrder, payment.getOrder());
  }

  @Test
  @DisplayName("setOrder sets correct value")
  void testSetOrder() {
    Order newOrder = Order.builder().id(2L).build();
    payment.setOrder(newOrder);
    assertEquals(newOrder, payment.getOrder());
  }

  @Test
  @DisplayName("getUser returns correct value")
  void testGetUser() {
    assertEquals(testUser, payment.getUser());
  }

  @Test
  @DisplayName("setUser sets correct value")
  void testSetUser() {
    User newUser = User.builder().id(2L).name("Jane Doe").build();
    payment.setUser(newUser);
    assertEquals(newUser, payment.getUser());
  }

  @Test
  @DisplayName("getAmount returns correct value")
  void testGetAmount() {
    assertEquals(new BigDecimal("99.99"), payment.getAmount());
  }

  @Test
  @DisplayName("setAmount sets correct value")
  void testSetAmount() {
    payment.setAmount(new BigDecimal("200.00"));
    assertEquals(new BigDecimal("200.00"), payment.getAmount());
  }

  @Test
  @DisplayName("getStatus returns correct value")
  void testGetStatus() {
    assertEquals(PaymentStatus.CAPTURED, payment.getStatus());
  }

  @Test
  @DisplayName("setStatus sets correct value")
  void testSetStatus() {
    payment.setStatus(PaymentStatus.FAILED);
    assertEquals(PaymentStatus.FAILED, payment.getStatus());
  }

  @Test
  @DisplayName("getPaymentMethod returns correct value")
  void testGetPaymentMethod() {
    assertEquals("CREDIT_CARD", payment.getPaymentMethod());
  }

  @Test
  @DisplayName("setPaymentMethod sets correct value")
  void testSetPaymentMethod() {
    payment.setPaymentMethod("DEBIT_CARD");
    assertEquals("DEBIT_CARD", payment.getPaymentMethod());
  }

  @Test
  @DisplayName("getTransactionId returns correct value")
  void testGetTransactionId() {
    assertEquals("TXN123456", payment.getTransactionId());
  }

  @Test
  @DisplayName("setTransactionId sets correct value")
  void testSetTransactionId() {
    payment.setTransactionId("TXN999999");
    assertEquals("TXN999999", payment.getTransactionId());
  }

  @Test
  @DisplayName("getFailureReason returns correct value")
  void testGetFailureReason() {
    assertEquals("", payment.getFailureReason());
  }

  @Test
  @DisplayName("setFailureReason sets correct value")
  void testSetFailureReason() {
    payment.setFailureReason("Insufficient funds");
    assertEquals("Insufficient funds", payment.getFailureReason());
  }

  @Test
  @DisplayName("getRefundReason returns correct value")
  void testGetRefundReason() {
    assertEquals("", payment.getRefundReason());
  }

  @Test
  @DisplayName("setRefundReason sets correct value")
  void testSetRefundReason() {
    payment.setRefundReason("Duplicate charge");
    assertEquals("Duplicate charge", payment.getRefundReason());
  }

  @Test
  @DisplayName("getCreatedAt returns correct value")
  void testGetCreatedAt() {
    assertNotNull(payment.getCreatedAt());
  }

  @Test
  @DisplayName("setCreatedAt sets correct value")
  void testSetCreatedAt() {
    LocalDateTime newDate = LocalDateTime.of(2025, 1, 1, 12, 0);
    payment.setCreatedAt(newDate);
    assertEquals(newDate, payment.getCreatedAt());
  }

  @Test
  @DisplayName("getUpdatedAt returns correct value")
  void testGetUpdatedAt() {
    assertNotNull(payment.getUpdatedAt());
  }

  @Test
  @DisplayName("setUpdatedAt sets correct value")
  void testSetUpdatedAt() {
    LocalDateTime newDate = LocalDateTime.of(2025, 2, 1, 12, 0);
    payment.setUpdatedAt(newDate);
    assertEquals(newDate, payment.getUpdatedAt());
  }

  @Test
  @DisplayName("getPaymentDate returns correct value")
  void testGetPaymentDate() {
    assertNull(payment.getPaymentDate());
  }

  @Test
  @DisplayName("setPaymentDate sets correct value")
  void testSetPaymentDate() {
    LocalDateTime paymentDate = LocalDateTime.now();
    payment.setPaymentDate(paymentDate);
    assertEquals(paymentDate, payment.getPaymentDate());
  }

  // ==================== LIFECYCLE TESTS ====================

  @Test
  @DisplayName("preUpdate updates updatedAt timestamp")
  void testPreUpdate() {
    LocalDateTime originalUpdatedAt = payment.getUpdatedAt();

    // Simulate some delay
    try {
      Thread.sleep(10);
    } catch (InterruptedException e) {
      // Ignore
    }

    payment.preUpdate();

    assertNotNull(payment.getUpdatedAt());
    assertTrue(payment.getUpdatedAt().isAfter(originalUpdatedAt));
  }

  // ==================== PAYMENT STATUS TESTS ====================

  @Test
  @DisplayName("Payment can have PENDING status")
  void testPaymentStatus_Pending() {
    payment.setStatus(PaymentStatus.PENDING);
    assertEquals(PaymentStatus.PENDING, payment.getStatus());
  }

  @Test
  @DisplayName("Payment can have COMPLETED status")
  void testPaymentStatus_Completed() {
    payment.setStatus(PaymentStatus.CAPTURED);
    assertEquals(PaymentStatus.CAPTURED, payment.getStatus());
  }

  @Test
  @DisplayName("Payment can have FAILED status")
  void testPaymentStatus_Failed() {
    payment.setStatus(PaymentStatus.FAILED);
    assertEquals(PaymentStatus.FAILED, payment.getStatus());
  }

  @Test
  @DisplayName("Payment can have REFUNDED status")
  void testPaymentStatus_Refunded() {
    payment.setStatus(PaymentStatus.REFUNDED);
    assertEquals(PaymentStatus.REFUNDED, payment.getStatus());
  }

  // ==================== AMOUNT TESTS ====================

  @Test
  @DisplayName("Payment amount can be zero")
  void testAmount_Zero() {
    payment.setAmount(BigDecimal.ZERO);
    assertEquals(BigDecimal.ZERO, payment.getAmount());
  }

  @Test
  @DisplayName("Payment amount can be large value")
  void testAmount_Large() {
    BigDecimal largeAmount = new BigDecimal("99999999.99");
    payment.setAmount(largeAmount);
    assertEquals(largeAmount, payment.getAmount());
  }

  @Test
  @DisplayName("Payment amount preserves decimal precision")
  void testAmount_DecimalPrecision() {
    BigDecimal preciseAmount = new BigDecimal("123.45");
    payment.setAmount(preciseAmount);
    assertEquals(preciseAmount, payment.getAmount());
    assertEquals(2, payment.getAmount().scale());
  }

  // ==================== RELATIONSHIP TESTS ====================

  @Test
  @DisplayName("Payment has one-to-one relationship with Order")
  void testOrderRelationship() {
    assertNotNull(payment.getOrder());
    assertEquals(1L, payment.getOrder().getId());
  }

  @Test
  @DisplayName("Payment has many-to-one relationship with User")
  void testUserRelationship() {
    assertNotNull(payment.getUser());
    assertEquals(1L, payment.getUser().getId());
    assertEquals("John Doe", payment.getUser().getName());
  }

  @Test
  @DisplayName("Payment can have null order temporarily")
  void testNullOrder() {
    payment.setOrder(null);
    assertNull(payment.getOrder());
  }

  @Test
  @DisplayName("Payment can have null user temporarily")
  void testNullUser() {
    payment.setUser(null);
    assertNull(payment.getUser());
  }

  // ==================== CONSTRUCTOR TESTS ====================

  @Test
  @DisplayName("NoArgsConstructor creates empty payment")
  void testNoArgsConstructor() {
    Payment emptyPayment = new Payment();
    assertNotNull(emptyPayment);
  }

  @Test
  @DisplayName("AllArgsConstructor creates payment with all parameters")
  void testAllArgsConstructor() {
    LocalDateTime now = LocalDateTime.now();
    Payment testPayment =
        new Payment(
            5L,
            testOrder,
            new BigDecimal("75.50"),
            PaymentStatus.CAPTURED,
            testUser,
            "CASH",
            "TXN555",
            "None",
            "Customer return",
            now,
            now,
            now);

    assertNotNull(testPayment);
    assertEquals(5L, testPayment.getId());
    assertEquals(testOrder, testPayment.getOrder());
    assertEquals(new BigDecimal("75.50"), testPayment.getAmount());
    assertEquals(PaymentStatus.CAPTURED, testPayment.getStatus());
    assertEquals(testUser, testPayment.getUser());
    assertEquals("CASH", testPayment.getPaymentMethod());
    assertEquals("TXN555", testPayment.getTransactionId());
  }

  // ==================== EDGE CASES ====================

  @Test
  @DisplayName("Payment with null payment method")
  void testNullPaymentMethod() {
    payment.setPaymentMethod(null);
    assertNull(payment.getPaymentMethod());
  }

  @Test
  @DisplayName("Payment with null transaction ID")
  void testNullTransactionId() {
    payment.setTransactionId(null);
    assertNull(payment.getTransactionId());
  }

  @Test
  @DisplayName("Payment with null payment date")
  void testNullPaymentDate() {
    payment.setPaymentDate(null);
    assertNull(payment.getPaymentDate());
  }

  @Test
  @DisplayName("Payment can be updated multiple times")
  void testMultipleUpdates() {
    LocalDateTime firstUpdate = payment.getUpdatedAt();

    try {
      Thread.sleep(10);
    } catch (InterruptedException e) {
      // Ignore
    }

    payment.preUpdate();
    LocalDateTime secondUpdate = payment.getUpdatedAt();

    try {
      Thread.sleep(10);
    } catch (InterruptedException e) {
      // Ignore
    }

    payment.preUpdate();
    LocalDateTime thirdUpdate = payment.getUpdatedAt();

    assertTrue(secondUpdate.isAfter(firstUpdate));
    assertTrue(thirdUpdate.isAfter(secondUpdate));
  }

  @Test
  @DisplayName("Payment failure reason can be empty string")
  void testEmptyFailureReason() {
    payment.setFailureReason("");
    assertEquals("", payment.getFailureReason());
  }

  @Test
  @DisplayName("Payment refund reason can be empty string")
  void testEmptyRefundReason() {
    payment.setRefundReason("");
    assertEquals("", payment.getRefundReason());
  }

  @Test
  @DisplayName("Payment failure reason can be long text")
  void testLongFailureReason() {
    String longReason =
        "This is a very long failure reason that explains in detail why the payment failed. "
            .repeat(10);
    payment.setFailureReason(longReason);
    assertEquals(longReason, payment.getFailureReason());
  }

  @Test
  @DisplayName("Payment refund reason can be long text")
  void testLongRefundReason() {
    String longReason =
        "This is a very long refund reason that explains in detail why the refund was issued. "
            .repeat(10);
    payment.setRefundReason(longReason);
    assertEquals(longReason, payment.getRefundReason());
  }
}
