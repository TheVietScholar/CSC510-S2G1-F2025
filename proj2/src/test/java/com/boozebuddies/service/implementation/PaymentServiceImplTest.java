package com.boozebuddies.service.implementation;

import com.boozebuddies.entity.Order;
import com.boozebuddies.entity.Payment;
import com.boozebuddies.entity.User;
import com.boozebuddies.model.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PaymentServiceImplTest {

    @Test
    void testProcessPaymentSuccessAndGetPaymentsByUserAndByOrderId() {
        PaymentServiceImpl service = new PaymentServiceImpl();

        User user = new User();
        Order order = new Order();
        order.setId(1L);
        order.setUser(user);
        order.setTotalAmount(new BigDecimal("12.50"));

        Payment payment = service.processPayment(order, "credit_card");

        assertNotNull(payment);
        assertEquals(new BigDecimal("12.50"), payment.getAmount());
        assertEquals(PaymentStatus.AUTHORIZED, payment.getStatus());
        assertSame(user, payment.getUser());

        List<Payment> paymentsByUser = service.getPaymentsByUser(user);
        assertEquals(1, paymentsByUser.size());
        assertEquals(payment, paymentsByUser.get(0));

        Payment fetched = service.getPaymentByOrderId(1L);
        assertNotNull(fetched);
        assertEquals(order.getId(), fetched.getOrder().getId());
    }

    @Test
    void testProcessPaymentInvalidMethodThrows() {
        PaymentServiceImpl service = new PaymentServiceImpl();
        User user = new User();
        Order order = new Order();
        order.setId(2L);
        order.setUser(user);
        order.setTotalAmount(new BigDecimal("5.00"));

        assertThrows(RuntimeException.class, () -> service.processPayment(order, ""));
        assertThrows(RuntimeException.class, () -> service.processPayment(order, null));
    }

    @Test
    void testRefundPaymentAddsRefundRecordAndDoesNotRemoveOriginal() {
        PaymentServiceImpl service = new PaymentServiceImpl();

        User user = new User();
        Order order = new Order();
        order.setId(3L);
        order.setUser(user);
        order.setTotalAmount(new BigDecimal("20.00"));

        Payment authorized = service.processPayment(order, "paypal");

        // Refund the payment
        Payment refunded = service.refundPayment(order, "customer_requested");

        assertNotNull(refunded);
        assertEquals(PaymentStatus.REFUNDED, refunded.getStatus());
        assertEquals("customer_requested", refunded.getRefundReason());
        // Both payments (original authorized and refund) should exist for the user
        List<Payment> payments = service.getPaymentsByUser(user);
        assertEquals(2, payments.size());

        // getPaymentByOrderId returns the first matching payment in the list (likely the original authorized payment)
        Payment returned = service.getPaymentByOrderId(order.getId());
        assertNotNull(returned);
        assertEquals(PaymentStatus.AUTHORIZED, returned.getStatus(),
                "Implementation currently returns the original AUTHORIZED payment for an order, even after a refund record is created.");
    }

    @Test
    void testRefundPaymentWhenNotFoundThrows() {
        PaymentServiceImpl service = new PaymentServiceImpl();

        User user = new User();
        Order order = new Order();
        order.setId(99L);
        order.setUser(user);
        order.setTotalAmount(new BigDecimal("1.00"));

        assertThrows(RuntimeException.class, () -> service.refundPayment(order, "no_payment"));
    }

    @Test
    void testCalculateTotalRevenueRespectsDateRangeAndStatus() {
        PaymentServiceImpl service = new PaymentServiceImpl();

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
        Payment pA = service.processPayment(orderA, "card");
        Payment pB = service.processPayment(orderB, "card");

        // Set payment dates so only pA is in the target range
        LocalDateTime now = LocalDateTime.now();
        pA.setPaymentDate(now.minusDays(1));
        pB.setPaymentDate(now.minusDays(10));

        LocalDateTime rangeStart = now.minusDays(2);
        LocalDateTime rangeEnd = now.plusDays(1);

        // Only pA is AUTHORIZED and in range -> should be counted
        BigDecimal revenue = service.calculateTotalRevenue(rangeStart, rangeEnd);
        assertEquals(new BigDecimal("15.00"), revenue);

        // Refund pA (creates a separate REFUNDED payment record). Current implementation leaves the original AUTHORIZED in place.
        service.refundPayment(orderA, "returned");

        // After refund, because implementation does not change the original AUTHORIZED payment, calculateTotalRevenue still counts it.
        BigDecimal revenueAfterRefund = service.calculateTotalRevenue(rangeStart, rangeEnd);
        assertEquals(new BigDecimal("15.00"), revenueAfterRefund,
                "Note: refunded payments currently do not remove or mark the original AUTHORIZED payment; revenue still includes the original amount.");
    }

    @Test
    void testCalculateTotalRevenueOutsideRangeIsZero() {
        PaymentServiceImpl service = new PaymentServiceImpl();

        User user = new User();
        Order order = new Order();
        order.setId(20L);
        order.setUser(user);
        order.setTotalAmount(new BigDecimal("30.00"));

        Payment p = service.processPayment(order, "card");
        p.setPaymentDate(LocalDateTime.of(2000, 1, 1, 0, 0));

        BigDecimal revenue = service.calculateTotalRevenue(LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        assertEquals(BigDecimal.ZERO, revenue);
    }

    @Test
    void testValidatePaymentMethod() {
        PaymentServiceImpl service = new PaymentServiceImpl();
        User user = new User();

        assertTrue(service.validatePaymentMethod(user, "card"));
        assertFalse(service.validatePaymentMethod(null, "card"));
        assertFalse(service.validatePaymentMethod(user, ""));
        assertFalse(service.validatePaymentMethod(user, "   "));
        assertFalse(service.validatePaymentMethod(user, null));
    }
}
