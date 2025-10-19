package com.boozebuddies.service.implementation;

import com.boozebuddies.dto.RegisterUserRequest;
import com.boozebuddies.entity.User;
import com.boozebuddies.service.ValidationService;
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
  private ValidationService validationService;

  @BeforeEach
  void setUp() {
    validationService = new ValidationServiceImpl();
    userService = new UserServiceImpl(validationService);
  } 

  // ==================== REGISTRATION TESTS ====================

  @Test
  @DisplayName("Should register a valid user")
  void testRegisterValidUser() {
    User user = User.builder()
        .name("John Doe")
        .email("john@example.com")
        .passwordHash("Password123")
        .dateOfBirth(LocalDate.of(1990, 5, 15))
        .phone("252-555-1234")
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
  @DisplayName("Should throw exception when email is invalid")
  void testRegisterUserWithInvalidEmail() {
    User user = User.builder()
        .name("John Doe")
        .email("invalidemail")
        .passwordHash("Password123")
        .build();

    assertThrows(IllegalArgumentException.class, () -> userService.register(user));
  }

  @Test
  @DisplayName("Should throw exception when password is invalid")
  void testRegisterUserWithInvalidPassword() {
    User user = User.builder()
        .name("John Doe")
        .email("john@example.com")
        .passwordHash("weak")
        .build();

    assertThrows(IllegalArgumentException.class, () -> userService.register(user));
  }

  @Test
  @DisplayName("Should reject duplicate email registration")
  void testRegisterDuplicateEmail() {
    User user1 = User.builder()
        .name("John Doe")
        .email("john@example.com")
        .passwordHash("Password123")
        .dateOfBirth(LocalDate.of(1990, 1, 1))
        .phone("252-555-1234")
        .build();
    
    userService.register(user1);

    User user2 = User.builder()
        .name("Jane Doe")
        .email("john@example.com")
        .passwordHash("Password456")
        .dateOfBirth(LocalDate.of(1992, 3, 20))
        .build();

    assertThrows(IllegalArgumentException.class, () -> userService.register(user2));
  }

  @Test
  @DisplayName("Should reject empty email string")
  void testRegisterUserWithEmptyEmail() {
    User user = User.builder()
        .name("John Doe")
        .email("")
        .passwordHash("Password123")
        .dateOfBirth(LocalDate.of(1990, 1, 1))
        .build();

    assertThrows(IllegalArgumentException.class, () -> userService.register(user));
  }

  @Test
  @DisplayName("Should reject empty password string")
  void testRegisterUserWithEmptyPassword() {
    User user = User.builder()
        .name("John Doe")
        .email("john@example.com")
        .passwordHash("")
        .dateOfBirth(LocalDate.of(1990, 1, 1))
        .build();

    assertThrows(IllegalArgumentException.class, () -> userService.register(user));
  }

  @Test
  @DisplayName("Should reject duplicate email with different case")
  void testRegisterDuplicateEmailDifferentCase() {
    User user1 = User.builder()
        .name("John Doe")
        .email("john@example.com")
        .passwordHash("Password123")
        .dateOfBirth(LocalDate.of(1990, 1, 1))
        .phone("252-555-1234")
        .build();
    
    userService.register(user1);

    User user2 = User.builder()
        .name("Jane Doe")
        .email("JOHN@EXAMPLE.COM")
        .passwordHash("Password456")
        .dateOfBirth(LocalDate.of(1992, 3, 20))
        .build();

    assertThrows(IllegalArgumentException.class, () -> userService.register(user2));
  }

  @Test
  @DisplayName("Should auto-verify age for legal drinking age user")
  void testRegisterAdultSetsAgeVerified() {
    User user = User.builder()
        .name("Adult")
        .email("adult@example.com")
        .passwordHash("Password123")
        .dateOfBirth(LocalDate.of(1990, 1, 1))
        .phone("252-555-1234")
        .build();

    User registered = userService.register(user);

    assertTrue(registered.isAgeVerified());
  }

  @Test
  @DisplayName("Should verify age on exact 21st birthday")
  void testRegisterUserOnExact21stBirthday() {
    User user = User.builder()
        .name("JustTurned21")
        .email("justturned21@example.com")
        .passwordHash("Password123")
        .dateOfBirth(LocalDate.now().minusYears(21))
        .phone("252-555-1234")
        .build();

    User registered = userService.register(user);

    assertTrue(registered.isAgeVerified());
  }

  @Test
  @DisplayName("Should NOT verify age for user one day before 21st birthday")
  void testRegisterUserOneDayBefore21stBirthday() {
    User user = User.builder()
        .name("Almost21")
        .email("almost21@example.com")
        .passwordHash("Password123")
        .dateOfBirth(LocalDate.now().minusYears(21).plusDays(1))
        .phone("252-555-1234")
        .build();

    User registered = userService.register(user);

    assertFalse(registered.isAgeVerified());
  }

  @Test
  @DisplayName("Should verify age for user much older than 21")
  void testRegisterVeryOldUser() {
    User user = User.builder()
        .name("Senior")
        .email("senior@example.com")
        .passwordHash("Password123")
        .dateOfBirth(LocalDate.of(1950, 1, 1))
        .phone("252-555-1234")
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
        .passwordHash("Password123")
        .dateOfBirth(LocalDate.of(2015, 1, 1))
        .phone("252-555-1234")
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
    request.setPassword("Password123");
    request.setPhone("919-555-1234");
    request.setDateOfBirth(LocalDate.of(1992, 3, 20));

    User registered = userService.registerUser(request);

    assertNotNull(registered);
    assertEquals("jane@example.com", registered.getEmail());
    assertEquals("Jane Doe", registered.getName());
    assertEquals("919-555-1234", registered.getPhone());
  }

  @Test
  @DisplayName("Should throw exception when RegisterUserRequest is null")
  void testRegisterUserFromNullRequest() {
    assertThrows(IllegalArgumentException.class, () -> userService.registerUser(null));
  }

  @Test
  @DisplayName("Should throw exception when RegisterUserRequest email is invalid")
  void testRegisterUserFromRequestWithInvalidEmail() {
    RegisterUserRequest request = new RegisterUserRequest();
    request.setName("Test");
    request.setEmail("invalidemail");
    request.setPassword("Password123");

    assertThrows(IllegalArgumentException.class, () -> userService.registerUser(request));
  }

  @Test
  @DisplayName("Should throw exception when RegisterUserRequest password is invalid")
  void testRegisterUserFromRequestWithInvalidPassword() {
    RegisterUserRequest request = new RegisterUserRequest();
    request.setName("Test");
    request.setEmail("test@example.com");
    request.setPassword("weak");

    assertThrows(IllegalArgumentException.class, () -> userService.registerUser(request));
  }

  @Test
  @DisplayName("Should throw exception when name is null")
  void testRegisterUserFromRequestWithNullName() {
    RegisterUserRequest request = new RegisterUserRequest();
    request.setName(null);
    request.setEmail("jane@example.com");
    request.setPassword("Password123");
    request.setPhone("333-555-1234");
    request.setDateOfBirth(LocalDate.of(1992, 3, 20));

    assertThrows(IllegalArgumentException.class, () -> userService.registerUser(request));
  }

  @Test
  @DisplayName("Should throw exception when phone is null")
  void testRegisterUserFromRequestWithNullPhone() {
    RegisterUserRequest request = new RegisterUserRequest();
    request.setName("Jane Doe");
    request.setEmail("jane@example.com");
    request.setPassword("Password123");
    request.setPhone(null);
    request.setDateOfBirth(LocalDate.of(1992, 3, 20));

    assertThrows(IllegalArgumentException.class, () -> userService.registerUser(request));
  }

  @Test
  @DisplayName("Should throw exception when date of birth is null")
  void testRegisterUserFromRequestWithNullDateOfBirth() {
    RegisterUserRequest request = new RegisterUserRequest();
    request.setName("Jane Doe");
    request.setEmail("jane@example.com");
    request.setPassword("Password123");
    request.setPhone("333-555-1234");
    request.setDateOfBirth(null);

    assertThrows(IllegalArgumentException.class, () -> userService.registerUser(request));
  }

  @Test
  @DisplayName("Should reject duplicate email in RegisterUserRequest")
  void testRegisterDuplicateEmailFromRequest() {
    RegisterUserRequest request1 = new RegisterUserRequest();
    request1.setName("User One");
    request1.setEmail("duplicate@example.com");
    request1.setPassword("Password123");
    request1.setPhone("555-1111");
    request1.setDateOfBirth(LocalDate.of(1990, 1, 1));

    userService.registerUser(request1);

    RegisterUserRequest request2 = new RegisterUserRequest();
    request2.setName("User Two");
    request2.setEmail("duplicate@example.com");
    request2.setPassword("Password456");
    request2.setPhone("555-2222");
    request2.setDateOfBirth(LocalDate.of(1992, 3, 20));

    assertThrows(IllegalArgumentException.class, () -> userService.registerUser(request2));
  }

  // ==================== LOGIN TESTS ====================

  @Test
  @DisplayName("Should login with correct email and password")
  void testLoginSuccess() {
    User user = User.builder()
        .name("John Doe")
        .email("john@example.com")
        .passwordHash("Password123")
        .dateOfBirth(LocalDate.of(1990, 5, 15))
        .phone("252-555-1234")
        .build();
    
    userService.register(user);

    User loggedIn = userService.login("john@example.com", "Password123");

    assertNotNull(loggedIn);
    assertEquals("john@example.com", loggedIn.getEmail());
  }

  @Test
  @DisplayName("Should return null for incorrect password")
  void testLoginIncorrectPassword() {
    User user = User.builder()
        .name("John Doe")
        .email("john@example.com")
        .passwordHash("Password123")
        .dateOfBirth(LocalDate.of(1990, 5, 15))
        .phone("252-555-1234")
        .build();
    
    userService.register(user);

    User loggedIn = userService.login("john@example.com", "wrongpassword");

    assertNull(loggedIn);
  }

  @Test
  @DisplayName("Should return null for non-existent user")
  void testLoginUserNotFound() {
    User loggedIn = userService.login("nonexistent@example.com", "Password123");

    assertNull(loggedIn);
  }

  @Test
  @DisplayName("Should login with case-insensitive email")
  void testLoginCaseInsensitiveEmail() {
    User user = User.builder()
        .name("John Doe")
        .email("john@example.com")
        .passwordHash("Password123")
        .dateOfBirth(LocalDate.of(1990, 5, 15))
        .phone("252-555-1234")
        .build();
    
    userService.register(user);

    User loggedIn = userService.login("JOHN@EXAMPLE.COM", "Password123");

    assertNotNull(loggedIn);
    assertEquals("john@example.com", loggedIn.getEmail());
  }
  @Test
  @DisplayName("Should return null when password is case-sensitive wrong")
  void testLoginPasswordCaseSensitive() {
    User user = User.builder()
        .name("John Doe")
        .email("john@example.com")
        .passwordHash("Password123")
        .phone("555-123-4567")
        .dateOfBirth(LocalDate.of(1990, 5, 15))
        .build();
    
    userService.register(user);

    User loggedIn = userService.login("john@example.com", "password123");

    assertNull(loggedIn);
  }

  @Test
  @DisplayName("Should return null for non-existent email")
  void testLoginNonExistentEmail() {
    User loggedIn = userService.login("nonexistent@example.com", "Password123");

    assertNull(loggedIn);
  }

  @Test
  @DisplayName("Should return null for empty email")
  void testLoginEmptyEmail() {
    User loggedIn = userService.login("", "Password123");

    assertNull(loggedIn);
  }

  @Test
  @DisplayName("Should return null for empty password")
  void testLoginEmptyPassword() {
    User user = User.builder()
        .name("John Doe")
        .email("john@example.com")
        .passwordHash("Password123")
        .phone("555-123-4567")
        .dateOfBirth(LocalDate.of(1990, 5, 15))
        .build();
    
    userService.register(user);

    User loggedIn = userService.login("john@example.com", "");

    assertNull(loggedIn);
  }

  // ==================== RETRIEVAL TESTS ====================

  @Test
  @DisplayName("Should retrieve user by ID")
  void testGetUserById() {
    User user = User.builder()
        .name("John Doe")
        .email("john@example.com")
        .passwordHash("Password123")
        .dateOfBirth(LocalDate.of(1990, 5, 15))
        .phone("252-555-1234")
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
        .passwordHash("Password123")
        .dateOfBirth(LocalDate.of(1990, 1, 1))
        .phone("252-555-1234")
        .build();
    User user2 = User.builder()
        .name("Jane")
        .email("jane@example.com")
        .passwordHash("Password456")
        .dateOfBirth(LocalDate.of(1992, 3, 20))
        .phone("252-555-1235")
        .build();

    userService.register(user1);
    userService.register(user2);

    List<User> allUsers = userService.getAllUsers();

    assertEquals(2, allUsers.size());
  }

  @Test
  @DisplayName("Should return empty optional for null user ID")
  void testGetUserByIdNullId() {
    Optional<User> retrieved = userService.getUserById(null);

    assertFalse(retrieved.isPresent());
  }

  @Test
  @DisplayName("Should return empty optional for negative user ID")
  void testGetUserByIdNegativeId() {
    Optional<User> retrieved = userService.getUserById(-1L);

    assertFalse(retrieved.isPresent());
  }

  @Test
  @DisplayName("Should return empty optional for zero user ID")
  void testGetUserByIdZeroId() {
    Optional<User> retrieved = userService.getUserById(0L);

    assertFalse(retrieved.isPresent());
  }

  @Test
  @DisplayName("Should retrieve all users returns empty list initially")
  void testGetAllUsersEmpty() {
    List<User> allUsers = userService.getAllUsers();

    assertTrue(allUsers.isEmpty());
  }

  @Test
  @DisplayName("Should retrieve all users does not affect original list")
  void testGetAllUsersReturnsIndependentList() {
    User user = User.builder()
        .name("John")
        .email("john@example.com")
        .passwordHash("Password123")
        .dateOfBirth(LocalDate.of(1990, 1, 1))
        .phone("252-555-1234")
        .build();

    userService.register(user);

    List<User> allUsers = userService.getAllUsers();
    allUsers.clear();

    // Original list should not be affected
    assertEquals(1, userService.getAllUsers().size());
  }

  // ==================== UPDATE TESTS ====================

  @Test
  @DisplayName("Should update user email")
  void testUpdateUserEmail() {
    User user = User.builder()
        .name("John")
        .email("john@example.com")
        .passwordHash("Password123")
        .dateOfBirth(LocalDate.of(1990, 1, 1))
        .phone("252-555-1234")
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
        .passwordHash("Password123")
        .dateOfBirth(LocalDate.of(1990, 1, 1))
        .phone("252-555-1234")
        .build();
    
    User registered = userService.register(user);

    User updatedUser = User.builder()
        .passwordHash("NewPassword456")
        .build();
    User result = userService.updateUser(registered.getId(), updatedUser);

    assertNotNull(result);
    assertEquals("NewPassword456", result.getPasswordHash());
  }

  @Test
  @DisplayName("Should update user date of birth and verify age")
  void testUpdateUserDateOfBirthAndVerifyAge() {
    User user = User.builder()
        .name("John")
        .email("john@example.com")
        .passwordHash("Password123")
        .dateOfBirth(LocalDate.of(1990, 1, 1))
        .phone("252-555-1234")
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

  @Test
  @DisplayName("Should update user and preserve other fields")
  void testUpdateUserPreservesOtherFields() {
    User user = User.builder()
        .name("John")
        .email("john@example.com")
        .passwordHash("Password123")
        .dateOfBirth(LocalDate.of(1990, 1, 1))
        .phone("252-555-1234")
        .build();
    
    User registered = userService.register(user);

    User updatedUser = User.builder()
        .email("newemail@example.com")
        .build();
    User result = userService.updateUser(registered.getId(), updatedUser);

    // Email should be updated
    assertEquals("newemail@example.com", result.getEmail());
    // But other fields should remain unchanged
    assertEquals("John", result.getName());
    assertEquals("252-555-1234", result.getPhone());
    assertEquals(LocalDate.of(1990, 1, 1), result.getDateOfBirth());
  }

  @Test
  @DisplayName("Should return null when updating with null ID")
  void testUpdateUserWithNullId() {
    User updatedUser = User.builder()
        .email("newemail@example.com")
        .build();
    User result = userService.updateUser(null, updatedUser);

    assertNull(result);
  }

  @Test
  @DisplayName("Should not update user when updated user is null")
  void testUpdateUserWithNullUpdatedUser() {
    User user = User.builder()
        .name("John")
        .email("john@example.com")
        .passwordHash("Password123")
        .dateOfBirth(LocalDate.of(1990, 1, 1))
        .phone("252-555-1234")
        .build();
    
    User registered = userService.register(user);

    assertThrows(IllegalArgumentException.class, 
        () -> userService.updateUser(registered.getId(), null));
  }

  @Test
  @DisplayName("Should verify age when updating date of birth to valid age")
  void testUpdateDateOfBirthToValidAge() {
    User user = User.builder()
        .name("John")
        .email("john@example.com")
        .passwordHash("Password123")
        .dateOfBirth(LocalDate.of(2015, 1, 1))
        .phone("252-555-1234")
        .ageVerified(false)
        .build();
    
    User registered = userService.register(user);

    User updatedUser = User.builder()
        .dateOfBirth(LocalDate.of(1990, 1, 1))
        .build();
    User result = userService.updateUser(registered.getId(), updatedUser);

    assertTrue(result.isAgeVerified());
  }

  @Test
  @DisplayName("Should update multiple fields at once")
  void testUpdateMultipleFields() {
    User user = User.builder()
        .name("John")
        .email("john@example.com")
        .passwordHash("Password123")
        .dateOfBirth(LocalDate.of(1990, 1, 1))
        .phone("252-555-1234")
        .build();
    
    User registered = userService.register(user);

    User updatedUser = User.builder()
        .email("newemail@example.com")
        .passwordHash("NewPassword456")
        .dateOfBirth(LocalDate.of(1995, 6, 15))
        .build();
    User result = userService.updateUser(registered.getId(), updatedUser);

    assertEquals("newemail@example.com", result.getEmail());
    assertEquals("NewPassword456", result.getPasswordHash());
    assertEquals(LocalDate.of(1995, 6, 15), result.getDateOfBirth());
  }

  // ==================== DELETE TESTS ====================

  @Test
  @DisplayName("Should delete user by ID")
  void testDeleteUser() {
    User user = User.builder()
        .name("John")
        .email("john@example.com")
        .passwordHash("Password123")
        .dateOfBirth(LocalDate.of(1990, 1, 1))
        .phone("252-555-1234")
        .build();
    
    User registered = userService.register(user);

    userService.deleteUser(registered.getId());

    Optional<User> deleted = userService.getUserById(registered.getId());
    assertFalse(deleted.isPresent());
  }

  @Test
  @DisplayName("Should return false when deleting non-existent user")
  void testDeleteNonExistentUser() {
    boolean deleted = userService.deleteUser(999L);
    
    assertFalse(deleted);
  }
  
  @Test
  @DisplayName("Should have correct user count after deletion")
  void testUserCountAfterDeletion() {
    User user1 = User.builder()
        .name("John")
        .email("john@example.com")
        .passwordHash("Password123")
        .dateOfBirth(LocalDate.of(1990, 1, 1))
        .phone("252-555-1235")
        .build();
    User user2 = User.builder()
        .name("Jane")
        .email("jane@example.com")
        .passwordHash("Password456")
        .dateOfBirth(LocalDate.of(1992, 3, 20))
        .phone("252-555-1234")
        .build();

    User registered1 = userService.register(user1);
    userService.register(user2);

    userService.deleteUser(registered1.getId());

    assertEquals(1, userService.getAllUsers().size());
  }

  @Test
  @DisplayName("Should throw exception when deleting with null ID")
  void testDeleteUserWithNullId() {
    assertThrows(IllegalArgumentException.class, () -> userService.deleteUser(null));
  }

  @Test
  @DisplayName("Should only delete specified user, not all users")
  void testDeleteOnlySpecificUser() {
    User user1 = User.builder()
        .name("John")
        .email("john@example.com")
        .passwordHash("Password123")
        .dateOfBirth(LocalDate.of(1990, 1, 1))
        .phone("252-555-1235")
        .build();
    User user2 = User.builder()
        .name("Jane")
        .email("jane@example.com")
        .passwordHash("Password456")
        .dateOfBirth(LocalDate.of(1992, 3, 20))
        .phone("252-555-1234")
        .build();
    User user3 = User.builder()
        .name("Bob")
        .email("bob@example.com")
        .passwordHash("Password789")
        .dateOfBirth(LocalDate.of(1988, 6, 10))
        .phone("252-555-1236")
        .build();

    User registered1 = userService.register(user1);
    User registered2 = userService.register(user2);
    userService.register(user3);

    userService.deleteUser(registered1.getId());

    assertEquals(2, userService.getAllUsers().size());
    assertFalse(userService.getUserById(registered1.getId()).isPresent());
    assertTrue(userService.getUserById(registered2.getId()).isPresent());
  }

  @Test
  @DisplayName("Should delete all users one by one")
  void testDeleteAllUsersSequentially() {
    User user1 = User.builder()
        .name("John")
        .email("john@example.com")
        .passwordHash("Password123")
        .dateOfBirth(LocalDate.of(1990, 1, 1))
        .phone("252-555-1235")
        .build();
    User user2 = User.builder()
        .name("Jane")
        .email("jane@example.com")
        .passwordHash("Password456")
        .dateOfBirth(LocalDate.of(1992, 3, 20))
        .phone("252-555-1234")
        .build();

    User registered1 = userService.register(user1);
    User registered2 = userService.register(user2);

    userService.deleteUser(registered1.getId());
    assertEquals(1, userService.getAllUsers().size());

    userService.deleteUser(registered2.getId());
    assertEquals(0, userService.getAllUsers().size());
  }

  // ==================== UNIQUE ID GENERATION TESTS ====================

  @Test
  @DisplayName("Should generate unique user IDs sequentially")
  void testUserIdGenerationIsUnique() {
    User user1 = User.builder()
        .name("John")
        .email("john@example.com")
        .passwordHash("Password123")
        .dateOfBirth(LocalDate.of(1990, 1, 1))
        .phone("252-555-1235")
        .build();
    User user2 = User.builder()
        .name("Jane")
        .email("jane@example.com")
        .passwordHash("Password456")
        .dateOfBirth(LocalDate.of(1992, 3, 20))
        .phone("252-555-1236")
        .build();
    User user3 = User.builder()
        .name("Bob")
        .email("bob@example.com")
        .passwordHash("Password789")
        .dateOfBirth(LocalDate.of(1988, 6, 10))
        .phone("252-555-1234")
        .build();

    User registered1 = userService.register(user1);
    User registered2 = userService.register(user2);
    User registered3 = userService.register(user3);

    assertNotEquals(registered1.getId(), registered2.getId());
    assertNotEquals(registered2.getId(), registered3.getId());
    assertNotEquals(registered1.getId(), registered3.getId());

    assertEquals(registered1.getId() + 1, registered2.getId());
    assertEquals(registered2.getId() + 1, registered3.getId());
  }
}