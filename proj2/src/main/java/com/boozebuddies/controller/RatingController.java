package com.boozebuddies.controller;

import com.boozebuddies.dto.RatingDTO;
import com.boozebuddies.entity.Driver;
import com.boozebuddies.entity.Merchant;
import com.boozebuddies.entity.Product;
import com.boozebuddies.entity.Rating;
import com.boozebuddies.entity.User;
import com.boozebuddies.mapper.RatingMapper;
import com.boozebuddies.service.RatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
public class RatingController {

  private final RatingService ratingService;
  private final RatingMapper ratingMapper;

  // -----------------------------
  // Rate a product
  // -----------------------------
  @PostMapping("/product")
  public ResponseEntity<RatingDTO> rateProduct(
      @RequestParam Long userId,
      @RequestParam Long productId,
      @RequestParam int rating,
      @RequestParam(required = false) String review) {

    User user = new User();
    user.setUserId(userId);

    Product product = new Product();
    product.setProductId(productId);

    Rating savedRating = ratingService.rateProduct(user, product, rating, review);
    return ResponseEntity.status(HttpStatus.CREATED).body(ratingMapper.toDTO(savedRating));
  }

  // -----------------------------
  // Rate a driver
  // -----------------------------
  @PostMapping("/driver")
  public ResponseEntity<RatingDTO> rateDriver(
      @RequestParam Long userId,
      @RequestParam Long driverId,
      @RequestParam int rating,
      @RequestParam(required = false) String review) {

    User user = new User();
    user.setUserId(userId);

    Driver driver = new Driver();
    driver.setDriverId(driverId);

    Rating savedRating = ratingService.rateDriver(user, driver, rating, review);
    return ResponseEntity.status(HttpStatus.CREATED).body(ratingMapper.toDTO(savedRating));
  }

  // -----------------------------
  // Rate a merchant
  // -----------------------------
  @PostMapping("/merchant")
  public ResponseEntity<RatingDTO> rateMerchant(
      @RequestParam Long userId,
      @RequestParam Long merchantId,
      @RequestParam int rating,
      @RequestParam(required = false) String review) {

    User user = new User();
    user.setUserId(userId);

    Merchant merchant = new Merchant();
    merchant.setMerchantId(merchantId);

    Rating savedRating = ratingService.rateMerchant(user, merchant, rating, review);
    return ResponseEntity.status(HttpStatus.CREATED).body(ratingMapper.toDTO(savedRating));
  }

  // -----------------------------
  // Get average rating for a product
  // -----------------------------
  @GetMapping("/product/{productId}/average")
  public ResponseEntity<Double> getAverageRatingForProduct(@PathVariable Long productId) {
    Product product = new Product();
    product.setProductId(productId);

    double average = ratingService.getAverageRatingForProduct(product);
    return ResponseEntity.ok(average);
  }

  // -----------------------------
  // Get average rating for a driver
  // -----------------------------
  @GetMapping("/driver/{driverId}/average")
  public ResponseEntity<Double> getAverageRatingForDriver(@PathVariable Long driverId) {
    Driver driver = new Driver();
    driver.setDriverId(driverId);

    double average = ratingService.getAverageRatingForDriver(driver);
    return ResponseEntity.ok(average);
  }

  // -----------------------------
  // Get average rating for a merchant
  // -----------------------------
  @GetMapping("/merchant/{merchantId}/average")
  public ResponseEntity<Double> getAverageRatingForMerchant(@PathVariable Long merchantId) {
    Merchant merchant = new Merchant();
    merchant.setMerchantId(merchantId);

    double average = ratingService.getAverageRatingForMerchant(merchant);
    return ResponseEntity.ok(average);
  }
}
