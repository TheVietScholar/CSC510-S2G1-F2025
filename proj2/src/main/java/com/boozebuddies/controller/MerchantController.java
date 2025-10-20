package com.boozebuddies.controller;

import com.boozebuddies.dto.MerchantDTO;
import com.boozebuddies.entity.Merchant;
import com.boozebuddies.entity.Order;
import com.boozebuddies.mapper.MerchantMapper;
import com.boozebuddies.service.MerchantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/merchants")
public class MerchantController {

    private final MerchantService merchantService;
    private final MerchantMapper merchantMapper;

    @Autowired
    public MerchantController(MerchantService merchantService, MerchantMapper merchantMapper) {
        this.merchantService = merchantService;
        this.merchantMapper = merchantMapper;
    }

    // ==================== REGISTER ====================

    @PostMapping("/register")
    public ResponseEntity<MerchantDTO> registerMerchant(@RequestBody MerchantDTO merchantDTO) {
        Merchant merchant = merchantMapper.toEntity(merchantDTO);
        Merchant registered = merchantService.registerMerchant(merchant);
        return ResponseEntity.status(HttpStatus.CREATED).body(merchantMapper.toDTO(registered));
    }

    // ==================== VERIFY ====================

    @PutMapping("/{id}/verify")
    public ResponseEntity<MerchantDTO> verifyMerchant(@PathVariable Long id, @RequestParam boolean verified) {
        Merchant verifiedMerchant = merchantService.verifyMerchant(id, verified);
        if (verifiedMerchant == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(merchantMapper.toDTO(verifiedMerchant));
    }

    // ==================== RETRIEVE ====================

    @GetMapping("/{id}")
    public ResponseEntity<MerchantDTO> getMerchantById(@PathVariable Long id) {
        Merchant merchant = merchantService.getMerchantById(id);
        if (merchant == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(merchantMapper.toDTO(merchant));
    }

    @GetMapping
    public ResponseEntity<List<MerchantDTO>> getAllMerchants() {
        List<MerchantDTO> merchants = merchantService.getAllMerchants()
                .stream()
                .map(merchantMapper::toDTO)
                .toList();
        return ResponseEntity.ok(merchants);
    }

    // ==================== DELETE ====================

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMerchant(@PathVariable Long id) {
        boolean deleted = merchantService.deleteMerchant(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    // ==================== ORDERS BY MERCHANT ====================

    @GetMapping("/{id}/orders")
    public ResponseEntity<Page<Order>> getOrdersByMerchant(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orders = merchantService.getOrdersByMerchant(id, pageable);
        return ResponseEntity.ok(orders);
    }
}
