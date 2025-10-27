package com.boozebuddies.service.implementation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.boozebuddies.entity.Driver;
import com.boozebuddies.model.CertificationStatus;
import com.boozebuddies.repository.DriverRepository;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
    when(repository.findById(999L)).thenReturn(Optional.empty());
    when(repository.save(any(Driver.class))).thenAnswer(inv -> inv.getArgument(0));

    Driver updated = service.updateCertificationStatus(1L, CertificationStatus.APPROVED);
    assertNotNull(updated);
    assertEquals(CertificationStatus.APPROVED, updated.getCertificationStatus());
    assertNull(service.updateCertificationStatus(999L, CertificationStatus.REVOKED));
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
  @DisplayName("getDriverById returns driver or null")
  void getDriverById_returnsOrNull() {
    Driver d1 = Driver.builder().id(1L).name("d1").build();
    when(repository.findById(1L)).thenReturn(Optional.of(d1));
    when(repository.findById(999L)).thenReturn(Optional.empty());

    assertNotNull(service.getDriverById(1L));
    assertNull(service.getDriverById(999L));
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
}
