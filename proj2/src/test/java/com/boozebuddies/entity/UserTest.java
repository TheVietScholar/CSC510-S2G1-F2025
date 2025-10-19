package com.boozebuddies.entity;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

class UserTest {

  private User user;

@BeforeEach
void setUp() {
  user = User.builder()
      .id(1L)
      .name("John Doe")
      .email("john@example.com")
      .passwordHash("hashedPassword123")
      .phone("555-1234")
      .dateOfBirth(LocalDate.of(1990, 5, 15))
      .ageVerified(true)
      .build();
  user.getRoles().add("CUSTOMER");
}

  // Basic construction and field validation
  @Test
  void testUserCreation() {
    assertNotNull(user);
    assertEquals("John Doe", user.getName());
    assertEquals("john@example.com", user.getEmail());
    assertEquals(1L, user.getId());
  }

  @Test
  void testUserBuilderDefaults() {
    User newUser = User.builder()
        .name("Jane Doe")
        .email("jane@example.com")
        .passwordHash("hash")
        .build();

    assertFalse(newUser.isAgeVerified());
    assertNotNull(newUser.getCreatedAt());
    assertNotNull(newUser.getUpdatedAt());
    assertNotNull(newUser.getRoles());
    assertTrue(newUser.getRoles().isEmpty());
  }

  // Age verification logic
  @Test
  void testUserIsOfLegalDrinkingAge() {
    User adult = User.builder()
        .dateOfBirth(LocalDate.of(1990, 1, 1))
        .build();
    assertTrue(isLegalAge(adult));
  }

  @Test
  void testUserIsNotOfLegalDrinkingAge() {
    User minor = User.builder()
        .dateOfBirth(LocalDate.of(2015, 1, 1))
        .build();
    assertFalse(isLegalAge(minor));
  }

  @Test
  void testUserOnTurning21() {
    User justTurned21 = User.builder()
        .dateOfBirth(LocalDate.now().minusYears(21))
        .build();
    assertTrue(isLegalAge(justTurned21));
  }

  // Age verification flag
  @Test
  void testAgeVerificationFlag() {
    User unverifiedUser = User.builder()
        .dateOfBirth(LocalDate.of(1990, 1, 1))
        .ageVerified(false)
        .build();
    assertFalse(unverifiedUser.isAgeVerified());

    unverifiedUser.setAgeVerified(true);
    assertTrue(unverifiedUser.isAgeVerified());
  }

  // Roles management
  @Test
  void testUserRoles() {
    assertEquals(1, user.getRoles().size());
    assertTrue(user.getRoles().contains("CUSTOMER"));
  }

  @Test
  void testAddRoleToUser() {
    user.getRoles().add("ADMIN");
    assertEquals(2, user.getRoles().size());
    assertTrue(user.getRoles().contains("ADMIN"));
    assertTrue(user.getRoles().contains("CUSTOMER"));
  }

  @Test
  void testUserWithMultipleRoles() {
    User multiRoleUser = User.builder()
        .roles(List.of("CUSTOMER", "DELIVERY_DRIVER"))
        .build();
    assertEquals(2, multiRoleUser.getRoles().size());
  }

  // Orders relationship
  @Test
  void testUserOrdersInitialization() {
    User newUser = User.builder()
        .name("Test User")
        .email("test@example.com")
        .passwordHash("hash")
        .build();
    assertNotNull(newUser.getOrders());
    assertTrue(newUser.getOrders().isEmpty());
  }

  @Test
  void testAddOrderToUser() {
    Order order = new Order();
    user.getOrders().add(order);
    assertEquals(1, user.getOrders().size());
  }

  // Ratings relationship
  @Test
  void testUserRatingsInitialization() {
    User newUser = User.builder()
        .name("Test User")
        .email("test@example.com")
        .passwordHash("hash")
        .build();
    assertNotNull(newUser.getRatings());
    assertTrue(newUser.getRatings().isEmpty());
  }

  @Test
  void testAddRatingToUser() {
    Rating rating = new Rating();
    user.getRatings().add(rating);
    assertEquals(1, user.getRatings().size());
  }

  // Timestamps
  @Test
  void testCreatedAtTimestamp() {
    User newUser = User.builder()
        .name("Test")
        .email("test@example.com")
        .passwordHash("hash")
        .build();
    assertNotNull(newUser.getCreatedAt());
  }

  @Test
  void testPreUpdateModifiesTimestamp() throws InterruptedException {
    LocalDateTime originalUpdatedAt = user.getUpdatedAt();
    Thread.sleep(10); // Small delay to ensure time difference
    user.preUpdate();
    assertTrue(user.getUpdatedAt().isAfter(originalUpdatedAt));
  }

  @Test
  void testTimestampsAreNotNull() {
    assertNotNull(user.getCreatedAt());
    assertNotNull(user.getUpdatedAt());
  }

  // Contact information
  @Test
  void testPhoneNumber() {
    assertEquals("555-1234", user.getPhone());
  }

  @Test
  void testUserWithoutPhone() {
    User userNoPhone = User.builder()
        .name("No Phone")
        .email("nophone@example.com")
        .passwordHash("hash")
        .build();
    assertNull(userNoPhone.getPhone());
  }

  @Test
  void testEmailIsStored() {
    assertEquals("john@example.com", user.getEmail());
  }

  // Helper method to check legal drinking age
  private boolean isLegalAge(User user) {
    if (user.getDateOfBirth() == null) {
      return false;
    }
    LocalDate legalAge = LocalDate.now().minusYears(21);
    return user.getDateOfBirth().isBefore(legalAge) || user.getDateOfBirth().isEqual(legalAge);
  }
}