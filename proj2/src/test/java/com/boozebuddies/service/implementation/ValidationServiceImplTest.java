package com.boozebuddies.service.implementation;

import static org.junit.jupiter.api.Assertions.*;

import com.boozebuddies.entity.Category;
import com.boozebuddies.entity.Product;
import com.boozebuddies.entity.User;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ValidationServiceImplTest {

  private ValidationServiceImpl validationService;

  @BeforeEach
  void setUp() {
    validationService = new ValidationServiceImpl();
  }

  @Test
  void testValidateEmail_ValidEmail_ReturnsTrue() {
    assertTrue(validationService.validateEmail("test@example.com"));
    assertTrue(validationService.validateEmail("user.name@domain.co.uk"));
    assertTrue(validationService.validateEmail("user+tag@example.org"));
  }

  @Test
  void testValidateEmail_InvalidEmail_ReturnsFalse() {
    assertFalse(validationService.validateEmail("invalid-email")); // No @ symbol
    assertFalse(validationService.validateEmail("user@.com")); // No domain
    assertFalse(validationService.validateEmail("@example.com")); // No username
    assertFalse(validationService.validateEmail("user@com")); // Invalid domain
  }

  @Test
  void testValidateEmail_NullOrEmpty_ReturnsFalse() {
    assertFalse(validationService.validateEmail(null));
    assertFalse(validationService.validateEmail(""));
    assertFalse(validationService.validateEmail("   "));
  }

  @Test
  void testValidatePassword_ValidPassword_ReturnsTrue() {
    assertTrue(validationService.validatePassword("password123")); // Letters + numbers
    assertTrue(validationService.validatePassword("1234567a")); // Minimum length
    assertTrue(validationService.validatePassword("SecurePass99")); // Mixed case
  }

  @Test
  void testValidatePassword_InvalidPassword_ReturnsFalse() {
    assertFalse(validationService.validatePassword("short1")); // Too short (6 chars)
    assertFalse(validationService.validatePassword("password")); // No numbers
    assertFalse(validationService.validatePassword("12345678")); // No letters
    assertFalse(validationService.validatePassword("pass")); // Too short, no numbers
  }

  @Test
  void testValidatePassword_NullOrEmpty_ReturnsFalse() {
    assertFalse(validationService.validatePassword(null));
    assertFalse(validationService.validatePassword(""));
  }

  @Test
  void testValidateAge_UserOver21_ReturnsTrue() {
    User user =
        User.builder()
            .dateOfBirth(LocalDate.now().minusYears(25)) // 25 years old
            .build();
    assertTrue(validationService.validateAge(user));
  }

  @Test
  void testValidateAge_UserExactly21_ReturnsTrue() {
    User user =
        User.builder()
            .dateOfBirth(LocalDate.now().minusYears(21)) // Exactly 21
            .build();
    assertTrue(validationService.validateAge(user));
  }

  @Test
  void testValidateAge_UserUnder21_ReturnsFalse() {
    User user =
        User.builder()
            .dateOfBirth(LocalDate.now().minusYears(20)) // 20 years old
            .build();
    assertFalse(validationService.validateAge(user));
  }

  @Test
  void testValidateAge_UserUnder18_ReturnsFalse() {
    User user =
        User.builder()
            .dateOfBirth(LocalDate.now().minusYears(17)) // 17 years old
            .build();
    assertFalse(validationService.validateAge(user));
  }

  @Test
  void testValidateAge_NullUser_ReturnsFalse() {
    assertFalse(validationService.validateAge(null));
  }

  @Test
  void testValidateAge_UserWithNullBirthdate_ReturnsFalse() {
    User user =
        User.builder()
            .dateOfBirth(null) // No birthdate set
            .build();
    assertFalse(validationService.validateAge(user));
  }

  @Test
  void testValidateProduct_NullProduct_ReturnsFalse() {
    assertFalse(validationService.validateProduct(null));
  }

  @Test
  void testValidateProduct_ProductWithNullName_ReturnsFalse() {
    Category category = Category.builder().name("Beer").build();
    Product product =
        Product.builder()
            .name(null) // Missing name
            .price(new BigDecimal("8.99"))
            .category(category)
            .build();

    assertFalse(validationService.validateProduct(product));
  }

  @Test
  void testValidateProduct_ProductWithEmptyName_ReturnsFalse() {
    Category category = Category.builder().name("Beer").build();
    Product product =
        Product.builder()
            .name("") // Empty name
            .price(new BigDecimal("8.99"))
            .category(category)
            .build();

    assertFalse(validationService.validateProduct(product));
  }

  @Test
  void testValidateProduct_ProductWithNegativePrice_ReturnsFalse() {
    Category category = Category.builder().name("Beer").build();
    Product product =
        Product.builder()
            .name("Craft IPA")
            .price(new BigDecimal("-5.00")) // Negative price
            .category(category)
            .build();

    assertFalse(validationService.validateProduct(product));
  }

  @Test
  void testValidateProduct_ProductWithNullPrice_ReturnsFalse() {
    Category category = Category.builder().name("Beer").build();
    Product product =
        Product.builder()
            .name("Craft IPA")
            .price(null) // No price
            .category(category)
            .build();

    assertFalse(validationService.validateProduct(product));
  }

  @Test
  void testValidateProduct_ProductWithNullCategory_ReturnsFalse() {
    Product product =
        Product.builder()
            .name("Craft IPA")
            .price(new BigDecimal("8.99"))
            .category(null) // No category
            .build();

    assertFalse(validationService.validateProduct(product));
  }

  @Test
  void testValidateProduct_ProductWithEmptyCategoryName_ReturnsFalse() {
    Category category =
        Category.builder()
            .name("") // Empty category name
            .build();

    Product product =
        Product.builder()
            .name("Craft IPA")
            .price(new BigDecimal("8.99"))
            .category(category)
            .build();

    assertFalse(validationService.validateProduct(product));
  }

  @Test
  void testValidateProduct_ValidProduct_ReturnsTrue() {
    Category category = Category.builder().name("Beer").build();

    Product product =
        Product.builder()
            .name("Craft IPA")
            .price(new BigDecimal("8.99"))
            .available(true) // Just check this boolean
            .category(category)
            .build();

    assertTrue(validationService.validateProduct(product));
  }

  // REMOVE these tests since stock quantity doesn't matter:
  // testValidateProduct_ProductWithNegativeStock_ReturnsFalse()
  // testValidateProduct_ProductWithNullStock_ReturnsFalse()

  @Test
  void testValidateProductQuantity_ProductAvailable_ReturnsTrue() {
    Product product =
        Product.builder()
            .available(true) // Product is in stock
            .build();

    assertTrue(validationService.validateProductAvailability(product));
  }

  @Test
  void testValidateProductQuantity_ProductNotAvailable_ReturnsFalse() {
    Product product =
        Product.builder()
            .available(false) // Product is out of stock
            .build();

    assertFalse(validationService.validateProductAvailability(product)); // Even quantity 1 fails
  }
}
