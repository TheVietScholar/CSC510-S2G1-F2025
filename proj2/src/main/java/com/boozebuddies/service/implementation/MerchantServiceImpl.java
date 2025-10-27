package com.boozebuddies.service.implementation;

import com.boozebuddies.entity.Merchant;
import com.boozebuddies.entity.Order;
import com.boozebuddies.repository.MerchantRepository;
import com.boozebuddies.repository.OrderRepository;
import com.boozebuddies.service.MerchantService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class MerchantServiceImpl implements MerchantService {

  private final MerchantRepository merchantRepository;
  private final OrderRepository orderRepository;

  @Autowired
  public MerchantServiceImpl(
      MerchantRepository merchantRepository, OrderRepository orderRepository) {
    this.merchantRepository = merchantRepository;
    this.orderRepository = orderRepository;
  }

  @Override
  public Merchant registerMerchant(Merchant merchant) {
    if (merchant == null) {
      throw new IllegalArgumentException("Merchant cannot be null");
    }

    if (merchant.getName() == null || merchant.getName().isEmpty()) {
      throw new IllegalArgumentException("Merchant name is required");
    }

    if (merchant.getEmail() == null || merchant.getEmail().isEmpty()) {
      throw new IllegalArgumentException("Merchant email is required");
    }

    if (merchant.getPhone() == null || merchant.getPhone().isEmpty()) {
      throw new IllegalArgumentException("Merchant phone is required");
    }

    merchant.setActive(false);
    return merchantRepository.save(merchant);
  }

  @Override
  public Merchant verifyMerchant(Long merchantId, boolean verified) {
    if (merchantId == null || merchantId <= 0) {
      throw new IllegalArgumentException("Invalid merchant ID");
    }

    Optional<Merchant> opt = merchantRepository.findById(merchantId);
    if (opt.isEmpty()) {
      throw new IllegalArgumentException("Merchant not found");
    }

    Merchant merchant = opt.get();
    merchant.setActive(verified);
    return merchantRepository.save(merchant);
  }

  @Override
  public Merchant getMerchantById(Long merchantId) {
    if (merchantId == null || merchantId <= 0) {
      throw new IllegalArgumentException("Invalid merchant ID");
    }

    return merchantRepository
        .findById(merchantId)
        .orElseThrow(() -> new IllegalArgumentException("Merchant not found"));
  }

  @Override
  public List<Merchant> getAllMerchants() {
    return merchantRepository.findAll();
  }

  @Override
  public boolean deleteMerchant(Long merchantId) {
    if (merchantId == null || merchantId <= 0) {
      throw new IllegalArgumentException("Invalid merchant ID");
    }

    if (!merchantRepository.existsById(merchantId)) {
      return false;
    }

    merchantRepository.deleteById(merchantId);
    return true;
  }

  @Override
  public Page<Order> getOrdersByMerchant(Long merchantId, Pageable pageable) {
    if (merchantId == null || merchantId <= 0) {
      throw new IllegalArgumentException("Invalid merchant ID");
    }

    if (pageable == null) {
      throw new IllegalArgumentException("Pageable cannot be null");
    }

    // Verify merchant exists
    if (!merchantRepository.existsById(merchantId)) {
      throw new IllegalArgumentException("Merchant not found");
    }

    return orderRepository.findByMerchantId(merchantId, pageable);
  }
}
