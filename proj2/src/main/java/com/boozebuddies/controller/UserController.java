package com.boozebuddies.controller;

import com.boozebuddies.dto.ApiResponse;
import com.boozebuddies.dto.LoginRequest;
import com.boozebuddies.dto.RegisterUserRequest;
import com.boozebuddies.dto.UserDTO;
import com.boozebuddies.entity.User;
import com.boozebuddies.mapper.UserMapper;
import com.boozebuddies.service.UserService;
import com.boozebuddies.service.ValidationService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;
  private final ValidationService validationService;
  private final UserMapper userMapper;

  // ==================== REGISTER ====================

  @PostMapping("/register")
  public ResponseEntity<?> registerUser(@RequestBody RegisterUserRequest request) {
    try {
      User user = userService.registerUser(request);
      UserDTO userDTO = userMapper.toDTO(user);
      return ResponseEntity.status(HttpStatus.CREATED)
          .body(ApiResponse.success(userDTO, "User registered successfully"));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("An error occurred during registration"));
    }
  }

  // ==================== LOGIN ====================

  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody LoginRequest request) {
    try {
      if (request.getEmail() == null || request.getPassword() == null) {
        return ResponseEntity.badRequest()
            .body(ApiResponse.error("Email and password are required"));
      }

      User user = userService.login(request.getEmail(), request.getPassword());

      if (user != null) {
        UserDTO userDTO = userMapper.toDTO(user);
        return ResponseEntity.ok(ApiResponse.success(userDTO, "Login successful"));
      } else {
        return ResponseEntity.badRequest()
            .body(ApiResponse.error("Invalid email or password"));
      }
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("An error occurred during login"));
    }
  }

  // ==================== RETRIEVE ====================

  @GetMapping("/{id}")
  public ResponseEntity<?> getUserById(@PathVariable Long id) {
    try {
      if (id == null || id <= 0) {
        return ResponseEntity.badRequest().body(ApiResponse.error("Invalid user ID"));
      }

      return userService
          .getUserById(id)
          .map(user -> ResponseEntity.ok(ApiResponse.success(userMapper.toDTO(user), "User retrieved successfully")))
          .orElse(ResponseEntity.notFound().build());
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("An error occurred retrieving user"));
    }
  }

  @GetMapping
  public ResponseEntity<?> getAllUsers() {
    try {
      List<UserDTO> users =
          userService.getAllUsers().stream()
              .map(userMapper::toDTO)
              .collect(Collectors.toList());
      return ResponseEntity.ok(
          ApiResponse.success(users, "Users retrieved successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("An error occurred retrieving users"));
    }
  }

  // ==================== UPDATE ====================

  @PutMapping("/{id}")
  public ResponseEntity<?> updateUser(
      @PathVariable Long id, @RequestBody UserDTO userDTO) {
    try {
      if (id == null || id <= 0) {
        return ResponseEntity.badRequest().body(ApiResponse.error("Invalid user ID"));
      }

      User updatedUser = userService.updateUser(id, userMapper.toEntity(userDTO));
      if (updatedUser == null) {
        return ResponseEntity.notFound().build();
      }
      return ResponseEntity.ok(
          ApiResponse.success(userMapper.toDTO(updatedUser), "User updated successfully"));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("An error occurred updating user"));
    }
  }

  // ==================== VERIFY AGE ====================

  @PostMapping("/{id}/verify-age")
  public ResponseEntity<?> verifyAge(@PathVariable Long id) {
    try {
      if (id == null || id <= 0) {
        return ResponseEntity.badRequest().body(ApiResponse.error("Invalid user ID"));
      }

      User user =
          userService
              .getUserById(id)
              .orElseThrow(() -> new RuntimeException("User not found"));

      // In real app, this would integrate with external age verification service
      boolean isVerified = validationService.validateAge(user);

      if (isVerified) {
        user.setAgeVerified(true);
        userService.updateUser(id, user);
        return ResponseEntity.ok(
            ApiResponse.success(userMapper.toDTO(user), "Age verification successful"));
      } else {
        return ResponseEntity.badRequest()
            .body(ApiResponse.error("Age verification failed"));
      }
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("An error occurred during age verification"));
    }
  }

  // ==================== DELETE ====================

  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteUser(@PathVariable Long id) {
    try {
      if (id == null || id <= 0) {
        return ResponseEntity.badRequest().body(ApiResponse.error("Invalid user ID"));
      }

      boolean deleted = userService.deleteUser(id);
      if (deleted) {
        return ResponseEntity.ok(ApiResponse.success(null, "User deleted successfully"));
      } else {
        return ResponseEntity.notFound().build();
      }
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("An error occurred deleting user"));
    }
  }
}