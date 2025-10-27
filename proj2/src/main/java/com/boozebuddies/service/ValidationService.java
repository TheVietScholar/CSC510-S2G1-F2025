package com.boozebuddies.service;

import com.boozebuddies.entity.Product;
import com.boozebuddies.entity.User;

public interface ValidationService {

  /**
   * Validates a user's email format.
   *
   * @param email The email to validate.
   * @return True if the email format is valid, false otherwise.
   */
  boolean validateEmail(String email);

  /**
   * Validates a user's password strength. E.g., minimum length, contains numbers and letters, etc.
   *
   * @param password The password to validate.
   * @return True if the password meets the strength requirements, false otherwise.
   */
  boolean validatePassword(String password);

  /**
   * Validates that a user is of legal drinking age.
   *
   * @param user The user to check.
   * @return True if the user is of legal age, false otherwise.
   */
  boolean validateAge(User user);

  /**
   * Validates that a product has valid data (name, type, price, stock).
   *
   * @param product The product to validate.
   * @return True if the product data is valid, false otherwise.
   */
  boolean validateProduct(Product product);

  /** Validates if a product is available for ordering. */
  public boolean validateProductAvailability(Product product);
}
