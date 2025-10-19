package com.boozebuddies.service.implementation;

import com.boozebuddies.entity.Product;
import com.boozebuddies.entity.User;
import com.boozebuddies.entity.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

public class ValidationServiceImplTest {

    private ValidationServiceImpl validationService;

    @BeforeEach
    void setUp() {
        validationService = new ValidationServiceImpl();
    }

    // ===== EMAIL VALIDATION TESTS =====
    // What it's validating: Email format (must have @ and valid domain)
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

    // ===== PASSWORD VALIDATION TESTS =====
    // What it's validating: At least 8 chars, must have letters AND numbers
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

    // ===== AGE VALIDATION TESTS =====
    // What it's validating: User must be 21+ years old for alcohol purchases
    @Test
    void testValidateAge_UserOver21_ReturnsTrue() {
        User user = User.builder()
            .dateOfBirth(LocalDate.now().minusYears(25)) // 25 years old
            .build();
        assertTrue(validationService.validateAge(user));
    }

    @Test
    void testValidateAge_UserExactly21_ReturnsTrue() {
        User user = User.builder()
            .dateOfBirth(LocalDate.now().minusYears(21)) // Exactly 21
            .build();
        assertTrue(validationService.validateAge(user));
    }

    @Test
    void testValidateAge_UserUnder21_ReturnsFalse() {
        User user = User.builder()
            .dateOfBirth(LocalDate.now().minusYears(20)) // 20 years old
            .build();
        assertFalse(validationService.validateAge(user));
    }

    @Test
    void testValidateAge_UserUnder18_ReturnsFalse() {
        User user = User.builder()
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
        User user = User.builder()
            .dateOfBirth(null) // No birthdate set
            .build();
        assertFalse(validationService.validateAge(user));
    }

    // ===== PRODUCT VALIDATION TESTS =====
    // What it's validating: Product has all required fields with valid values
    @Test
    void testValidateProduct_ValidProduct_ReturnsTrue() {
        Category category = Category.builder()
            .name("Beer")
            .build();
            
        Product product = Product.builder()
            .name("Craft IPA")
            .price(new BigDecimal("8.99"))
            .stockQuantity(50)
            .category(category)
            .build();
            
        assertTrue(validationService.validateProduct(product));
    }

    @Test
    void testValidateProduct_NullProduct_ReturnsFalse() {
        assertFalse(validationService.validateProduct(null));
    }

    @Test
    void testValidateProduct_ProductWithNullName_ReturnsFalse() {
        Category category = Category.builder().name("Beer").build();
        Product product = Product.builder()
            .name(null) // Missing name
            .price(new BigDecimal("8.99"))
            .stockQuantity(50)
            .category(category)
            .build();
            
        assertFalse(validationService.validateProduct(product));
    }

    @Test
    void testValidateProduct_ProductWithEmptyName_ReturnsFalse() {
        Category category = Category.builder().name("Beer").build();
        Product product = Product.builder()
            .name("") // Empty name
            .price(new BigDecimal("8.99"))
            .stockQuantity(50)
            .category(category)
            .build();
            
        assertFalse(validationService.validateProduct(product));
    }

    @Test
    void testValidateProduct_ProductWithNegativePrice_ReturnsFalse() {
        Category category = Category.builder().name("Beer").build();
        Product product = Product.builder()
            .name("Craft IPA")
            .price(new BigDecimal("-5.00")) // Negative price
            .stockQuantity(50)
            .category(category)
            .build();
            
        assertFalse(validationService.validateProduct(product));
    }

    @Test
    void testValidateProduct_ProductWithNullPrice_ReturnsFalse() {
        Category category = Category.builder().name("Beer").build();
        Product product = Product.builder()
            .name("Craft IPA")
            .price(null) // No price
            .stockQuantity(50)
            .category(category)
            .build();
            
        assertFalse(validationService.validateProduct(product));
    }

    @Test
    void testValidateProduct_ProductWithNegativeStock_ReturnsFalse() {
        Category category = Category.builder().name("Beer").build();
        Product product = Product.builder()
            .name("Craft IPA")
            .price(new BigDecimal("8.99"))
            .stockQuantity(-10) // Negative stock
            .category(category)
            .build();
            
        assertFalse(validationService.validateProduct(product));
    }

    @Test
    void testValidateProduct_ProductWithNullStock_ReturnsFalse() {
        Category category = Category.builder().name("Beer").build();
        Product product = Product.builder()
            .name("Craft IPA")
            .price(new BigDecimal("8.99"))
            .stockQuantity(null) // No stock quantity
            .category(category)
            .build();
            
        assertFalse(validationService.validateProduct(product));
    }

    @Test
    void testValidateProduct_ProductWithNullCategory_ReturnsFalse() {
        Product product = Product.builder()
            .name("Craft IPA")
            .price(new BigDecimal("8.99"))
            .stockQuantity(50)
            .category(null) // No category
            .build();
            
        assertFalse(validationService.validateProduct(product));
    }

    @Test
    void testValidateProduct_ProductWithEmptyCategoryName_ReturnsFalse() {
        Category category = Category.builder()
            .name("") // Empty category name
            .build();
            
        Product product = Product.builder()
            .name("Craft IPA")
            .price(new BigDecimal("8.99"))
            .stockQuantity(50)
            .category(category)
            .build();
            
        assertFalse(validationService.validateProduct(product));
    }

    // ===== PRODUCT QUANTITY VALIDATION TESTS =====
    // What it's validating: Requested quantity is positive AND available in stock
    @Test
    void testValidateProductQuantity_ValidQuantity_ReturnsTrue() {
        Product product = Product.builder()
            .stockQuantity(10) // 10 in stock
            .build();
            
        assertTrue(validationService.validateProductQuantity(product, 5)); // Requesting 5
        assertTrue(validationService.validateProductQuantity(product, 10)); // Requesting all
    }

    @Test
    void testValidateProductQuantity_InsufficientStock_ReturnsFalse() {
        Product product = Product.builder()
            .stockQuantity(5) // Only 5 in stock
            .build();
            
        assertFalse(validationService.validateProductQuantity(product, 10)); // Requesting 10
    }

    @Test
    void testValidateProductQuantity_ZeroQuantity_ReturnsFalse() {
        Product product = Product.builder()
            .stockQuantity(10)
            .build();
            
        assertFalse(validationService.validateProductQuantity(product, 0)); // Can't order 0
    }

    @Test
    void testValidateProductQuantity_NegativeQuantity_ReturnsFalse() {
        Product product = Product.builder()
            .stockQuantity(10)
            .build();
            
        assertFalse(validationService.validateProductQuantity(product, -5)); // Can't order negative
    }

    @Test
    void testValidateProductQuantity_NullProduct_ReturnsFalse() {
        assertFalse(validationService.validateProductQuantity(null, 5));
    }
}