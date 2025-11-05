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

/**
 * Implementation of the {@link UserService} interface providing user-related operations such as
 * registration, update, deletion, and authentication token management.
 *
 * <p>This service ensures validation of input data, encryption of passwords, and enforcement of
 * unique email constraints. It also manages refresh tokens and user activation/deactivation states.
 */
@Service
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final ValidationService validationService;
  private final PasswordEncoder passwordEncoder;

  /**
   * Constructs a {@code UserServiceImpl} instance.
   *
   * @param userRepository the repository for user persistence
   * @param validationService the service for validating user data such as email, password, and age
   * @param passwordEncoder the encoder for securing user passwords; defaults to {@link
   *     BCryptPasswordEncoder} if null
   */
  @Autowired
  public UserServiceImpl(
      UserRepository userRepository,
      ValidationService validationService,
      PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.validationService = validationService;
    this.passwordEncoder = passwordEncoder != null ? passwordEncoder : new BCryptPasswordEncoder();
  }

  /**
   * Registers a new user after validating input fields such as name, phone, email, and password.
   * Assigns a default {@link Role#USER} and encodes the user's password.
   *
   * @param request the registration data
   * @return the newly created {@link User}
   * @throws IllegalArgumentException if required fields are missing or invalid
   * @throws UserAlreadyExistsException if a user with the given email already exists
   */
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

  /**
   * Retrieves a user by ID wrapped in an {@link Optional}.
   *
   * @param userId the unique identifier of the user
   * @return an {@code Optional<User>} if found, otherwise empty
   */
  @Override
  public Optional<User> getUserById(Long userId) {
    return userRepository.findById(userId);
  }

  /**
   * Retrieves a user by ID or throws an exception if not found.
   *
   * @param userId the unique identifier of the user
   * @return the {@link User} entity
   * @throws UserNotFoundException if no user exists with the given ID
   */
  @Override
  public User findById(Long userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
  }

  /**
   * Finds a user by their email address.
   *
   * @param email the email address to search
   * @return an {@code Optional<User>} if found, otherwise empty
   */
  @Override
  public Optional<User> findByEmail(String email) {
    return userRepository.findByEmailIgnoreCase(email);
  }

  /**
   * Retrieves all users in the system.
   *
   * @return a list of all {@link User} entities
   */
  @Override
  public List<User> getAllUsers() {
    return userRepository.findAll();
  }

  /**
   * Updates an existing user's information.
   *
   * @param userId the unique ID of the user to update
   * @param updatedUser the new user details to apply
   * @return the updated {@link User}
   * @throws IllegalArgumentException if updatedUser is null
   * @throws UserAlreadyExistsException if the new email already belongs to another user
   * @throws UserNotFoundException if the user does not exist
   */
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

  /**
   * Deletes a user by ID.
   *
   * @param userId the unique identifier of the user
   * @return {@code true} if the user was deleted, {@code false} if not found
   */
  @Override
  @Transactional
  public boolean deleteUser(Long userId) {
    if (!userRepository.existsById(userId)) {
      return false;
    }
    userRepository.deleteById(userId);
    return true;
  }

  /**
   * Updates the last login timestamp for a given user.
   *
   * @param userId the ID of the user to update
   */
  @Override
  @Transactional
  public void updateLastLogin(Long userId) {
    User user = findById(userId);
    user.setLastLoginAt(LocalDateTime.now());
    userRepository.save(user);
  }

  /**
   * Saves a refresh token for a user with an expiration date.
   *
   * @param userId the ID of the user
   * @param refreshToken the token to save
   * @param expiryDate the date when the token expires
   */
  @Override
  @Transactional
  public void saveRefreshToken(Long userId, String refreshToken, LocalDateTime expiryDate) {
    User user = findById(userId);
    user.setRefreshToken(refreshToken);
    user.setRefreshTokenExpiryDate(expiryDate);
    userRepository.save(user);
  }

  /**
   * Checks if a given refresh token is valid and not expired.
   *
   * @param refreshToken the refresh token string
   * @return {@code true} if the token is valid and active, otherwise {@code false}
   */
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

  /**
   * Retrieves a user by their refresh token.
   *
   * @param refreshToken the refresh token
   * @return an {@code Optional<User>} if found, otherwise empty
   */
  @Override
  public Optional<User> findByRefreshToken(String refreshToken) {
    return userRepository.findByRefreshToken(refreshToken);
  }

  /**
   * Revokes a user's refresh token, making it invalid for future authentication.
   *
   * @param userId the ID of the user
   */
  @Override
  @Transactional
  public void revokeRefreshToken(Long userId) {
    User user = findById(userId);
    user.setRefreshToken(null);
    user.setRefreshTokenExpiryDate(null);
    userRepository.save(user);
  }

  /**
   * Deactivates a user, disabling account access and revoking any active tokens.
   *
   * @param userId the ID of the user to deactivate
   */
  @Override
  @Transactional
  public void deactivateUser(Long userId) {
    User user = findById(userId);
    user.setActive(false);
    user.setRefreshToken(null);
    user.setRefreshTokenExpiryDate(null);
    userRepository.save(user);
  }

  /**
   * Activates a user, restoring access to their account.
   *
   * @param userId the ID of the user to activate
   */
  @Override
  @Transactional
  public void activateUser(Long userId) {
    User user = findById(userId);
    user.setActive(true);
    userRepository.save(user);
  }

  /**
   * Determines whether a user is eligible to place orders based on their account and age status.
   *
   * @param user the user to check
   * @return {@code true} if the user is active and age-verified; otherwise {@code false}
   */
  @Override
  public boolean canPlaceOrders(User user) {
    return user.isActive() && user.isAgeVerified();
  }
}
