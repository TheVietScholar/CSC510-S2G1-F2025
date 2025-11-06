package com.boozebuddies.service.implementation;

import com.boozebuddies.entity.Product;
import com.boozebuddies.entity.User;
import com.boozebuddies.service.ValidationService;
import java.math.BigDecimal;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

/**
 * Implementation of the {@link ValidationService} that provides input and business validation
 * methods for users and products within the BoozeBuddies application.
 *
 * <p>This service includes validation for user registration fields such as email, password, and
 * age, as well as product-related fields like pricing, category, and availability.
 */
@Service
public class ValidationServiceImpl implements ValidationService {

  /** Regular expression pattern for validating standard email formats. */
  private static final Pattern EMAIL_PATTERN =
      Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

  /**
   * Regular expression pattern for validating strong passwords (minimum 8 characters, letters, and
   * numbers).
   */
  private static final Pattern PASSWORD_PATTERN =
      Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$");

  /**
   * Validates that an email string is in a proper format.
   *
   * @param email The email address to validate.
   * @return {@code true} if the email matches the expected pattern; {@code false} otherwise.
   */
  @Override
  public boolean validateEmail(String email) {
    if (email == null || email.isEmpty()) {
      return false;
    }
    return EMAIL_PATTERN.matcher(email).matches();
  }

  /**
   * Validates the strength of a user's password.
   *
   * <p>Passwords must be at least 8 characters long and contain both letters and digits.
   *
   * @param password The password to validate.
   * @return {@code true} if the password meets the required criteria; {@code false} otherwise.
   */
  @Override
  public boolean validatePassword(String password) {
    if (password == null || password.isEmpty()) {
      return false;
    }
    return PASSWORD_PATTERN.matcher(password).matches();
  }

  /**
   * Validates that a user meets the legal drinking age requirement (21 years or older).
   *
   * @param user The {@link User} whose age will be validated.
   * @return {@code true} if the user is 21 or older; {@code false} if underage or data is missing.
   */
  @Override
  public boolean validateAge(User user) {
    if (user == null || user.getDateOfBirth() == null) {
      return false;
    }
    int legalAge = 21;
    java.time.LocalDate today = java.time.LocalDate.now();
    java.time.Period age = java.time.Period.between(user.getDateOfBirth(), today);
    return age.getYears() >= legalAge;
  }

  /**
   * Validates that a product contains valid and complete information.
   *
   * <p>Checks that the product name is provided, price is non-negative, and category is defined.
   *
   * @param product The {@link Product} to validate.
   * @return {@code true} if the product data is valid; {@code false} otherwise.
   */
  @Override
  public boolean validateProduct(Product product) {
    if (product == null) {
      return false;
    }
    if (product.getName() == null || product.getName().isEmpty()) {
      return false;
    }
    if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) < 0) {
      return false;
    }
    if (product.getCategory() == null || product.getCategory().getName().isEmpty()) {
      return false;
    }
    return true;
  }

  /**
   * Validates whether a product is currently available for ordering.
   *
   * @param product The {@link Product} to check.
   * @return {@code true} if the product is available; {@code false} otherwise.
   */
  @Override
  public boolean validateProductAvailability(Product product) {
    if (product == null) {
      return false;
    }
    return product.isAvailable();
  }
}
