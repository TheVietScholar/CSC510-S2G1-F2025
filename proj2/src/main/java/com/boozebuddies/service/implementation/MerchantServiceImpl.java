package com.boozebuddies.service.implementation;

import com.boozebuddies.entity.Merchant;
import com.boozebuddies.entity.Order;
import com.boozebuddies.repository.MerchantRepository;
import com.boozebuddies.repository.OrderRepository;
import com.boozebuddies.service.MerchantService;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
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

  @Override
  public Merchant getMerchantByName(String name) {
    return merchantRepository.findByName(name).orElse(null);
  }

  @Override
  public List<Merchant> getMerchantsSortedByDistance(Double latitude, Double longitude) {
    if (latitude == null || longitude == null) {
      throw new IllegalArgumentException("Latitude and longitude are required");
    }

    List<Merchant> allMerchants = getAllMerchants();

    return allMerchants.stream()
        .filter(m -> m.getLatitude() != null && m.getLongitude() != null)
        .sorted(
            (m1, m2) -> {
              double dist1 =
                  calculateDistance(latitude, longitude, m1.getLatitude(), m1.getLongitude());
              double dist2 =
                  calculateDistance(latitude, longitude, m2.getLatitude(), m2.getLongitude());
              return Double.compare(dist1, dist2);
            })
        .collect(Collectors.toList());
  }

  /**
   * Calculate distance between two points using Haversine formula. Returns distance in kilometers.
   */
  private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
    final int EARTH_RADIUS_KM = 6371;

    double dLat = Math.toRadians(lat2 - lat1);
    double dLon = Math.toRadians(lon2 - lon1);

    double a =
        Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);

    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    return EARTH_RADIUS_KM * c;
  }
}
