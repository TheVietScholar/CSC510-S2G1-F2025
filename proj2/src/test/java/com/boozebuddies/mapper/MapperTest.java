package com.boozebuddies.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.boozebuddies.dto.CreateOrderRequest;
import com.boozebuddies.dto.OrderDTO;
import com.boozebuddies.dto.OrderItemRequest;
import com.boozebuddies.dto.PaymentDTO;
import com.boozebuddies.dto.PaymentRequest;
import com.boozebuddies.entity.Driver;
import com.boozebuddies.entity.Merchant;
import com.boozebuddies.entity.Order;
import com.boozebuddies.entity.OrderItem;
import com.boozebuddies.entity.Payment;
import com.boozebuddies.entity.Product;
import com.boozebuddies.entity.User;
import com.boozebuddies.model.OrderStatus;
import com.boozebuddies.model.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MapperTest {
  private OrderMapper orderMapper;
  private PaymentMapper paymentMapper;
  private Order testOrder;
  private CreateOrderRequest testRequest;
  private Payment testPayment;
  private PaymentRequest testPaymentRequest;
  private LocalDateTime now;

  @BeforeEach
  void setUp() {
    orderMapper = new OrderMapper();
    paymentMapper = new PaymentMapper();
    now = LocalDateTime.now();

    // Set up test entities
    User user = new User();
    user.setId(1L);

    Merchant merchant = new Merchant();
    merchant.setId(1L);

    Driver driver = new Driver();
    driver.setId(1L);

    Product product = new Product();
    product.setId(1L);
    product.setName("Test Product");

    OrderItem orderItem = new OrderItem();
    orderItem.setId(1L);
    orderItem.setProduct(product);
    orderItem.setQuantity(2);
    orderItem.setUnitPrice(new BigDecimal("10.00"));
    orderItem.setSubtotal(new BigDecimal("20.00"));

    testOrder = new Order();
    testOrder.setId(1L);
    testOrder.setUser(user);
    testOrder.setMerchant(merchant);
    testOrder.setDriver(driver);
    testOrder.setTotalAmount(new BigDecimal("20.00"));
    testOrder.setStatus(OrderStatus.PENDING);
    testOrder.setDeliveryAddress("123 Test St");
    testOrder.setItems(List.of(orderItem));
    testOrder.setCreatedAt(now);
    testOrder.setUpdatedAt(now);
    testOrder.setEstimatedDeliveryTime(now.plusHours(1));

    // Set up test request
    OrderItemRequest itemRequest = new OrderItemRequest();
    itemRequest.setProductId(1L);
    itemRequest.setQuantity(2);
    itemRequest.setUnitPrice(new BigDecimal("10.00"));

    testRequest = new CreateOrderRequest();
    testRequest.setUserId(1L);
    testRequest.setMerchantId(1L);
    testRequest.setDeliveryAddress("123 Test St");
    testRequest.setItems(List.of(itemRequest));
    testRequest.setSpecialInstructions("Test instructions");
  }

  @Test
  void toDTO_fullOrder_mapsAllFields() {
    OrderDTO dto = orderMapper.toDTO(testOrder);

    assertNotNull(dto);
    assertEquals(testOrder.getId(), dto.getId());
    assertEquals(testOrder.getUser().getId(), dto.getUserId());
    assertEquals(testOrder.getMerchant().getId(), dto.getMerchantId());
    assertEquals(testOrder.getDriver().getId(), dto.getDriverId());
    assertEquals(testOrder.getTotalAmount(), dto.getTotalAmount());
    assertEquals(testOrder.getStatus().name(), dto.getStatus());
    assertEquals(testOrder.getDeliveryAddress(), dto.getDeliveryAddress());
    assertEquals(testOrder.getCreatedAt(), dto.getCreatedAt());
    assertEquals(testOrder.getUpdatedAt(), dto.getUpdatedAt());
    assertEquals(testOrder.getEstimatedDeliveryTime(), dto.getEstimatedDeliveryTime());

    assertNotNull(dto.getItems());
    assertEquals(1, dto.getItems().size());

    var firstItem = dto.getItems().get(0);
    var sourceItem = testOrder.getItems().get(0);
    assertEquals(sourceItem.getId(), firstItem.getId());
    assertEquals(sourceItem.getProduct().getId(), firstItem.getProductId());
    assertEquals(sourceItem.getProduct().getName(), firstItem.getProductName());
    assertEquals(sourceItem.getQuantity(), firstItem.getQuantity());
    assertEquals(sourceItem.getUnitPrice(), firstItem.getUnitPrice());
    assertEquals(sourceItem.getSubtotal(), firstItem.getSubtotal());
  }

  @Test
  void toDTO_nullOrder_returnsNull() {
    assertNull(orderMapper.toDTO(null));
  }

  @Test
  void toDTO_orderWithNullFields_mapsWithNulls() {
    testOrder.setUser(null);
    testOrder.setMerchant(null);
    testOrder.setDriver(null);
    testOrder.setItems(null);

    OrderDTO dto = orderMapper.toDTO(testOrder);

    assertNotNull(dto);
    assertNull(dto.getUserId());
    assertNull(dto.getMerchantId());
    assertNull(dto.getDriverId());
    assertNull(dto.getItems());
  }

  @Test
  void toEntity_fullRequest_mapsAllFields() {
    Order entity = orderMapper.toEntity(testRequest);

    assertNotNull(entity);
    assertEquals(testRequest.getUserId(), entity.getUser().getId());
    assertEquals(testRequest.getMerchantId(), entity.getMerchant().getId());
    assertEquals(testRequest.getDeliveryAddress(), entity.getDeliveryAddress());
    assertEquals(testRequest.getSpecialInstructions(), entity.getSpecialInstructions());

    assertNotNull(entity.getItems());
    assertEquals(1, entity.getItems().size());

    var firstItem = entity.getItems().get(0);
    var sourceItem = testRequest.getItems().get(0);
    assertEquals(sourceItem.getQuantity(), firstItem.getQuantity());
    assertEquals(sourceItem.getUnitPrice(), firstItem.getUnitPrice());
  }

  @Test
  void toEntity_nullRequest_returnsNull() {
    assertNull(orderMapper.toEntity(null));
  }

  @Test
  void toEntity_requestWithNullFields_mapsWithNulls() {
    testRequest.setUserId(null);
    testRequest.setMerchantId(null);
    testRequest.setItems(null);

    Order entity = orderMapper.toEntity(testRequest);

    assertNotNull(entity);
    assertNull(entity.getUser());
    assertNull(entity.getMerchant());
    assertNull(entity.getItems());
  }

  @Test
  void paymentToDTO_fullPayment_mapsAllFields() {
    testPayment =
        Payment.builder()
            .id(1L)
            .order(testOrder)
            .amount(new BigDecimal("20.00"))
            .status(PaymentStatus.CAPTURED)
            .paymentMethod("CREDIT_CARD")
            .transactionId("txn_123")
            .paymentDate(now)
            .failureReason(null)
            .build();

    PaymentDTO dto = paymentMapper.toDTO(testPayment);

    assertNotNull(dto);
    assertEquals(testPayment.getId(), dto.getId());
    assertEquals(testPayment.getOrder().getId(), dto.getOrderId());
    assertEquals(testPayment.getAmount(), dto.getAmount());
    assertEquals(testPayment.getStatus().name(), dto.getStatus());
    assertEquals(testPayment.getPaymentMethod(), dto.getPaymentMethod());
    assertEquals(testPayment.getTransactionId(), dto.getTransactionId());
    assertEquals(testPayment.getPaymentDate(), dto.getPaymentDate());
    assertNull(dto.getFailureReason());
  }

  @Test
  void paymentToDTO_nullPayment_returnsNull() {
    assertNull(paymentMapper.toDTO(null));
  }

  @Test
  void paymentToDTO_paymentWithNullFields_mapsWithNulls() {
    testPayment =
        Payment.builder()
            .id(1L)
            .order(null)
            .amount(new BigDecimal("20.00"))
            .status(PaymentStatus.CAPTURED)
            .build();

    PaymentDTO dto = paymentMapper.toDTO(testPayment);

    assertNotNull(dto);
    assertNull(dto.getOrderId());
    assertNull(dto.getPaymentMethod());
    assertNull(dto.getTransactionId());
    assertNull(dto.getPaymentDate());
  }

  @Test
  void paymentToEntity_fullRequest_mapsAllFields() {
    testPaymentRequest = new PaymentRequest();
    testPaymentRequest.setAmount(new BigDecimal("20.00"));
    testPaymentRequest.setPaymentMethod("CREDIT_CARD");

    Payment entity = paymentMapper.toEntity(testPaymentRequest);

    assertNotNull(entity);
    assertEquals(testPaymentRequest.getAmount(), entity.getAmount());
    assertEquals(testPaymentRequest.getPaymentMethod(), entity.getPaymentMethod());
  }

  @Test
  void paymentToEntity_nullRequest_returnsNull() {
    assertNull(paymentMapper.toEntity(null));
  }
}
