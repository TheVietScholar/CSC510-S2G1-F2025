package com.boozebuddies.entity;

import static org.junit.jupiter.api.Assertions.*;

import com.boozebuddies.model.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Order Entity Tests")
class OrderTest {

  private Order order;
  private User testUser;
  private Merchant testMerchant;
  private Driver testDriver;
  private OrderItem testItem1;
  private OrderItem testItem2;

  @BeforeEach
  void setUp() {
    testUser = User.builder().id(1L).name("John Doe").email("john@example.com").build();
    testMerchant = Merchant.builder().id(1L).name("Test Store").build();
    testDriver = Driver.builder().id(1L).build();

    testItem1 =
        OrderItem.builder()
            .id(1L)
            .name("Beer")
            .quantity(2)
            .unitPrice(new BigDecimal("10.00"))
            .subtotal(new BigDecimal("20.00"))
            .build();

    testItem2 =
        OrderItem.builder()
            .id(2L)
            .name("Wine")
            .quantity(1)
            .unitPrice(new BigDecimal("25.00"))
            .subtotal(new BigDecimal("25.00"))
            .build();

    order =
        Order.builder()
            .id(1L)
            .user(testUser)
            .merchant(testMerchant)
            .driver(testDriver)
            .status(OrderStatus.PENDING)
            .totalAmount(new BigDecimal("45.00"))
            .deliveryAddress("123 Main St")
            .specialInstructions("Ring doorbell")
            .ageVerified(true)
            .items(new ArrayList<>())
            .build();
  }

  // ==================== BUILDER TESTS ====================

  @Test
  @DisplayName("Builder creates order with all fields")
  void testBuilder_AllFields() {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime estimatedDelivery = now.plusHours(1);
    List<OrderItem> items = new ArrayList<>();
    items.add(testItem1);

    Order testOrder =
        Order.builder()
            .id(2L)
            .user(testUser)
            .merchant(testMerchant)
            .driver(testDriver)
            .status(OrderStatus.CONFIRMED)
            .totalAmount(new BigDecimal("100.00"))
            .deliveryAddress("456 Oak Ave")
            .specialInstructions("Leave at door")
            .ageVerified(true)
            .createdAt(now)
            .updatedAt(now)
            .estimatedDeliveryTime(estimatedDelivery)
            .promoCode("SAVE20")
            .items(items)
            .build();

    assertNotNull(testOrder);
    assertEquals(2L, testOrder.getId());
    assertEquals(testUser, testOrder.getUser());
    assertEquals(testMerchant, testOrder.getMerchant());
    assertEquals(testDriver, testOrder.getDriver());
    assertEquals(OrderStatus.CONFIRMED, testOrder.getStatus());
    assertEquals(new BigDecimal("100.00"), testOrder.getTotalAmount());
    assertEquals("456 Oak Ave", testOrder.getDeliveryAddress());
    assertEquals("Leave at door", testOrder.getSpecialInstructions());
    assertTrue(testOrder.isAgeVerified());
    assertEquals(now, testOrder.getCreatedAt());
    assertEquals(now, testOrder.getUpdatedAt());
    assertEquals(estimatedDelivery, testOrder.getEstimatedDeliveryTime());
    assertEquals("SAVE20", testOrder.getPromoCode());
    assertEquals(1, testOrder.getItems().size());
  }

  @Test
  @DisplayName("Builder creates order with default values")
  void testBuilder_Defaults() {
    Order testOrder =
        Order.builder()
            .user(testUser)
            .merchant(testMerchant)
            .status(OrderStatus.PENDING)
            .deliveryAddress("123 Main St")
            .build();

    assertNotNull(testOrder);
    assertFalse(testOrder.isAgeVerified());
    assertNotNull(testOrder.getCreatedAt());
    assertNotNull(testOrder.getUpdatedAt());
  }

  // ==================== GETTERS AND SETTERS TESTS ====================

  @Test
  @DisplayName("getId returns correct value")
  void testGetId() {
    assertEquals(1L, order.getId());
  }

  @Test
  @DisplayName("setId sets correct value")
  void testSetId() {
    order.setId(999L);
    assertEquals(999L, order.getId());
  }

  @Test
  @DisplayName("getUser returns correct value")
  void testGetUser() {
    assertEquals(testUser, order.getUser());
  }

  @Test
  @DisplayName("setUser sets correct value")
  void testSetUser() {
    User newUser = User.builder().id(2L).name("Jane Doe").build();
    order.setUser(newUser);
    assertEquals(newUser, order.getUser());
  }

  @Test
  @DisplayName("getMerchant returns correct value")
  void testGetMerchant() {
    assertEquals(testMerchant, order.getMerchant());
  }

  @Test
  @DisplayName("setMerchant sets correct value")
  void testSetMerchant() {
    Merchant newMerchant = Merchant.builder().id(2L).name("New Store").build();
    order.setMerchant(newMerchant);
    assertEquals(newMerchant, order.getMerchant());
  }

  @Test
  @DisplayName("getDriver returns correct value")
  void testGetDriver() {
    assertEquals(testDriver, order.getDriver());
  }

  @Test
  @DisplayName("setDriver sets correct value")
  void testSetDriver() {
    Driver newDriver = Driver.builder().id(2L).build();
    order.setDriver(newDriver);
    assertEquals(newDriver, order.getDriver());
  }

  @Test
  @DisplayName("getStatus returns correct value")
  void testGetStatus() {
    assertEquals(OrderStatus.PENDING, order.getStatus());
  }

  @Test
  @DisplayName("setStatus sets correct value")
  void testSetStatus() {
    order.setStatus(OrderStatus.COMPLETED);
    assertEquals(OrderStatus.COMPLETED, order.getStatus());
  }

  @Test
  @DisplayName("getTotalAmount returns correct value")
  void testGetTotalAmount() {
    assertEquals(new BigDecimal("45.00"), order.getTotalAmount());
  }

  @Test
  @DisplayName("setTotalAmount sets correct value")
  void testSetTotalAmount() {
    order.setTotalAmount(new BigDecimal("100.00"));
    assertEquals(new BigDecimal("100.00"), order.getTotalAmount());
  }

  // ==================== ADD ITEM TESTS ====================

  @Test
  @DisplayName("addItem adds item to order and sets line number")
  void testAddItem_Success() {
    order.addItem(testItem1);

    assertEquals(1, order.getItems().size());
    assertEquals(order, testItem1.getOrder());
    assertEquals(1, testItem1.getLineNo());
  }

  @Test
  @DisplayName("addItem sets sequential line numbers")
  void testAddItem_SequentialLineNumbers() {
    order.addItem(testItem1);
    order.addItem(testItem2);

    assertEquals(2, order.getItems().size());
    assertEquals(1, testItem1.getLineNo());
    assertEquals(2, testItem2.getLineNo());
  }

  @Test
  @DisplayName("addItem maintains bidirectional relationship")
  void testAddItem_BidirectionalRelationship() {
    order.addItem(testItem1);

    assertEquals(order, testItem1.getOrder());
    assertTrue(order.getItems().contains(testItem1));
  }

  // ==================== REMOVE ITEM TESTS ====================

  @Test
  @DisplayName("removeItem removes item from order")
  void testRemoveItem_Success() {
    order.addItem(testItem1);
    order.addItem(testItem2);

    order.removeItem(testItem1);

    assertEquals(1, order.getItems().size());
    assertFalse(order.getItems().contains(testItem1));
    assertNull(testItem1.getOrder());
  }

  @Test
  @DisplayName("removeItem renormalizes line numbers")
  void testRemoveItem_RenormalizesLineNumbers() {
    order.addItem(testItem1);
    order.addItem(testItem2);

    OrderItem testItem3 =
        OrderItem.builder()
            .id(3L)
            .name("Whiskey")
            .quantity(1)
            .unitPrice(new BigDecimal("50.00"))
            .subtotal(new BigDecimal("50.00"))
            .build();
    order.addItem(testItem3);

    // Remove middle item
    order.removeItem(testItem2);

    assertEquals(2, order.getItems().size());
    assertEquals(1, testItem1.getLineNo());
    assertEquals(2, testItem3.getLineNo());
  }

  @Test
  @DisplayName("removeItem breaks bidirectional relationship")
  void testRemoveItem_BreaksBidirectionalRelationship() {
    order.addItem(testItem1);
    order.removeItem(testItem1);

    assertNull(testItem1.getOrder());
    assertFalse(order.getItems().contains(testItem1));
  }

  // ==================== CALCULATE TOTAL TESTS ====================

  @Test
  @DisplayName("calculateTotal sums all item subtotals")
  void testCalculateTotal_Success() {
    order.addItem(testItem1);
    order.addItem(testItem2);

    order.calculateTotal();

    assertEquals(new BigDecimal("45.00"), order.getTotalAmount());
  }

  @Test
  @DisplayName("calculateTotal returns zero for empty order")
  void testCalculateTotal_EmptyOrder() {
    order.setItems(new ArrayList<>());
    order.calculateTotal();

    assertEquals(BigDecimal.ZERO, order.getTotalAmount());
  }

  @Test
  @DisplayName("calculateTotal returns zero for null items")
  void testCalculateTotal_NullItems() {
    order.setItems(null);
    order.calculateTotal();

    assertEquals(BigDecimal.ZERO, order.getTotalAmount());
  }

  @Test
  @DisplayName("calculateTotal filters out null subtotals")
  void testCalculateTotal_FilterNullSubtotals() {
    OrderItem itemWithNullSubtotal =
        OrderItem.builder().id(3L).name("Item with null subtotal").subtotal(null).build();

    order.addItem(testItem1);
    order.addItem(itemWithNullSubtotal);
    order.addItem(testItem2);

    order.calculateTotal();

    assertEquals(new BigDecimal("45.00"), order.getTotalAmount());
  }

  @Test
  @DisplayName("calculateTotal handles single item")
  void testCalculateTotal_SingleItem() {
    order.addItem(testItem1);
    order.calculateTotal();

    assertEquals(new BigDecimal("20.00"), order.getTotalAmount());
  }

  // ==================== CAN BE CANCELLED TESTS ====================

  @Test
  @DisplayName("canBeCancelled returns true for PENDING status")
  void testCanBeCancelled_Pending() {
    order.setStatus(OrderStatus.PENDING);
    assertTrue(order.canBeCancelled());
  }

  @Test
  @DisplayName("canBeCancelled returns true for CONFIRMED status")
  void testCanBeCancelled_Confirmed() {
    order.setStatus(OrderStatus.CONFIRMED);
    assertTrue(order.canBeCancelled());
  }

  @Test
  @DisplayName("canBeCancelled returns false for PREPARING status")
  void testCanBeCancelled_Preparing() {
    order.setStatus(OrderStatus.PREPARING);
    assertFalse(order.canBeCancelled());
  }

  @Test
  @DisplayName("canBeCancelled returns false for READY_FOR_PICKUP status")
  void testCanBeCancelled_ReadyForPickup() {
    order.setStatus(OrderStatus.READY_FOR_PICKUP);
    assertFalse(order.canBeCancelled());
  }

  @Test
  @DisplayName("canBeCancelled returns false for OUT_FOR_DELIVERY status")
  void testCanBeCancelled_OutForDelivery() {
    order.setStatus(OrderStatus.IN_TRANSIT);
    assertFalse(order.canBeCancelled());
  }

  @Test
  @DisplayName("canBeCancelled returns false for COMPLETED status")
  void testCanBeCancelled_Completed() {
    order.setStatus(OrderStatus.COMPLETED);
    assertFalse(order.canBeCancelled());
  }

  @Test
  @DisplayName("canBeCancelled returns false for CANCELLED status")
  void testCanBeCancelled_Cancelled() {
    order.setStatus(OrderStatus.CANCELLED);
    assertFalse(order.canBeCancelled());
  }

  // ==================== IS VALID STATUS TRANSITION TESTS ====================

  @Test
  @DisplayName("isValidStatusTransition returns true (simplified)")
  void testIsValidStatusTransition() {
    // Simplified implementation always returns true
    assertTrue(order.isValidStatusTransition(OrderStatus.CONFIRMED));
    assertTrue(order.isValidStatusTransition(OrderStatus.COMPLETED));
    assertTrue(order.isValidStatusTransition(OrderStatus.CANCELLED));
  }

  // ==================== LIFECYCLE TESTS ====================

  @Test
  @DisplayName("preUpdate updates updatedAt timestamp")
  void testPreUpdate() {
    LocalDateTime originalUpdatedAt = order.getUpdatedAt();

    try {
      Thread.sleep(10);
    } catch (InterruptedException e) {
      // Ignore
    }

    order.preUpdate();

    assertNotNull(order.getUpdatedAt());
    assertTrue(order.getUpdatedAt().isAfter(originalUpdatedAt));
  }

  // ==================== BOOLEAN GETTER TESTS ====================

  @Test
  @DisplayName("isAgeVerified returns correct value")
  void testIsAgeVerified() {
    order.setAgeVerified(true);
    assertTrue(order.isAgeVerified());

    order.setAgeVerified(false);
    assertFalse(order.isAgeVerified());
  }

  // ==================== RELATIONSHIP TESTS ====================

  @Test
  @DisplayName("Order has many-to-one relationship with User")
  void testUserRelationship() {
    assertNotNull(order.getUser());
    assertEquals(1L, order.getUser().getId());
  }

  @Test
  @DisplayName("Order has many-to-one relationship with Merchant")
  void testMerchantRelationship() {
    assertNotNull(order.getMerchant());
    assertEquals(1L, order.getMerchant().getId());
  }

  @Test
  @DisplayName("Order has many-to-one relationship with Driver")
  void testDriverRelationship() {
    assertNotNull(order.getDriver());
    assertEquals(1L, order.getDriver().getId());
  }

  @Test
  @DisplayName("Order can have null driver")
  void testNullDriver() {
    order.setDriver(null);
    assertNull(order.getDriver());
  }

  @Test
  @DisplayName("Order has one-to-many relationship with OrderItems")
  void testOrderItemsRelationship() {
    order.addItem(testItem1);
    order.addItem(testItem2);

    assertEquals(2, order.getItems().size());
    assertTrue(order.getItems().contains(testItem1));
    assertTrue(order.getItems().contains(testItem2));
  }

  @Test
  @DisplayName("Order has one-to-one relationship with Delivery")
  void testDeliveryRelationship() {
    Delivery delivery = new Delivery();
    order.setDelivery(delivery);

    assertNotNull(order.getDelivery());
    assertEquals(delivery, order.getDelivery());
  }

  @Test
  @DisplayName("Order has one-to-one relationship with Payment")
  void testPaymentRelationship() {
    Payment payment = new Payment();
    order.setPayment(payment);

    assertNotNull(order.getPayment());
    assertEquals(payment, order.getPayment());
  }

  // ==================== CONSTRUCTOR TESTS ====================

  @Test
  @DisplayName("NoArgsConstructor creates empty order")
  void testNoArgsConstructor() {
    Order emptyOrder = new Order();
    assertNotNull(emptyOrder);
  }

  @Test
  @DisplayName("AllArgsConstructor creates order with all parameters")
  void testAllArgsConstructor() {
    LocalDateTime now = LocalDateTime.now();
    List<OrderItem> items = new ArrayList<>();

    Order testOrder =
        new Order(
            5L,
            testUser,
            testMerchant,
            testDriver,
            OrderStatus.PENDING,
            new BigDecimal("100.00"),
            "789 Pine St",
            "Ring twice",
            true,
            now,
            now,
            now.plusHours(2),
            "DISCOUNT10",
            items,
            null,
            null);

    assertNotNull(testOrder);
    assertEquals(5L, testOrder.getId());
    assertEquals(testUser, testOrder.getUser());
    assertEquals(testMerchant, testOrder.getMerchant());
  }

  // ==================== EDGE CASES ====================

  @Test
  @DisplayName("Order with null special instructions")
  void testNullSpecialInstructions() {
    order.setSpecialInstructions(null);
    assertNull(order.getSpecialInstructions());
  }

  @Test
  @DisplayName("Order with empty special instructions")
  void testEmptySpecialInstructions() {
    order.setSpecialInstructions("");
    assertEquals("", order.getSpecialInstructions());
  }

  @Test
  @DisplayName("Order with null promo code")
  void testNullPromoCode() {
    order.setPromoCode(null);
    assertNull(order.getPromoCode());
  }

  @Test
  @DisplayName("Order with null estimated delivery time")
  void testNullEstimatedDeliveryTime() {
    order.setEstimatedDeliveryTime(null);
    assertNull(order.getEstimatedDeliveryTime());
  }

  @Test
  @DisplayName("Order total amount can be zero")
  void testZeroTotalAmount() {
    order.setTotalAmount(BigDecimal.ZERO);
    assertEquals(BigDecimal.ZERO, order.getTotalAmount());
  }

  @Test
  @DisplayName("Order total amount can be large value")
  void testLargeTotalAmount() {
    BigDecimal largeAmount = new BigDecimal("99999999.99");
    order.setTotalAmount(largeAmount);
    assertEquals(largeAmount, order.getTotalAmount());
  }

  @Test
  @DisplayName("Order can be updated multiple times")
  void testMultipleUpdates() {
    LocalDateTime firstUpdate = order.getUpdatedAt();

    try {
      Thread.sleep(10);
    } catch (InterruptedException e) {
      // Ignore
    }

    order.preUpdate();
    LocalDateTime secondUpdate = order.getUpdatedAt();

    try {
      Thread.sleep(10);
    } catch (InterruptedException e) {
      // Ignore
    }

    order.preUpdate();
    LocalDateTime thirdUpdate = order.getUpdatedAt();

    assertTrue(secondUpdate.isAfter(firstUpdate));
    assertTrue(thirdUpdate.isAfter(secondUpdate));
  }

  @Test
  @DisplayName("Adding and removing multiple items maintains correct state")
  void testAddRemoveMultipleItems() {
    OrderItem item3 =
        OrderItem.builder()
            .id(3L)
            .name("Vodka")
            .quantity(1)
            .unitPrice(new BigDecimal("30.00"))
            .subtotal(new BigDecimal("30.00"))
            .build();

    order.addItem(testItem1);
    order.addItem(testItem2);
    order.addItem(item3);

    assertEquals(3, order.getItems().size());

    order.removeItem(testItem2);

    assertEquals(2, order.getItems().size());
    assertEquals(1, testItem1.getLineNo());
    assertEquals(2, item3.getLineNo());
  }
}
