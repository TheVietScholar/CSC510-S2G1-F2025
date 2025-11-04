package com.boozebuddies.service.implementation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.boozebuddies.entity.Merchant;
import com.boozebuddies.entity.Order;
import com.boozebuddies.repository.MerchantRepository;
import com.boozebuddies.repository.OrderRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class MerchantServiceImplTest {

  @Mock private MerchantRepository merchantRepository;

  @Mock private OrderRepository orderRepository;

  @InjectMocks private MerchantServiceImpl merchantService;

  private Merchant testMerchant;

  @BeforeEach
  void setUp() {
    testMerchant =
        Merchant.builder()
            .id(1L)
            .name("Test Merchant")
            .email("merchant@example.com")
            .phone("555-1234")
            .isActive(false)
            .build();
  }

  // ==================== registerMerchant Tests ====================

  @Test
  void testRegisterMerchant_Success() {
    when(merchantRepository.save(testMerchant)).thenReturn(testMerchant);

    Merchant result = merchantService.registerMerchant(testMerchant);

    assertNotNull(result);
    assertFalse(result.isActive());
    verify(merchantRepository, times(1)).save(testMerchant);
  }

  @Test
  void testRegisterMerchant_MerchantNull() {
    assertThrows(
        IllegalArgumentException.class,
        () -> merchantService.registerMerchant(null),
        "Merchant cannot be null");
  }

  @Test
  void testRegisterMerchant_NameNull() {
    testMerchant.setName(null);
    assertThrows(
        IllegalArgumentException.class,
        () -> merchantService.registerMerchant(testMerchant),
        "Merchant name is required");
  }

  @Test
  void testRegisterMerchant_NameEmpty() {
    testMerchant.setName("");
    assertThrows(
        IllegalArgumentException.class,
        () -> merchantService.registerMerchant(testMerchant),
        "Merchant name is required");
  }

  @Test
  void testRegisterMerchant_EmailNull() {
    testMerchant.setEmail(null);
    assertThrows(
        IllegalArgumentException.class,
        () -> merchantService.registerMerchant(testMerchant),
        "Merchant email is required");
  }

  @Test
  void testRegisterMerchant_EmailEmpty() {
    testMerchant.setEmail("");
    assertThrows(
        IllegalArgumentException.class,
        () -> merchantService.registerMerchant(testMerchant),
        "Merchant email is required");
  }

  @Test
  void testRegisterMerchant_PhoneNull() {
    testMerchant.setPhone(null);
    assertThrows(
        IllegalArgumentException.class,
        () -> merchantService.registerMerchant(testMerchant),
        "Merchant phone is required");
  }

  @Test
  void testRegisterMerchant_PhoneEmpty() {
    testMerchant.setPhone("");
    assertThrows(
        IllegalArgumentException.class,
        () -> merchantService.registerMerchant(testMerchant),
        "Merchant phone is required");
  }

  @Test
  void testRegisterMerchant_StartsInactive() {
    testMerchant.setActive(true); // Set to true to verify it gets changed
    when(merchantRepository.save(any(Merchant.class))).thenReturn(testMerchant);

    Merchant result = merchantService.registerMerchant(testMerchant);

    assertFalse(result.isActive());
  }

  // ==================== verifyMerchant Tests ====================

  @Test
  void testVerifyMerchant_Success_Activate() {
    when(merchantRepository.findById(1L)).thenReturn(Optional.of(testMerchant));
    when(merchantRepository.save(testMerchant)).thenReturn(testMerchant);

    Merchant result = merchantService.verifyMerchant(1L, true);

    assertNotNull(result);
    assertTrue(result.isActive());
    verify(merchantRepository, times(1)).save(testMerchant);
  }

  @Test
  void testVerifyMerchant_Success_Deactivate() {
    testMerchant.setActive(true);
    when(merchantRepository.findById(1L)).thenReturn(Optional.of(testMerchant));
    when(merchantRepository.save(testMerchant)).thenReturn(testMerchant);

    Merchant result = merchantService.verifyMerchant(1L, false);

    assertNotNull(result);
    assertFalse(result.isActive());
  }

  @Test
  void testVerifyMerchant_MerchantIdNull() {
    assertThrows(
        IllegalArgumentException.class,
        () -> merchantService.verifyMerchant(null, true),
        "Invalid merchant ID");
  }

  @Test
  void testVerifyMerchant_MerchantIdZero() {
    assertThrows(
        IllegalArgumentException.class,
        () -> merchantService.verifyMerchant(0L, true),
        "Invalid merchant ID");
  }

  @Test
  void testVerifyMerchant_MerchantIdNegative() {
    assertThrows(
        IllegalArgumentException.class,
        () -> merchantService.verifyMerchant(-1L, true),
        "Invalid merchant ID");
  }

  @Test
  void testVerifyMerchant_MerchantNotFound() {
    when(merchantRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(
        IllegalArgumentException.class,
        () -> merchantService.verifyMerchant(999L, true),
        "Merchant not found");
  }

  // ==================== getMerchantById Tests ====================

  @Test
  void testGetMerchantById_Success() {
    when(merchantRepository.findById(1L)).thenReturn(Optional.of(testMerchant));

    Merchant result = merchantService.getMerchantById(1L);

    assertNotNull(result);
    assertEquals("Test Merchant", result.getName());
  }

  @Test
  void testGetMerchantById_MerchantIdNull() {
    assertThrows(
        IllegalArgumentException.class,
        () -> merchantService.getMerchantById(null),
        "Invalid merchant ID");
  }

  @Test
  void testGetMerchantById_MerchantIdZero() {
    assertThrows(
        IllegalArgumentException.class,
        () -> merchantService.getMerchantById(0L),
        "Invalid merchant ID");
  }

  @Test
  void testGetMerchantById_MerchantIdNegative() {
    assertThrows(
        IllegalArgumentException.class,
        () -> merchantService.getMerchantById(-5L),
        "Invalid merchant ID");
  }

  @Test
  void testGetMerchantById_NotFound() {
    when(merchantRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(
        IllegalArgumentException.class,
        () -> merchantService.getMerchantById(999L),
        "Merchant not found");
  }

  // ==================== getAllMerchants Tests ====================

  @Test
  void testGetAllMerchants_Success() {
    List<Merchant> merchants = List.of(testMerchant);
    when(merchantRepository.findAll()).thenReturn(merchants);

    List<Merchant> result = merchantService.getAllMerchants();

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("Test Merchant", result.get(0).getName());
  }

  @Test
  void testGetAllMerchants_Empty() {
    when(merchantRepository.findAll()).thenReturn(List.of());

    List<Merchant> result = merchantService.getAllMerchants();

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void testGetAllMerchants_MultipleMerchants() {
    Merchant merchant2 =
        Merchant.builder()
            .id(2L)
            .name("Second Merchant")
            .email("merchant2@example.com")
            .phone("555-5678")
            .isActive(true)
            .build();

    List<Merchant> merchants = List.of(testMerchant, merchant2);
    when(merchantRepository.findAll()).thenReturn(merchants);

    List<Merchant> result = merchantService.getAllMerchants();

    assertEquals(2, result.size());
  }

  // ==================== deleteMerchant Tests ====================

  @Test
  void testDeleteMerchant_Success() {
    when(merchantRepository.existsById(1L)).thenReturn(true);

    boolean result = merchantService.deleteMerchant(1L);

    assertTrue(result);
    verify(merchantRepository, times(1)).deleteById(1L);
  }

  @Test
  void testDeleteMerchant_MerchantNotFound() {
    when(merchantRepository.existsById(999L)).thenReturn(false);

    boolean result = merchantService.deleteMerchant(999L);

    assertFalse(result);
    verify(merchantRepository, never()).deleteById(999L);
  }

  @Test
  void testDeleteMerchant_MerchantIdNull() {
    assertThrows(
        IllegalArgumentException.class,
        () -> merchantService.deleteMerchant(null),
        "Invalid merchant ID");
  }

  @Test
  void testDeleteMerchant_MerchantIdZero() {
    assertThrows(
        IllegalArgumentException.class,
        () -> merchantService.deleteMerchant(0L),
        "Invalid merchant ID");
  }

  @Test
  void testDeleteMerchant_MerchantIdNegative() {
    assertThrows(
        IllegalArgumentException.class,
        () -> merchantService.deleteMerchant(-1L),
        "Invalid merchant ID");
  }

  // ==================== getOrdersByMerchant Tests ====================

  @Test
  void testGetOrdersByMerchant_Success() {
    Pageable pageable = PageRequest.of(0, 10);
    Order order = Order.builder().id(1L).merchant(testMerchant).build();
    Page<Order> ordersPage = new PageImpl<>(List.of(order), pageable, 1);

    when(merchantRepository.existsById(1L)).thenReturn(true);
    when(orderRepository.findByMerchantId(1L, pageable)).thenReturn(ordersPage);

    Page<Order> result = merchantService.getOrdersByMerchant(1L, pageable);

    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    verify(orderRepository, times(1)).findByMerchantId(1L, pageable);
  }

  @Test
  void testGetOrdersByMerchant_MerchantIdNull() {
    Pageable pageable = PageRequest.of(0, 10);

    assertThrows(
        IllegalArgumentException.class,
        () -> merchantService.getOrdersByMerchant(null, pageable),
        "Invalid merchant ID");
  }

  @Test
  void testGetOrdersByMerchant_MerchantIdZero() {
    Pageable pageable = PageRequest.of(0, 10);

    assertThrows(
        IllegalArgumentException.class,
        () -> merchantService.getOrdersByMerchant(0L, pageable),
        "Invalid merchant ID");
  }

  @Test
  void testGetOrdersByMerchant_PageableNull() {
    assertThrows(
        IllegalArgumentException.class,
        () -> merchantService.getOrdersByMerchant(1L, null),
        "Pageable cannot be null");
  }

  @Test
  void testGetOrdersByMerchant_MerchantNotFound() {
    Pageable pageable = PageRequest.of(0, 10);
    when(merchantRepository.existsById(999L)).thenReturn(false);

    assertThrows(
        IllegalArgumentException.class,
        () -> merchantService.getOrdersByMerchant(999L, pageable),
        "Merchant not found");
  }

  @Test
  void testGetOrdersByMerchant_EmptyPage() {
    Pageable pageable = PageRequest.of(0, 10);
    Page<Order> emptyPage = new PageImpl<>(List.of(), pageable, 0);

    when(merchantRepository.existsById(1L)).thenReturn(true);
    when(orderRepository.findByMerchantId(1L, pageable)).thenReturn(emptyPage);

    Page<Order> result = merchantService.getOrdersByMerchant(1L, pageable);

    assertNotNull(result);
    assertEquals(0, result.getTotalElements());
  }

  // ==================== getMerchantByName Tests ====================

  @Test
  void testGetMerchantByName_Found() {
    when(merchantRepository.findByName("Test Merchant")).thenReturn(Optional.of(testMerchant));

    Merchant result = merchantService.getMerchantByName("Test Merchant");

    assertNotNull(result);
    assertEquals("Test Merchant", result.getName());
    verify(merchantRepository, times(1)).findByName("Test Merchant");
  }

  @Test
  void testGetMerchantByName_NotFound() {
    when(merchantRepository.findByName("Unknown")).thenReturn(Optional.empty());

    Merchant result = merchantService.getMerchantByName("Unknown");

    assertNull(result);
    verify(merchantRepository, times(1)).findByName("Unknown");
  }

  // ==================== getMerchantsSortedByDistance Tests ====================

  @Test
  void testGetMerchantsSortedByDistance_Success() {
    Merchant m1 = Merchant.builder().id(1L).name("A").latitude(35.0).longitude(-80.0).build();
    Merchant m2 = Merchant.builder().id(2L).name("B").latitude(36.0).longitude(-81.0).build();
    Merchant m3 = Merchant.builder().id(3L).name("C").latitude(34.5).longitude(-79.5).build();

    when(merchantRepository.findAll()).thenReturn(List.of(m2, m1, m3));

    List<Merchant> sorted = merchantService.getMerchantsSortedByDistance(35.0, -80.0);

    assertNotNull(sorted);
    assertEquals(3, sorted.size());
    // m1 should be closest to (35, -80)
    assertEquals("A", sorted.get(0).getName());
  }

  @Test
  void testGetMerchantsSortedByDistance_IgnoresNullCoordinates() {
    Merchant m1 = Merchant.builder().id(1L).name("A").latitude(35.0).longitude(-80.0).build();
    Merchant m2 = Merchant.builder().id(2L).name("B").latitude(null).longitude(null).build();

    when(merchantRepository.findAll()).thenReturn(List.of(m1, m2));

    List<Merchant> result = merchantService.getMerchantsSortedByDistance(35.0, -80.0);

    assertEquals(1, result.size());
    assertEquals("A", result.get(0).getName());
  }

  @Test
  void testGetMerchantsSortedByDistance_LatitudeNull() {
    assertThrows(
        IllegalArgumentException.class,
        () -> merchantService.getMerchantsSortedByDistance(null, -80.0),
        "Latitude and longitude are required");
  }

  @Test
  void testGetMerchantsSortedByDistance_LongitudeNull() {
    assertThrows(
        IllegalArgumentException.class,
        () -> merchantService.getMerchantsSortedByDistance(35.0, null),
        "Latitude and longitude are required");
  }
}
