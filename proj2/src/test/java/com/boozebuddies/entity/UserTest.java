package com.boozebuddies.entity;

import static org.junit.jupiter.api.Assertions.*;

import com.boozebuddies.model.Role;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("User Entity Tests")
class UserTest {

  private User user;

  @BeforeEach
  void setUp() {
    user =
        User.builder()
            .id(1L)
            .name("John Doe")
            .email("john@example.com")
            .passwordHash("hashedPassword")
            .phone("1234567890")
            .dateOfBirth(LocalDate.of(1990, 1, 1))
            .ageVerified(true)
            .isActive(true)
            .isEmailVerified(false)
            .build();
  }

  // ==================== BUILDER TESTS ====================

  @Test
  @DisplayName("Builder creates user with all fields")
  void testBuilder_AllFields() {
    LocalDateTime now = LocalDateTime.now();
    User testUser =
        User.builder()
            .id(1L)
            .name("Jane Doe")
            .email("jane@example.com")
            .passwordHash("hash123")
            .phone("9876543210")
            .dateOfBirth(LocalDate.of(1995, 5, 15))
            .ageVerified(true)
            .latitude(35.7796)
            .longitude(-78.6382)
            .merchantId(10L)
            .isActive(true)
            .isEmailVerified(true)
            .lastLoginAt(now)
            .refreshToken("token123")
            .refreshTokenExpiryDate(now.plusDays(7))
            .build();

    assertNotNull(testUser);
    assertEquals("Jane Doe", testUser.getName());
    assertEquals("jane@example.com", testUser.getEmail());
    assertEquals("hash123", testUser.getPasswordHash());
    assertEquals("9876543210", testUser.getPhone());
    assertEquals(LocalDate.of(1995, 5, 15), testUser.getDateOfBirth());
    assertTrue(testUser.isAgeVerified());
    assertEquals(35.7796, testUser.getLatitude());
    assertEquals(-78.6382, testUser.getLongitude());
    assertEquals(10L, testUser.getMerchantId());
    assertTrue(testUser.isActive());
    assertTrue(testUser.isEmailVerified());
  }

  @Test
  @DisplayName("Builder creates user with default values")
  void testBuilder_Defaults() {
    User testUser =
        User.builder().name("Test User").email("test@example.com").passwordHash("hash").build();

    assertNotNull(testUser.getRoles());
    assertTrue(testUser.getRoles().isEmpty());
    assertTrue(testUser.isActive());
    assertFalse(testUser.isEmailVerified());
    assertFalse(testUser.isAgeVerified());
    assertNotNull(testUser.getCreatedAt());
    assertNotNull(testUser.getUpdatedAt());
    assertNotNull(testUser.getOrders());
    assertNotNull(testUser.getRatings());
  }

  // ==================== ROLE MANAGEMENT TESTS ====================

  @Test
  @DisplayName("hasRole returns true when user has the role")
  void testHasRole_True() {
    user.addRole(Role.USER);
    assertTrue(user.hasRole(Role.USER));
  }

  @Test
  @DisplayName("hasRole returns false when user does not have the role")
  void testHasRole_False() {
    user.addRole(Role.USER);
    assertFalse(user.hasRole(Role.ADMIN));
  }

  @Test
  @DisplayName("hasRole returns false when roles is null")
  void testHasRole_NullRoles() {
    user.setRoles(null);
    assertFalse(user.hasRole(Role.USER));
  }

  @Test
  @DisplayName("hasAnyRole returns true when user has one of the roles")
  void testHasAnyRole_True() {
    user.addRole(Role.USER);
    assertTrue(user.hasAnyRole(Role.ADMIN, Role.USER, Role.DRIVER));
  }

  @Test
  @DisplayName("hasAnyRole returns false when user has none of the roles")
  void testHasAnyRole_False() {
    user.addRole(Role.USER);
    assertFalse(user.hasAnyRole(Role.ADMIN, Role.DRIVER));
  }

  @Test
  @DisplayName("hasAnyRole returns false when roles is null")
  void testHasAnyRole_NullRoles() {
    user.setRoles(null);
    assertFalse(user.hasAnyRole(Role.USER, Role.ADMIN));
  }

  @Test
  @DisplayName("hasAnyRole returns false when input is null")
  void testHasAnyRole_NullInput() {
    user.addRole(Role.USER);
    assertFalse(user.hasAnyRole((Role[]) null));
  }

  @Test
  @DisplayName("hasAllRoles returns true when user has all roles")
  void testHasAllRoles_True() {
    user.addRole(Role.USER);
    user.addRole(Role.ADMIN);
    assertTrue(user.hasAllRoles(Role.USER, Role.ADMIN));
  }

  @Test
  @DisplayName("hasAllRoles returns false when user is missing one role")
  void testHasAllRoles_False() {
    user.addRole(Role.USER);
    assertFalse(user.hasAllRoles(Role.USER, Role.ADMIN));
  }

  @Test
  @DisplayName("hasAllRoles returns false when roles is null")
  void testHasAllRoles_NullRoles() {
    user.setRoles(null);
    assertFalse(user.hasAllRoles(Role.USER));
  }

  @Test
  @DisplayName("hasAllRoles returns false when input is null")
  void testHasAllRoles_NullInput() {
    user.addRole(Role.USER);
    assertFalse(user.hasAllRoles((Role[]) null));
  }

  @Test
  @DisplayName("addRole adds role to user")
  void testAddRole_Success() {
    user.addRole(Role.ADMIN);
    assertTrue(user.hasRole(Role.ADMIN));
  }

  @Test
  @DisplayName("addRole initializes roles set if null")
  void testAddRole_NullRoles() {
    user.setRoles(null);
    user.addRole(Role.USER);
    assertNotNull(user.getRoles());
    assertTrue(user.hasRole(Role.USER));
  }

  @Test
  @DisplayName("removeRole removes role from user")
  void testRemoveRole_Success() {
    user.addRole(Role.USER);
    user.addRole(Role.ADMIN);

    user.removeRole(Role.ADMIN);

    assertTrue(user.hasRole(Role.USER));
    assertFalse(user.hasRole(Role.ADMIN));
  }

  @Test
  @DisplayName("removeRole handles null roles gracefully")
  void testRemoveRole_NullRoles() {
    user.setRoles(null);
    assertDoesNotThrow(() -> user.removeRole(Role.USER));
  }

  // ==================== ROLE-SPECIFIC METHODS TESTS ====================

  @Test
  @DisplayName("isAdmin returns true when user has ADMIN role")
  void testIsAdmin_True() {
    user.addRole(Role.ADMIN);
    assertTrue(user.isAdmin());
  }

  @Test
  @DisplayName("isAdmin returns false when user does not have ADMIN role")
  void testIsAdmin_False() {
    user.addRole(Role.USER);
    assertFalse(user.isAdmin());
  }

  @Test
  @DisplayName("isMerchantAdmin returns true when user has role and merchantId")
  void testIsMerchantAdmin_True() {
    user.addRole(Role.MERCHANT_ADMIN);
    user.setMerchantId(5L);
    assertTrue(user.isMerchantAdmin());
  }

  @Test
  @DisplayName("isMerchantAdmin returns false when merchantId is null")
  void testIsMerchantAdmin_NullMerchantId() {
    user.addRole(Role.MERCHANT_ADMIN);
    user.setMerchantId(null);
    assertFalse(user.isMerchantAdmin());
  }

  @Test
  @DisplayName("isMerchantAdmin returns false when user does not have role")
  void testIsMerchantAdmin_NoRole() {
    user.setMerchantId(5L);
    assertFalse(user.isMerchantAdmin());
  }

  @Test
  @DisplayName("isDriver returns true when user has role and driver entity")
  void testIsDriver_True() {
    Driver driver = new Driver();
    user.addRole(Role.DRIVER);
    user.setDriver(driver);
    assertTrue(user.isDriver());
  }

  @Test
  @DisplayName("isDriver returns false when driver entity is null")
  void testIsDriver_NullDriver() {
    user.addRole(Role.DRIVER);
    user.setDriver(null);
    assertFalse(user.isDriver());
  }

  @Test
  @DisplayName("isDriver returns false when user does not have role")
  void testIsDriver_NoRole() {
    Driver driver = new Driver();
    user.setDriver(driver);
    assertFalse(user.isDriver());
  }

  @Test
  @DisplayName("ownsMerchant returns true when user owns the merchant")
  void testOwnsMerchant_True() {
    user.addRole(Role.MERCHANT_ADMIN);
    user.setMerchantId(10L);
    assertTrue(user.ownsMerchant(10L));
  }

  @Test
  @DisplayName("ownsMerchant returns false when merchantId does not match")
  void testOwnsMerchant_DifferentMerchant() {
    user.addRole(Role.MERCHANT_ADMIN);
    user.setMerchantId(10L);
    assertFalse(user.ownsMerchant(20L));
  }

  @Test
  @DisplayName("ownsMerchant returns false when user is not merchant admin")
  void testOwnsMerchant_NotMerchantAdmin() {
    user.addRole(Role.USER);
    user.setMerchantId(10L);
    assertFalse(user.ownsMerchant(10L));
  }

  @Test
  @DisplayName("ownsMerchant returns false when merchantId is null")
  void testOwnsMerchant_NullMerchantId() {
    user.addRole(Role.MERCHANT_ADMIN);
    user.setMerchantId(null);
    assertFalse(user.ownsMerchant(10L));
  }

  // ==================== LIFECYCLE TESTS ====================

  @Test
  @DisplayName("preUpdate updates updatedAt timestamp")
  void testPreUpdate() {
    LocalDateTime originalUpdatedAt = user.getUpdatedAt();

    // Simulate some delay
    try {
      Thread.sleep(10);
    } catch (InterruptedException e) {
      // Ignore
    }

    user.preUpdate();

    assertNotNull(user.getUpdatedAt());
    assertTrue(user.getUpdatedAt().isAfter(originalUpdatedAt));
  }

  // ==================== BOOLEAN GETTERS TESTS ====================

  @Test
  @DisplayName("isActive returns correct value")
  void testIsActive() {
    user.setActive(true);
    assertTrue(user.isActive());

    user.setActive(false);
    assertFalse(user.isActive());
  }

  @Test
  @DisplayName("isEmailVerified returns correct value")
  void testIsEmailVerified() {
    user.setEmailVerified(true);
    assertTrue(user.isEmailVerified());

    user.setEmailVerified(false);
    assertFalse(user.isEmailVerified());
  }

  @Test
  @DisplayName("isAgeVerified returns correct value")
  void testIsAgeVerified() {
    user.setAgeVerified(true);
    assertTrue(user.isAgeVerified());

    user.setAgeVerified(false);
    assertFalse(user.isAgeVerified());
  }

  // ==================== EDGE CASES ====================

  @Test
  @DisplayName("setRoles replaces entire role set")
  void testSetRoles() {
    user.addRole(Role.USER);

    Set<Role> newRoles = new HashSet<>();
    newRoles.add(Role.ADMIN);
    newRoles.add(Role.MERCHANT_ADMIN);

    user.setRoles(newRoles);

    assertFalse(user.hasRole(Role.USER));
    assertTrue(user.hasRole(Role.ADMIN));
    assertTrue(user.hasRole(Role.MERCHANT_ADMIN));
  }

  @Test
  @DisplayName("User can have multiple roles simultaneously")
  void testMultipleRoles() {
    user.addRole(Role.USER);
    user.addRole(Role.ADMIN);
    user.addRole(Role.DRIVER);

    assertTrue(user.hasRole(Role.USER));
    assertTrue(user.hasRole(Role.ADMIN));
    assertTrue(user.hasRole(Role.DRIVER));
    assertEquals(3, user.getRoles().size());
  }

  @Test
  @DisplayName("NoArgsConstructor creates empty user")
  void testNoArgsConstructor() {
    User emptyUser = new User();
    assertNotNull(emptyUser);
  }

  @Test
  @DisplayName("AllArgsConstructor creates user with all parameters")
  void testAllArgsConstructor() {
    LocalDateTime now = LocalDateTime.now();
    Set<Role> roles = new HashSet<>();
    roles.add(Role.USER);

    User testUser =
        new User(
            1L, // id
            "Test User", // name
            "test@example.com", // email
            "hash", // passwordHash
            "1234567890", // phone
            LocalDate.of(1990, 1, 1), // dateOfBirth
            true, // ageVerified
            35.0, // latitude
            -78.0, // longitude
            roles, // roles
            10L, // merchantId
            null, // driver
            true, // isActive
            false, // isEmailVerified
            now, // lastLoginAt
            "token", // refreshToken
            now.plusDays(7), // refreshTokenExpiryDate
            null, // address
            now, // createdAt
            now, // updatedAt
            new ArrayList<>(), // orders
            new ArrayList<>() // ratings
            );

    assertNotNull(testUser);
    assertEquals("Test User", testUser.getName());
    assertEquals("test@example.com", testUser.getEmail());
  }
}
