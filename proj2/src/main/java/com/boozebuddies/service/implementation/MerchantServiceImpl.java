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

/**
 * Implementation of the {@link MerchantService} interface.
 *
 * <p>This service handles business logic related to merchant management, including registration,
 * verification, retrieval, deletion, and distance-based sorting. It also provides access to
 * merchant order histories via pagination.
 *
 * <p>All data persistence is delegated to the {@link MerchantRepository} and {@link
 * OrderRepository}.
 */
@Service
public class MerchantServiceImpl implements MerchantService {

  private final MerchantRepository merchantRepository;
  private final OrderRepository orderRepository;

  /**
   * Constructs a new {@code MerchantServiceImpl} with the required repositories.
   *
   * @param merchantRepository the repository for merchant entities
   * @param orderRepository the repository for order entities
   */
  @Autowired
  public MerchantServiceImpl(
      MerchantRepository merchantRepository, OrderRepository orderRepository) {
    this.merchantRepository = merchantRepository;
    this.orderRepository = orderRepository;
  }

  /**
   * Registers a new merchant in the system.
   *
   * <p>Performs validation to ensure all required merchant details (name, email, and phone) are
   * provided before persisting. Newly registered merchants are inactive by default until verified.
   *
   * @param merchant the {@link Merchant} entity to register
   * @return the saved {@link Merchant} entity
   * @throws IllegalArgumentException if required fields are missing or merchant is {@code null}
   */
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

  /**
   * Verifies or un-verifies a merchant’s account.
   *
   * @param merchantId the ID of the merchant to verify
   * @param verified {@code true} to activate the merchant, {@code false} to deactivate
   * @return the updated {@link Merchant} entity
   * @throws IllegalArgumentException if the merchant ID is invalid or not found
   */
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

  /**
   * Retrieves a merchant by its ID.
   *
   * @param merchantId the merchant’s unique identifier
   * @return the corresponding {@link Merchant} entity
   * @throws IllegalArgumentException if the ID is invalid or the merchant is not found
   */
  @Override
  public Merchant getMerchantById(Long merchantId) {
    if (merchantId == null || merchantId <= 0) {
      throw new IllegalArgumentException("Invalid merchant ID");
    }

    return merchantRepository
        .findById(merchantId)
        .orElseThrow(() -> new IllegalArgumentException("Merchant not found"));
  }

  /**
   * Retrieves all merchants in the system.
   *
   * @return a list of all {@link Merchant} entities
   */
  @Override
  public List<Merchant> getAllMerchants() {
    return merchantRepository.findAll();
  }

  /**
   * Deletes a merchant by ID if it exists.
   *
   * @param merchantId the ID of the merchant to delete
   * @return {@code true} if the merchant was deleted, {@code false} if not found
   * @throws IllegalArgumentException if the ID is invalid
   */
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

  /**
   * Retrieves all orders associated with a given merchant using pagination.
   *
   * @param merchantId the merchant’s ID
   * @param pageable pagination and sorting parameters
   * @return a {@link Page} of {@link Order} objects associated with the merchant
   * @throws IllegalArgumentException if the ID or pageable is invalid, or merchant not found
   */
  @Override
  public Page<Order> getOrdersByMerchant(Long merchantId, Pageable pageable) {
    if (merchantId == null || merchantId <= 0) {
      throw new IllegalArgumentException("Invalid merchant ID");
    }

    if (pageable == null) {
      throw new IllegalArgumentException("Pageable cannot be null");
    }

    if (!merchantRepository.existsById(merchantId)) {
      throw new IllegalArgumentException("Merchant not found");
    }

    return orderRepository.findByMerchantId(merchantId, pageable);
  }

  /**
   * Retrieves a merchant by name.
   *
   * @param name the merchant’s name
   * @return the {@link Merchant} entity, or {@code null} if not found
   */
  @Override
  public Merchant getMerchantByName(String name) {
    return merchantRepository.findByName(name).orElse(null);
  }

  /**
   * Retrieves a list of merchants sorted by proximity to a given geographic location.
   *
   * <p>Uses the Haversine formula to calculate distances between coordinates. Only merchants with
   * valid latitude and longitude values are considered.
   *
   * @param latitude the latitude of the reference point
   * @param longitude the longitude of the reference point
   * @return a list of {@link Merchant} entities sorted by distance (ascending)
   * @throws IllegalArgumentException if latitude or longitude is missing
   */
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
   * Calculates the distance between two sets of latitude and longitude coordinates using the
   * Haversine formula.
   *
   * @param lat1 latitude of the first point
   * @param lon1 longitude of the first point
   * @param lat2 latitude of the second point
   * @param lon2 longitude of the second point
   * @return the distance in kilometers
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
