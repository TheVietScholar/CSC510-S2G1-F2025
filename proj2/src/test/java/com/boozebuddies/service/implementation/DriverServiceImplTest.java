package com.boozebuddies.service.implementation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.boozebuddies.entity.Driver;
import com.boozebuddies.exception.DriverNotFoundException;
import com.boozebuddies.model.CertificationStatus;
import com.boozebuddies.repository.DriverRepository;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DriverServiceImplTest {

  private DriverRepository repository;
  private DriverServiceImpl service;

  @BeforeEach
  void setUp() {
    repository = mock(DriverRepository.class);
    service = new DriverServiceImpl(repository);
  }

  @Test
  @DisplayName("registerDriver assigns id and defaults")
  void registerDriver_assignsIdAndDefaults() {
    Driver driver = Driver.builder().name("Alice").email("a@b.com").build();

    when(repository.save(any(Driver.class))).thenAnswer(inv -> inv.getArgument(0));

    Driver saved = service.registerDriver(driver);

    assertEquals(CertificationStatus.PENDING, saved.getCertificationStatus());
    assertFalse(saved.isAvailable());
    verify(repository).save(any(Driver.class));
  }

  @Test
  @DisplayName("updateCertificationStatus updates when driver exists, null otherwise")
  void updateCertificationStatus_updatesOrNull() {
    Driver driver = Driver.builder().id(1L).name("Bob").build();
    when(repository.findById(1L)).thenReturn(Optional.of(driver));
    when(repository.findById(999L)).thenThrow(DriverNotFoundException.class);
    when(repository.save(any(Driver.class))).thenAnswer(inv -> inv.getArgument(0));

    Driver updated = service.updateCertificationStatus(1L, CertificationStatus.APPROVED);
    assertNotNull(updated);
    assertEquals(CertificationStatus.APPROVED, updated.getCertificationStatus());
    assertThrows(
        DriverNotFoundException.class,
        () -> service.updateCertificationStatus(999L, CertificationStatus.REVOKED));
  }

  @Test
  @DisplayName("updateAvailability toggles availability when driver exists, null otherwise")
  void updateAvailability_updatesOrNull() {
    Driver driver = Driver.builder().id(2L).name("Charlie").build();
    when(repository.findById(2L)).thenReturn(Optional.of(driver));
    when(repository.findById(404L)).thenReturn(Optional.empty());
    when(repository.save(any(Driver.class))).thenAnswer(inv -> inv.getArgument(0));

    Driver updated = service.updateAvailability(2L, true);
    assertNotNull(updated);
    assertTrue(updated.isAvailable());
    assertNull(service.updateAvailability(404L, true));
  }

  @Test
  @DisplayName("getAvailableDrivers returns only available drivers")
  void getAvailableDrivers_filters() {
    Driver d1 = Driver.builder().id(1L).name("d1").build();
    d1.setAvailable(false);
    Driver d2 = Driver.builder().id(2L).name("d2").build();
    d2.setAvailable(true);
    when(repository.findByIsAvailable(true)).thenReturn(Collections.singletonList(d2));

    List<Driver> available = service.getAvailableDrivers();
    assertEquals(1, available.size());
    assertEquals(d2.getId(), available.get(0).getId());
  }

  @Test
  @DisplayName("getDriverById returns driver or empty optional")
  void getDriverById_returnsOrNull() {
    Driver d1 = Driver.builder().id(1L).name("d1").build();
    when(repository.findById(1L)).thenReturn(Optional.of(d1));
    when(repository.findById(999L)).thenReturn(Optional.empty());

    assertNotNull(service.getDriverById(1L));
    assertThrows(NoSuchElementException.class, service.getDriverById(999L)::get);
  }

  @Test
  @DisplayName("getAllDrivers returns copy list of drivers")
  void getAllDrivers_returnsCopy() {
    List<Driver> list =
        Arrays.asList(
            Driver.builder().id(1L).name("d1").build(), Driver.builder().id(2L).name("d2").build());
    when(repository.findAll()).thenReturn(list);

    List<Driver> all = service.getAllDrivers();
    assertEquals(2, all.size());
    verify(repository).findAll();
  }

  @Test
  @DisplayName("registerDriver throws exception when driver is null")
  void registerDriver_throwsWhenNull() {
    assertThrows(IllegalArgumentException.class, () -> service.registerDriver(null));
  }

  @Test
  @DisplayName("updateDriver saves and returns driver")
  void updateDriver_savesDriver() {
    Driver driver = Driver.builder().id(5L).name("UpdatedDriver").build();
    when(repository.save(driver)).thenReturn(driver);

    Driver result = service.updateDriver(driver);
    assertEquals(driver, result);
    verify(repository).save(driver);
  }

  @Test
  @DisplayName("updateDriverLocation updates coordinates and time when found")
  void updateDriverLocation_updatesSuccessfully() {
    Driver existing = Driver.builder().id(1L).name("GeoDriver").build();
    when(repository.findById(1L)).thenReturn(Optional.of(existing));
    when(repository.save(any(Driver.class))).thenAnswer(inv -> inv.getArgument(0));

    Driver updated = service.updateDriverLocation(1L, 40.7128, -74.0060);

    assertEquals(40.7128, updated.getCurrentLatitude());
    assertEquals(-74.0060, updated.getCurrentLongitude());
    assertNotNull(updated.getUpdatedAt());
    verify(repository).save(any(Driver.class));
  }

  @Test
  @DisplayName("updateDriverLocation throws when driver not found")
  void updateDriverLocation_notFoundThrows() {
    when(repository.findById(404L)).thenReturn(Optional.empty());
    assertThrows(
        IllegalArgumentException.class, () -> service.updateDriverLocation(404L, 0.0, 0.0));
  }

  @Test
  @DisplayName("getDriverProfile returns driver when found")
  void getDriverProfile_returnsDriver() {
    var user = new com.boozebuddies.entity.User();
    user.setId(10L);
    Driver driver = Driver.builder().id(10L).name("ProfileDriver").build();
    when(repository.findById(10L)).thenReturn(Optional.of(driver));

    Driver found = service.getDriverProfile(user);

    assertEquals(driver, found);
    verify(repository).findById(10L);
  }

  @Test
  @DisplayName("getDriverProfile throws when driver not found")
  void getDriverProfile_notFoundThrows() {
    var user = new com.boozebuddies.entity.User();
    user.setId(99L);
    when(repository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class, () -> service.getDriverProfile(user));
  }

  @Test
  @DisplayName("getNearbyAvailableDrivers delegates to repository")
  void getNearbyAvailableDrivers_delegatesCall() {
    Driver d = Driver.builder().id(1L).name("Nearby").build();
    when(repository.findNearbyAvailableDrivers(10.0, 20.0, 5000.0))
        .thenReturn(Collections.singletonList(d));

    List<Driver> result = service.getNearbyAvailableDrivers(10.0, 20.0, 5000.0);

    assertEquals(1, result.size());
    assertEquals(d.getId(), result.get(0).getId());
    verify(repository).findNearbyAvailableDrivers(10.0, 20.0, 5000.0);
  }
}
