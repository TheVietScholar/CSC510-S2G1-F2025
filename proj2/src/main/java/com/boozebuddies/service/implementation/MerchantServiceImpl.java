package com.boozebuddies.service.implementation;

import com.boozebuddies.entity.Merchant;
import com.boozebuddies.entity.Order;
import com.boozebuddies.service.MerchantService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class MerchantServiceImpl implements MerchantService {

  private final List<Merchant> merchants = new ArrayList<>();
  private final List<Order> orders = new ArrayList<>(); // Simulated order storage
  private long nextMerchantId = 1;

  /** Registers a new merchant on the platform. */
  @Override
  public Merchant registerMerchant(Merchant merchant) {
    merchant.setId(nextMerchantId++);
    merchant.setActive(false);
    merchants.add(merchant);
    return merchant;
  }

  /** Verifies a merchant's credentials or business license. might need setVerified here */
  @Override
  public Merchant verifyMerchant(Long merchantId, boolean verified) {
    Merchant merchant = getMerchantById(merchantId);
    if (merchant != null) {
      merchant.setActive(verified);
    }
    return merchant;
  }

  /** Retrieves a merchant by their unique ID. */
  @Override
  public Merchant getMerchantById(Long merchantId) {
    Optional<Merchant> merchantOpt =
        merchants.stream().filter(m -> m.getId().equals(merchantId)).findFirst();
    return merchantOpt.orElse(null);
  }

  /** Retrieves all merchants currently registered on the platform. */
  @Override
  public List<Merchant> getAllMerchants() {
    return new ArrayList<>(merchants);
  }

  /** Retrieves all orders placed with a specific merchant. */
  @Override
  public List<Order> getOrdersByMerchant(Long merchantId) {
    return orders.stream()
        .filter(o -> o.getMerchant() != null && o.getMerchant().getId().equals(merchantId))
        .collect(Collectors.toList());
  }

  /** Deletes a merchant from the system. */
  @Override
  public boolean deleteMerchant(Long merchantId) {
    Merchant merchant = getMerchantById(merchantId);
    if (merchant != null) {
      merchants.remove(merchant);
      // Optionally, also remove their orders
      orders.removeIf(o -> o.getMerchant() != null && o.getMerchant().getId().equals(merchantId));
      return true;
    }
    return false;
  }
}
