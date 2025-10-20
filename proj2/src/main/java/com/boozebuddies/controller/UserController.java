package com.boozebuddies.controller;

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

  @PostMapping("/register")
  public ResponseEntity<?> registerUser(@RequestBody RegisterUserRequest request) {
    try {
      User user = userService.registerUser(request);
      UserDTO userDTO = userMapper.toDTO(user);
      return ResponseEntity.status(HttpStatus.CREATED).body(userDTO);
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
    return userService
        .getUserById(id)
        .map(user -> ResponseEntity.ok(userMapper.toDTO(user)))
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping
  public ResponseEntity<List<UserDTO>> getAllUsers() {
    List<UserDTO> users =
        userService.getAllUsers().stream().map(userMapper::toDTO).collect(Collectors.toList());
    return ResponseEntity.ok(users);
  }
  
  @PutMapping("/{id}")
  public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
    try {
      User updatedUser = userService.updateUser(id, userMapper.toEntity(userDTO));
      if (updatedUser == null) {
        return ResponseEntity.notFound().build();
      }
      return ResponseEntity.ok(userMapper.toDTO(updatedUser));
    } catch (Exception e) {
      return ResponseEntity.notFound().build();
    }
  }

  @PostMapping("/{id}/verify-age")
  public ResponseEntity<?> verifyAge(@PathVariable Long id) {
    try {
      User user =
          userService.getUserById(id).orElseThrow(() -> new RuntimeException("User not found"));

      // In real app, this would integrate with external age verification service
      boolean isVerified = validationService.validateAge(user);

      if (isVerified) {
        user.setAgeVerified(true);
        userService.updateUser(id, user);
        return ResponseEntity.ok("Age verification successful");
      } else {
        return ResponseEntity.badRequest().body("Age verification failed");
      }
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteUser(@PathVariable Long id) {
    boolean deleted = userService.deleteUser(id);
    if (deleted) {
      return ResponseEntity.ok("User deleted successfully");
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody LoginRequest request) {
    try {
      if (request.getEmail() == null || request.getPassword() == null) {
        return ResponseEntity.badRequest().body("Email and password are required");
      }
      
      User user = userService.login(request.getEmail(), request.getPassword());
      
      if (user != null) {
        UserDTO userDTO = userMapper.toDTO(user);
        return ResponseEntity.ok(userDTO);
      } else {
        return ResponseEntity.badRequest().body("Invalid email or password");
      }
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }
}
