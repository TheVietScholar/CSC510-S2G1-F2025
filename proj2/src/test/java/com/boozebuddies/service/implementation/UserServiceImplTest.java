package com.boozebuddies.service.implementation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.boozebuddies.dto.RegisterUserRequest;
import com.boozebuddies.entity.User;
import com.boozebuddies.exception.UserAlreadyExistsException;
import com.boozebuddies.exception.UserNotFoundException;
import com.boozebuddies.repository.UserRepository;
import com.boozebuddies.service.ValidationService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

  @Mock private UserRepository userRepository;
  @Mock private ValidationService validationService;
  @InjectMocks private UserServiceImpl userService;

  private User testUser;
  private RegisterUserRequest testRequest;

  @BeforeEach
  void setUp() {
    testUser =
        User.builder()
            .id(1L)
            .name("John Doe")
            .email("john@example.com")
            .passwordHash("SecurePass123")
            .phone("1234567890")
            .dateOfBirth(LocalDate.of(1990, 5, 15))
            .ageVerified(true)
            .isActive(true)
            .build();

    testRequest = new RegisterUserRequest();
    testRequest.setName("Jane Doe");
    testRequest.setEmail("jane@example.com");
    testRequest.setPassword("SecurePass123");
    testRequest.setPhone("0987654321");
    testRequest.setDateOfBirth(LocalDate.of(1992, 3, 20));
  }

  // ==================== registerUser(RegisterUserRequest) ====================

  @Test
  void testRegisterUser_Success() {
    when(validationService.validateEmail(anyString())).thenReturn(true);
    when(validationService.validatePassword(anyString())).thenReturn(true);
    when(validationService.validateAge(any(User.class))).thenReturn(true);
    when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    User result = userService.registerUser(testRequest);

    assertNotNull(result);
    verify(userRepository, times(1)).save(any(User.class));
  }

  @Test
  void testRegisterUser_RequestNull() {
    assertThrows(IllegalArgumentException.class, () -> userService.registerUser(null));
  }

  @Test
  void testRegisterUser_InvalidEmail() {
    when(validationService.validateEmail(anyString())).thenReturn(false);
    assertThrows(IllegalArgumentException.class, () -> userService.registerUser(testRequest));
  }

  @Test
  void testRegisterUser_EmailAlreadyExists() {
    when(validationService.validateEmail(anyString())).thenReturn(true);
    when(validationService.validatePassword(anyString())).thenReturn(true);
    when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(true);
    assertThrows(UserAlreadyExistsException.class, () -> userService.registerUser(testRequest));
  }

  // ==================== findById(Long) ====================

  @Test
  void testFindById_Success() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

    User result = userService.findById(1L);
    assertEquals("John Doe", result.getName());
  }

  @Test
  void testFindById_NotFound() {
    when(userRepository.findById(1L)).thenReturn(Optional.empty());
    assertThrows(UserNotFoundException.class, () -> userService.findById(1L));
  }

  // ==================== findByEmail(String) ====================

  @Test
  void testFindByEmail_Success() {
    when(userRepository.findByEmailIgnoreCase("john@example.com"))
        .thenReturn(Optional.of(testUser));

    Optional<User> result = userService.findByEmail("john@example.com");
    assertTrue(result.isPresent());
  }

  // ==================== updateLastLogin(Long) ====================

  @Test
  void testUpdateLastLogin_Success() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

    userService.updateLastLogin(1L);

    verify(userRepository, times(1)).save(any(User.class));
    assertNotNull(testUser.getLastLoginAt());
  }

  // ==================== saveRefreshToken(Long, String, LocalDateTime) ====================

  @Test
  void testSaveRefreshToken_Success() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

    userService.saveRefreshToken(1L, "refresh123", LocalDateTime.now().plusDays(1));

    verify(userRepository, times(1)).save(any(User.class));
    assertEquals("refresh123", testUser.getRefreshToken());
  }

  // ==================== isRefreshTokenValid(String) ====================

  @Test
  void testIsRefreshTokenValid_True() {
    testUser.setActive(true);
    testUser.setRefreshTokenExpiryDate(LocalDateTime.now().plusDays(1));
    when(userRepository.findByRefreshToken("validToken")).thenReturn(Optional.of(testUser));

    boolean result = userService.isRefreshTokenValid("validToken");

    assertTrue(result);
  }

  @Test
  void testIsRefreshTokenValid_False_NoUser() {
    when(userRepository.findByRefreshToken("missing")).thenReturn(Optional.empty());
    assertFalse(userService.isRefreshTokenValid("missing"));
  }

  @Test
  void testIsRefreshTokenValid_False_Expired() {
    testUser.setRefreshTokenExpiryDate(LocalDateTime.now().minusDays(1));
    when(userRepository.findByRefreshToken("expired")).thenReturn(Optional.of(testUser));
    assertFalse(userService.isRefreshTokenValid("expired"));
  }

  // ==================== findByRefreshToken(String) ====================

  @Test
  void testFindByRefreshToken_Success() {
    when(userRepository.findByRefreshToken("token")).thenReturn(Optional.of(testUser));

    Optional<User> result = userService.findByRefreshToken("token");

    assertTrue(result.isPresent());
  }

  // ==================== revokeRefreshToken(Long) ====================

  @Test
  void testRevokeRefreshToken_Success() {
    testUser.setRefreshToken("abc");
    testUser.setRefreshTokenExpiryDate(LocalDateTime.now().plusDays(1));
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

    userService.revokeRefreshToken(1L);

    assertNull(testUser.getRefreshToken());
    verify(userRepository, times(1)).save(any(User.class));
  }

  // ==================== deactivateUser(Long) ====================

  @Test
  void testDeactivateUser_Success() {
    testUser.setActive(true);
    testUser.setRefreshToken("abc");
    testUser.setRefreshTokenExpiryDate(LocalDateTime.now().plusDays(1));
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

    userService.deactivateUser(1L);

    assertFalse(testUser.isActive());
    assertNull(testUser.getRefreshToken());
  }

  // ==================== activateUser(Long) ====================

  @Test
  void testActivateUser_Success() {
    testUser.setActive(false);
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

    userService.activateUser(1L);

    assertTrue(testUser.isActive());
    verify(userRepository, times(1)).save(testUser);
  }

  // ==================== canPlaceOrders(User) ====================

  @Test
  void testCanPlaceOrders_True() {
    testUser.setActive(true);
    testUser.setAgeVerified(true);
    assertTrue(userService.canPlaceOrders(testUser));
  }

  @Test
  void testCanPlaceOrders_False_Inactive() {
    testUser.setActive(false);
    testUser.setAgeVerified(true);
    assertFalse(userService.canPlaceOrders(testUser));
  }

  @Test
  void testCanPlaceOrders_False_NotAgeVerified() {
    testUser.setActive(true);
    testUser.setAgeVerified(false);
    assertFalse(userService.canPlaceOrders(testUser));
  }

  // ==================== existing tests kept ====================

  @Test
  void testGetUserById_Success() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    Optional<User> result = userService.getUserById(1L);
    assertTrue(result.isPresent());
  }

  @Test
  void testGetUserById_NotFound() {
    when(userRepository.findById(999L)).thenReturn(Optional.empty());
    Optional<User> result = userService.getUserById(999L);
    assertFalse(result.isPresent());
  }

  @Test
  void testGetAllUsers_Success() {
    when(userRepository.findAll()).thenReturn(List.of(testUser));
    List<User> result = userService.getAllUsers();
    assertEquals(1, result.size());
  }

  @Test
  void testDeleteUser_Success() {
    when(userRepository.existsById(1L)).thenReturn(true);
    boolean result = userService.deleteUser(1L);
    assertTrue(result);
    verify(userRepository).deleteById(1L);
  }

  @Test
  void testDeleteUser_NotFound() {
    when(userRepository.existsById(1L)).thenReturn(false);
    boolean result = userService.deleteUser(1L);
    assertFalse(result);
  }

  // ==================== registerUser - Additional Coverage ====================

  @Test
  void testRegisterUser_NameNull() {
    testRequest.setName(null);
    assertThrows(IllegalArgumentException.class, () -> userService.registerUser(testRequest));
  }

  @Test
  void testRegisterUser_NameEmpty() {
    testRequest.setName("");
    assertThrows(IllegalArgumentException.class, () -> userService.registerUser(testRequest));
  }

  @Test
  void testRegisterUser_PhoneNull() {
    testRequest.setPhone(null);
    assertThrows(IllegalArgumentException.class, () -> userService.registerUser(testRequest));
  }

  @Test
  void testRegisterUser_PhoneEmpty() {
    testRequest.setPhone("");
    assertThrows(IllegalArgumentException.class, () -> userService.registerUser(testRequest));
  }

  @Test
  void testRegisterUser_DateOfBirthNull() {
    testRequest.setDateOfBirth(null);
    assertThrows(IllegalArgumentException.class, () -> userService.registerUser(testRequest));
  }

  @Test
  void testRegisterUser_InvalidPassword() {
    when(validationService.validateEmail(anyString())).thenReturn(true);
    when(validationService.validatePassword(anyString())).thenReturn(false);

    assertThrows(IllegalArgumentException.class, () -> userService.registerUser(testRequest));
  }

  // ==================== updateUser - Complete Coverage ====================

  @Test
  void testUpdateUser_Success_AllFields() {
    User updatedData =
        User.builder()
            .name("Updated Name")
            .email("newemail@example.com")
            .phone("9999999999")
            .dateOfBirth(LocalDate.of(1995, 6, 10))
            .address("123 New Street")
            .build();

    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(userRepository.existsByEmailIgnoreCase("newemail@example.com")).thenReturn(false);
    when(validationService.validateAge(any(User.class))).thenReturn(true);
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    User result = userService.updateUser(1L, updatedData);

    assertNotNull(result);
    verify(userRepository, times(1)).save(any(User.class));
  }

  @Test
  void testUpdateUser_OnlyName() {
    User updatedData = User.builder().name("Only Name Changed").build();

    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    User result = userService.updateUser(1L, updatedData);

    assertNotNull(result);
    assertEquals("Only Name Changed", testUser.getName());
  }

  @Test
  void testUpdateUser_OnlyEmail() {
    User updatedData = User.builder().email("newemail@example.com").build();

    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(userRepository.existsByEmailIgnoreCase("newemail@example.com")).thenReturn(false);
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    User result = userService.updateUser(1L, updatedData);

    assertNotNull(result);
    assertEquals("newemail@example.com", testUser.getEmail());
    assertFalse(testUser.isEmailVerified()); // Email verification reset
  }

  @Test
  void testUpdateUser_EmailAlreadyExists() {
    User updatedData = User.builder().email("existing@example.com").build();

    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(userRepository.existsByEmailIgnoreCase("existing@example.com")).thenReturn(true);

    assertThrows(UserAlreadyExistsException.class, () -> userService.updateUser(1L, updatedData));
  }

  @Test
  void testUpdateUser_SameEmail_NoError() {
    // User updating with their own email should not throw error
    User updatedData =
        User.builder()
            .email("john@example.com") // Same as testUser's email
            .name("Updated Name")
            .build();

    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    User result = userService.updateUser(1L, updatedData);

    assertNotNull(result);
    // Email should remain verified since it's the same email
  }

  @Test
  void testUpdateUser_OnlyPhone() {
    User updatedData = User.builder().phone("1111111111").build();

    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    User result = userService.updateUser(1L, updatedData);

    assertNotNull(result);
    assertEquals("1111111111", testUser.getPhone());
  }

  @Test
  void testUpdateUser_OnlyDateOfBirth() {
    User updatedData = User.builder().dateOfBirth(LocalDate.of(2000, 1, 1)).build();

    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(validationService.validateAge(any(User.class))).thenReturn(false);
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    User result = userService.updateUser(1L, updatedData);

    assertNotNull(result);
    assertEquals(LocalDate.of(2000, 1, 1), testUser.getDateOfBirth());
    assertFalse(testUser.isAgeVerified()); // Age verification updated
  }

  @Test
  void testUpdateUser_OnlyAddress() {
    User updatedData = User.builder().address("456 Updated St").build();

    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    User result = userService.updateUser(1L, updatedData);

    assertNotNull(result);
    assertEquals("456 Updated St", testUser.getAddress());
  }

  @Test
  void testUpdateUser_NullUser() {
    assertThrows(IllegalArgumentException.class, () -> userService.updateUser(1L, null));
  }

  @Test
  void testUpdateUser_UserNotFound() {
    User updatedData = User.builder().name("Test").build();

    when(userRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> userService.updateUser(999L, updatedData));
  }

  @Test
  void testUpdateUser_EmptyName_NotUpdated() {
    User updatedData = User.builder().name("").build();

    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    User result = userService.updateUser(1L, updatedData);

    // Empty name should not update
    assertEquals("John Doe", testUser.getName());
  }

  @Test
  void testUpdateUser_EmptyEmail_NotUpdated() {
    User updatedData = User.builder().email("").build();

    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    User result = userService.updateUser(1L, updatedData);

    // Empty email should not update
    assertEquals("john@example.com", testUser.getEmail());
  }

  @Test
  void testUpdateUser_EmptyPhone_NotUpdated() {
    User updatedData = User.builder().phone("").build();

    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    User result = userService.updateUser(1L, updatedData);

    // Empty phone should not update
    assertEquals("1234567890", testUser.getPhone());
  }

  // ==================== isRefreshTokenValid - Additional Coverage ====================

  @Test
  void testIsRefreshTokenValid_False_UserInactive() {
    testUser.setActive(false);
    testUser.setRefreshTokenExpiryDate(LocalDateTime.now().plusDays(1));
    when(userRepository.findByRefreshToken("token")).thenReturn(Optional.of(testUser));

    boolean result = userService.isRefreshTokenValid("token");

    assertFalse(result);
  }

  @Test
  void testIsRefreshTokenValid_False_NullExpiryDate() {
    testUser.setActive(true);
    testUser.setRefreshTokenExpiryDate(null);
    when(userRepository.findByRefreshToken("token")).thenReturn(Optional.of(testUser));

    boolean result = userService.isRefreshTokenValid("token");

    assertFalse(result);
  }
}
