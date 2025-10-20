package com.boozebuddies.service.implementation;

import com.boozebuddies.entity.Merchant;
import com.boozebuddies.entity.Order;
import com.boozebuddies.repository.MerchantRepository;
import com.boozebuddies.repository.OrderRepository;
import com.boozebuddies.service.MerchantService;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class MerchantServiceImpl implements MerchantService {

  private final MerchantRepository merchantRepository;
  private final OrderRepository orderRepository;

  public MerchantServiceImpl(MerchantRepository merchantRepository, OrderRepository orderRepository) {
    this.merchantRepository = merchantRepository;
    this.orderRepository = orderRepository;
  }

  @Override
  public Merchant registerMerchant(Merchant merchant) {
    merchant.setActive(false); // new merchants start inactive
    return merchantRepository.save(merchant);
  }

  @Override
  public Merchant verifyMerchant(Long merchantId, boolean verified) {
    Optional<Merchant> opt = merchantRepository.findById(merchantId);
    if (opt.isEmpty()) return null;

    Merchant merchant = opt.get();
    merchant.setActive(verified);
    return merchantRepository.save(merchant);
  }

  @Override
  public Merchant getMerchantById(Long merchantId) {
    return merchantRepository.findById(merchantId).orElse(null);
  }

  @Override
  public List<Merchant> getAllMerchants() {
    return merchantRepository.findAll();
  }

  @Override
  public boolean deleteMerchant(Long merchantId) {
    if (!merchantRepository.existsById(merchantId)) {
      return false;
    }

    // Optional cleanup logic if you later add a delete query in OrderRepository
    // orderRepository.deleteAllByMerchantId(merchantId);

    merchantRepository.deleteById(merchantId);
    return true;
  }

  /** Retrieves orders for a merchant using pagination. */
  @Override
  public Page<Order> getOrdersByMerchant(Long merchantId, Pageable pageable) {
    return orderRepository.findByMerchantId(merchantId, pageable);
  }
}


