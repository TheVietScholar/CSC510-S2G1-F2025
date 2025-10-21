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
    public ResponseEntity<?> registerMerchant(@RequestBody MerchantDTO merchantDTO) {
      try {
        Merchant merchant = merchantMapper.toEntity(merchantDTO);
        Merchant registered = merchantService.registerMerchant(merchant);
        return ResponseEntity.status(HttpStatus.CREATED).body(merchantMapper.toDTO(registered));
      } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
      } catch (Exception e) {
        return ResponseEntity.badRequest().body("An error occurred during registration");
      }
    }

    // ==================== VERIFY ====================

    @PutMapping("/{id}/verify")
    public ResponseEntity<?> verifyMerchant(@PathVariable Long id, @RequestParam boolean verified) {
      try {
        Merchant verifiedMerchant = merchantService.verifyMerchant(id, verified);
        return ResponseEntity.ok(merchantMapper.toDTO(verifiedMerchant));
      } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
      } catch (Exception e) {
        return ResponseEntity.badRequest().body("An error occurred during verification");
      }
    }

    // ==================== RETRIEVE ====================

    @GetMapping("/{id}")
    public ResponseEntity<?> getMerchantById(@PathVariable Long id) {
      try {
        Merchant merchant = merchantService.getMerchantById(id);
        return ResponseEntity.ok(merchantMapper.toDTO(merchant));
      } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
      } catch (Exception e) {
        return ResponseEntity.badRequest().body("An error occurred retrieving merchant");
      }
    }

    @GetMapping
    public ResponseEntity<List<MerchantDTO>> getAllMerchants() {
      try {
        List<MerchantDTO> merchants = merchantService.getAllMerchants()
            .stream()
            .map(merchantMapper::toDTO)
            .toList();
        return ResponseEntity.ok(merchants);
      } catch (Exception e) {
        return ResponseEntity.badRequest().build();
      }
    }

    // ==================== DELETE ====================

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMerchant(@PathVariable Long id) {
      try {
        boolean deleted = merchantService.deleteMerchant(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
      } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
      } catch (Exception e) {
        return ResponseEntity.badRequest().body("An error occurred during deletion");
      }
    }

    // ==================== ORDERS BY MERCHANT ====================

    @GetMapping("/{id}/orders")
    public ResponseEntity<?> getOrdersByMerchant(
        @PathVariable Long id,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
      try {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orders = merchantService.getOrdersByMerchant(id, pageable);
        return ResponseEntity.ok(orders);
      } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
      } catch (Exception e) {
        return ResponseEntity.badRequest().body("An error occurred retrieving orders");
      }
    }
  }