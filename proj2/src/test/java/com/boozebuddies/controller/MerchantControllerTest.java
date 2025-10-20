package com.boozebuddies.controller;

import com.boozebuddies.dto.MerchantDTO;
import com.boozebuddies.entity.Merchant;
import com.boozebuddies.entity.Order;
import com.boozebuddies.mapper.MerchantMapper;
import com.boozebuddies.service.MerchantService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MerchantController.class)
@DisplayName("MerchantController Unit Tests")
class MerchantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MerchantService merchantService;

    @MockBean
    private MerchantMapper merchantMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private Merchant merchant;
    private MerchantDTO merchantDTO;

    @BeforeEach
    void setUp() {
        merchant = new Merchant();
        merchant.setId(1L);
        merchant.setName("Beer Co");
        merchant.setEmail("beer@brew.com");
        merchant.setActive(true);

        merchantDTO = MerchantDTO.builder()
                .id(1L)
                .name("Beer Co")
                .email("beer@brew.com")
                .isActive(true)
                .build();
    }

    // ==================== REGISTER ====================

    @Test
    @DisplayName("Should register a new merchant")
    void testRegisterMerchant() throws Exception {
        when(merchantMapper.toEntity(any(MerchantDTO.class))).thenReturn(merchant);
        when(merchantService.registerMerchant(any(Merchant.class))).thenReturn(merchant);
        when(merchantMapper.toDTO(any(Merchant.class))).thenReturn(merchantDTO);

        mockMvc.perform(post("/api/merchants/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(merchantDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Beer Co"));

        verify(merchantService, times(1)).registerMerchant(any(Merchant.class));
    }

    // ==================== VERIFY ====================

    @Test
    @DisplayName("Should verify merchant and return updated DTO")
    void testVerifyMerchant() throws Exception {
        when(merchantService.verifyMerchant(1L, true)).thenReturn(merchant);
        when(merchantMapper.toDTO(any(Merchant.class))).thenReturn(merchantDTO);

        mockMvc.perform(put("/api/merchants/1/verify?verified=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));

        verify(merchantService).verifyMerchant(1L, true);
    }

    @Test
    @DisplayName("Should return 404 when verifying non-existent merchant")
    void testVerifyMerchantNotFound() throws Exception {
        when(merchantService.verifyMerchant(999L, true)).thenReturn(null);

        mockMvc.perform(put("/api/merchants/999/verify?verified=true"))
                .andExpect(status().isNotFound());
    }

    // ==================== RETRIEVE ====================

    @Test
    @DisplayName("Should get merchant by ID")
    void testGetMerchantById() throws Exception {
        when(merchantService.getMerchantById(1L)).thenReturn(merchant);
        when(merchantMapper.toDTO(any(Merchant.class))).thenReturn(merchantDTO);

        mockMvc.perform(get("/api/merchants/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Beer Co"));
    }

    @Test
    @DisplayName("Should return 404 when merchant not found")
    void testGetMerchantByIdNotFound() throws Exception {
        when(merchantService.getMerchantById(1L)).thenReturn(null);

        mockMvc.perform(get("/api/merchants/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should get all merchants")
    void testGetAllMerchants() throws Exception {
        when(merchantService.getAllMerchants()).thenReturn(List.of(merchant));
        when(merchantMapper.toDTO(any(Merchant.class))).thenReturn(merchantDTO);

        mockMvc.perform(get("/api/merchants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Beer Co"));
    }

    // ==================== DELETE ====================

    @Test
    @DisplayName("Should delete merchant by ID")
    void testDeleteMerchant() throws Exception {
        when(merchantService.deleteMerchant(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/merchants/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should return 404 when deleting non-existent merchant")
    void testDeleteMerchantNotFound() throws Exception {
        when(merchantService.deleteMerchant(1L)).thenReturn(false);

        mockMvc.perform(delete("/api/merchants/1"))
                .andExpect(status().isNotFound());
    }

    // ==================== ORDERS BY MERCHANT ====================

    @Test
    @DisplayName("Should get orders by merchant ID with pagination")
    void testGetOrdersByMerchant() throws Exception {
        Order order1 = new Order();
        Order order2 = new Order();
        Page<Order> orderPage = new PageImpl<>(List.of(order1, order2));

        when(merchantService.getOrdersByMerchant(eq(1L), any(Pageable.class))).thenReturn(orderPage);

        mockMvc.perform(get("/api/merchants/1/orders?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(merchantService).getOrdersByMerchant(eq(1L), any(Pageable.class));
    }

    @Test
    @DisplayName("Should return empty page when merchant has no orders")
    void testGetOrdersByMerchantNoOrders() throws Exception {
        Page<Order> emptyPage = Page.empty();
        when(merchantService.getOrdersByMerchant(eq(1L), any(Pageable.class))).thenReturn(emptyPage);

        mockMvc.perform(get("/api/merchants/1/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }
}
