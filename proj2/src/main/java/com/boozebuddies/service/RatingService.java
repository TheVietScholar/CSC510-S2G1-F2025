package com.boozebuddies.service;

import com.boozebuddies.model.User;
import com.boozebuddies.model.Driver;
import com.boozebuddies.model.Merchant;
import com.boozebuddies.model.Product;

public interface RatingService {

    /**
     * Allows a user to rate a product they purchased.
     *
     * @param user The user submitting the rating.
     * @param product The product being rated.
     * @param rating The rating value (e.g., 1-5 stars).
     * @param review Optional textual review.
     */
    void rateProduct(User user, Product product, int rating, String review);

    /**
     * Allows a user to rate a driver who delivered their order.
     *
     * @param user The user submitting the rating.
     * @param driver The driver being rated.
     * @param rating The rating value (e.g., 1-5 stars).
     * @param review Optional textual review.
     */
    void rateDriver(User user, Driver driver, int rating, String review);

    /**
     * Allows a user to rate a merchant from whom they purchased.
     *
     * @param user The user submitting the rating.
     * @param merchant The merchant being rated.
     * @param rating The rating value (e.g., 1-5 stars).
     * @param review Optional textual review.
     */
    void rateMerchant(User user, Merchant merchant, int rating, String review);

    /**
     * Retrieves the average rating for a product.
     *
     * @param product The product for which to calculate the average rating.
     * @return The average rating value.
     */
    double getAverageRatingForProduct(Product product);

    /**
     * Retrieves the average rating for a driver.
     *
     * @param driver The driver for which to calculate the average rating.
     * @return The average rating value.
     */
    double getAverageRatingForDriver(Driver driver);

    /**
     * Retrieves the average rating for a merchant.
     *
     * @param merchant The merchant for which to calculate the average rating.
     * @return The average rating value.
     */
    double getAverageRatingForMerchant(Merchant merchant);
}
