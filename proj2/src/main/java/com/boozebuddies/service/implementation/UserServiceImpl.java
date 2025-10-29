package com.boozebuddies.service.implementation;

import com.boozebuddies.dto.RegisterUserRequest;
import com.boozebuddies.entity.User;
import com.boozebuddies.exception.UserAlreadyExistsException;
import com.boozebuddies.exception.UserNotFoundException;
import com.boozebuddies.model.Role;
import com.boozebuddies.repository.UserRepository;
import com.boozebuddies.service.UserService;
import com.boozebuddies.service.ValidationService;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final ValidationService validationService;
  private final PasswordEncoder passwordEncoder;

  @Autowired
  public UserServiceImpl(
      UserRepository userRepository,
      ValidationService validationService,
      PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.validationService = validationService;
    this.passwordEncoder = passwordEncoder != null ? passwordEncoder : new BCryptPasswordEncoder();
  }

  @Override
  @Transactional
  public User register(User user) {
    if (user == null) {
      throw new IllegalArgumentException("User cannot be null");
    }

    if (user.getName() == null || user.getName().isEmpty()) {
      throw new IllegalArgumentException("Name is required");
    }

    if (user.getPhone() == null || user.getPhone().isEmpty()) {
      throw new IllegalArgumentException("Phone is required");
    }

    if (user.getDateOfBirth() == null) {
      throw new IllegalArgumentException("Date of birth is required");
    }

    if (!validationService.validateEmail(user.getEmail())) {
      throw new IllegalArgumentException("Email is invalid or empty");
    }

    if (!validationService.validatePassword(user.getPasswordHash())) {
      throw new IllegalArgumentException(
          "Password must be at least 8 characters with letters and numbers");
    }

    if (userRepository.existsByEmailIgnoreCase(user.getEmail())) {
      throw new UserAlreadyExistsException("Email already registered");
    }

    // Encrypt password before saving
    user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
    user.setAgeVerified(validationService.validateAge(user));
    user.setActive(true);
    user.setEmailVerified(false);

    // Assign default USER role
    Set<Role> roles = new HashSet<>();
    roles.add(Role.USER);
    user.setRoles(roles);

    return userRepository.save(user);
  }

  @Override
  @Transactional
  public User registerUser(RegisterUserRequest request) {
    if (request == null) {
      throw new IllegalArgumentException("Registration request cannot be null");
    }

    if (request.getName() == null || request.getName().isEmpty()) {
      throw new IllegalArgumentException("Name is required");
    }

    if (request.getPhone() == null || request.getPhone().isEmpty()) {
      throw new IllegalArgumentException("Phone is required");
    }

    if (request.getDateOfBirth() == null) {
      throw new IllegalArgumentException("Date of birth is required");
    }

    if (!validationService.validateEmail(request.getEmail())) {
      throw new IllegalArgumentException("Email is invalid or empty");
    }

    if (!validationService.validatePassword(request.getPassword())) {
      throw new IllegalArgumentException(
          "Password must be at least 8 characters with letters and numbers");
    }

    if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
      throw new UserAlreadyExistsException("Email already registered");
    }

    User user =
        User.builder()
            .name(request.getName())
            .email(request.getEmail())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .phone(request.getPhone())
            .dateOfBirth(request.getDateOfBirth())
            .isActive(true)
            .isEmailVerified(false)
            .build();

    user.setAgeVerified(validationService.validateAge(user));

    // Assign default USER role
    Set<Role> roles = new HashSet<>();
    roles.add(Role.USER);
    user.setRoles(roles);

    return userRepository.save(user);
  }

  @Override
  public User login(String email, String password) {
    // This method is now deprecated - use AuthenticationService instead
    Optional<User> userOpt = userRepository.findByEmailIgnoreCase(email);
    if (userOpt.isPresent()) {
      User user = userOpt.get();
      if (passwordEncoder.matches(password, user.getPasswordHash())) {
        updateLastLogin(user.getId());
        return user;
      }
    }
    return null;
  }

  @Override
  public Optional<User> getUserById(Long userId) {
    return userRepository.findById(userId);
  }

  @Override
  public User findById(Long userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
  }

  @Override
  public Optional<User> findByEmail(String email) {
    return userRepository.findByEmailIgnoreCase(email);
  }

  @Override
  public List<User> getAllUsers() {
    return userRepository.findAll();
  }

  @Override
  @Transactional
  public User updateUser(Long userId, User updatedUser) {
    if (updatedUser == null) {
      throw new IllegalArgumentException("Updated user cannot be null");
    }

    return userRepository
        .findById(userId)
        .map(
            user -> {
              if (updatedUser.getName() != null && !updatedUser.getName().isEmpty()) {
                user.setName(updatedUser.getName());
              }
              if (updatedUser.getEmail() != null && !updatedUser.getEmail().isEmpty()) {
                if (!user.getEmail().equals(updatedUser.getEmail())
                    && userRepository.existsByEmailIgnoreCase(updatedUser.getEmail())) {
                  throw new UserAlreadyExistsException("Email already in use");
                }
                user.setEmail(updatedUser.getEmail());
                user.setEmailVerified(false);
              }
              if (updatedUser.getPhone() != null && !updatedUser.getPhone().isEmpty()) {
                user.setPhone(updatedUser.getPhone());
              }
              if (updatedUser.getDateOfBirth() != null) {
                user.setDateOfBirth(updatedUser.getDateOfBirth());
                user.setAgeVerified(validationService.validateAge(user));
              }
              if (updatedUser.getAddress() != null) {
                user.setAddress(updatedUser.getAddress());
              }
              return userRepository.save(user);
            })
        .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
  }

  @Override
  @Transactional
  public boolean deleteUser(Long userId) {
    if (!userRepository.existsById(userId)) {
      return false;
    }
    userRepository.deleteById(userId);
    return true;
  }

  @Override
  @Transactional
  public void updateLastLogin(Long userId) {
    User user = findById(userId);
    user.setLastLoginAt(LocalDateTime.now());
    userRepository.save(user);
  }

  @Override
  @Transactional
  public void saveRefreshToken(Long userId, String refreshToken, LocalDateTime expiryDate) {
    User user = findById(userId);
    user.setRefreshToken(refreshToken);
    user.setRefreshTokenExpiryDate(expiryDate);
    userRepository.save(user);
  }

  @Override
  public boolean isRefreshTokenValid(String refreshToken) {
    Optional<User> userOpt = userRepository.findByRefreshToken(refreshToken);
    if (userOpt.isEmpty()) {
      return false;
    }

    User user = userOpt.get();
    return user.isActive()
        && user.getRefreshTokenExpiryDate() != null
        && user.getRefreshTokenExpiryDate().isAfter(LocalDateTime.now());
  }

  @Override
  public Optional<User> findByRefreshToken(String refreshToken) {
    return userRepository.findByRefreshToken(refreshToken);
  }

  @Override
  @Transactional
  public void revokeRefreshToken(Long userId) {
    User user = findById(userId);
    user.setRefreshToken(null);
    user.setRefreshTokenExpiryDate(null);
    userRepository.save(user);
  }

  @Override
  @Transactional
  public void verifyEmail(Long userId) {
    User user = findById(userId);
    user.setEmailVerified(true);
    userRepository.save(user);
  }

  @Override
  public boolean hasRole(User user, Role role) {
    return user.getRoles() != null && user.getRoles().contains(role);
  }

  @Override
  @Transactional
  public void assignRole(Long userId, Role role) {
    User user = findById(userId);
    if (user.getRoles() == null) {
      user.setRoles(new HashSet<>());
    }
    user.getRoles().add(role);
    userRepository.save(user);
  }

  @Override
  @Transactional
  public void removeRole(Long userId, Role role) {
    User user = findById(userId);
    if (user.getRoles() != null) {
      user.getRoles().remove(role);
      userRepository.save(user);
    }
  }

  @Override
  @Transactional
  public void deactivateUser(Long userId) {
    User user = findById(userId);
    user.setActive(false);
    user.setRefreshToken(null);
    user.setRefreshTokenExpiryDate(null);
    userRepository.save(user);
  }

  @Override
  @Transactional
  public void activateUser(Long userId) {
    User user = findById(userId);
    user.setActive(true);
    userRepository.save(user);
  }

  @Override
  @Transactional
  public void changePassword(Long userId, String oldPassword, String newPassword) {
    User user = findById(userId);

    if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
      throw new IllegalArgumentException("Current password is incorrect");
    }

    if (!validationService.validatePassword(newPassword)) {
      throw new IllegalArgumentException(
          "Password must be at least 8 characters with letters and numbers");
    }

    user.setPasswordHash(passwordEncoder.encode(newPassword));
    userRepository.save(user);
  }

  @Override
  @Transactional
  public void resetPassword(Long userId, String newPassword) {
    User user = findById(userId);

    if (!validationService.validatePassword(newPassword)) {
      throw new IllegalArgumentException(
          "Password must be at least 8 characters with letters and numbers");
    }

    user.setPasswordHash(passwordEncoder.encode(newPassword));
    userRepository.save(user);
  }

  @Override
  public boolean canPlaceOrders(User user) {
    return user.isActive() && user.isEmailVerified() && user.isAgeVerified();
  }
}