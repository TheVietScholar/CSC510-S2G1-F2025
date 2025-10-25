package com.boozebuddies.service.implementation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.boozebuddies.entity.Delivery;
import com.boozebuddies.entity.Merchant;
import com.boozebuddies.entity.Order;
import com.boozebuddies.entity.OrderItem;
import com.boozebuddies.entity.Product;
import com.boozebuddies.entity.User;
import com.boozebuddies.model.DeliveryStatus;
import com.boozebuddies.model.OrderStatus;
import com.boozebuddies.repository.DeliveryRepository;
import com.boozebuddies.repository.MerchantRepository;
import com.boozebuddies.repository.OrderRepository;
import com.boozebuddies.repository.UserRepository;
import com.boozebuddies.service.NotificationService;
import com.boozebuddies.service.PaymentService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class OrderServiceImplTest {

  @Mock private OrderRepository orderRepository;
  @Mock private UserRepository userRepository;
  @Mock private MerchantRepository merchantRepository;
  @Mock private DeliveryRepository deliveryRepository;
  @Mock private PaymentService paymentService;
  @Mock private NotificationService notificationService;

  @InjectMocks private OrderServiceImpl orderService;

  private User user;
  private Merchant merchant;
  private OrderItem item;
  private Product product;

  @BeforeEach
  public void setupCommonMocks() {
    user = mock(User.class);
    merchant = mock(Merchant.class);
    item = mock(OrderItem.class);
    product = mock(Product.class);
    // when(item.getProduct()).thenReturn(product);
  }

  @Test
  public void createOrder_success_savesProcessesPaymentAndCreatesDelivery() {
    Order order = mock(Order.class);

    when(order.getUser()).thenReturn(user);
    when(order.getMerchant()).thenReturn(merchant);
    when(order.getItems()).thenReturn(List.of(item));
    when(order.getTotalAmount()).thenReturn(null);
    when(orderRepository.save(order)).thenReturn(order);
    // Make deliveryRepository.save return the same delivery instance passed
    when(deliveryRepository.save(any(Delivery.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Order returned = orderService.createOrder(order);

    // repository save returned the same mock
    assertSame(order, returned);

    // verify that total calculation was attempted when totalAmount was null
    verify(order).getTotalAmount();
    verify(order).calculateTotal();

    // payment processed and notification sent
    verify(paymentService).processPayment(order, null);
    ArgumentCaptor<Delivery> deliveryCaptor = ArgumentCaptor.forClass(Delivery.class);
    verify(deliveryRepository).save(deliveryCaptor.capture());
    Delivery savedDelivery = deliveryCaptor.getValue();
    assertNotNull(savedDelivery);
    assertEquals(DeliveryStatus.PENDING, savedDelivery.getStatus());
    verify(notificationService).sendOrderConfirmation(savedDelivery);

    // ensure initial status was set to PENDING
    verify(order).setStatus(OrderStatus.PENDING);
  }

  @Test
  public void createOrder_failsWhenAlcoholAndUserNotAgeVerified() {
    Order order = mock(Order.class);
    when(order.getUser()).thenReturn(user);
    when(order.getMerchant()).thenReturn(merchant);
    when(order.getItems()).thenReturn(List.of(item));
    when(item.getProduct()).thenReturn(product);
    when(product.isAlcohol()).thenReturn(true);
    when(user.isAgeVerified()).thenReturn(false);

    RuntimeException ex =
        assertThrows(RuntimeException.class, () -> orderService.createOrder(order));
    assertEquals(ex.getMessage(), "User must be age verified for alcohol orders");
    verify(orderRepository, never()).save(any());
  }

  @Test
  public void cancelOrder_success_callsRefundAndNotifies() {
    Long id = 1L;
    Order order = mock(Order.class);
    Delivery delivery = mock(Delivery.class);

    when(orderRepository.findById(id)).thenReturn(Optional.of(order));
    when(order.canBeCancelled()).thenReturn(true);
    when(orderRepository.save(order)).thenReturn(order);
    when(order.getDelivery()).thenReturn(delivery);

    Order result = orderService.cancelOrder(id);

    assertSame(order, result);
    verify(order).setStatus(OrderStatus.CANCELLED);
    verify(order).setUpdatedAt(any());
    verify(orderRepository).save(order);
    verify(paymentService).refundPayment(order, "Order cancelled by user");
    verify(notificationService).sendOrderCancellation(delivery);
  }

  @Test
  public void cancelOrder_throwsWhenNotCancellable() {
    Long id = 2L;
    Order order = mock(Order.class);
    when(orderRepository.findById(id)).thenReturn(Optional.of(order));
    when(order.canBeCancelled()).thenReturn(false);

    RuntimeException ex = assertThrows(RuntimeException.class, () -> orderService.cancelOrder(id));
    assertTrue(ex.getMessage().contains("Order cannot be cancelled"));
    verify(orderRepository).findById(id);
    verify(orderRepository, never()).save(any());
    verify(paymentService, never()).refundPayment(any(), any());
  }

  @Test
  public void updateOrderStatus_invalidTransition_throws() {
    Long id = 3L;
    Order order = mock(Order.class);
    when(orderRepository.findById(id)).thenReturn(Optional.of(order));
    when(order.isValidStatusTransition(any())).thenReturn(false);

    RuntimeException ex =
        assertThrows(RuntimeException.class, () -> orderService.updateOrderStatus(id, "COMPLETED"));
    assertTrue(ex.getMessage().contains("Invalid status transition"));
    verify(orderRepository).findById(id);
    verify(orderRepository, never()).save(any());
  }

  @Test
  public void updateOrderStatus_confirmed_triggersNotification() {
    Long id = 4L;
    Order order = mock(Order.class);
    Delivery delivery = mock(Delivery.class);
    when(orderRepository.findById(id)).thenReturn(Optional.of(order));
    when(order.isValidStatusTransition(OrderStatus.CONFIRMED)).thenReturn(true);
    when(orderRepository.save(order)).thenReturn(order);
    when(order.getUser()).thenReturn(user);
    when(order.getDelivery()).thenReturn(delivery);

    Order result = orderService.updateOrderStatus(id, "CONFIRMED");

    assertSame(order, result);
    verify(order).setStatus(OrderStatus.CONFIRMED);
    verify(order).setUpdatedAt(any());
    verify(orderRepository).save(order);
    verify(notificationService).sendDeliveryStatusUpdate(user, delivery);
  }

  @Test
  public void getOrderById_delegatesToRepository() {
    Long id = 5L;
    Order order = mock(Order.class);
    when(orderRepository.findById(id)).thenReturn(Optional.of(order));
    Optional<Order> found = orderService.getOrderById(id);
    assertTrue(found.isPresent());
    assertSame(order, found.get());
  }

  @Test
  public void getOrdersByUser_delegatesToRepository() {
    Long userId = 6L;
    when(orderRepository.findByCustomerId(userId)).thenReturn(List.of());
    List<Order> orders = orderService.getOrdersByUser(userId);
    assertNotNull(orders);
    verify(orderRepository).findByCustomerId(userId);
  }

  @Test
  public void getAllOrders_delegatesToRepository() {
    when(orderRepository.findAll()).thenReturn(List.of());
    List<Order> orders = orderService.getAllOrders();
    assertNotNull(orders);
    verify(orderRepository).findAll();
  }
}
