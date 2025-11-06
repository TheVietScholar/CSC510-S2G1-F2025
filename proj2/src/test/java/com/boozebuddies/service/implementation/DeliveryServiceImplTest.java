package com.boozebuddies.service.implementation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

import com.boozebuddies.entity.Delivery;
import com.boozebuddies.entity.Driver;
import com.boozebuddies.entity.Order;
import com.boozebuddies.model.DeliveryStatus;
import com.boozebuddies.repository.DeliveryRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DeliveryServiceImplTest {

  private DeliveryRepository repository;
  private DeliveryServiceImpl service;

  @BeforeEach
  void setUp() {
    repository = mock(DeliveryRepository.class);
    service = new DeliveryServiceImpl(repository);
  }

  @Test
  @DisplayName("assignDriverToOrder creates delivery with PENDING status")
  void assignDriverToOrder_createsPending() {
    Order order = new Order();
    order.setId(100L);
    Driver driver = Driver.builder().id(10L).build();
    when(repository.save(any(Delivery.class))).thenAnswer(inv -> inv.getArgument(0));
    Delivery delivery = service.assignDriverToOrder(order, driver);

    assertEquals(order, delivery.getOrder());
    assertEquals(driver, delivery.getDriver());
    assertEquals(DeliveryStatus.ASSIGNED, delivery.getStatus());
  }

  @Test
  @DisplayName("updateDeliveryStatus updates status or returns null for missing id")
  void updateDeliveryStatus_updatesOrNull() {
    Order order = new Order();
    order.setId(1L);
    Driver driver = Driver.builder().id(1L).build();
    Delivery created =
        Delivery.builder()
            .id(1L)
            .order(order)
            .driver(driver)
            .status(DeliveryStatus.PENDING)
            .build();
    when(repository.findById(1L)).thenReturn(Optional.of(created));
    when(repository.findById(999L)).thenReturn(Optional.empty());
    when(repository.save(any(Delivery.class))).thenAnswer(inv -> inv.getArgument(0));

    Delivery updated = service.updateDeliveryStatus(1L, DeliveryStatus.IN_TRANSIT);
    assertNotNull(updated);
    assertEquals(DeliveryStatus.IN_TRANSIT, updated.getStatus());
    assertThrows(
        RuntimeException.class, () -> service.updateDeliveryStatus(999L, DeliveryStatus.DELIVERED));
  }

  @Test
  @DisplayName("cancelDelivery sets CANCELLED and reason or returns null if missing")
  void cancelDelivery_setsCancelledOrNull() {
    Order order = new Order();
    order.setId(2L);
    Driver driver = Driver.builder().id(2L).build();
    Delivery created =
        Delivery.builder()
            .id(2L)
            .order(order)
            .driver(driver)
            .status(DeliveryStatus.PENDING)
            .build();
    when(repository.findById(2L)).thenReturn(Optional.of(created));
    when(repository.findById(404L)).thenReturn(Optional.empty());
    when(repository.save(any(Delivery.class))).thenAnswer(inv -> inv.getArgument(0));

    Delivery cancelled = service.cancelDelivery(2L, "Customer requested");
    assertNotNull(cancelled);
    assertEquals(DeliveryStatus.CANCELLED, cancelled.getStatus());
    assertEquals("Customer requested", cancelled.getCancellationReason());
    assertThrows(RuntimeException.class, () -> service.cancelDelivery(404L, "x"));
  }

  @Test
  @DisplayName("getDeliveriesByDriver filters by driver id")
  void getDeliveriesByDriver_filters() {
    Order order1 = new Order();
    order1.setId(1L);
    Order order2 = new Order();
    order2.setId(2L);
    Driver d1 = Driver.builder().id(10L).build();
    Delivery del1 =
        Delivery.builder().id(1L).order(order1).driver(d1).status(DeliveryStatus.PENDING).build();
    when(repository.findByDriverId(10L)).thenReturn(Collections.singletonList(del1));

    List<Delivery> forD1 = service.getDeliveriesByDriver(10L);
    assertEquals(1, forD1.size());
    assertEquals(10L, forD1.get(0).getDriver().getId());
  }

  @Test
  @DisplayName("getDeliveryById returns item or null")
  void getDeliveryById_returnsOrNull() {
    Order order = new Order();
    order.setId(5L);
    Driver driver = Driver.builder().id(50L).build();
    Delivery created =
        Delivery.builder()
            .id(3L)
            .order(order)
            .driver(driver)
            .status(DeliveryStatus.PENDING)
            .build();
    when(repository.findById(3L)).thenReturn(Optional.of(created));
    when(repository.findById(999L)).thenReturn(Optional.empty());

    assertNotNull(service.getDeliveryById(3L));
    assertNull(service.getDeliveryById(999L));
  }

  @Test
  @DisplayName("getActiveDeliveries excludes DELIVERED and CANCELLED")
  void getActiveDeliveries_filters() {
    Order o1 = new Order();
    o1.setId(1L);
    Order o2 = new Order();
    o2.setId(2L);
    Order o3 = new Order();
    o3.setId(3L);
    Driver d = Driver.builder().id(1L).build();
    Delivery d1 =
        Delivery.builder().id(11L).order(o1).driver(d).status(DeliveryStatus.IN_TRANSIT).build();
    when(repository.findByStatus(DeliveryStatus.PENDING)).thenReturn(new ArrayList<>());
    when(repository.findByStatus(DeliveryStatus.ASSIGNED)).thenReturn(new ArrayList<>());
    when(repository.findByStatus(DeliveryStatus.PICKED_UP)).thenReturn(new ArrayList<>());
    when(repository.findByStatus(DeliveryStatus.IN_TRANSIT))
        .thenReturn(Collections.singletonList(d1));
    when(repository.findByStatus(DeliveryStatus.FAILED)).thenReturn(new ArrayList<>());

    List<Delivery> active = service.getActiveDeliveries();
    assertEquals(1, active.size());
    assertEquals(d1.getId(), active.get(0).getId());
  }

  @Test
  @DisplayName("getAllDeliveries returns list from repository")
  void getAllDeliveries_returnsList() {
    Delivery d1 = Delivery.builder().id(1L).status(DeliveryStatus.PENDING).build();
    Delivery d2 = Delivery.builder().id(2L).status(DeliveryStatus.DELIVERED).build();
    when(repository.findAll()).thenReturn(List.of(d1, d2));

    List<Delivery> all = service.getAllDeliveries();

    assertEquals(2, all.size());
    assertEquals(1L, all.get(0).getId());
  }

  @Test
  @DisplayName("updateDeliveryWithAgeVerification truncates ID number properly")
  void updateDeliveryWithAgeVerification_truncatesIdNumber() {
    Delivery existing = Delivery.builder().id(1L).build();
    when(repository.findById(1L)).thenReturn(Optional.of(existing));
    when(repository.save(any(Delivery.class))).thenAnswer(inv -> inv.getArgument(0));

    Delivery updated =
        service.updateDeliveryWithAgeVerification(1L, true, "Driver License", "A1234567");

    assertTrue(updated.getAgeVerified());
    assertEquals("Driver License", updated.getIdType());
    assertEquals("4567", updated.getIdNumber()); // only last 4 digits stored
    assertNotNull(updated.getAgeVerifiedAt());
    assertNotNull(updated.getUpdatedAt());
  }

  @Test
  @DisplayName("updateDeliveryWithAgeVerification accepts short ID numbers unchanged")
  void updateDeliveryWithAgeVerification_acceptsShortId() {
    Delivery existing = Delivery.builder().id(2L).build();
    when(repository.findById(2L)).thenReturn(Optional.of(existing));
    when(repository.save(any(Delivery.class))).thenAnswer(inv -> inv.getArgument(0));

    Delivery updated = service.updateDeliveryWithAgeVerification(2L, true, "Passport", "123");
    assertEquals("123", updated.getIdNumber());
  }

  @Test
  @DisplayName("updateDeliveryWithAgeVerification throws if delivery not found")
  void updateDeliveryWithAgeVerification_notFoundThrows() {
    when(repository.findById(999L)).thenReturn(Optional.empty());
    assertThrows(
        RuntimeException.class,
        () -> service.updateDeliveryWithAgeVerification(999L, true, "ID", "0000"));
  }

  @Test
  @DisplayName("updateDeliveryLocation updates coordinates and timestamps")
  void updateDeliveryLocation_updatesCoordinates() {
    Delivery existing = Delivery.builder().id(3L).build();
    when(repository.findById(3L)).thenReturn(Optional.of(existing));
    when(repository.save(any(Delivery.class))).thenAnswer(inv -> inv.getArgument(0));

    service.updateDeliveryLocation(3L, 35.123, -80.987);

    verify(repository, times(1))
        .save(
            argThat(
                delivery ->
                    delivery.getCurrentLatitude().equals(35.123)
                        && delivery.getCurrentLongitude().equals(-80.987)
                        && delivery.getLastLocationUpdate() != null
                        && delivery.getUpdatedAt() != null));
  }

  @Test
  @DisplayName("updateDeliveryLocation throws when delivery not found")
  void updateDeliveryLocation_notFoundThrows() {
    when(repository.findById(404L)).thenReturn(Optional.empty());
    assertThrows(RuntimeException.class, () -> service.updateDeliveryLocation(404L, 1.0, 2.0));
  }

  @Test
  @DisplayName("updateDeliveryStatus sets pickup and delivered timestamps correctly")
  void updateDeliveryStatus_setsTimestamps() {
    Delivery delivery = Delivery.builder().id(10L).status(DeliveryStatus.ASSIGNED).build();
    when(repository.findById(10L)).thenReturn(Optional.of(delivery));
    when(repository.save(any(Delivery.class))).thenAnswer(inv -> inv.getArgument(0));

    // PICKED_UP sets pickupTime
    Delivery pickedUp = service.updateDeliveryStatus(10L, DeliveryStatus.PICKED_UP);
    assertNotNull(pickedUp.getPickupTime());
    assertEquals(DeliveryStatus.PICKED_UP, pickedUp.getStatus());

    // DELIVERED sets deliveredTime
    when(repository.findById(10L)).thenReturn(Optional.of(pickedUp));
    Delivery delivered = service.updateDeliveryStatus(10L, DeliveryStatus.DELIVERED);
    assertNotNull(delivered.getDeliveredTime());
    assertEquals(DeliveryStatus.DELIVERED, delivered.getStatus());
  }

  @Test
  @DisplayName("getDeliveryByOrderId returns delivery when found")
  void getDeliveryByOrderId_returnsDelivery() {
    Order order = new Order();
    order.setId(100L);
    Driver driver = Driver.builder().id(5L).build();
    Delivery delivery =
        Delivery.builder()
            .id(50L)
            .order(order)
            .driver(driver)
            .status(DeliveryStatus.PENDING)
            .build();
    when(repository.findByOrderId(100L)).thenReturn(Optional.of(delivery));

    Delivery found = service.getDeliveryByOrderId(100L);

    assertNotNull(found);
    assertEquals(50L, found.getId());
    assertEquals(100L, found.getOrder().getId());
    verify(repository).findByOrderId(100L);
  }

  @Test
  @DisplayName("getDeliveryByOrderId returns null when not found")
  void getDeliveryByOrderId_returnsNullWhenNotFound() {
    when(repository.findByOrderId(999L)).thenReturn(Optional.empty());

    Delivery found = service.getDeliveryByOrderId(999L);

    assertNull(found);
    verify(repository).findByOrderId(999L);
  }
}
