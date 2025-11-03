package com.boozebuddies.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.boozebuddies.dto.MerchantDTO;
import com.boozebuddies.entity.Merchant;
import com.boozebuddies.entity.Order;
import com.boozebuddies.mapper.MerchantMapper;
import com.boozebuddies.service.MerchantService;
import com.boozebuddies.service.PermissionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MerchantController.class)
@DisplayName("MerchantController Tests")
class MerchantControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private MerchantService merchantService;

  @MockBean private MerchantMapper merchantMapper;

  @MockBean private PermissionService permissionService;

  private Merchant testMerchant;
  private MerchantDTO testMerchantDTO;

  @BeforeEach
  void setUp() {
    testMerchant =
        Merchant.builder()
            .id(1L)
            .name("Test Restaurant")
            .description("A great place to eat")
            .address("123 Main St")
            .phone("555-1234")
            .email("restaurant@example.com")
            .cuisineType("Italian")
            .openingTime(LocalTime.of(10, 0))
            .closingTime(LocalTime.of(22, 0))
            .isActive(true)
            .rating(4.5)
            .totalRatings(100)
            .imageUrl("http://example.com/image.jpg")
            .build();

    testMerchantDTO =
        MerchantDTO.builder()
            .id(1L)
            .name("Test Restaurant")
            .description("A great place to eat")
            .address("123 Main St")
            .phone("555-1234")
            .email("restaurant@example.com")
            .cuisineType("Italian")
            .openingTime(LocalTime.of(10, 0))
            .closingTime(LocalTime.of(22, 0))
            .isActive(true)
            .rating(4.5)
            .totalRatings(100)
            .imageUrl("http://example.com/image.jpg")
            .build();
  }

  // ==================== REGISTER TESTS ====================

  @Test
  @DisplayName("POST /api/merchants/register should return 201 on successful registration")
  void testRegisterMerchant_Success() throws Exception {
    MerchantDTO registerDTO =
        MerchantDTO.builder()
            .name("Test Restaurant")
            .address("123 Main St")
            .phone("555-1234")
            .email("restaurant@example.com")
            .build();

    when(merchantMapper.toEntity(registerDTO)).thenReturn(testMerchant);
    when(merchantService.registerMerchant(any(Merchant.class))).thenReturn(testMerchant);
    when(merchantMapper.toDTO(testMerchant)).thenReturn(testMerchantDTO);

    mockMvc
        .perform(
            post("/api/merchants/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDTO)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Merchant registered successfully"))
        .andExpect(jsonPath("$.data.name").value("Test Restaurant"));

    verify(merchantService, times(1)).registerMerchant(any());
    verify(merchantMapper, times(1)).toDTO(testMerchant);
  }

  @Test
  @DisplayName("POST /api/merchants/register should return 400 on invalid input")
  void testRegisterMerchant_InvalidInput() throws Exception {
    MerchantDTO registerDTO =
        MerchantDTO.builder()
            .address("123 Main St")
            .phone("555-1234")
            .email("restaurant@example.com")
            .build();

    when(merchantMapper.toEntity(registerDTO)).thenReturn(testMerchant);
    when(merchantService.registerMerchant(any(Merchant.class)))
        .thenThrow(new IllegalArgumentException("Merchant name is required"));

    mockMvc
        .perform(
            post("/api/merchants/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDTO)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Merchant name is required"));
  }

  @Test
  @DisplayName("POST /api/merchants/register should handle unexpected exceptions")
  void testRegisterMerchant_UnexpectedException() throws Exception {
    MerchantDTO registerDTO =
        MerchantDTO.builder()
            .name("Test Restaurant")
            .address("123 Main St")
            .phone("555-1234")
            .email("restaurant@example.com")
            .build();

    when(merchantMapper.toEntity(registerDTO)).thenReturn(testMerchant);
    when(merchantService.registerMerchant(any(Merchant.class)))
        .thenThrow(new RuntimeException("Database error"));

    mockMvc
        .perform(
            post("/api/merchants/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDTO)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("An error occurred during registration"));
  }

  // ==================== VERIFY TESTS ====================

  @Test
  @DisplayName("PUT /api/merchants/{id}/verify should return 200 on successful verification")
  void testVerifyMerchant_Success() throws Exception {
    when(merchantService.verifyMerchant(1L, true)).thenReturn(testMerchant);
    when(merchantMapper.toDTO(testMerchant)).thenReturn(testMerchantDTO);

    mockMvc
        .perform(put("/api/merchants/1/verify?verified=true"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Merchant verified successfully"))
        .andExpect(jsonPath("$.data.name").value("Test Restaurant"));

    verify(merchantService, times(1)).verifyMerchant(1L, true);
  }

  @Test
  @DisplayName("PUT /api/merchants/{id}/verify should return 400 on invalid merchant ID")
  void testVerifyMerchant_InvalidId() throws Exception {
    when(merchantService.verifyMerchant(999L, true))
        .thenThrow(new IllegalArgumentException("Merchant not found"));

    mockMvc
        .perform(put("/api/merchants/999/verify?verified=true"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Merchant not found"));
  }

  @Test
  @DisplayName("PUT /api/merchants/{id}/verify should handle unexpected exceptions")
  void testVerifyMerchant_UnexpectedException() throws Exception {
    when(merchantService.verifyMerchant(1L, true))
        .thenThrow(new RuntimeException("Database error"));

    mockMvc
        .perform(put("/api/merchants/1/verify?verified=true"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("An error occurred during verification"));
  }

  // ==================== GET MERCHANT TESTS ====================

  @Test
  @DisplayName("GET /api/merchants/{id} should return 200 with merchant data")
  void testGetMerchantById_Success() throws Exception {
    when(merchantService.getMerchantById(1L)).thenReturn(testMerchant);
    when(merchantMapper.toDTO(testMerchant)).thenReturn(testMerchantDTO);

    mockMvc
        .perform(get("/api/merchants/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Merchant retrieved successfully"))
        .andExpect(jsonPath("$.data.name").value("Test Restaurant"));

    verify(merchantService, times(1)).getMerchantById(1L);
  }

  @Test
  @DisplayName("GET /api/merchants/{id} should return 400 when merchant not found")
  void testGetMerchantById_NotFound() throws Exception {
    when(merchantService.getMerchantById(999L))
        .thenThrow(new IllegalArgumentException("Merchant not found"));

    mockMvc
        .perform(get("/api/merchants/999"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Merchant not found"));
  }

  @Test
  @DisplayName("GET /api/merchants/{id} should return 400 when ID is invalid")
  void testGetMerchantById_InvalidId() throws Exception {
    mockMvc
        .perform(get("/api/merchants/0"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Invalid merchant ID"));

    verify(merchantService, never()).getMerchantById(any());
  }

  @Test
  @DisplayName("GET /api/merchants/{id} should handle unexpected exceptions")
  void testGetMerchantById_UnexpectedException() throws Exception {
    when(merchantService.getMerchantById(1L))
        .thenThrow(new RuntimeException("Database error"));

    mockMvc
        .perform(get("/api/merchants/1"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("An error occurred retrieving merchant"));
  }

  // ==================== GET ALL MERCHANTS TESTS ====================

  @Test
  @DisplayName("GET /api/merchants should return 200 with list of merchants")
  void testGetAllMerchants_Success() throws Exception {
    MerchantDTO dto2 =
        MerchantDTO.builder()
            .id(2L)
            .name("Second Restaurant")
            .build();
    Merchant merchant2 =
        Merchant.builder()
            .id(2L)
            .name("Second Restaurant")
            .build();

    when(merchantService.getAllMerchants()).thenReturn(List.of(testMerchant, merchant2));
    when(merchantMapper.toDTO(testMerchant)).thenReturn(testMerchantDTO);
    when(merchantMapper.toDTO(merchant2)).thenReturn(dto2);

    mockMvc
        .perform(get("/api/merchants"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Merchants retrieved successfully"))
        .andExpect(jsonPath("$.data[0].name").value("Test Restaurant"))
        .andExpect(jsonPath("$.data[1].name").value("Second Restaurant"));

    verify(merchantService, times(1)).getAllMerchants();
  }

  @Test
  @DisplayName("GET /api/merchants should return 200 with empty list")
  void testGetAllMerchants_Empty() throws Exception {
    when(merchantService.getAllMerchants()).thenReturn(List.of());

    mockMvc
        .perform(get("/api/merchants"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Merchants retrieved successfully"))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data.length()").value(0));
  }

  @Test
  @DisplayName("GET /api/merchants should handle unexpected exceptions")
  void testGetAllMerchants_UnexpectedException() throws Exception {
    when(merchantService.getAllMerchants())
        .thenThrow(new RuntimeException("Database error"));

    mockMvc
        .perform(get("/api/merchants"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("An error occurred retrieving merchants"));
  }

  // ==================== DELETE TESTS ====================

  @Test
  @DisplayName("DELETE /api/merchants/{id} should return 200 on successful deletion")
  void testDeleteMerchant_Success() throws Exception {
    when(merchantService.deleteMerchant(1L)).thenReturn(true);

    mockMvc
        .perform(delete("/api/merchants/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Merchant deleted successfully"));

    verify(merchantService, times(1)).deleteMerchant(1L);
  }

  @Test
  @DisplayName("DELETE /api/merchants/{id} should return 404 when merchant not found")
  void testDeleteMerchant_NotFound() throws Exception {
    when(merchantService.deleteMerchant(999L)).thenReturn(false);

    mockMvc.perform(delete("/api/merchants/999")).andExpect(status().isNotFound());

    verify(merchantService, times(1)).deleteMerchant(999L);
  }

  @Test
  @DisplayName("DELETE /api/merchants/{id} should return 400 when ID is invalid")
  void testDeleteMerchant_InvalidId() throws Exception {
    mockMvc
        .perform(delete("/api/merchants/0"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Invalid merchant ID"));

    verify(merchantService, never()).deleteMerchant(any());
  }

  @Test
  @DisplayName("DELETE /api/merchants/{id} should handle unexpected exceptions")
  void testDeleteMerchant_UnexpectedException() throws Exception {
    when(merchantService.deleteMerchant(1L)).thenThrow(new RuntimeException("Database error"));

    mockMvc
        .perform(delete("/api/merchants/1"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("An error occurred during deletion"));
  }

  // ==================== GET ORDERS BY MERCHANT TESTS ====================

  @Test
  @DisplayName("GET /api/merchants/{id}/orders should return 200 with paginated orders")
  void testGetOrdersByMerchant_Success() throws Exception {
    Pageable pageable = PageRequest.of(0, 10);
    Order order = Order.builder().id(1L).merchant(testMerchant).build();
    Page<Order> ordersPage = new PageImpl<>(List.of(order), pageable, 1);

    when(merchantService.getOrdersByMerchant(1L, pageable)).thenReturn(ordersPage);

    mockMvc
        .perform(get("/api/merchants/1/orders?page=0&size=10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Orders retrieved successfully"))
        .andExpect(jsonPath("$.data.totalElements").value(1));

    verify(merchantService, times(1)).getOrdersByMerchant(1L, pageable);
  }

  @Test
  @DisplayName("GET /api/merchants/{id}/orders should return 200 with empty page")
  void testGetOrdersByMerchant_EmptyPage() throws Exception {
    Pageable pageable = PageRequest.of(0, 10);
    Page<Order> emptyPage = new PageImpl<>(List.of(), pageable, 0);

    when(merchantService.getOrdersByMerchant(1L, pageable)).thenReturn(emptyPage);

    mockMvc
        .perform(get("/api/merchants/1/orders?page=0&size=10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.totalElements").value(0));
  }

  @Test
  @DisplayName("GET /api/merchants/{id}/orders should return 400 when merchant not found")
  void testGetOrdersByMerchant_MerchantNotFound() throws Exception {
    Pageable pageable = PageRequest.of(0, 10);
    when(merchantService.getOrdersByMerchant(999L, pageable))
        .thenThrow(new IllegalArgumentException("Merchant not found"));

    mockMvc
        .perform(get("/api/merchants/999/orders?page=0&size=10"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Merchant not found"));
  }

  @Test
  @DisplayName("GET /api/merchants/{id}/orders should return 400 when ID is invalid")
  void testGetOrdersByMerchant_InvalidId() throws Exception {
    mockMvc
        .perform(get("/api/merchants/0/orders?page=0&size=10"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Invalid merchant ID"));

    verify(merchantService, never()).getOrdersByMerchant(any(), any());
  }

  @Test
  @DisplayName("GET /api/merchants/{id}/orders should handle unexpected exceptions")
  void testGetOrdersByMerchant_UnexpectedException() throws Exception {
    Pageable pageable = PageRequest.of(0, 10);
    when(merchantService.getOrdersByMerchant(1L, pageable))
        .thenThrow(new RuntimeException("Database error"));

    mockMvc
        .perform(get("/api/merchants/1/orders?page=0&size=10"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("An error occurred retrieving orders"));
  }
}