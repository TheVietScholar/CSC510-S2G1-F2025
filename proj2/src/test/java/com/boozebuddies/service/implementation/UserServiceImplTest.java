package com.boozebuddies.service.implementation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.boozebuddies.dto.RegisterUserRequest;
import com.boozebuddies.entity.User;
import com.boozebuddies.exception.UserAlreadyExistsException;
import com.boozebuddies.exception.UserNotFoundException;
import com.boozebuddies.model.Role;
import com.boozebuddies.repository.UserRepository;
import com.boozebuddies.service.ValidationService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceImplTest {

  @Mock private UserRepository userRepository;
  @Mock private ValidationService validationService;
  @Mock private PasswordEncoder passwordEncoder;

  @InjectMocks private UserServiceImpl userService;

  private User testUser;

  @BeforeEach
  void setUp() {
    testUser =
        User.builder()
            .id(1L)
            .name("John Doe")
            .email("john@example.com")
            .phone("555-1234")
            .dateOfBirth(LocalDate.of(1990, 1, 1))
            .isActive(true)
            .ageVerified(true)
            .build();
  }

  // ==================== REGISTER USER ====================

  @Test
  @DisplayName("registerUser should successfully register a valid user")
  void registerUser_ValidRequest_Success() {
    RegisterUserRequest request = new RegisterUserRequest();
    request.setName("John Doe");
    request.setEmail("john@example.com");
    request.setPassword("Password123");
    request.setPhone("555-1234");
    request.setDateOfBirth(LocalDate.of(1990, 1, 1));

    when(validationService.validateEmail("john@example.com")).thenReturn(true);
    when(validationService.validatePassword("Password123")).thenReturn(true);
    when(userRepository.existsByEmailIgnoreCase("john@example.com")).thenReturn(false);
    when(validationService.validateAge(any(User.class))).thenReturn(true);
    when(passwordEncoder.encode("Password123")).thenReturn("encoded-password");
    when(userRepository.save(any(User.class)))
        .thenAnswer(
            invocation -> {
              User user = invocation.getArgument(0);
              user.setId(1L);
              return user;
            });

    User result = userService.registerUser(request);

    assertNotNull(result);
    assertEquals("John Doe", result.getName());
    assertEquals("john@example.com", result.getEmail());
    assertEquals("encoded-password", result.getPasswordHash());
    assertEquals("555-1234", result.getPhone());
    assertTrue(result.isActive());
    assertFalse(result.isEmailVerified());
    assertTrue(result.isAgeVerified());
    assertTrue(result.getRoles().contains(Role.USER));
    verify(userRepository, times(1)).save(any(User.class));
  }

  @Test
  @DisplayName("registerUser should throw exception when request is null")
  void registerUser_NullRequest_ThrowsException() {
    assertThrows(IllegalArgumentException.class, () -> userService.registerUser(null));
    verify(userRepository, never()).save(any());
  }

  @Test
  @DisplayName("registerUser should throw exception when name is null")
  void registerUser_NullName_ThrowsException() {
    RegisterUserRequest request = new RegisterUserRequest();
    request.setName(null);
    request.setEmail("john@example.com");
    request.setPassword("Password123");
    request.setPhone("555-1234");
    request.setDateOfBirth(LocalDate.of(1990, 1, 1));

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(request));

    assertEquals("Name is required", exception.getMessage());
    verify(userRepository, never()).save(any());
  }

  @Test
  @DisplayName("registerUser should throw exception when name is empty")
  void registerUser_EmptyName_ThrowsException() {
    RegisterUserRequest request = new RegisterUserRequest();
    request.setName("");
    request.setEmail("john@example.com");
    request.setPassword("Password123");
    request.setPhone("555-1234");
    request.setDateOfBirth(LocalDate.of(1990, 1, 1));

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(request));

    assertEquals("Name is required", exception.getMessage());
    verify(userRepository, never()).save(any());
  }

  @Test
  @DisplayName("registerUser should throw exception when phone is null")
  void registerUser_NullPhone_ThrowsException() {
    RegisterUserRequest request = new RegisterUserRequest();
    request.setName("John Doe");
    request.setEmail("john@example.com");
    request.setPassword("Password123");
    request.setPhone(null);
    request.setDateOfBirth(LocalDate.of(1990, 1, 1));

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(request));

    assertEquals("Phone is required", exception.getMessage());
    verify(userRepository, never()).save(any());
  }

  @Test
  @DisplayName("registerUser should throw exception when phone is empty")
  void registerUser_EmptyPhone_ThrowsException() {
    RegisterUserRequest request = new RegisterUserRequest();
    request.setName("John Doe");
    request.setEmail("john@example.com");
    request.setPassword("Password123");
    request.setPhone("");
    request.setDateOfBirth(LocalDate.of(1990, 1, 1));

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(request));

    assertEquals("Phone is required", exception.getMessage());
    verify(userRepository, never()).save(any());
  }

  @Test
  @DisplayName("registerUser should throw exception when date of birth is null")
  void registerUser_NullDateOfBirth_ThrowsException() {
    RegisterUserRequest request = new RegisterUserRequest();
    request.setName("John Doe");
    request.setEmail("john@example.com");
    request.setPassword("Password123");
    request.setPhone("555-1234");
    request.setDateOfBirth(null);

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(request));

    assertEquals("Date of birth is required", exception.getMessage());
    verify(userRepository, never()).save(any());
  }

  @Test
  @DisplayName("registerUser should throw exception when email is invalid")
  void registerUser_InvalidEmail_ThrowsException() {
    RegisterUserRequest request = new RegisterUserRequest();
    request.setName("John Doe");
    request.setEmail("invalid-email");
    request.setPassword("Password123");
    request.setPhone("555-1234");
    request.setDateOfBirth(LocalDate.of(1990, 1, 1));

    when(validationService.validateEmail("invalid-email")).thenReturn(false);

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(request));

    assertEquals("Email is invalid or empty", exception.getMessage());
    verify(userRepository, never()).save(any());
  }

  @Test
  @DisplayName("registerUser should throw exception when password is invalid")
  void registerUser_InvalidPassword_ThrowsException() {
    RegisterUserRequest request = new RegisterUserRequest();
    request.setName("John Doe");
    request.setEmail("john@example.com");
    request.setPassword("weak");
    request.setPhone("555-1234");
    request.setDateOfBirth(LocalDate.of(1990, 1, 1));

    when(validationService.validateEmail("john@example.com")).thenReturn(true);
    when(validationService.validatePassword("weak")).thenReturn(false);

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(request));

    assertEquals(
        "Password must be at least 8 characters with letters and numbers", exception.getMessage());
    verify(userRepository, never()).save(any());
  }

  @Test
  @DisplayName("registerUser should throw exception when email already exists")
  void registerUser_EmailExists_ThrowsException() {
    RegisterUserRequest request = new RegisterUserRequest();
    request.setName("John Doe");
    request.setEmail("existing@example.com");
    request.setPassword("Password123");
    request.setPhone("555-1234");
    request.setDateOfBirth(LocalDate.of(1990, 1, 1));

    when(validationService.validateEmail("existing@example.com")).thenReturn(true);
    when(validationService.validatePassword("Password123")).thenReturn(true);
    when(userRepository.existsByEmailIgnoreCase("existing@example.com")).thenReturn(true);

    assertThrows(UserAlreadyExistsException.class, () -> userService.registerUser(request));
    verify(userRepository, never()).save(any());
  }

  @Test
  @DisplayName("registerUser should set ageVerified to false for underage user")
  void registerUser_UnderageUser_AgeVerifiedFalse() {
    RegisterUserRequest request = new RegisterUserRequest();
    request.setName("Young User");
    request.setEmail("young@example.com");
    request.setPassword("Password123");
    request.setPhone("555-1234");
    request.setDateOfBirth(LocalDate.of(2015, 1, 1)); // Underage

    when(validationService.validateEmail("young@example.com")).thenReturn(true);
    when(validationService.validatePassword("Password123")).thenReturn(true);
    when(userRepository.existsByEmailIgnoreCase("young@example.com")).thenReturn(false);
    when(validationService.validateAge(any(User.class))).thenReturn(false);
    when(passwordEncoder.encode("Password123")).thenReturn("encoded-password");
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    User result = userService.registerUser(request);

    assertFalse(result.isAgeVerified());
    verify(validationService, times(1)).validateAge(any(User.class));
  }

  @Test
  @DisplayName("registerUser should assign USER role by default")
  void registerUser_AssignsUserRole() {
    RegisterUserRequest request = new RegisterUserRequest();
    request.setName("John Doe");
    request.setEmail("john@example.com");
    request.setPassword("Password123");
    request.setPhone("555-1234");
    request.setDateOfBirth(LocalDate.of(1990, 1, 1));

    when(validationService.validateEmail("john@example.com")).thenReturn(true);
    when(validationService.validatePassword("Password123")).thenReturn(true);
    when(userRepository.existsByEmailIgnoreCase("john@example.com")).thenReturn(false);
    when(validationService.validateAge(any(User.class))).thenReturn(true);
    when(passwordEncoder.encode("Password123")).thenReturn("encoded-password");
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    User result = userService.registerUser(request);

    assertNotNull(result.getRoles());
    assertEquals(1, result.getRoles().size());
    assertTrue(result.getRoles().contains(Role.USER));
  }

  @Test
  @DisplayName("registerUser should encode password before saving")
  void registerUser_EncodesPassword() {
    RegisterUserRequest request = new RegisterUserRequest();
    request.setName("John Doe");
    request.setEmail("john@example.com");
    request.setPassword("PlainPassword123");
    request.setPhone("555-1234");
    request.setDateOfBirth(LocalDate.of(1990, 1, 1));

    when(validationService.validateEmail("john@example.com")).thenReturn(true);
    when(validationService.validatePassword("PlainPassword123")).thenReturn(true);
    when(userRepository.existsByEmailIgnoreCase("john@example.com")).thenReturn(false);
    when(validationService.validateAge(any(User.class))).thenReturn(true);
    when(passwordEncoder.encode("PlainPassword123")).thenReturn("$2a$10$encoded...");
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    User result = userService.registerUser(request);

    assertEquals("$2a$10$encoded...", result.getPasswordHash());
    verify(passwordEncoder, times(1)).encode("PlainPassword123");
  }

  @Test
  @DisplayName("registerUser should set emailVerified to false by default")
  void registerUser_EmailVerifiedFalse() {
    RegisterUserRequest request = new RegisterUserRequest();
    request.setName("John Doe");
    request.setEmail("john@example.com");
    request.setPassword("Password123");
    request.setPhone("555-1234");
    request.setDateOfBirth(LocalDate.of(1990, 1, 1));

    when(validationService.validateEmail("john@example.com")).thenReturn(true);
    when(validationService.validatePassword("Password123")).thenReturn(true);
    when(userRepository.existsByEmailIgnoreCase("john@example.com")).thenReturn(false);
    when(validationService.validateAge(any(User.class))).thenReturn(true);
    when(passwordEncoder.encode("Password123")).thenReturn("encoded-password");
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    User result = userService.registerUser(request);

    assertFalse(result.isEmailVerified());
  }

  @Test
  @DisplayName("registerUser should set isActive to true by default")
  void registerUser_ActiveByDefault() {
    RegisterUserRequest request = new RegisterUserRequest();
    request.setName("John Doe");
    request.setEmail("john@example.com");
    request.setPassword("Password123");
    request.setPhone("555-1234");
    request.setDateOfBirth(LocalDate.of(1990, 1, 1));

    when(validationService.validateEmail("john@example.com")).thenReturn(true);
    when(validationService.validatePassword("Password123")).thenReturn(true);
    when(userRepository.existsByEmailIgnoreCase("john@example.com")).thenReturn(false);
    when(validationService.validateAge(any(User.class))).thenReturn(true);
    when(passwordEncoder.encode("Password123")).thenReturn("encoded-password");
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    User result = userService.registerUser(request);

    assertTrue(result.isActive());
  }

  // ==================== GET USER BY ID ====================
  @Test
  @DisplayName("getUserById should return user when exists")
  void getUserById_UserExists_ReturnsUser() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

    Optional<User> result = userService.getUserById(1L);

    assertTrue(result.isPresent());
    assertEquals("john@example.com", result.get().getEmail());
    verify(userRepository, times(1)).findById(1L);
  }

  @Test
  @DisplayName("getUserById should return empty when user not found")
  void getUserById_UserNotFound_ReturnsEmpty() {
    when(userRepository.findById(999L)).thenReturn(Optional.empty());

    Optional<User> result = userService.getUserById(999L);

    assertFalse(result.isPresent());
    verify(userRepository, times(1)).findById(999L);
  }

  // ==================== FIND BY ID (throws exception) ====================

  @Test
  @DisplayName("findById should return user when exists")
  void findById_UserExists_ReturnsUser() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

    User result = userService.findById(1L);

    assertNotNull(result);
    assertEquals("john@example.com", result.getEmail());
    verify(userRepository, times(1)).findById(1L);
  }

  @Test
  @DisplayName("findById should throw exception when user not found")
  void findById_UserNotFound_ThrowsException() {
    when(userRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> userService.findById(999L));
    verify(userRepository, times(1)).findById(999L);
  }

  // ==================== FIND BY EMAIL ====================

  @Test
  @DisplayName("findByEmail should return user when exists")
  void findByEmail_UserExists_ReturnsUser() {
    when(userRepository.findByEmailIgnoreCase("john@example.com"))
        .thenReturn(Optional.of(testUser));

    Optional<User> result = userService.findByEmail("john@example.com");

    assertTrue(result.isPresent());
    assertEquals("john@example.com", result.get().getEmail());
    verify(userRepository, times(1)).findByEmailIgnoreCase("john@example.com");
  }

  @Test
  @DisplayName("findByEmail should return empty when user not found")
  void findByEmail_UserNotFound_ReturnsEmpty() {
    when(userRepository.findByEmailIgnoreCase("unknown@example.com")).thenReturn(Optional.empty());

    Optional<User> result = userService.findByEmail("unknown@example.com");

    assertFalse(result.isPresent());
    verify(userRepository, times(1)).findByEmailIgnoreCase("unknown@example.com");
  }

  // ==================== GET ALL USERS ====================

  @Test
  @DisplayName("getAllUsers should return list of all users")
  void getAllUsers_ReturnsAllUsers() {
    User user2 = User.builder().id(2L).email("jane@example.com").build();
    when(userRepository.findAll()).thenReturn(Arrays.asList(testUser, user2));

    List<User> result = userService.getAllUsers();

    assertEquals(2, result.size());
    verify(userRepository, times(1)).findAll();
  }

  @Test
  @DisplayName("getAllUsers should return empty list when no users")
  void getAllUsers_NoUsers_ReturnsEmptyList() {
    when(userRepository.findAll()).thenReturn(Arrays.asList());

    List<User> result = userService.getAllUsers();

    assertTrue(result.isEmpty());
    verify(userRepository, times(1)).findAll();
  }

  // ==================== UPDATE USER ====================

  @Test
  @DisplayName("updateUser should update name when provided")
  void updateUser_UpdateName_Success() {
    User updates = User.builder().name("Jane Doe").build();
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    User result = userService.updateUser(1L, updates);

    assertEquals("Jane Doe", testUser.getName());
    verify(userRepository, times(1)).save(testUser);
  }

  @Test
  @DisplayName("updateUser should update email and reset verification")
  void updateUser_UpdateEmail_ResetsVerification() {
    User updates = User.builder().email("newemail@example.com").build();
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(userRepository.existsByEmailIgnoreCase("newemail@example.com")).thenReturn(false);
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    User result = userService.updateUser(1L, updates);

    assertEquals("newemail@example.com", testUser.getEmail());
    assertFalse(testUser.isEmailVerified());
    verify(userRepository, times(1)).save(testUser);
  }

  @Test
  @DisplayName("updateUser should throw exception when email already exists")
  void updateUser_EmailExists_ThrowsException() {
    User updates = User.builder().email("existing@example.com").build();
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(userRepository.existsByEmailIgnoreCase("existing@example.com")).thenReturn(true);

    assertThrows(UserAlreadyExistsException.class, () -> userService.updateUser(1L, updates));
    verify(userRepository, never()).save(any());
  }

  @Test
  @DisplayName("updateUser should update date of birth and revalidate age")
  void updateUser_UpdateDateOfBirth_RevalidatesAge() {
    LocalDate newDob = LocalDate.of(2010, 1, 1);
    User updates = User.builder().dateOfBirth(newDob).build();
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(validationService.validateAge(any(User.class))).thenReturn(false);
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    User result = userService.updateUser(1L, updates);

    assertEquals(newDob, testUser.getDateOfBirth());
    assertFalse(testUser.isAgeVerified());
    verify(validationService, times(1)).validateAge(testUser);
    verify(userRepository, times(1)).save(testUser);
  }

  @Test
  @DisplayName("updateUser should throw exception when user not found")
  void updateUser_UserNotFound_ThrowsException() {
    User updates = User.builder().name("New Name").build();
    when(userRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> userService.updateUser(999L, updates));
    verify(userRepository, never()).save(any());
  }

  @Test
  @DisplayName("updateUser should throw exception when updated user is null")
  void updateUser_NullUser_ThrowsException() {
    assertThrows(IllegalArgumentException.class, () -> userService.updateUser(1L, null));
    verify(userRepository, never()).findById(any());
  }

  // ==================== DELETE USER ====================

  @Test
  @DisplayName("deleteUser should return true when user exists")
  void deleteUser_UserExists_ReturnsTrue() {
    when(userRepository.existsById(1L)).thenReturn(true);

    boolean result = userService.deleteUser(1L);

    assertTrue(result);
    verify(userRepository, times(1)).deleteById(1L);
  }

  @Test
  @DisplayName("deleteUser should return false when user not found")
  void deleteUser_UserNotFound_ReturnsFalse() {
    when(userRepository.existsById(999L)).thenReturn(false);

    boolean result = userService.deleteUser(999L);

    assertFalse(result);
    verify(userRepository, never()).deleteById(any());
  }

  // ==================== REFRESH TOKEN MANAGEMENT ====================

  @Test
  @DisplayName("saveRefreshToken should save token and expiry date")
  void saveRefreshToken_Success() {
    LocalDateTime expiry = LocalDateTime.now().plusDays(7);
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    userService.saveRefreshToken(1L, "refresh-token-123", expiry);

    assertEquals("refresh-token-123", testUser.getRefreshToken());
    assertEquals(expiry, testUser.getRefreshTokenExpiryDate());
    verify(userRepository, times(1)).save(testUser);
  }

  @Test
  @DisplayName("isRefreshTokenValid should return true for valid token")
  void isRefreshTokenValid_ValidToken_ReturnsTrue() {
    testUser.setRefreshToken("valid-token");
    testUser.setRefreshTokenExpiryDate(LocalDateTime.now().plusDays(1));
    when(userRepository.findByRefreshToken("valid-token")).thenReturn(Optional.of(testUser));

    boolean result = userService.isRefreshTokenValid("valid-token");

    assertTrue(result);
  }

  @Test
  @DisplayName("isRefreshTokenValid should return false for expired token")
  void isRefreshTokenValid_ExpiredToken_ReturnsFalse() {
    testUser.setRefreshToken("expired-token");
    testUser.setRefreshTokenExpiryDate(LocalDateTime.now().minusDays(1));
    when(userRepository.findByRefreshToken("expired-token")).thenReturn(Optional.of(testUser));

    boolean result = userService.isRefreshTokenValid("expired-token");

    assertFalse(result);
  }

  @Test
  @DisplayName("isRefreshTokenValid should return false for inactive user")
  void isRefreshTokenValid_InactiveUser_ReturnsFalse() {
    testUser.setActive(false);
    testUser.setRefreshToken("token");
    testUser.setRefreshTokenExpiryDate(LocalDateTime.now().plusDays(1));
    when(userRepository.findByRefreshToken("token")).thenReturn(Optional.of(testUser));

    boolean result = userService.isRefreshTokenValid("token");

    assertFalse(result);
  }

  @Test
  @DisplayName("isRefreshTokenValid should return false when token not found")
  void isRefreshTokenValid_TokenNotFound_ReturnsFalse() {
    when(userRepository.findByRefreshToken("unknown-token")).thenReturn(Optional.empty());

    boolean result = userService.isRefreshTokenValid("unknown-token");

    assertFalse(result);
  }

  @Test
  @DisplayName("revokeRefreshToken should clear token and expiry")
  void revokeRefreshToken_Success() {
    testUser.setRefreshToken("token");
    testUser.setRefreshTokenExpiryDate(LocalDateTime.now().plusDays(1));
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    userService.revokeRefreshToken(1L);

    assertNull(testUser.getRefreshToken());
    assertNull(testUser.getRefreshTokenExpiryDate());
    verify(userRepository, times(1)).save(testUser);
  }

  // ==================== USER ACTIVATION ====================

  @Test
  @DisplayName("deactivateUser should deactivate and revoke tokens")
  void deactivateUser_Success() {
    testUser.setRefreshToken("token");
    testUser.setRefreshTokenExpiryDate(LocalDateTime.now().plusDays(1));
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    userService.deactivateUser(1L);

    assertFalse(testUser.isActive());
    assertNull(testUser.getRefreshToken());
    assertNull(testUser.getRefreshTokenExpiryDate());
    verify(userRepository, times(1)).save(testUser);
  }

  @Test
  @DisplayName("activateUser should activate user")
  void activateUser_Success() {
    testUser.setActive(false);
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    userService.activateUser(1L);

    assertTrue(testUser.isActive());
    verify(userRepository, times(1)).save(testUser);
  }

  // ==================== BUSINESS LOGIC ====================

  @Test
  @DisplayName("canPlaceOrders should return true when user is active and age verified")
  void canPlaceOrders_ActiveAndAgeVerified_ReturnsTrue() {
    testUser.setActive(true);
    testUser.setAgeVerified(true);

    boolean result = userService.canPlaceOrders(testUser);

    assertTrue(result);
  }

  @Test
  @DisplayName("canPlaceOrders should return false when user is inactive")
  void canPlaceOrders_Inactive_ReturnsFalse() {
    testUser.setActive(false);
    testUser.setAgeVerified(true);

    boolean result = userService.canPlaceOrders(testUser);

    assertFalse(result);
  }

  @Test
  @DisplayName("canPlaceOrders should return false when age not verified")
  void canPlaceOrders_AgeNotVerified_ReturnsFalse() {
    testUser.setActive(true);
    testUser.setAgeVerified(false);

    boolean result = userService.canPlaceOrders(testUser);

    assertFalse(result);
  }

  // ==================== UPDATE LAST LOGIN ====================

  @Test
  @DisplayName("updateLastLogin should update timestamp")
  void updateLastLogin_Success() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    userService.updateLastLogin(1L);

    assertNotNull(testUser.getLastLoginAt());
    verify(userRepository, times(1)).save(testUser);
  }
}
