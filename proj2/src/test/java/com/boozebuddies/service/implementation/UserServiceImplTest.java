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
}
