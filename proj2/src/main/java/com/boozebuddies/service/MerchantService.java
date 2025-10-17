package com.boozebuddies.service;

import com.boozebuddies.model.Merchant;
import com.boozebuddies.model.Order;
import java.util.List;

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
     * @return The updated Merchant object reflecting the verification status.
     */
    Merchant verifyMerchant(Long merchantId, boolean verified);

    /**
     * Updates the merchant’s inventory information.
     *
     * @param merchantId The ID of the merchant.
     * @param inventoryDetails Details or reference to the updated inventory data.
     * @return The updated Merchant object.
     */
    Merchant updateInventory(Long merchantId, String inventoryDetails);

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
     * Retrieves all orders placed with a specific merchant.
     *
     * @param merchantId The ID of the merchant.
     * @return A list of orders associated with the merchant.
     */
    List<Order> getOrdersByMerchant(Long merchantId);

    /**
     * Deletes a merchant from the system.
     *
     * @param merchantId The ID of the merchant to remove.
     * @return True if the merchant was successfully deleted, false otherwise.
     */
    boolean deleteMerchant(Long merchantId);
}
