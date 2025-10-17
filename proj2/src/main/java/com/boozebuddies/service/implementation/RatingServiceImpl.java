package com.boozebuddies.service.implementation;

import com.boozebuddies.entity.User;
import com.boozebuddies.entity.Driver;
import com.boozebuddies.entity.Merchant;
import com.boozebuddies.entity.Product;
import com.boozebuddies.entity.Rating;
import com.boozebuddies.service.RatingService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RatingServiceImpl implements RatingService {

    private final List<Rating> productRatings = new ArrayList<>();
    private final List<Rating> driverRatings = new ArrayList<>();
    private final List<Rating> merchantRatings = new ArrayList<>();

    /**
     * Allows a user to rate a product they purchased.
     */
    @Override
    public void rateProduct(User user, Product product, int rating, String review) {
        if (user == null || product == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Invalid user, product, or rating value");
        }
        Rating r = new Rating(user, product, rating, review);
        productRatings.add(r);
        System.out.println("[RATING] User " + user.getUserId() +
                " rated Product " + product.getProductId() +
                " with " + rating + " stars. Review: " + review);
    }

    /**
     * Allows a user to rate a driver.
     */
    @Override
    public void rateDriver(User user, Driver driver, int rating, String review) {
        if (user == null || driver == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Invalid user, driver, or rating value");
        }
        Rating r = new Rating(user, driver, rating, review);
        driverRatings.add(r);
        System.out.println("[RATING] User " + user.getUserId() +
                " rated Driver " + driver.getDriverId() +
                " with " + rating + " stars. Review: " + review);
    }

    /**
     * Allows a user to rate a merchant.
     */
    @Override
    public void rateMerchant(User user, Merchant merchant, int rating, String review) {
        if (user == null || merchant == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Invalid user, merchant, or rating value");
        }
        Rating r = new Rating(user, merchant, rating, review);
        merchantRatings.add(r);
        System.out.println("[RATING] User " + user.getUserId() +
                " rated Merchant " + merchant.getMerchantId() +
                " with " + rating + " stars. Review: " + review);
    }

    /**
     * Retrieves the average rating for a product.
     */
    @Override
    public double getAverageRatingForProduct(Product product) {
        List<Rating> ratings = productRatings.stream()
                .filter(r -> r.getProduct() != null && r.getProduct().equals(product))
                .collect(Collectors.toList());
        if (ratings.isEmpty()) return 0.0;
        return ratings.stream().mapToInt(Rating::getRating).average().orElse(0.0);
    }

    /**
     * Retrieves the average rating for a driver.
     */
    @Override
    public double getAverageRatingForDriver(Driver driver) {
        List<Rating> ratings = driverRatings.stream()
                .filter(r -> r.getDriver() != null && r.getDriver().equals(driver))
                .collect(Collectors.toList());
        if (ratings.isEmpty()) return 0.0;
        return ratings.stream().mapToInt(Rating::getRating).average().orElse(0.0);
    }

    /**
     * Retrieves the average rating for a merchant.
     */
    @Override
    public double getAverageRatingForMerchant(Merchant merchant) {
        List<Rating> ratings = merchantRatings.stream()
                .filter(r -> r.getMerchant() != null && r.getMerchant().equals(merchant))
                .collect(Collectors.toList());
        if (ratings.isEmpty()) return 0.0;
        return ratings.stream().mapToInt(Rating::getRating).average().orElse(0.0);
    }
}
