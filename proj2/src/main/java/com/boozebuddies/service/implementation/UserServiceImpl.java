package com.boozebuddies.service.implementation;

import com.boozebuddies.dto.RegisterUserRequest;
import com.boozebuddies.entity.User;
import com.boozebuddies.service.UserService;
import com.boozebuddies.service.ValidationService;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

  private final List<User> users = new ArrayList<>();
  private long nextUserId = 1;

  @Autowired
  private ValidationService validationService;

  public UserServiceImpl(ValidationService validationService) {
    this.validationService = validationService;
  }

  @Override
  public User register(User user) {
    if (user == null) {
      throw new IllegalArgumentException("User cannot be null");
    }
    
    // Use ValidationService to validate email
    if (!validationService.validateEmail(user.getEmail())) {
      throw new IllegalArgumentException("Email is invalid or empty");
    }
    
    // Use ValidationService to validate password
    if (!validationService.validatePassword(user.getPasswordHash())) {
      throw new IllegalArgumentException("Password must be at least 8 characters with letters and numbers");
    }
    
    // Check for duplicate email (case-insensitive)
    boolean emailExists = users.stream()
        .anyMatch(u -> u.getEmail().equalsIgnoreCase(user.getEmail()));
    if (emailExists) {
      throw new IllegalArgumentException("Email already registered");
    }
    
    user.setId(nextUserId++);
    user.setAgeVerified(verifyAge(user));
    users.add(user);
    System.out.println(
        "[USER REGISTER] User ID " + user.getId() + " registered with email " + user.getEmail());
    return user;
  }

  @Override
  public User registerUser(RegisterUserRequest request) {
    if (request == null) {
      throw new IllegalArgumentException("Registration request cannot be null");
    }
    
    // Validate required fields
    if (request.getName() == null || request.getName().isEmpty()) {
      throw new IllegalArgumentException("Name is required");
    }
    
    if (request.getPhone() == null || request.getPhone().isEmpty()) {
      throw new IllegalArgumentException("Phone is required");
    }
    
    if (request.getDateOfBirth() == null) {
      throw new IllegalArgumentException("Date of birth is required");
    }
    
    // Use ValidationService to validate email
    if (!validationService.validateEmail(request.getEmail())) {
      throw new IllegalArgumentException("Email is invalid or empty");
    }
    
    // Use ValidationService to validate password
    if (!validationService.validatePassword(request.getPassword())) {
      throw new IllegalArgumentException("Password must be at least 8 characters with letters and numbers");
    }
    
    // Check for duplicate email (case-insensitive)
    boolean emailExists = users.stream()
        .anyMatch(u -> u.getEmail().equalsIgnoreCase(request.getEmail()));
    if (emailExists) {
      throw new IllegalArgumentException("Email already registered");
    }

    User user =
        User.builder()
            .id(nextUserId++)
            .name(request.getName())
            .email(request.getEmail())
            .passwordHash(request.getPassword())
            .phone(request.getPhone())
            .dateOfBirth(request.getDateOfBirth())
            .build();

    user.setAgeVerified(verifyAge(user));
    users.add(user);

    System.out.println(
        "[USER REGISTER] User ID " + user.getId() + " registered with email " + user.getEmail());
    return user;
  }
  
  /** Authenticates a user with email and password. */
  @Override
  public User login(String email, String password) {
    Optional<User> userOpt =
        users.stream()
            .filter(
                u -> u.getEmail().equalsIgnoreCase(email) && u.getPasswordHash().equals(password))
            .findFirst();
    return userOpt.orElse(null);
  }

  /** Verifies that a user is of legal drinking age (21+ in the US). */
  @Override
  public boolean verifyAge(User user) {
    if (user == null || user.getDateOfBirth() == null) return false;
    int age = Period.between(user.getDateOfBirth(), LocalDate.now()).getYears();
    return age >= 21;
  }

  /** Retrieves a user by their unique ID. */
  @Override
  public Optional<User> getUserById(Long userId) {
    return users.stream().filter(u -> u.getId().equals(userId)).findFirst();
  }

  /** Retrieves all users in the system. */
  @Override
  public List<User> getAllUsers() {
    return new ArrayList<>(users);
  }

  /** Updates a user's information. */
  @Override
  public User updateUser(Long userId, User updatedUser) {
    Optional<User> userOpt = getUserById(userId);
    if (userOpt.isPresent()) {
      User user = userOpt.get();
      if (updatedUser.getEmail() != null) user.setEmail(updatedUser.getEmail());
      if (updatedUser.getPasswordHash() != null)
        user.setPasswordHash(updatedUser.getPasswordHash());
      if (updatedUser.getDateOfBirth() != null) {
        user.setDateOfBirth(updatedUser.getDateOfBirth());
        user.setAgeVerified(verifyAge(user));
      }
      return user;
    }
    return null;
  }

  /** Deletes a user from the system. */
  @Override
  public void deleteUser(Long userId) {
    users.removeIf(u -> u.getId().equals(userId));
    System.out.println("[USER DELETE] User ID " + userId + " deleted");
  }
}
