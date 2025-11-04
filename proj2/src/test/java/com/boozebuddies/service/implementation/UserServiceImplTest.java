package com.boozebuddies.service.implementation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.boozebuddies.dto.RegisterUserRequest;
import com.boozebuddies.entity.User;
import com.boozebuddies.exception.UserNotFoundException;
import com.boozebuddies.repository.UserRepository;
import com.boozebuddies.service.ValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ValidationService validationService;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private RegisterUserRequest testRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .passwordHash("SecurePass123")
                .phone("1234567890")
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .ageVerified(true)
                .build();

        testRequest = new RegisterUserRequest();
        testRequest.setName("Jane Doe");
        testRequest.setEmail("jane@example.com");
        testRequest.setPassword("SecurePass123");
        testRequest.setPhone("0987654321");
        testRequest.setDateOfBirth(LocalDate.of(1992, 3, 20));
    }

    // // ==================== register(User user) Tests ====================

    // @Test
    // void testRegister_Success() {
    //     when(validationService.validateEmail("john@example.com")).thenReturn(true);
    //     when(validationService.validatePassword("SecurePass123")).thenReturn(true);
    //     when(validationService.validateAge(testUser)).thenReturn(true);
    //     when(userRepository.existsByEmailIgnoreCase("john@example.com")).thenReturn(false);
    //     when(userRepository.save(testUser)).thenReturn(testUser);

    //     User result = userService.register(testUser);

    //     assertNotNull(result);
    //     assertEquals("John Doe", result.getName());
    //     assertTrue(result.isAgeVerified());
    //     verify(userRepository, times(1)).save(testUser);
    // }

    // @Test
    // void testRegister_UserNull() {
    //     assertThrows(IllegalArgumentException.class, () -> userService.register(null),
    //             "User cannot be null");
    // }

    // @Test
    // void testRegister_NameNull() {
    //     testUser.setName(null);
    //     assertThrows(IllegalArgumentException.class, () -> userService.register(testUser),
    //             "Name is required");
    // }

    // @Test
    // void testRegister_NameEmpty() {
    //     testUser.setName("");
    //     assertThrows(IllegalArgumentException.class, () -> userService.register(testUser),
    //             "Name is required");
    // }

    // @Test
    // void testRegister_PhoneNull() {
    //     testUser.setPhone(null);
    //     assertThrows(IllegalArgumentException.class, () -> userService.register(testUser),
    //             "Phone is required");
    // }

    // @Test
    // void testRegister_PhoneEmpty() {
    //     testUser.setPhone("");
    //     assertThrows(IllegalArgumentException.class, () -> userService.register(testUser),
    //             "Phone is required");
    // }

    // @Test
    // void testRegister_DateOfBirthNull() {
    //     testUser.setDateOfBirth(null);
    //     assertThrows(IllegalArgumentException.class, () -> userService.register(testUser),
    //             "Date of birth is required");
    // }

    // @Test
    // void testRegister_InvalidEmail() {
    //     when(validationService.validateEmail("invalid-email")).thenReturn(false);
    //     testUser.setEmail("invalid-email");

    //     assertThrows(IllegalArgumentException.class, () -> userService.register(testUser),
    //             "Email is invalid or empty");
    // }

    // @Test
    // void testRegister_WeakPassword() {
    //     testUser.setPasswordHash("weak");
    //     when(validationService.validateEmail("john@example.com")).thenReturn(true);
    //     when(validationService.validatePassword("weak")).thenReturn(false);

    //     assertThrows(IllegalArgumentException.class, () -> userService.register(testUser),
    //             "Password must be at least 8 characters with letters and numbers");
    // }

    // @Test
    // void testRegister_EmailAlreadyExists() {
    //     when(validationService.validateEmail("john@example.com")).thenReturn(true);
    //     when(validationService.validatePassword("SecurePass123")).thenReturn(true);
    //     when(userRepository.existsByEmailIgnoreCase("john@example.com")).thenReturn(true);

    //     assertThrows(IllegalArgumentException.class, () -> userService.register(testUser),
    //             "Email already registered");
    // }

    // @Test
    // void testRegister_AgeNotVerified() {
    //     when(validationService.validateEmail("john@example.com")).thenReturn(true);
    //     when(validationService.validatePassword("SecurePass123")).thenReturn(true);
    //     when(validationService.validateAge(testUser)).thenReturn(false);
    //     when(userRepository.existsByEmailIgnoreCase("john@example.com")).thenReturn(false);
    //     when(userRepository.save(testUser)).thenReturn(testUser);

    //     User result = userService.register(testUser);

    //     assertNotNull(result);
    //     assertFalse(result.isAgeVerified());
    // }

    // // ==================== registerUser(RegisterUserRequest request) Tests ====================

    // @Test
    // void testRegisterUser_Success() {
    //     when(validationService.validateEmail("jane@example.com")).thenReturn(true);
    //     when(validationService.validatePassword("SecurePass123")).thenReturn(true);
    //     when(validationService.validateAge(any(User.class))).thenReturn(true);
    //     when(userRepository.existsByEmailIgnoreCase("jane@example.com")).thenReturn(false);
    //     when(userRepository.save(any(User.class))).thenReturn(testUser);

    //     User result = userService.registerUser(testRequest);

    //     assertNotNull(result);
    //     verify(userRepository, times(1)).save(any(User.class));
    // }

    // @Test
    // void testRegisterUser_RequestNull() {
    //     assertThrows(IllegalArgumentException.class, () -> userService.registerUser(null),
    //             "Registration request cannot be null");
    // }

    // @Test
    // void testRegisterUser_NameNull() {
    //     testRequest.setName(null);
    //     assertThrows(IllegalArgumentException.class, () -> userService.registerUser(testRequest),
    //             "Name is required");
    // }

    // @Test
    // void testRegisterUser_NameEmpty() {
    //     testRequest.setName("");
    //     assertThrows(IllegalArgumentException.class, () -> userService.registerUser(testRequest),
    //             "Name is required");
    // }

    // @Test
    // void testRegisterUser_PhoneNull() {
    //     testRequest.setPhone(null);
    //     assertThrows(IllegalArgumentException.class, () -> userService.registerUser(testRequest),
    //             "Phone is required");
    // }

    // @Test
    // void testRegisterUser_PhoneEmpty() {
    //     testRequest.setPhone("");
    //     assertThrows(IllegalArgumentException.class, () -> userService.registerUser(testRequest),
    //             "Phone is required");
    // }

    // @Test
    // void testRegisterUser_DateOfBirthNull() {
    //     testRequest.setDateOfBirth(null);
    //     assertThrows(IllegalArgumentException.class, () -> userService.registerUser(testRequest),
    //             "Date of birth is required");
    // }

    // @Test
    // void testRegisterUser_InvalidEmail() {
    //     when(validationService.validateEmail("invalid")).thenReturn(false);
    //     testRequest.setEmail("invalid");

    //     assertThrows(IllegalArgumentException.class, () -> userService.registerUser(testRequest),
    //             "Email is invalid or empty");
    // }

    // @Test
    // void testRegisterUser_WeakPassword() {
    //     when(validationService.validateEmail("jane@example.com")).thenReturn(true);
    //     when(validationService.validatePassword("weak")).thenReturn(false);
    //     testRequest.setPassword("weak");

    //     assertThrows(IllegalArgumentException.class, () -> userService.registerUser(testRequest),
    //             "Password must be at least 8 characters with letters and numbers");
    // }

    // @Test
    // void testRegisterUser_EmailAlreadyExists() {
    //     when(validationService.validateEmail("jane@example.com")).thenReturn(true);
    //     when(validationService.validatePassword("SecurePass123")).thenReturn(true);
    //     when(userRepository.existsByEmailIgnoreCase("jane@example.com")).thenReturn(true);

    //     assertThrows(IllegalArgumentException.class, () -> userService.registerUser(testRequest),
    //             "Email already registered");
    // }

    // // ==================== login(String email, String password) Tests ====================

    // @Test
    // void testLogin_Success() {
    //     when(userRepository.findByEmailIgnoreCase("john@example.com"))
    //             .thenReturn(Optional.of(testUser));

    //     User result = userService.login("john@example.com", "SecurePass123");

    //     assertNotNull(result);
    //     assertEquals("John Doe", result.getName());
    // }

    // @Test
    // void testLogin_UserNotFound() {
    //     when(userRepository.findByEmailIgnoreCase("unknown@example.com"))
    //             .thenReturn(Optional.empty());

    //     User result = userService.login("unknown@example.com", "SecurePass123");

    //     assertNull(result);
    // }

    // @Test
    // void testLogin_WrongPassword() {
    //     when(userRepository.findByEmailIgnoreCase("john@example.com"))
    //             .thenReturn(Optional.of(testUser));

    //     User result = userService.login("john@example.com", "WrongPassword123");

    //     assertNull(result);
    // }

    // @Test
    // void testLogin_CaseInsensitiveEmail() {
    //     when(userRepository.findByEmailIgnoreCase("JOHN@EXAMPLE.COM"))
    //             .thenReturn(Optional.of(testUser));

    //     User result = userService.login("JOHN@EXAMPLE.COM", "SecurePass123");

    //     assertNotNull(result);
    // }

    // ==================== getUserById(Long userId) Tests ====================

    @Test
    void testGetUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.getUserById(1L);

        assertTrue(result.isPresent());
        assertEquals("John Doe", result.get().getName());
    }

    @Test
    void testGetUserById_NotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<User> result = userService.getUserById(999L);

        assertFalse(result.isPresent());
    }

    // ==================== getAllUsers() Tests ====================

    @Test
    void testGetAllUsers_Success() {
        List<User> users = List.of(testUser);
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getName());
    }

    @Test
    void testGetAllUsers_Empty() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<User> result = userService.getAllUsers();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== updateUser(Long userId, User updatedUser) Tests ====================

    @Test
    void testUpdateUser_Success() {
        User updatedUser = User.builder()
                .email("newemail@example.com")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.updateUser(1L, updatedUser);

        assertNotNull(result);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testUpdateUser_UpdatedUserNull() {
        assertThrows(IllegalArgumentException.class, 
                () -> userService.updateUser(1L, null),
                "Updated user cannot be null");
    }

    @Test
    void testUpdateUser_UserNotFound() {
        User updatedUser = User.builder()
                .email("newemail@example.com")
                .build();

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, ()-> userService.updateUser(999L, updatedUser));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testUpdateUser_UpdateEmail() {
        User updatedUser = User.builder()
                .email("newemail@example.com")
                .build();

        User existingUser = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateUser(1L, updatedUser);

        assertEquals("newemail@example.com", result.getEmail());
    }

    @Test
    void testUpdateUser_UpdatePassword() {
        User updatedUser = User.builder()
                .passwordHash("NewPass123")
                .build();

        User existingUser = User.builder()
                .id(1L)
                .passwordHash("OldPass123")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        User result = userService.updateUser(1L, updatedUser);

        assertEquals("NewPass123", result.getPasswordHash());
    }

    @Test
    void testUpdateUser_UpdateDateOfBirth() {
        LocalDate newDate = LocalDate.of(1995, 6, 10);
        User updatedUser = User.builder()
                .dateOfBirth(newDate)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(validationService.validateAge(any(User.class))).thenReturn(true);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateUser(1L, updatedUser);

        assertEquals(newDate, result.getDateOfBirth());
        verify(validationService, times(1)).validateAge(any(User.class));
    }

    @Test
    void testUpdateUser_AllFieldsNull() {
        User updatedUser = new User();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.updateUser(1L, updatedUser);

        assertNotNull(result);
        verify(userRepository, times(1)).save(any(User.class));
    }

    // ==================== deleteUser(Long userId) Tests ====================

    @Test
    void testDeleteUser_Success() {
        when(userRepository.existsById(1L)).thenReturn(true);

        boolean result = userService.deleteUser(1L);

        assertTrue(result);
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteUser_UserNotFound() {
        when(userRepository.existsById(999L)).thenReturn(false);

        boolean result = userService.deleteUser(999L);

        assertFalse(result);
        verify(userRepository, never()).deleteById(999L);
    }
}