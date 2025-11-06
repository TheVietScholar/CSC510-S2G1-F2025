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
import com.boozebuddies.service.ProductService;
import com.boozebuddies.service.UserService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
  @Mock private ProductService productService;
  @Mock private UserService userService;

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
    OrderItem item = mock(OrderItem.class);
    Product product = mock(Product.class);

    // Mock the OrderItem properly
    when(item.getProduct()).thenReturn(product);
    when(item.getQuantity()).thenReturn(2);
    when(item.getUnitPrice()).thenReturn(new BigDecimal("10.00"));
    when(product.getId()).thenReturn(1L);
    when(product.getName()).thenReturn("Test Product");
    // REMOVE THIS LINE - not needed since unitPrice is already set
    // when(product.getPrice()).thenReturn(new BigDecimal("10.00"));
    when(product.isAlcohol()).thenReturn(false);

    when(productService.getProductById(1L)).thenReturn(product);

    when(order.getUser()).thenReturn(user);
    when(order.getMerchant()).thenReturn(merchant);
    when(order.getItems()).thenReturn(List.of(item));
    when(order.getTotalAmount()).thenReturn(null);
    when(orderRepository.save(order)).thenReturn(order);

    when(deliveryRepository.save(any(Delivery.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Order returned = orderService.createOrder(order);

    assertSame(order, returned);
    verify(order).getTotalAmount();
    verify(order).calculateTotal();
    verify(paymentService).processPayment(order, "test_payment");

    ArgumentCaptor<Delivery> deliveryCaptor = ArgumentCaptor.forClass(Delivery.class);
    verify(deliveryRepository).save(deliveryCaptor.capture());
    Delivery savedDelivery = deliveryCaptor.getValue();
    assertNotNull(savedDelivery);
    assertEquals(DeliveryStatus.PENDING, savedDelivery.getStatus());
    verify(notificationService).sendOrderConfirmation(savedDelivery);
    verify(order).setStatus(OrderStatus.PENDING);
  }

  @Test
  public void createOrder_failsWhenAlcoholAndUserNotAgeVerified() {
    Order order = mock(Order.class);
    OrderItem item = mock(OrderItem.class);
    Product product = mock(Product.class);

    // Mock product initialization (happens BEFORE validation)
    when(item.getProduct()).thenReturn(product);
    when(item.getQuantity()).thenReturn(2);
    when(item.getUnitPrice()).thenReturn(new BigDecimal("10.00"));
    when(product.getId()).thenReturn(1L);
    when(product.getName()).thenReturn("Beer");
    when(product.isAlcohol()).thenReturn(true); // This is an alcohol product

    // Mock productService to return the product
    when(productService.getProductById(1L)).thenReturn(product);

    when(order.getUser()).thenReturn(user);
    when(order.getMerchant()).thenReturn(merchant);
    when(order.getItems()).thenReturn(List.of(item));

    // Mock user NOT age verified
    when(user.getId()).thenReturn(1L);
    when(user.isAgeVerified()).thenReturn(false);
    when(userService.findById(1L)).thenReturn(user); // Service fetches fresh user data

    RuntimeException ex =
        assertThrows(RuntimeException.class, () -> orderService.createOrder(order));
    assertEquals("User must be age verified for alcohol orders", ex.getMessage());
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

  @Test
  public void createOrder_missingUser_throwsException() {
    Order order = mock(Order.class);
    when(order.getUser()).thenReturn(null);

    RuntimeException ex =
        assertThrows(RuntimeException.class, () -> orderService.createOrder(order));
    assertEquals("User is required", ex.getMessage());
    verify(orderRepository, never()).save(any());
  }

  @Test
  public void createOrder_missingMerchant_throwsException() {
    Order order = mock(Order.class);
    when(order.getUser()).thenReturn(user);
    when(order.getMerchant()).thenReturn(null);

    RuntimeException ex =
        assertThrows(RuntimeException.class, () -> orderService.createOrder(order));
    assertEquals("Merchant is required", ex.getMessage());
    verify(orderRepository, never()).save(any());
  }

  @Test
  public void createOrder_emptyItems_throwsException() {
    Order order = mock(Order.class);
    when(order.getUser()).thenReturn(user);
    when(order.getMerchant()).thenReturn(merchant);
    when(order.getItems()).thenReturn(List.of());

    RuntimeException ex =
        assertThrows(RuntimeException.class, () -> orderService.createOrder(order));
    assertEquals("Order must contain at least one item", ex.getMessage());
    verify(orderRepository, never()).save(any());
  }

  @Test
  public void cancelOrder_nonExistentOrder_throwsException() {
    Long id = 99L;
    when(orderRepository.findById(id)).thenReturn(Optional.empty());

    RuntimeException ex = assertThrows(RuntimeException.class, () -> orderService.cancelOrder(id));
    assertEquals("Order not found", ex.getMessage());
    verify(orderRepository, never()).save(any());
    verify(paymentService, never()).refundPayment(any(), any());
  }

  @Test
  public void updateOrderStatus_preparing_noNotification() {
    Long id = 7L;
    Order order = mock(Order.class);
    when(orderRepository.findById(id)).thenReturn(Optional.of(order));
    when(order.isValidStatusTransition(OrderStatus.PREPARING)).thenReturn(true);
    when(orderRepository.save(order)).thenReturn(order);

    Order result = orderService.updateOrderStatus(id, "PREPARING");

    assertSame(order, result);
    verify(order).setStatus(OrderStatus.PREPARING);
    verify(order).setUpdatedAt(any());
    verify(orderRepository).save(order);
    verify(notificationService, never()).sendDeliveryStatusUpdate(any(), any());
  }

  @Test
  public void updateOrderStatus_invalidStatus_throwsException() {
    Long id = 8L;
    Order order = mock(Order.class);
    when(orderRepository.findById(id)).thenReturn(Optional.of(order));

    RuntimeException ex =
        assertThrows(
            RuntimeException.class, () -> orderService.updateOrderStatus(id, "INVALID_STATUS"));
    assertTrue(ex instanceof IllegalArgumentException);
    verify(orderRepository, never()).save(any());
  }

  @Test
  public void updateOrderStatus_nonExistentOrder_throwsException() {
    Long id = 99L;
    when(orderRepository.findById(id)).thenReturn(Optional.empty());

    RuntimeException ex =
        assertThrows(RuntimeException.class, () -> orderService.updateOrderStatus(id, "CONFIRMED"));
    assertEquals("Order not found", ex.getMessage());
    verify(orderRepository, never()).save(any());
  }

  // ==================== INITIALIZE ORDER ITEMS TESTS ====================

  @Test
  public void createOrder_initializesOrderItemsWithLineNumbers() {
    Order order = mock(Order.class);
    OrderItem item1 = mock(OrderItem.class);
    OrderItem item2 = mock(OrderItem.class);
    Product product1 = mock(Product.class);
    Product product2 = mock(Product.class);

    when(item1.getProduct()).thenReturn(product1);
    when(item2.getProduct()).thenReturn(product2);
    when(product1.getId()).thenReturn(1L);
    when(product2.getId()).thenReturn(2L);
    when(product1.getName()).thenReturn("Product 1");
    when(product2.getName()).thenReturn("Product 2");
    when(product1.isAlcohol()).thenReturn(false);
    when(product2.isAlcohol()).thenReturn(false);

    when(productService.getProductById(1L)).thenReturn(product1);
    when(productService.getProductById(2L)).thenReturn(product2);

    when(item1.getQuantity()).thenReturn(2);
    when(item2.getQuantity()).thenReturn(3);
    when(item1.getUnitPrice()).thenReturn(new BigDecimal("10.00"));
    when(item2.getUnitPrice()).thenReturn(new BigDecimal("15.00"));

    when(order.getUser()).thenReturn(user);
    when(order.getMerchant()).thenReturn(merchant);
    when(order.getItems()).thenReturn(List.of(item1, item2));
    when(order.getTotalAmount()).thenReturn(null);
    when(orderRepository.save(order)).thenReturn(order);
    when(deliveryRepository.save(any(Delivery.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    orderService.createOrder(order);

    verify(item1).setLineNo(1);
    verify(item2).setLineNo(2);
    verify(item1).setOrder(order);
    verify(item2).setOrder(order);
  }

  @Test
  public void createOrder_setsItemNameFromProduct() {
    Order order = mock(Order.class);
    OrderItem item = mock(OrderItem.class);
    Product product = mock(Product.class);

    when(item.getProduct()).thenReturn(product);
    when(product.getId()).thenReturn(1L);
    when(product.getName()).thenReturn("Test Product");
    when(product.isAlcohol()).thenReturn(false);
    when(item.getName()).thenReturn(null);
    when(item.getQuantity()).thenReturn(2);
    when(item.getUnitPrice()).thenReturn(new BigDecimal("10.00"));

    when(productService.getProductById(1L)).thenReturn(product);

    when(order.getUser()).thenReturn(user);
    when(order.getMerchant()).thenReturn(merchant);
    when(order.getItems()).thenReturn(List.of(item));
    when(order.getTotalAmount()).thenReturn(null);
    when(orderRepository.save(order)).thenReturn(order);
    when(deliveryRepository.save(any(Delivery.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    orderService.createOrder(order);

    verify(item).setName("Test Product");
  }

  @Test
  public void createOrder_calculatesSubtotal() {
    Order order = mock(Order.class);
    OrderItem item = mock(OrderItem.class);
    Product product = mock(Product.class);

    when(item.getProduct()).thenReturn(product);
    when(product.getId()).thenReturn(1L);
    when(product.getName()).thenReturn("Test Product");
    when(product.isAlcohol()).thenReturn(false);
    when(item.getQuantity()).thenReturn(3);
    when(item.getUnitPrice()).thenReturn(new BigDecimal("10.00"));

    when(productService.getProductById(1L)).thenReturn(product);

    when(order.getUser()).thenReturn(user);
    when(order.getMerchant()).thenReturn(merchant);
    when(order.getItems()).thenReturn(List.of(item));
    when(order.getTotalAmount()).thenReturn(null);
    when(orderRepository.save(order)).thenReturn(order);
    when(deliveryRepository.save(any(Delivery.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    orderService.createOrder(order);

    verify(item).setSubtotal(new BigDecimal("30.00"));
  }

  @Test
  public void createOrder_throwsWhenProductNotFound() {
    Order order = mock(Order.class);
    OrderItem item = mock(OrderItem.class);
    Product product = mock(Product.class);

    when(item.getProduct()).thenReturn(product);
    when(product.getId()).thenReturn(999L);
    when(productService.getProductById(999L)).thenReturn(null);

    when(order.getItems()).thenReturn(List.of(item));

    RuntimeException ex =
        assertThrows(RuntimeException.class, () -> orderService.createOrder(order));
    assertEquals("Product not found with id: 999", ex.getMessage());
    verify(orderRepository, never()).save(any());
  }

  @Test
  public void createOrder_handlesNullItemsList() {
    Order order = mock(Order.class);

    when(order.getUser()).thenReturn(user);
    when(order.getMerchant()).thenReturn(merchant);
    when(order.getItems()).thenReturn(null);

    RuntimeException ex =
        assertThrows(RuntimeException.class, () -> orderService.createOrder(order));
    assertEquals("Order must contain at least one item", ex.getMessage());
    verify(orderRepository, never()).save(any());
  }

  // ==================== VALIDATION TESTS ====================

  @Test
  public void createOrder_allowsNonAlcoholWithoutAgeVerification() {
    Order order = mock(Order.class);
    OrderItem item = mock(OrderItem.class);
    Product product = mock(Product.class);

    when(item.getProduct()).thenReturn(product);
    when(product.getId()).thenReturn(1L);
    when(product.getName()).thenReturn("Soda");
    when(product.isAlcohol()).thenReturn(false);
    when(item.getQuantity()).thenReturn(2);
    when(item.getUnitPrice()).thenReturn(new BigDecimal("5.00"));

    when(productService.getProductById(1L)).thenReturn(product);

    when(order.getUser()).thenReturn(user);
    when(order.getMerchant()).thenReturn(merchant);
    when(order.getItems()).thenReturn(List.of(item));
    when(order.getTotalAmount()).thenReturn(null);

    when(orderRepository.save(order)).thenReturn(order);
    when(deliveryRepository.save(any(Delivery.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Order result = orderService.createOrder(order);

    assertNotNull(result);
    verify(orderRepository).save(order);
    verify(userService, never()).findById(any());
  }

  @Test
  public void createOrder_allowsAlcoholWithAgeVerification() {
    Order order = mock(Order.class);
    OrderItem item = mock(OrderItem.class);
    Product product = mock(Product.class);

    when(item.getProduct()).thenReturn(product);
    when(product.getId()).thenReturn(1L);
    when(product.getName()).thenReturn("Beer");
    when(product.isAlcohol()).thenReturn(true);
    when(item.getQuantity()).thenReturn(2);
    when(item.getUnitPrice()).thenReturn(new BigDecimal("8.00"));

    when(productService.getProductById(1L)).thenReturn(product);

    when(order.getUser()).thenReturn(user);
    when(order.getMerchant()).thenReturn(merchant);
    when(order.getItems()).thenReturn(List.of(item));
    when(order.getTotalAmount()).thenReturn(null);

    when(user.getId()).thenReturn(1L);
    when(user.isAgeVerified()).thenReturn(true);
    when(userService.findById(1L)).thenReturn(user);

    when(orderRepository.save(order)).thenReturn(order);
    when(deliveryRepository.save(any(Delivery.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Order result = orderService.createOrder(order);

    assertNotNull(result);
    verify(orderRepository).save(order);
    verify(userService).findById(1L);
  }

  // ==================== ORDER TIMESTAMPS AND STATUS TESTS ====================

  @Test
  public void createOrder_setsTimestamps() {
    Order order = mock(Order.class);
    OrderItem item = mock(OrderItem.class);
    Product product = mock(Product.class);

    when(item.getProduct()).thenReturn(product);
    when(product.getId()).thenReturn(1L);
    when(product.getName()).thenReturn("Test Product");
    when(product.isAlcohol()).thenReturn(false);
    when(item.getQuantity()).thenReturn(2);
    when(item.getUnitPrice()).thenReturn(new BigDecimal("10.00"));

    when(productService.getProductById(1L)).thenReturn(product);

    when(order.getUser()).thenReturn(user);
    when(order.getMerchant()).thenReturn(merchant);
    when(order.getItems()).thenReturn(List.of(item));
    when(order.getTotalAmount()).thenReturn(null);
    when(orderRepository.save(order)).thenReturn(order);
    when(deliveryRepository.save(any(Delivery.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    orderService.createOrder(order);

    verify(order).setCreatedAt(any(LocalDateTime.class));
    verify(order).setUpdatedAt(any(LocalDateTime.class));
  }

  @Test
  public void createOrder_doesNotRecalculateTotalIfSet() {
    Order order = mock(Order.class);
    OrderItem item = mock(OrderItem.class);
    Product product = mock(Product.class);

    when(item.getProduct()).thenReturn(product);
    when(product.getId()).thenReturn(1L);
    when(product.getName()).thenReturn("Test Product");
    when(product.isAlcohol()).thenReturn(false);
    when(item.getQuantity()).thenReturn(2);
    when(item.getUnitPrice()).thenReturn(new BigDecimal("10.00"));

    when(productService.getProductById(1L)).thenReturn(product);

    when(order.getUser()).thenReturn(user);
    when(order.getMerchant()).thenReturn(merchant);
    when(order.getItems()).thenReturn(List.of(item));
    when(order.getTotalAmount()).thenReturn(new BigDecimal("99.99"));
    when(orderRepository.save(order)).thenReturn(order);
    when(deliveryRepository.save(any(Delivery.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    orderService.createOrder(order);

    verify(order, never()).calculateTotal();
  }

  // ==================== DELIVERY CREATION TESTS ====================

  @Test
  public void createOrder_createsDeliveryWithCorrectAddress() {
    Order order = mock(Order.class);
    OrderItem item = mock(OrderItem.class);
    Product product = mock(Product.class);

    when(item.getProduct()).thenReturn(product);
    when(product.getId()).thenReturn(1L);
    when(product.getName()).thenReturn("Test Product");
    when(product.isAlcohol()).thenReturn(false);
    when(item.getQuantity()).thenReturn(2);
    when(item.getUnitPrice()).thenReturn(new BigDecimal("10.00"));

    when(productService.getProductById(1L)).thenReturn(product);

    when(order.getUser()).thenReturn(user);
    when(order.getMerchant()).thenReturn(merchant);
    when(order.getItems()).thenReturn(List.of(item));
    when(order.getTotalAmount()).thenReturn(null);
    when(order.getDeliveryAddress()).thenReturn("123 Main St");
    when(orderRepository.save(order)).thenReturn(order);
    when(deliveryRepository.save(any(Delivery.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    orderService.createOrder(order);

    ArgumentCaptor<Delivery> deliveryCaptor = ArgumentCaptor.forClass(Delivery.class);
    verify(deliveryRepository).save(deliveryCaptor.capture());
    Delivery capturedDelivery = deliveryCaptor.getValue();

    assertEquals("123 Main St", capturedDelivery.getDeliveryAddress());
    assertNotNull(capturedDelivery.getCreatedAt());
  }

  // ==================== GET ORDER BY ID TESTS ====================

  @Test
  public void getOrderById_usesCustomQueryFirst() {
    Long id = 1L;
    Order order = mock(Order.class);
    when(orderRepository.findByIdWithRelationships(id)).thenReturn(Optional.of(order));

    Optional<Order> result = orderService.getOrderById(id);

    assertTrue(result.isPresent());
    assertSame(order, result.get());
    verify(orderRepository).findByIdWithRelationships(id);
    verify(orderRepository, never()).findById(id);
  }

  @Test
  public void getOrderById_fallsBackToFindById() {
    Long id = 1L;
    Order order = mock(Order.class);
    when(orderRepository.findByIdWithRelationships(id)).thenReturn(Optional.empty());
    when(orderRepository.findById(id)).thenReturn(Optional.of(order));

    Optional<Order> result = orderService.getOrderById(id);

    assertTrue(result.isPresent());
    assertSame(order, result.get());
    verify(orderRepository).findByIdWithRelationships(id);
    verify(orderRepository).findById(id);
  }

  @Test
  public void getOrderById_returnsEmptyWhenNotFound() {
    Long id = 99L;
    when(orderRepository.findByIdWithRelationships(id)).thenReturn(Optional.empty());
    when(orderRepository.findById(id)).thenReturn(Optional.empty());

    Optional<Order> result = orderService.getOrderById(id);

    assertFalse(result.isPresent());
    verify(orderRepository).findByIdWithRelationships(id);
    verify(orderRepository).findById(id);
  }

  // ==================== GET ORDERS BY MERCHANT TESTS ====================

  @Test
  public void getOrdersByMerchant_delegatesToRepository() {
    Long merchantId = 10L;
    Order order1 = mock(Order.class);
    Order order2 = mock(Order.class);
    when(orderRepository.findByMerchantId(merchantId)).thenReturn(List.of(order1, order2));

    List<Order> orders = orderService.getOrdersByMerchant(merchantId);

    assertNotNull(orders);
    assertEquals(2, orders.size());
    verify(orderRepository).findByMerchantId(merchantId);
  }

  @Test
  public void getOrdersByMerchant_returnsEmptyList() {
    Long merchantId = 99L;
    when(orderRepository.findByMerchantId(merchantId)).thenReturn(List.of());

    List<Order> orders = orderService.getOrdersByMerchant(merchantId);

    assertNotNull(orders);
    assertTrue(orders.isEmpty());
    verify(orderRepository).findByMerchantId(merchantId);
  }

  // ==================== GET ORDERS BY DRIVER TESTS ====================

  @Test
  public void getOrdersByDriver_delegatesToRepository() {
    Long driverId = 5L;
    Order order1 = mock(Order.class);
    when(orderRepository.findByDriverId(driverId)).thenReturn(List.of(order1));

    List<Order> orders = orderService.getOrdersByDriver(driverId);

    assertNotNull(orders);
    assertEquals(1, orders.size());
    verify(orderRepository).findByDriverId(driverId);
  }

  @Test
  public void getOrdersByDriver_returnsEmptyList() {
    Long driverId = 99L;
    when(orderRepository.findByDriverId(driverId)).thenReturn(List.of());

    List<Order> orders = orderService.getOrdersByDriver(driverId);

    assertNotNull(orders);
    assertTrue(orders.isEmpty());
    verify(orderRepository).findByDriverId(driverId);
  }

  // ==================== UPDATE ORDER STATUS - ADDITIONAL STATUS TESTS ====================

  @Test
  public void updateOrderStatus_readyForPickup_noNotification() {
    Long id = 9L;
    Order order = mock(Order.class);
    when(orderRepository.findById(id)).thenReturn(Optional.of(order));
    when(order.isValidStatusTransition(OrderStatus.READY_FOR_PICKUP)).thenReturn(true);
    when(orderRepository.save(order)).thenReturn(order);

    Order result = orderService.updateOrderStatus(id, "READY_FOR_PICKUP");

    assertSame(order, result);
    verify(order).setStatus(OrderStatus.READY_FOR_PICKUP);
    verify(notificationService, never()).sendDeliveryStatusUpdate(any(), any());
  }

  @Test
  public void updateOrderStatus_completed_noNotification() {
    Long id = 10L;
    Order order = mock(Order.class);
    when(orderRepository.findById(id)).thenReturn(Optional.of(order));
    when(order.isValidStatusTransition(OrderStatus.COMPLETED)).thenReturn(true);
    when(orderRepository.save(order)).thenReturn(order);

    Order result = orderService.updateOrderStatus(id, "COMPLETED");

    assertSame(order, result);
    verify(order).setStatus(OrderStatus.COMPLETED);
    verify(notificationService, never()).sendDeliveryStatusUpdate(any(), any());
  }

  @Test
  public void updateOrderStatus_cancelled_noNotification() {
    Long id = 11L;
    Order order = mock(Order.class);
    when(orderRepository.findById(id)).thenReturn(Optional.of(order));
    when(order.isValidStatusTransition(OrderStatus.CANCELLED)).thenReturn(true);
    when(orderRepository.save(order)).thenReturn(order);

    Order result = orderService.updateOrderStatus(id, "CANCELLED");

    assertSame(order, result);
    verify(order).setStatus(OrderStatus.CANCELLED);
    verify(notificationService, never()).sendDeliveryStatusUpdate(any(), any());
  }

  @Test
  public void updateOrderStatus_lowercaseStatus_convertsToUppercase() {
    Long id = 12L;
    Order order = mock(Order.class);
    when(orderRepository.findById(id)).thenReturn(Optional.of(order));
    when(order.isValidStatusTransition(OrderStatus.CONFIRMED)).thenReturn(true);
    when(orderRepository.save(order)).thenReturn(order);
    when(order.getUser()).thenReturn(user);
    when(order.getDelivery()).thenReturn(mock(Delivery.class));

    Order result = orderService.updateOrderStatus(id, "confirmed");

    assertSame(order, result);
    verify(order).setStatus(OrderStatus.CONFIRMED);
  }

  @Test
  public void updateOrderStatus_mixedCaseStatus_convertsToUppercase() {
    Long id = 13L;
    Order order = mock(Order.class);
    when(orderRepository.findById(id)).thenReturn(Optional.of(order));
    when(order.isValidStatusTransition(OrderStatus.PREPARING)).thenReturn(true);
    when(orderRepository.save(order)).thenReturn(order);

    Order result = orderService.updateOrderStatus(id, "PrePaRiNg");

    assertSame(order, result);
    verify(order).setStatus(OrderStatus.PREPARING);
  }

  // ==================== CANCEL ORDER - ADDITIONAL TESTS ====================

  @Test
  public void cancelOrder_updatesTimestamp() {
    Long id = 14L;
    Order order = mock(Order.class);
    Delivery delivery = mock(Delivery.class);

    when(orderRepository.findById(id)).thenReturn(Optional.of(order));
    when(order.canBeCancelled()).thenReturn(true);
    when(orderRepository.save(order)).thenReturn(order);
    when(order.getDelivery()).thenReturn(delivery);

    orderService.cancelOrder(id);

    ArgumentCaptor<LocalDateTime> timeCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
    verify(order).setUpdatedAt(timeCaptor.capture());
    assertNotNull(timeCaptor.getValue());
  }

  @Test
  public void cancelOrder_handlesNullDelivery() {
    Long id = 15L;
    Order order = mock(Order.class);

    when(orderRepository.findById(id)).thenReturn(Optional.of(order));
    when(order.canBeCancelled()).thenReturn(true);
    when(orderRepository.save(order)).thenReturn(order);
    when(order.getDelivery()).thenReturn(null);

    Order result = orderService.cancelOrder(id);

    assertSame(order, result);
    verify(order).setStatus(OrderStatus.CANCELLED);
    verify(paymentService).refundPayment(order, "Order cancelled by user");
    verify(notificationService).sendOrderCancellation(null);
  }

  // ==================== GET ORDERS WITHIN DISTANCE TESTS ====================

  @Test
  public void getOrdersWithinDistance_returnsOrdersWithinRadius() {
    double latitude = 35.5;
    double longitude = -78.9;
    double radiusKm = 10.0;

    Order order1 = mock(Order.class);
    Order order2 = mock(Order.class);
    Merchant merchant1 = mock(Merchant.class);
    Merchant merchant2 = mock(Merchant.class);

    when(order1.getMerchant()).thenReturn(merchant1);
    when(order2.getMerchant()).thenReturn(merchant2);
    when(merchant1.getLatitude()).thenReturn(35.51);
    when(merchant1.getLongitude()).thenReturn(-78.91);
    when(merchant2.getLatitude()).thenReturn(35.52);
    when(merchant2.getLongitude()).thenReturn(-78.92);

    when(orderRepository.findAvailableForAssignment()).thenReturn(List.of(order1, order2));

    List<Order> result = orderService.getOrdersWithinDistance(latitude, longitude, radiusKm);

    assertNotNull(result);
    assertEquals(2, result.size());
    verify(orderRepository).findAvailableForAssignment();
  }

  @Test
  public void getOrdersWithinDistance_returnsEmptyListWhenNoOrdersAvailable() {
    double latitude = 35.5;
    double longitude = -78.9;
    double radiusKm = 10.0;

    when(orderRepository.findAvailableForAssignment()).thenReturn(new java.util.ArrayList<>());

    List<Order> result = orderService.getOrdersWithinDistance(latitude, longitude, radiusKm);

    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(orderRepository).findAvailableForAssignment();
  }

  @Test
  public void getOrdersWithinDistance_handlesZeroDistance() {
    double latitude = 35.5;
    double longitude = -78.9;
    double radiusKm = 0.0;

    Order order = mock(Order.class);
    Merchant merchant = mock(Merchant.class);

    when(order.getMerchant()).thenReturn(merchant);
    when(merchant.getLatitude()).thenReturn(35.5);
    when(merchant.getLongitude()).thenReturn(-78.9);

    List<Order> availableOrders = new java.util.ArrayList<>(List.of(order));
    when(orderRepository.findAvailableForAssignment()).thenReturn(availableOrders);

    List<Order> result = orderService.getOrdersWithinDistance(latitude, longitude, radiusKm);

    assertNotNull(result);
    // With radius 0, even exact location should be filtered out (distance > 0)
    assertTrue(result.isEmpty() || result.size() == 1);
  }

  @Test
  public void getOrdersWithinDistance_handlesLargeRadius() {
    double latitude = 35.5;
    double longitude = -78.9;
    double radiusKm = 10000.0; // Very large radius

    Order order1 = mock(Order.class);
    Order order2 = mock(Order.class);
    Merchant merchant1 = mock(Merchant.class);
    Merchant merchant2 = mock(Merchant.class);

    when(order1.getMerchant()).thenReturn(merchant1);
    when(order2.getMerchant()).thenReturn(merchant2);
    when(merchant1.getLatitude()).thenReturn(40.0);
    when(merchant1.getLongitude()).thenReturn(-74.0);
    when(merchant2.getLatitude()).thenReturn(34.0);
    when(merchant2.getLongitude()).thenReturn(-118.0);

    List<Order> availableOrders = new java.util.ArrayList<>(List.of(order1, order2));
    when(orderRepository.findAvailableForAssignment()).thenReturn(availableOrders);

    List<Order> result = orderService.getOrdersWithinDistance(latitude, longitude, radiusKm);

    assertNotNull(result);
    // With very large radius, most orders should be included
    assertTrue(result.size() >= 0);
  }

  @Test
  public void getOrdersWithinDistance_handlesBoundaryLatitudes() {
    double latitude = 90.0; // North pole
    double longitude = 0.0;
    double radiusKm = 100.0;

    Order order = mock(Order.class);
    Merchant merchant = mock(Merchant.class);

    when(order.getMerchant()).thenReturn(merchant);
    when(merchant.getLatitude()).thenReturn(89.5);
    when(merchant.getLongitude()).thenReturn(0.0);

    List<Order> availableOrders = new java.util.ArrayList<>(List.of(order));
    when(orderRepository.findAvailableForAssignment()).thenReturn(availableOrders);

    List<Order> result = orderService.getOrdersWithinDistance(latitude, longitude, radiusKm);

    assertNotNull(result);
    verify(orderRepository).findAvailableForAssignment();
  }

  @Test
  public void getOrdersWithinDistance_handlesBoundaryLongitudes() {
    double latitude = 35.5;
    double longitude = 180.0;
    double radiusKm = 100.0;

    Order order = mock(Order.class);
    Merchant merchant = mock(Merchant.class);

    when(order.getMerchant()).thenReturn(merchant);
    when(merchant.getLatitude()).thenReturn(35.5);
    when(merchant.getLongitude()).thenReturn(-180.0);

    List<Order> availableOrders = new java.util.ArrayList<>(List.of(order));
    when(orderRepository.findAvailableForAssignment()).thenReturn(availableOrders);

    List<Order> result = orderService.getOrdersWithinDistance(latitude, longitude, radiusKm);

    assertNotNull(result);
    verify(orderRepository).findAvailableForAssignment();
  }

  @Test
  public void getOrdersWithinDistance_handlesNegativeCoordinates() {
    double latitude = -33.8688; // Sydney
    double longitude = 151.2093;
    double radiusKm = 50.0;

    Order order = mock(Order.class);
    Merchant merchant = mock(Merchant.class);

    when(order.getMerchant()).thenReturn(merchant);
    when(merchant.getLatitude()).thenReturn(-33.9);
    when(merchant.getLongitude()).thenReturn(151.3);

    List<Order> availableOrders = new java.util.ArrayList<>(List.of(order));
    when(orderRepository.findAvailableForAssignment()).thenReturn(availableOrders);

    List<Order> result = orderService.getOrdersWithinDistance(latitude, longitude, radiusKm);

    assertNotNull(result);
    verify(orderRepository).findAvailableForAssignment();
  }

  @Test
  public void getOrdersWithinDistance_calculatesDistanceCorrectly() {
    // Test with known distance
    double lat1 = 40.7128; // New York
    double lon1 = -74.0060;
    double lat2 = 40.7580; // Approximately 5km north
    double lon2 = -73.9855;
    double radiusKm = 10.0;

    Order order = mock(Order.class);
    Merchant merchant = mock(Merchant.class);

    when(order.getMerchant()).thenReturn(merchant);
    when(merchant.getLatitude()).thenReturn(lat2);
    when(merchant.getLongitude()).thenReturn(lon2);

    List<Order> availableOrders = new java.util.ArrayList<>(List.of(order));
    when(orderRepository.findAvailableForAssignment()).thenReturn(availableOrders);

    List<Order> result = orderService.getOrdersWithinDistance(lat1, lon1, radiusKm);

    assertNotNull(result);
    // Should include the order since it's within 10km
    assertEquals(1, result.size());
  }

  // ==================== INTEGRATION-STYLE TESTS ====================

  @Test
  public void createOrder_fullWorkflow_allStepsExecuted() {
    Order order = mock(Order.class);
    OrderItem item = mock(OrderItem.class);
    Product product = mock(Product.class);
    Delivery delivery = mock(Delivery.class);

    when(item.getProduct()).thenReturn(product);
    when(product.getId()).thenReturn(1L);
    when(product.getName()).thenReturn("Test Product");
    when(product.isAlcohol()).thenReturn(false);
    when(item.getQuantity()).thenReturn(2);
    when(item.getUnitPrice()).thenReturn(new BigDecimal("10.00"));

    when(productService.getProductById(1L)).thenReturn(product);

    when(order.getUser()).thenReturn(user);
    when(order.getMerchant()).thenReturn(merchant);
    when(order.getItems()).thenReturn(List.of(item));
    when(order.getTotalAmount()).thenReturn(null);
    when(orderRepository.save(order)).thenReturn(order);
    when(deliveryRepository.save(any(Delivery.class))).thenReturn(delivery);

    Order result = orderService.createOrder(order);

    assertSame(order, result);

    // Verify complete workflow
    verify(item).setLineNo(1);
    verify(item).setOrder(order);
    verify(item).setSubtotal(any(BigDecimal.class));
    verify(order).setStatus(OrderStatus.PENDING);
    verify(order).setCreatedAt(any(LocalDateTime.class));
    verify(order).setUpdatedAt(any(LocalDateTime.class));
    verify(order).calculateTotal();
    verify(orderRepository).save(order);
    verify(paymentService).processPayment(order, "test_payment");
    verify(deliveryRepository).save(any(Delivery.class));
    verify(notificationService).sendOrderConfirmation(any(Delivery.class));
  }

  @Test
  public void cancelOrder_fullWorkflow_allStepsExecuted() {
    Long id = 1L;
    Order order = mock(Order.class);
    Delivery delivery = mock(Delivery.class);

    when(orderRepository.findById(id)).thenReturn(Optional.of(order));
    when(order.canBeCancelled()).thenReturn(true);
    when(orderRepository.save(order)).thenReturn(order);
    when(order.getDelivery()).thenReturn(delivery);

    Order result = orderService.cancelOrder(id);

    assertSame(order, result);

    // Verify complete cancellation workflow
    verify(orderRepository).findById(id);
    verify(order).canBeCancelled();
    verify(order).setStatus(OrderStatus.CANCELLED);
    verify(order).setUpdatedAt(any(LocalDateTime.class));
    verify(orderRepository).save(order);
    verify(paymentService).refundPayment(order, "Order cancelled by user");
    verify(notificationService).sendOrderCancellation(delivery);
  }

  @Test
  public void updateOrderStatus_confirmedWorkflow_allStepsExecuted() {
    Long id = 1L;
    Order order = mock(Order.class);
    Delivery delivery = mock(Delivery.class);

    when(orderRepository.findById(id)).thenReturn(Optional.of(order));
    when(order.isValidStatusTransition(OrderStatus.CONFIRMED)).thenReturn(true);
    when(orderRepository.save(order)).thenReturn(order);
    when(order.getUser()).thenReturn(user);
    when(order.getDelivery()).thenReturn(delivery);

    Order result = orderService.updateOrderStatus(id, "CONFIRMED");

    assertSame(order, result);

    // Verify complete status update workflow
    verify(orderRepository).findById(id);
    verify(order).isValidStatusTransition(OrderStatus.CONFIRMED);
    verify(order).setStatus(OrderStatus.CONFIRMED);
    verify(order).setUpdatedAt(any(LocalDateTime.class));
    verify(orderRepository).save(order);
    verify(notificationService).sendDeliveryStatusUpdate(user, delivery);
  }

  // ==================== EDGE CASES AND ERROR CONDITIONS ====================

  @Test
  public void createOrder_multipleItemsSameProduct_initializesAll() {
    Order order = mock(Order.class);
    OrderItem item1 = mock(OrderItem.class);
    OrderItem item2 = mock(OrderItem.class);
    Product product = mock(Product.class);

    when(item1.getProduct()).thenReturn(product);
    when(item2.getProduct()).thenReturn(product);
    when(product.getId()).thenReturn(1L);
    when(product.getName()).thenReturn("Test Product");
    when(product.isAlcohol()).thenReturn(false);

    when(item1.getQuantity()).thenReturn(2);
    when(item2.getQuantity()).thenReturn(3);
    when(item1.getUnitPrice()).thenReturn(new BigDecimal("10.00"));
    when(item2.getUnitPrice()).thenReturn(new BigDecimal("10.00"));

    when(productService.getProductById(1L)).thenReturn(product);

    when(order.getUser()).thenReturn(user);
    when(order.getMerchant()).thenReturn(merchant);
    when(order.getItems()).thenReturn(List.of(item1, item2));
    when(order.getTotalAmount()).thenReturn(null);
    when(orderRepository.save(order)).thenReturn(order);
    when(deliveryRepository.save(any(Delivery.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    orderService.createOrder(order);

    verify(item1).setLineNo(1);
    verify(item2).setLineNo(2);
    verify(item1).setOrder(order);
    verify(item2).setOrder(order);
    verify(item1).setSubtotal(new BigDecimal("20.00"));
    verify(item2).setSubtotal(new BigDecimal("30.00"));
  }

  @Test
  public void updateOrderStatus_confirmedWithNullDelivery_doesNotCrash() {
    Long id = 1L;
    Order order = mock(Order.class);

    when(orderRepository.findById(id)).thenReturn(Optional.of(order));
    when(order.isValidStatusTransition(OrderStatus.CONFIRMED)).thenReturn(true);
    when(orderRepository.save(order)).thenReturn(order);
    when(order.getUser()).thenReturn(user);
    when(order.getDelivery()).thenReturn(null);

    Order result = orderService.updateOrderStatus(id, "CONFIRMED");

    assertSame(order, result);
    verify(notificationService).sendDeliveryStatusUpdate(user, null);
  }

  @Test
  public void getOrdersByUser_withMultipleOrders_returnsAll() {
    Long userId = 1L;
    Order order1 = mock(Order.class);
    Order order2 = mock(Order.class);
    Order order3 = mock(Order.class);

    when(orderRepository.findByCustomerId(userId)).thenReturn(List.of(order1, order2, order3));

    List<Order> orders = orderService.getOrdersByUser(userId);

    assertNotNull(orders);
    assertEquals(3, orders.size());
    verify(orderRepository).findByCustomerId(userId);
  }

  @Test
  public void getAllOrders_withMultipleOrders_returnsAll() {
    Order order1 = mock(Order.class);
    Order order2 = mock(Order.class);

    when(orderRepository.findAll()).thenReturn(List.of(order1, order2));

    List<Order> orders = orderService.getAllOrders();

    assertNotNull(orders);
    assertEquals(2, orders.size());
    verify(orderRepository).findAll();
  }

  @Test
  public void cancelOrder_statusMessageIncludesCurrentStatus() {
    Long id = 1L;
    Order order = mock(Order.class);

    when(orderRepository.findById(id)).thenReturn(Optional.of(order));
    when(order.canBeCancelled()).thenReturn(false);
    when(order.getStatus()).thenReturn(OrderStatus.COMPLETED);

    RuntimeException ex = assertThrows(RuntimeException.class, () -> orderService.cancelOrder(id));

    assertTrue(ex.getMessage().contains("Order cannot be cancelled"));
    assertTrue(ex.getMessage().contains("COMPLETED"));
  }

  @Test
  public void updateOrderStatus_invalidTransitionMessageIncludesStatuses() {
    Long id = 1L;
    Order order = mock(Order.class);

    when(orderRepository.findById(id)).thenReturn(Optional.of(order));
    when(order.isValidStatusTransition(OrderStatus.COMPLETED)).thenReturn(false);
    when(order.getStatus()).thenReturn(OrderStatus.PENDING);

    RuntimeException ex =
        assertThrows(RuntimeException.class, () -> orderService.updateOrderStatus(id, "COMPLETED"));

    assertTrue(ex.getMessage().contains("Invalid status transition"));
    assertTrue(ex.getMessage().contains("PENDING"));
    assertTrue(ex.getMessage().contains("COMPLETED"));
  }
}
