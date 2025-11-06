package com.boozebuddies.controller;

import com.boozebuddies.dto.RatingDTO;
import com.boozebuddies.entity.Driver;
import com.boozebuddies.entity.Merchant;
import com.boozebuddies.entity.Product;
import com.boozebuddies.entity.Rating;
import com.boozebuddies.entity.User;
import com.boozebuddies.mapper.RatingMapper;
import com.boozebuddies.model.Role;
import com.boozebuddies.security.annotation.RoleAnnotations.*;
import com.boozebuddies.service.PermissionService;
import com.boozebuddies.service.RatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/** REST controller for managing ratings for products, drivers, and merchants. */
@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
public class RatingController {

  private final RatingService ratingService;
  private final RatingMapper ratingMapper;
  private final PermissionService permissionService;

  // -----------------------------
  // Rate a product
  // -----------------------------

  /**
   * Creates a rating for a product. Users can rate products, or admins can rate on behalf of users.
   *
   * @param userId the user ID
   * @param productId the product ID
   * @param rating the rating value
   * @param review optional review text
   * @param authentication the authentication object
   * @return the created rating
   */
  @PostMapping("/product")
  @IsAuthenticated
  public ResponseEntity<RatingDTO> rateProduct(
      @RequestParam Long userId,
      @RequestParam Long productId,
      @RequestParam int rating,
      @RequestParam(required = false) String review,
      Authentication authentication) {

    // Only allow the authenticated user (or admin) to rate on behalf of the userId
    if (!permissionService.isSelf(authentication, userId)
        && !permissionService.hasRole(authentication, Role.ADMIN)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    User user = new User();
    user.setId(userId);

    Product product = new Product();
    product.setId(productId);

    Rating savedRating = ratingService.rateProduct(user, product, rating, review);
    return ResponseEntity.status(HttpStatus.CREATED).body(ratingMapper.toDTO(savedRating));
  }

  // -----------------------------
  // Rate a driver
  // -----------------------------

  /**
   * Creates a rating for a driver. Users can rate drivers, or admins can rate on behalf of users.
   *
   * @param userId the user ID
   * @param driverId the driver ID
   * @param rating the rating value
   * @param review optional review text
   * @param authentication the authentication object
   * @return the created rating
   */
  @PostMapping("/driver")
  @IsAuthenticated
  public ResponseEntity<RatingDTO> rateDriver(
      @RequestParam Long userId,
      @RequestParam Long driverId,
      @RequestParam int rating,
      @RequestParam(required = false) String review,
      Authentication authentication) {

    if (!permissionService.isSelf(authentication, userId)
        && !permissionService.hasRole(authentication, Role.ADMIN)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    User user = new User();
    user.setId(userId);

    Driver driver = new Driver();
    driver.setId(driverId);

    Rating savedRating = ratingService.rateDriver(user, driver, rating, review);
    return ResponseEntity.status(HttpStatus.CREATED).body(ratingMapper.toDTO(savedRating));
  }

  // -----------------------------
  // Rate a merchant
  // -----------------------------

  /**
   * Creates a rating for a merchant. Users can rate merchants, or admins can rate on behalf of
   * users.
   *
   * @param userId the user ID
   * @param merchantId the merchant ID
   * @param rating the rating value
   * @param review optional review text
   * @param authentication the authentication object
   * @return the created rating
   */
  @PostMapping("/merchant")
  @IsAuthenticated
  public ResponseEntity<RatingDTO> rateMerchant(
      @RequestParam Long userId,
      @RequestParam Long merchantId,
      @RequestParam int rating,
      @RequestParam(required = false) String review,
      Authentication authentication) {

    if (!permissionService.isSelf(authentication, userId)
        && !permissionService.hasRole(authentication, Role.ADMIN)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    User user = new User();
    user.setId(userId);

    Merchant merchant = new Merchant();
    merchant.setId(merchantId);

    Rating savedRating = ratingService.rateMerchant(user, merchant, rating, review);
    return ResponseEntity.status(HttpStatus.CREATED).body(ratingMapper.toDTO(savedRating));
  }

  // -----------------------------
  // Get average rating for a product
  // -----------------------------

  /**
   * Retrieves the average rating for a product.
   *
   * @param productId the product ID
   * @return the average rating
   */
  @GetMapping("/product/{productId}/average")
  public ResponseEntity<Double> getAverageRatingForProduct(@PathVariable Long productId) {
    Product product = new Product();
    product.setId(productId);

    double average = ratingService.getAverageRatingForProduct(product);
    return ResponseEntity.ok(average);
  }

  // -----------------------------
  // Get average rating for a driver
  // -----------------------------

  /**
   * Retrieves the average rating for a driver.
   *
   * @param driverId the driver ID
   * @return the average rating
   */
  @GetMapping("/driver/{driverId}/average")
  public ResponseEntity<Double> getAverageRatingForDriver(@PathVariable Long driverId) {
    Driver driver = new Driver();
    driver.setId(driverId);

    double average = ratingService.getAverageRatingForDriver(driver);
    return ResponseEntity.ok(average);
  }

  // -----------------------------
  // Get average rating for a merchant
  // -----------------------------

  /**
   * Retrieves the average rating for a merchant.
   *
   * @param merchantId the merchant ID
   * @return the average rating
   */
  @GetMapping("/merchant/{merchantId}/average")
  public ResponseEntity<Double> getAverageRatingForMerchant(@PathVariable Long merchantId) {
    Merchant merchant = new Merchant();
    merchant.setId(merchantId);

    double average = ratingService.getAverageRatingForMerchant(merchant);
    return ResponseEntity.ok(average);
  }
}
