package com.boozebuddies.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.boozebuddies.dto.DeliveryDTO;
import com.boozebuddies.entity.Delivery;
import com.boozebuddies.entity.Driver;
import com.boozebuddies.entity.Order;
import com.boozebuddies.model.DeliveryStatus;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("DeliveryMapper Tests")
public class DeliveryMapperTest {
  private DeliveryMapper deliveryMapper;
  private Delivery testDelivery;
  private Driver testDriver;
  private Order testOrder;
  private static final String TEST_ADDRESS = "123 Test St, City, ST 12345";

  @BeforeEach
  public void setUp() {
    deliveryMapper = new DeliveryMapper();

    // Set up test driver
    testDriver = Driver.builder().id(1L).name("John Doe").phone("+1234567890").build();

    // Set up test order
    testOrder = Order.builder().id(100L).build();

    // Set up test delivery
    testDelivery =
        Delivery.builder()
            .id(1000L)
            .order(testOrder)
            .driver(testDriver)
            .status(DeliveryStatus.IN_TRANSIT)
            .deliveryAddress(TEST_ADDRESS)
            .build();
  }

  @Test
  @DisplayName("Test toDTO with all fields populated")
  public void testToDTOAllFields() {
    DeliveryDTO dto = deliveryMapper.toDTO(testDelivery);

    assertNotNull(dto, "DTO should not be null");
    assertEquals(testDelivery.getId(), dto.getId(), "ID should match");
    assertEquals(testDelivery.getOrder().getId(), dto.getOrderId(), "Order ID should match");
    assertEquals(testDelivery.getDriver().getId(), dto.getDriverId(), "Driver ID should match");
    assertEquals(testDelivery.getStatus().name(), dto.getStatus(), "Status should match");
    assertEquals(
        testDelivery.getDeliveryAddress(), dto.getDeliveryAddress(), "Address should match");
    assertEquals(testDelivery.getUpdatedAt(), dto.getUpdatedAt(), "UpdatedAt should match");
  }

  @Test
  @DisplayName("Test toDTO with null driver")
  public void testToDTONullDriver() {
    testDelivery.setDriver(null);
    DeliveryDTO dto = deliveryMapper.toDTO(testDelivery);

    assertNotNull(dto, "DTO should not be null");
    assertNull(dto.getDriverId(), "Driver ID should be null");
  }

  @Test
  @DisplayName("Test toDTO with null delivery")
  public void testToDTONull() {
    assertNull(deliveryMapper.toDTO(null), "Should return null for null input");
  }

  @Test
  @DisplayName("Test toDTO with different delivery statuses")
  public void testToDTODeliveryStatus() {
    DeliveryStatus status = DeliveryStatus.DELIVERED;
    testDelivery.setStatus(status);
    DeliveryDTO dto = deliveryMapper.toDTO(testDelivery);

    assertEquals(status.name(), dto.getStatus(), "Status should match");
  }

  @Test
  @DisplayName("Test toEntity with all fields populated")
  public void testToEntityAllFields() {
    DeliveryDTO dto =
        DeliveryDTO.builder()
            .id(1000L)
            .orderId(100L)
            .driverId(1L)
            .status(DeliveryStatus.IN_TRANSIT.name())
            .deliveryAddress(TEST_ADDRESS)
            .updatedAt(LocalDateTime.of(2024, 1, 1, 12, 0))
            .build();

    Delivery entity = deliveryMapper.toEntity(dto);

    assertNotNull(entity, "Entity should not be null");
    assertEquals(dto.getId(), entity.getId(), "ID should match");
    assertEquals(dto.getOrderId(), entity.getOrder().getId(), "Order ID should match");
    assertEquals(dto.getDriverId(), entity.getDriver().getId(), "Driver ID should match");
    assertEquals(dto.getStatus(), entity.getStatus().name(), "Status should match");
    assertEquals(dto.getDeliveryAddress(), entity.getDeliveryAddress(), "Address should match");
    assertEquals(dto.getUpdatedAt(), entity.getUpdatedAt(), "UpdatedAt should match");
  }

  @Test
  @DisplayName("Test toEntity with null driver ID")
  public void testToEntityNullDriver() {
    DeliveryDTO dto =
        DeliveryDTO.builder()
            .id(1000L)
            .orderId(100L)
            .status(DeliveryStatus.IN_TRANSIT.name())
            .deliveryAddress(TEST_ADDRESS)
            .build();

    Delivery entity = deliveryMapper.toEntity(dto);

    assertNotNull(entity, "Entity should not be null");
    assertNull(entity.getDriver(), "Driver should be null");
  }

  @Test
  @DisplayName("Test toEntity with null DTO")
  public void testToEntityNull() {
    assertNull(deliveryMapper.toEntity(null), "Should return null for null input");
  }

  @Test
  public void toDTO_nullDelivery_returnsNull() {
    assertNull(deliveryMapper.toDTO(null));
  }

  @Test
  public void toDTO_fullDelivery_mapsAllFields() {
    DeliveryDTO dto = deliveryMapper.toDTO(testDelivery);

    assertNotNull(dto);
    assertEquals(testDelivery.getId(), dto.getId());
    assertEquals(testDelivery.getOrder().getId(), dto.getOrderId());
    assertEquals(testDelivery.getDriver().getId(), dto.getDriverId());
    assertEquals(testDelivery.getStatus().name(), dto.getStatus());
    assertEquals(testDelivery.getDeliveryAddress(), dto.getDeliveryAddress());
    assertEquals(testDelivery.getDeliveryLatitude(), dto.getDeliveryLatitude());
    assertEquals(testDelivery.getDeliveryLongitude(), dto.getDeliveryLongitude());
    assertEquals(testDelivery.getPickupTime(), dto.getPickupTime());
    assertEquals(testDelivery.getDeliveredTime(), dto.getDeliveredTime());
    assertEquals(testDelivery.getEstimatedDeliveryTime(), dto.getEstimatedDeliveryTime());
    assertEquals(testDriver.getName(), dto.getDriverName());
    assertEquals(testDriver.getPhone(), dto.getDriverPhone());
  }

  @Test
  public void toDTO_deliveryWithNullDriver_mapsOtherFields() {
    testDelivery.setDriver(null);

    DeliveryDTO dto = deliveryMapper.toDTO(testDelivery);

    assertNotNull(dto);
    assertNull(dto.getDriverId());
    assertNull(dto.getDriverName());
    assertNull(dto.getDriverPhone());
    // Other fields should still be mapped
    assertEquals(testDelivery.getId(), dto.getId());
    assertEquals(testDelivery.getOrder().getId(), dto.getOrderId());
    assertEquals(testDelivery.getStatus().name(), dto.getStatus());
  }

  @Test
  public void toDTO_deliveryWithNullOrder_mapsOtherFields() {
    testDelivery.setOrder(null);

    DeliveryDTO dto = deliveryMapper.toDTO(testDelivery);

    assertNotNull(dto);
    assertNull(dto.getOrderId());
    // Other fields should still be mapped
    assertEquals(testDelivery.getId(), dto.getId());
    assertEquals(testDelivery.getDriver().getId(), dto.getDriverId());
    assertEquals(testDelivery.getStatus().name(), dto.getStatus());
  }

  @Test
  public void toDTO_deliveryWithCancellationReason_mapsReason() {
    String reason = "Customer cancelled";
    testDelivery.setCancellationReason(reason);

    DeliveryDTO dto = deliveryMapper.toDTO(testDelivery);

    assertNotNull(dto);
    assertEquals(reason, dto.getCancellationReason());
  }

  @Test
  public void toDTO_deliveryWithNullOptionalFields_mapsRequiredFields() {
    testDelivery.setDeliveryLatitude(null);
    testDelivery.setDeliveryLongitude(null);
    testDelivery.setPickupTime(null);
    testDelivery.setDeliveredTime(null);
    testDelivery.setEstimatedDeliveryTime(null);
    testDelivery.setCancellationReason(null);

    DeliveryDTO dto = deliveryMapper.toDTO(testDelivery);

    assertNotNull(dto);
    // Required fields
    assertEquals(testDelivery.getId(), dto.getId());
    assertEquals(testDelivery.getOrder().getId(), dto.getOrderId());
    assertEquals(testDelivery.getDriver().getId(), dto.getDriverId());
    assertEquals(testDelivery.getStatus().name(), dto.getStatus());
    assertEquals(testDelivery.getDeliveryAddress(), dto.getDeliveryAddress());
    // Optional fields should be null
    assertNull(dto.getDeliveryLatitude());
    assertNull(dto.getDeliveryLongitude());
    assertNull(dto.getPickupTime());
    assertNull(dto.getDeliveredTime());
    assertNull(dto.getEstimatedDeliveryTime());
    assertNull(dto.getCancellationReason());
  }

  @Test
  public void toDTO_completedDelivery_mapsAllTimestamps() {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime pickup = now.minusMinutes(30);
    LocalDateTime delivered = now;
    LocalDateTime estimated = now.minusMinutes(5);

    testDelivery.setStatus(DeliveryStatus.DELIVERED);
    testDelivery.setPickupTime(pickup);
    testDelivery.setDeliveredTime(delivered);
    testDelivery.setEstimatedDeliveryTime(estimated);

    DeliveryDTO dto = deliveryMapper.toDTO(testDelivery);

    assertNotNull(dto);
    assertEquals(DeliveryStatus.DELIVERED.name(), dto.getStatus());
    assertEquals(pickup, dto.getPickupTime());
    assertEquals(delivered, dto.getDeliveredTime());
    assertEquals(estimated, dto.getEstimatedDeliveryTime());
  }
}
