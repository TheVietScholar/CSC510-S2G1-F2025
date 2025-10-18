package com.boozebuddies.controller;

import com.boozebuddies.entity.User;
import com.boozebuddies.entity.Product;
import com.boozebuddies.entity.Driver;
import com.boozebuddies.entity.Merchant;
import com.boozebuddies.service.RatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ratings")
public class RatingController {

    @Autowired
    private RatingService ratingService;

    // -----------------------------
    // Rate a product
    // -----------------------------
    @PostMapping("/product")
    public void rateProduct(
            @RequestParam Long userId,
            @RequestParam Long productId,
            @RequestParam int rating,
            @RequestParam(required = false) String review) {

        User user = new User();
        user.setUserId(userId);

        Product product = new Product();
        product.setProductId(productId);

        ratingService.rateProduct(user, product, rating, review);
    }

    // -----------------------------
    // Rate a driver
    // -----------------------------
    @PostMapping("/driver")
    public void rateDriver(
            @RequestParam Long userId,
            @RequestParam Long driverId,
            @RequestParam int rating,
            @RequestParam(required = false) String review) {

        User user = new User();
        user.setUserId(userId);

        Driver driver = new Driver();
        driver.setDriverId(driverId);

        ratingService.rateDriver(user, driver, rating, review);
    }

    // -----------------------------
    // Rate a merchant
    // -----------------------------
    @PostMapping("/merchant")
    public void rateMerchant(
            @RequestParam Long userId,
            @RequestParam Long merchantId,
            @RequestParam int rating,
            @RequestParam(required = false) String review) {

        User user = new User();
        user.setUserId(userId);

        Merchant merchant = new Merchant();
        merchant.setMerchantId(merchantId);

        ratingService.rateMerchant(user, merchant, rating, review);
    }

    // -----------------------------
    // Get average rating for a product
    // -----------------------------
    @GetMapping("/product/{productId}/average")
    public double getAverageRatingForProduct(@PathVariable Long productId) {
        Product product = new Product();
        product.setProductId(productId);
        return ratingService.getAverageRatingForProduct(product);
    }

    // -----------------------------
    // Get average rating for a driver
    // -----------------------------
    @GetMapping("/driver/{driverId}/average")
    public double getAverageRatingForDriver(@PathVariable Long driverId) {
        Driver driver = new Driver();
        driver.setDriverId(driverId);
        return ratingService.getAverageRatingForDriver(driver);
    }

    // -----------------------------
    // Get average rating for a merchant
    // -----------------------------
    @GetMapping("/merchant/{merchantId}/average")
    public double getAverageRatingForMerchant(@PathVariable Long merchantId) {
        Merchant merchant = new Merchant();
        merchant.setMerchantId(merchantId);
        return ratingService.getAverageRatingForMerchant(merchant);
    }
}
