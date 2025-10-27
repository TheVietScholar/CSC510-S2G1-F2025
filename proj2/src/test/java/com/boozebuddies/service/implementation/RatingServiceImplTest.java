package com.boozebuddies.service.implementation;

import static org.junit.jupiter.api.Assertions.*;

import com.boozebuddies.entity.Driver;
import com.boozebuddies.entity.Merchant;
import com.boozebuddies.entity.Product;
import com.boozebuddies.entity.Rating;
import com.boozebuddies.entity.User;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RatingServiceImplTest {

  private RatingServiceImpl ratingService;
  private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

  @BeforeEach
  void setUp() {
    ratingService = new RatingServiceImpl();
    System.setOut(new PrintStream(outputStream));
  }

  @Test
  void testRateProduct_ValidInput_CreatesRatingAndPrintsMessage() {
    User user = User.builder().id(1L).build();
    Product product = Product.builder().id(10L).build();

    Rating rating = ratingService.rateProduct(user, product, 5, "Great product!");

    assertNotNull(rating);
    assertEquals(user, rating.getUser());
    assertEquals(product, rating.getProduct());
    assertEquals(5, rating.getRating());
    assertEquals("Great product!", rating.getReview());

    String output = outputStream.toString();
    assertTrue(output.contains("[RATING] User 1 rated Product 10 with 5 stars"));
  }

  @Test
  void testRateProduct_NullUser_ThrowsException() {
    Product product = Product.builder().id(10L).build();

    assertThrows(
        IllegalArgumentException.class,
        () -> {
          ratingService.rateProduct(null, product, 5, "Great product!");
        });
  }

  @Test
  void testRateProduct_NullProduct_ThrowsException() {
    User user = User.builder().id(1L).build();

    assertThrows(
        IllegalArgumentException.class,
        () -> {
          ratingService.rateProduct(user, null, 5, "Great product!");
        });
  }

  @Test
  void testRateProduct_RatingBelow1_ThrowsException() {
    User user = User.builder().id(1L).build();
    Product product = Product.builder().id(10L).build();

    assertThrows(
        IllegalArgumentException.class,
        () -> {
          ratingService.rateProduct(user, product, 0, "Bad product");
        });
  }

  @Test
  void testRateProduct_RatingAbove5_ThrowsException() {
    User user = User.builder().id(1L).build();
    Product product = Product.builder().id(10L).build();

    assertThrows(
        IllegalArgumentException.class,
        () -> {
          ratingService.rateProduct(user, product, 6, "Amazing product");
        });
  }

  @Test
  void testRateDriver_ValidInput_CreatesRatingAndPrintsMessage() {
    User user = User.builder().id(1L).build();
    Driver driver = Driver.builder().id(20L).build();

    Rating rating = ratingService.rateDriver(user, driver, 4, "Good driver");

    assertNotNull(rating);
    assertEquals(user, rating.getUser());
    assertEquals(driver, rating.getDriver());
    assertEquals(4, rating.getRating());
    assertEquals("Good driver", rating.getReview());

    String output = outputStream.toString();
    assertTrue(output.contains("[RATING] User 1 rated Driver 20 with 4 stars"));
  }

  @Test
  void testRateDriver_NullDriver_ThrowsException() {
    User user = User.builder().id(1L).build();

    assertThrows(
        IllegalArgumentException.class,
        () -> {
          ratingService.rateDriver(user, null, 5, "Great driver!");
        });
  }

  @Test
  void testRateMerchant_ValidInput_CreatesRatingAndPrintsMessage() {
    User user = User.builder().id(1L).build();
    Merchant merchant = Merchant.builder().id(30L).build();

    Rating rating = ratingService.rateMerchant(user, merchant, 3, "Average service");

    assertNotNull(rating);
    assertEquals(user, rating.getUser());
    assertEquals(merchant, rating.getMerchant());
    assertEquals(3, rating.getRating());
    assertEquals("Average service", rating.getReview());

    String output = outputStream.toString();
    assertTrue(output.contains("[RATING] User 1 rated Merchant 30 with 3 stars"));
  }

  @Test
  void testRateMerchant_NullMerchant_ThrowsException() {
    User user = User.builder().id(1L).build();

    assertThrows(
        IllegalArgumentException.class,
        () -> {
          ratingService.rateMerchant(user, null, 5, "Great merchant!");
        });
  }

  @Test
  void testGetAverageRatingForProduct_NoRatings_ReturnsZero() {
    Product product = Product.builder().id(10L).build();

    double average = ratingService.getAverageRatingForProduct(product);

    assertEquals(0.0, average);
  }

  @Test
  void testGetAverageRatingForProduct_WithRatings_ReturnsCorrectAverage() {
    User user1 = User.builder().id(1L).build();
    User user2 = User.builder().id(2L).build();
    Product product = Product.builder().id(10L).build();

    ratingService.rateProduct(user1, product, 5, "Excellent");
    ratingService.rateProduct(user2, product, 3, "Average");

    double average = ratingService.getAverageRatingForProduct(product);

    assertEquals(4.0, average);
  }

  @Test
  void testGetAverageRatingForProduct_MultipleProducts_ReturnsCorrectAverageForSpecificProduct() {
    User user1 = User.builder().id(1L).build();
    User user2 = User.builder().id(2L).build();
    Product product1 = Product.builder().id(10L).build();
    Product product2 = Product.builder().id(20L).build();

    ratingService.rateProduct(user1, product1, 5, "Excellent");
    ratingService.rateProduct(user2, product1, 3, "Average");
    ratingService.rateProduct(user1, product2, 1, "Terrible");

    double average1 = ratingService.getAverageRatingForProduct(product1);
    double average2 = ratingService.getAverageRatingForProduct(product2);

    assertEquals(4.0, average1);
    assertEquals(1.0, average2);
  }

  @Test
  void testGetAverageRatingForDriver_NoRatings_ReturnsZero() {
    Driver driver = Driver.builder().id(20L).build();

    double average = ratingService.getAverageRatingForDriver(driver);

    assertEquals(0.0, average);
  }

  @Test
  void testGetAverageRatingForDriver_WithRatings_ReturnsCorrectAverage() {
    User user1 = User.builder().id(1L).build();
    User user2 = User.builder().id(2L).build();
    Driver driver = Driver.builder().id(20L).build();

    ratingService.rateDriver(user1, driver, 5, "Fast delivery");
    ratingService.rateDriver(user2, driver, 4, "Good service");

    double average = ratingService.getAverageRatingForDriver(driver);

    assertEquals(4.5, average);
  }

  @Test
  void testGetAverageRatingForMerchant_NoRatings_ReturnsZero() {
    Merchant merchant = Merchant.builder().id(30L).build();

    double average = ratingService.getAverageRatingForMerchant(merchant);

    assertEquals(0.0, average);
  }

  @Test
  void testGetAverageRatingForMerchant_WithRatings_ReturnsCorrectAverage() {
    User user1 = User.builder().id(1L).build();
    User user2 = User.builder().id(2L).build();
    User user3 = User.builder().id(3L).build();
    Merchant merchant = Merchant.builder().id(30L).build();

    ratingService.rateMerchant(user1, merchant, 5, "Great food");
    ratingService.rateMerchant(user2, merchant, 4, "Good service");
    ratingService.rateMerchant(user3, merchant, 2, "Slow delivery");

    double average = ratingService.getAverageRatingForMerchant(merchant);

    assertEquals(3.666, average, 0.001);
  }

  @Test
  void testRatingSeparation_DifferentTypesDontInterfere() {
    User user = User.builder().id(1L).build();
    Product product = Product.builder().id(10L).build();
    Driver driver = Driver.builder().id(20L).build();
    Merchant merchant = Merchant.builder().id(30L).build();

    ratingService.rateProduct(user, product, 5, "Great product");
    ratingService.rateDriver(user, driver, 4, "Good driver");
    ratingService.rateMerchant(user, merchant, 3, "Okay merchant");

    double productAvg = ratingService.getAverageRatingForProduct(product);
    double driverAvg = ratingService.getAverageRatingForDriver(driver);
    double merchantAvg = ratingService.getAverageRatingForMerchant(merchant);

    assertEquals(5.0, productAvg);
    assertEquals(4.0, driverAvg);
    assertEquals(3.0, merchantAvg);
  }
}
