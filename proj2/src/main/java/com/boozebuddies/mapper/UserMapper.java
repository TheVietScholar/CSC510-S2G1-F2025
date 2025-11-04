package com.boozebuddies.mapper;

import com.boozebuddies.dto.RegisterUserRequest;
import com.boozebuddies.dto.UserDTO;
import com.boozebuddies.entity.User;
import java.util.HashSet;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

  /** Converts User entity to UserDTO. Excludes sensitive fields like password and refresh token. */
  public UserDTO toDTO(User user) {
    if (user == null) return null;

    return UserDTO.builder()
        .id(user.getId())
        .name(user.getName())
        .email(user.getEmail())
        .phone(user.getPhone())
        .dateOfBirth(user.getDateOfBirth())
        .ageVerified(user.isAgeVerified())
        .roles(user.getRoles())
        .isActive(user.isActive())
        .isEmailVerified(user.isEmailVerified())
        .lastLoginAt(user.getLastLoginAt())
        .createdAt(user.getCreatedAt())
        .updatedAt(user.getUpdatedAt())
        .build();
  }

  /** Converts UserDTO to User entity. Note: This does NOT set password or refresh token fields. */
  public User toEntity(UserDTO userDTO) {
    if (userDTO == null) return null;

    return User.builder()
        .id(userDTO.getId())
        .name(userDTO.getName())
        .email(userDTO.getEmail())
        .phone(userDTO.getPhone())
        .dateOfBirth(userDTO.getDateOfBirth())
        .ageVerified(userDTO.isAgeVerified())
        .roles(userDTO.getRoles() != null ? userDTO.getRoles() : new HashSet<>())
        .isActive(userDTO.isActive())
        .isEmailVerified(userDTO.isEmailVerified())
        .lastLoginAt(userDTO.getLastLoginAt())
        .createdAt(userDTO.getCreatedAt())
        .updatedAt(userDTO.getUpdatedAt())
        .build();
  }

  /** Converts RegisterUserRequest to User entity. Sets default values for new users. */
  public User toEntity(RegisterUserRequest request) {
    if (request == null) return null;
    
    return User.builder()
        .name(request.getName())
        .email(request.getEmail())
        .phone(request.getPhone())
        .dateOfBirth(request.getDateOfBirth())
        .passwordHash(request.getPassword()) // Will be encrypted in service layer
        .ageVerified(false) // Default to not verified
        .isActive(true) // Default to active
        .isEmailVerified(false) // Default to not verified
        .roles(new HashSet<>()) // Empty roles set, will be assigned in service
        .build();
  }
}
