package com.boozebuddies.service;

import com.boozebuddies.entity.Merchant;
import com.boozebuddies.entity.Order;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MerchantService {

  /**
   * Registers a new merchant on the platform.
   *
   * @param merchant The merchant to register.
   * @return The registered Merchant object with an assigned ID.
   */
  Merchant registerMerchant(Merchant merchant);

  /**
   * Verifies a merchant's credentials or business license.
   *
   * @param merchantId The ID of the merchant to verify.
   * @param verified True if the merchant is verified, false otherwise.
   * @return The updated Merchant object reflecting the verification status, or null if not found.
   */
  Merchant verifyMerchant(Long merchantId, boolean verified);

  /**
   * Retrieves a merchant by their unique ID.
   *
   * @param merchantId The ID of the merchant.
   * @return The corresponding Merchant object, or null if not found.
   */
  Merchant getMerchantById(Long merchantId);

  /**
   * Retrieves all merchants currently registered on the platform.
   *
   * @return A list of all merchants.
   */
  List<Merchant> getAllMerchants();

  /**
   * Retrieves all orders placed with a specific merchant, paginated.
   *
   * @param merchantId The ID of the merchant.
   * @param pageable The pagination configuration (page number, size, sort).
   * @return A paginated list (Page) of orders associated with the merchant.
   */
  Page<Order> getOrdersByMerchant(Long merchantId, Pageable pageable);

  /**
   * Deletes a merchant from the system.
   *
   * @param merchantId The ID of the merchant to remove.
   * @return True if the merchant was successfully deleted, false otherwise.
   */
  boolean deleteMerchant(Long merchantId);

  /**
   * Find merchant by name.
   *
   * @param name The name of the merchant.
   * @return The corresponding Merchant object, or null if not found.
   */
  Merchant getMerchantByName(String name);

  /** Get all merchants sorted by distance from a location. */
  List<Merchant> getMerchantsSortedByDistance(Double latitude, Double longitude);
}
