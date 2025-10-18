package com.boozebuddies.service.implementation;

import com.boozebuddies.entity.Driver;
import com.boozebuddies.entity.Merchant;
import com.boozebuddies.entity.Product;
import com.boozebuddies.entity.Rating;
import com.boozebuddies.entity.User;
import com.boozebuddies.service.RatingService;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class RatingServiceImpl implements RatingService {

    private final List<Rating> productRatings = new ArrayList<>();
    private final List<Rating> driverRatings = new ArrayList<>();
    private final List<Rating> merchantRatings = new ArrayList<>();

    /** Allows a user to rate a product they purchased. */
    @Override
    public Rating rateProduct(User user, Product product, int rating, String review) {
        if (user == null || product == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Invalid user, product, or rating value");
        }
        Rating r = Rating.builder()
                         .user(user)
                         .product(product)
                         .rating(rating)
                         .review(review)
                         .build();
        productRatings.add(r);
        System.out.println("[RATING] User " + user.getId() + " rated Product " +
                product.getId() + " with " + rating + " stars. Review: " + review);
        return r;
    }

    /** Allows a user to rate a driver. */
    @Override
    public Rating rateDriver(User user, Driver driver, int rating, String review) {
        if (user == null || driver == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Invalid user, driver, or rating value");
        }
        Rating r = Rating.builder()
                         .user(user)
                         .driver(driver)
                         .rating(rating)
                         .review(review)
                         .build();
        driverRatings.add(r);
        System.out.println("[RATING] User " + user.getId() + " rated Driver " +
                driver.getId() + " with " + rating + " stars. Review: " + review);
        return r;
    }

    /** Allows a user to rate a merchant. */
    @Override
    public Rating rateMerchant(User user, Merchant merchant, int rating, String review) {
        if (user == null || merchant == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Invalid user, merchant, or rating value");
        }
        Rating r = Rating.builder()
                         .user(user)
                         .merchant(merchant)
                         .rating(rating)
                         .review(review)
                         .build();
        merchantRatings.add(r);
        System.out.println("[RATING] User " + user.getId() + " rated Merchant " +
                merchant.getId() + " with " + rating + " stars. Review: " + review);
        return r;
    }

    /** Retrieves the average rating for a product. */
    @Override
    public double getAverageRatingForProduct(Product product) {
        List<Rating> ratings = productRatings.stream()
                .filter(r -> r.getProduct() != null && r.getProduct().equals(product))
                .collect(Collectors.toList());
        if (ratings.isEmpty()) return 0.0;
        return ratings.stream().mapToInt(Rating::getRating).average().orElse(0.0);
    }

    /** Retrieves the average rating for a driver. */
    @Override
    public double getAverageRatingForDriver(Driver driver) {
        List<Rating> ratings = driverRatings.stream()
                .filter(r -> r.getDriver() != null && r.getDriver().equals(driver))
                .collect(Collectors.toList());
        if (ratings.isEmpty()) return 0.0;
        return ratings.stream().mapToInt(Rating::getRating).average().orElse(0.0);
    }

    /** Retrieves the average rating for a merchant. */
    @Override
    public double getAverageRatingForMerchant(Merchant merchant) {
        List<Rating> ratings = merchantRatings.stream()
                .filter(r -> r.getMerchant() != null && r.getMerchant().equals(merchant))
                .collect(Collectors.toList());
        if (ratings.isEmpty()) return 0.0;
        return ratings.stream().mapToInt(Rating::getRating).average().orElse(0.0);
    }
}
