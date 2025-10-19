package com.boozebuddies.service.implementation;

import com.boozebuddies.dto.RegisterUserRequest;
import com.boozebuddies.entity.User;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserServiceImpl Tests")
class UserServiceImplTest {

  private UserServiceImpl userService;

  @BeforeEach
  void setUp() {
    userService = new UserServiceImpl();
  }

  // ==================== REGISTRATION TESTS ====================

  @Test
  @DisplayName("Should register a valid user")
  void testRegisterValidUser() {
    User user = User.builder()
        .name("John Doe")
        .email("john@example.com")
        .passwordHash("hash123")
        .dateOfBirth(LocalDate.of(1990, 5, 15))
        .build();

    User registered = userService.register(user);

    assertNotNull(registered);
    assertNotNull(registered.getId());
    assertEquals("john@example.com", registered.getEmail());
  }

  @Test
  @DisplayName("Should throw exception when registering null user")
  void testRegisterNullUser() {
    assertThrows(IllegalArgumentException.class, () -> userService.register(null));
  }

  @Test
  @DisplayName("Should throw exception when email is null")
  void testRegisterUserWithoutEmail() {
    User user = User.builder()
        .name("John Doe")
        .passwordHash("hash123")
        .build();

    assertThrows(IllegalArgumentException.class, () -> userService.register(user));
  }

  @Test
  @DisplayName("Should throw exception when password is null")
  void testRegisterUserWithoutPassword() {
    User user = User.builder()
        .name("John Doe")
        .email("john@example.com")
        .build();

    assertThrows(IllegalArgumentException.class, () -> userService.register(user));
  }

  @Test
  @DisplayName("Should auto-verify age for legal drinking age user")
  void testRegisterAdultSetsAgeVerified() {
    User user = User.builder()
        .name("Adult")
        .email("adult@example.com")
        .passwordHash("hash")
        .dateOfBirth(LocalDate.of(1990, 1, 1))
        .build();

    User registered = userService.register(user);

    assertTrue(registered.isAgeVerified());
  }

  @Test
  @DisplayName("Should not verify age for minor")
  void testRegisterMinorDoesNotVerifyAge() {
    User user = User.builder()
        .name("Minor")
        .email("minor@example.com")
        .passwordHash("hash")
        .dateOfBirth(LocalDate.of(2015, 1, 1))
        .build();

    User registered = userService.register(user);

    assertFalse(registered.isAgeVerified());
  }

  @Test
  @DisplayName("Should register user from RegisterUserRequest")
  void testRegisterUserFromRequest() {
    RegisterUserRequest request = new RegisterUserRequest();
    request.setName("Jane Doe");
    request.setEmail("jane@example.com");
    request.setPassword("password123");
    request.setPhone("555-1234");
    request.setDateOfBirth(LocalDate.of(1992, 3, 20));

    User registered = userService.registerUser(request);

    assertNotNull(registered);
    assertEquals("jane@example.com", registered.getEmail());
    assertEquals("Jane Doe", registered.getName());
    assertEquals("555-1234", registered.getPhone());
  }

  @Test
  @DisplayName("Should throw exception when RegisterUserRequest is null")
  void testRegisterUserFromNullRequest() {
    assertThrows(IllegalArgumentException.class, () -> userService.registerUser(null));
  }

  @Test
  @DisplayName("Should throw exception when RegisterUserRequest email is null")
  void testRegisterUserFromRequestWithoutEmail() {
    RegisterUserRequest request = new RegisterUserRequest();
    request.setName("Test");
    request.setPassword("password123");

    assertThrows(IllegalArgumentException.class, () -> userService.registerUser(request));
  }

  @Test
  @DisplayName("Should throw exception when RegisterUserRequest password is null")
  void testRegisterUserFromRequestWithoutPassword() {
    RegisterUserRequest request = new RegisterUserRequest();
    request.setName("Test");
    request.setEmail("test@example.com");

    assertThrows(IllegalArgumentException.class, () -> userService.registerUser(request));
  }

  // ==================== LOGIN TESTS ====================

  @Test
  @DisplayName("Should login with correct email and password")
  void testLoginSuccess() {
    User user = User.builder()
        .name("John Doe")
        .email("john@example.com")
        .passwordHash("hash123")
        .dateOfBirth(LocalDate.of(1990, 5, 15))
        .build();
    userService.register(user);

    User loggedIn = userService.login("john@example.com", "hash123");

    assertNotNull(loggedIn);
    assertEquals("john@example.com", loggedIn.getEmail());
  }

  @Test
  @DisplayName("Should return null for incorrect password")
  void testLoginIncorrectPassword() {
    User user = User.builder()
        .name("John Doe")
        .email("john@example.com")
        .passwordHash("hash123")
        .dateOfBirth(LocalDate.of(1990, 5, 15))
        .build();
    userService.register(user);

    User loggedIn = userService.login("john@example.com", "wrongpassword");

    assertNull(loggedIn);
  }

  @Test
  @DisplayName("Should return null for non-existent user")
  void testLoginUserNotFound() {
    User loggedIn = userService.login("nonexistent@example.com", "password");

    assertNull(loggedIn);
  }

  @Test
  @DisplayName("Should login with case-insensitive email")
  void testLoginCaseInsensitiveEmail() {
    User user = User.builder()
        .name("John Doe")
        .email("john@example.com")
        .passwordHash("hash123")
        .dateOfBirth(LocalDate.of(1990, 5, 15))
        .build();
    userService.register(user);

    User loggedIn = userService.login("JOHN@EXAMPLE.COM", "hash123");

    assertNotNull(loggedIn);
    assertEquals("john@example.com", loggedIn.getEmail());
  }

  // ==================== AGE VERIFICATION TESTS ====================

  @Test
  @DisplayName("Should verify user is 21 or older")
  void testVerifyAgeAdult() {
    User user = User.builder()
        .dateOfBirth(LocalDate.of(1990, 1, 1))
        .build();

    assertTrue(userService.verifyAge(user));
  }

  @Test
  @DisplayName("Should reject user under 21")
  void testVerifyAgeMinor() {
    User user = User.builder()
        .dateOfBirth(LocalDate.of(2015, 1, 1))
        .build();

    assertFalse(userService.verifyAge(user));
  }

  @Test
  @DisplayName("Should return false for null user")
  void testVerifyAgeNullUser() {
    assertFalse(userService.verifyAge(null));
  }

  @Test
  @DisplayName("Should return false when date of birth is null")
  void testVerifyAgeNullDateOfBirth() {
    User user = User.builder().build();

    assertFalse(userService.verifyAge(user));
  }

  @Test
  @DisplayName("Should verify exactly 21 years old")
  void testVerifyAgeExactly21() {
    User user = User.builder()
        .dateOfBirth(LocalDate.now().minusYears(21))
        .build();

    assertTrue(userService.verifyAge(user));
  }

  // ==================== RETRIEVAL TESTS ====================

  @Test
  @DisplayName("Should retrieve user by ID")
  void testGetUserById() {
    User user = User.builder()
        .name("John Doe")
        .email("john@example.com")
        .passwordHash("hash123")
        .dateOfBirth(LocalDate.of(1990, 5, 15))
        .build();
    User registered = userService.register(user);

    Optional<User> retrieved = userService.getUserById(registered.getId());

    assertTrue(retrieved.isPresent());
    assertEquals(registered.getId(), retrieved.get().getId());
    assertEquals("john@example.com", retrieved.get().getEmail());
  }

  @Test
  @DisplayName("Should return empty optional for non-existent user ID")
  void testGetUserByIdNotFound() {
    Optional<User> retrieved = userService.getUserById(999L);

    assertFalse(retrieved.isPresent());
  }

  @Test
  @DisplayName("Should retrieve all users")
  void testGetAllUsers() {
    User user1 = User.builder()
        .name("John")
        .email("john@example.com")
        .passwordHash("hash1")
        .dateOfBirth(LocalDate.of(1990, 1, 1))
        .build();
    User user2 = User.builder()
        .name("Jane")
        .email("jane@example.com")
        .passwordHash("hash2")
        .dateOfBirth(LocalDate.of(1992, 3, 20))
        .build();

    userService.register(user1);
    userService.register(user2);

    List<User> allUsers = userService.getAllUsers();

    assertEquals(2, allUsers.size());
  }

  // ==================== UPDATE TESTS ====================

  @Test
  @DisplayName("Should update user email")
  void testUpdateUserEmail() {
    User user = User.builder()
        .name("John")
        .email("john@example.com")
        .passwordHash("hash123")
        .dateOfBirth(LocalDate.of(1990, 1, 1))
        .build();
    User registered = userService.register(user);

    User updatedUser = User.builder()
        .email("newemail@example.com")
        .build();
    User result = userService.updateUser(registered.getId(), updatedUser);

    assertNotNull(result);
    assertEquals("newemail@example.com", result.getEmail());
  }

  @Test
  @DisplayName("Should update user password")
  void testUpdateUserPassword() {
    User user = User.builder()
        .name("John")
        .email("john@example.com")
        .passwordHash("hash123")
        .dateOfBirth(LocalDate.of(1990, 1, 1))
        .build();
    User registered = userService.register(user);

    User updatedUser = User.builder()
        .passwordHash("newhash456")
        .build();
    User result = userService.updateUser(registered.getId(), updatedUser);

    assertNotNull(result);
    assertEquals("newhash456", result.getPasswordHash());
  }

  @Test
  @DisplayName("Should update user date of birth and verify age")
  void testUpdateUserDateOfBirthAndVerifyAge() {
    User user = User.builder()
        .name("John")
        .email("john@example.com")
        .passwordHash("hash123")
        .dateOfBirth(LocalDate.of(1990, 1, 1))
        .ageVerified(true)
        .build();
    User registered = userService.register(user);

    User updatedUser = User.builder()
        .dateOfBirth(LocalDate.of(2015, 1, 1))
        .build();
    User result = userService.updateUser(registered.getId(), updatedUser);

    assertNotNull(result);
    assertFalse(result.isAgeVerified());
  }

  @Test
  @DisplayName("Should return null when updating non-existent user")
  void testUpdateNonExistentUser() {
    User updatedUser = User.builder()
        .email("newemail@example.com")
        .build();
    User result = userService.updateUser(999L, updatedUser);

    assertNull(result);
  }

  // ==================== DELETE TESTS ====================

  @Test
  @DisplayName("Should delete user by ID")
  void testDeleteUser() {
    User user = User.builder()
        .name("John")
        .email("john@example.com")
        .passwordHash("hash123")
        .dateOfBirth(LocalDate.of(1990, 1, 1))
        .build();
    User registered = userService.register(user);

    userService.deleteUser(registered.getId());

    Optional<User> deleted = userService.getUserById(registered.getId());
    assertFalse(deleted.isPresent());
  }

  @Test
  @DisplayName("Should handle deleting non-existent user")
  void testDeleteNonExistentUser() {
    assertDoesNotThrow(() -> userService.deleteUser(999L));
  }

  @Test
  @DisplayName("Should have correct user count after deletion")
  void testUserCountAfterDeletion() {
    User user1 = User.builder()
        .name("John")
        .email("john@example.com")
        .passwordHash("hash1")
        .dateOfBirth(LocalDate.of(1990, 1, 1))
        .build();
    User user2 = User.builder()
        .name("Jane")
        .email("jane@example.com")
        .passwordHash("hash2")
        .dateOfBirth(LocalDate.of(1992, 3, 20))
        .build();

    User registered1 = userService.register(user1);
    userService.register(user2);

    userService.deleteUser(registered1.getId());

    assertEquals(1, userService.getAllUsers().size());
  }
}