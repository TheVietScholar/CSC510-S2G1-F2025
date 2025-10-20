package com.boozebuddies.service.implementation;

import com.boozebuddies.entity.Merchant;
import com.boozebuddies.entity.Order;
import com.boozebuddies.repository.MerchantRepository;
import com.boozebuddies.repository.OrderRepository;
import com.boozebuddies.service.implementation.MerchantServiceImpl;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("MerchantServiceImpl Unit Tests")
class MerchantServiceImplTest {

  @Mock
  private MerchantRepository merchantRepository;

  @Mock
  private OrderRepository orderRepository;

  @InjectMocks
  private MerchantServiceImpl merchantService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  // ==================== REGISTRATION TESTS ====================

  @Test
  @DisplayName("Should register a new merchant")
  void testRegisterMerchant() {
    Merchant merchant = new Merchant();
    merchant.setName("Beer Co");
    merchant.setEmail("contact@beerco.com");

    Merchant saved = new Merchant();
    saved.setId(1L);
    saved.setName("Beer Co");
    saved.setEmail("contact@beerco.com");
    saved.setActive(false);

    when(merchantRepository.save(any(Merchant.class))).thenReturn(saved);

    Merchant registered = merchantService.registerMerchant(merchant);

    assertNotNull(registered);
    assertEquals(1L, registered.getId());
    assertFalse(registered.isActive());
    verify(merchantRepository, times(1)).save(any(Merchant.class));
  }

  // ==================== VERIFICATION TESTS ====================

  @Test
  @DisplayName("Should verify merchant and set active")
  void testVerifyMerchant() {
    Merchant merchant = new Merchant();
    merchant.setId(1L);
    merchant.setActive(false);

    when(merchantRepository.findById(1L)).thenReturn(Optional.of(merchant));
    when(merchantRepository.save(any(Merchant.class))).thenReturn(merchant);

    Merchant verified = merchantService.verifyMerchant(1L, true);

    assertTrue(verified.isActive());
    verify(merchantRepository, times(1)).findById(1L);
    verify(merchantRepository, times(1)).save(merchant);
  }

  @Test
  @DisplayName("Should return null when verifying non-existent merchant")
  void testVerifyNonExistentMerchant() {
    when(merchantRepository.findById(999L)).thenReturn(Optional.empty());
    Merchant result = merchantService.verifyMerchant(999L, true);
    assertNull(result);
  }

  // ==================== RETRIEVAL TESTS ====================

  @Test
  @DisplayName("Should retrieve merchant by ID")
  void testGetMerchantById() {
    Merchant merchant = new Merchant();
    merchant.setId(1L);
    merchant.setName("Beer Co");

    when(merchantRepository.findById(1L)).thenReturn(Optional.of(merchant));

    Merchant result = merchantService.getMerchantById(1L);

    assertNotNull(result);
    assertEquals("Beer Co", result.getName());
  }

  @Test
  @DisplayName("Should retrieve all merchants")
  void testGetAllMerchants() {
    Merchant m1 = new Merchant();
    Merchant m2 = new Merchant();
    when(merchantRepository.findAll()).thenReturn(List.of(m1, m2));

    List<Merchant> result = merchantService.getAllMerchants();

    assertEquals(2, result.size());
    verify(merchantRepository, times(1)).findAll();
  }

  // ==================== DELETION TESTS ====================

  @Test
  @DisplayName("Should delete merchant by ID")
  void testDeleteMerchant() {
    when(merchantRepository.existsById(1L)).thenReturn(true);
    boolean result = merchantService.deleteMerchant(1L);

    assertTrue(result);
    verify(merchantRepository, times(1)).deleteById(1L);
  }

  @Test
  @DisplayName("Should return false when deleting non-existent merchant")
  void testDeleteNonExistentMerchant() {
    when(merchantRepository.existsById(999L)).thenReturn(false);
    boolean result = merchantService.deleteMerchant(999L);

    assertFalse(result);
  }

  // ==================== ORDERS BY MERCHANT TESTS ====================

  @Test
  @DisplayName("Should retrieve orders by merchant ID with pageable")
  void testGetOrdersByMerchant() {
    Pageable pageable = PageRequest.of(0, 10);
    List<Order> orders = List.of(new Order(), new Order());
    Page<Order> orderPage = new PageImpl<>(orders);

    when(orderRepository.findByMerchantId(1L, pageable)).thenReturn(orderPage);

    Page<Order> result = merchantService.getOrdersByMerchant(1L, pageable);

    assertNotNull(result);
    assertEquals(2, result.getContent().size());
    verify(orderRepository, times(1)).findByMerchantId(1L, pageable);
  }

  @Test
  @DisplayName("Should return empty page when merchant has no orders")
  void testGetOrdersByMerchantNoOrders() {
    Pageable pageable = PageRequest.of(0, 10);
    Page<Order> emptyPage = Page.empty();

    when(orderRepository.findByMerchantId(1L, pageable)).thenReturn(emptyPage);

    Page<Order> result = merchantService.getOrdersByMerchant(1L, pageable);

    assertTrue(result.isEmpty());
  }
}
