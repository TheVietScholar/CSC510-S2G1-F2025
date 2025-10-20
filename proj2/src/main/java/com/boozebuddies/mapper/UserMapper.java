package com.boozebuddies.mapper;

import com.boozebuddies.dto.RegisterUserRequest;
import com.boozebuddies.dto.UserDTO;
import com.boozebuddies.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

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
        .createdAt(user.getCreatedAt())
        .updatedAt(user.getUpdatedAt())
        .build();
  }

  public User toEntity(UserDTO userDTO) {
    if (userDTO == null) return null;

    return User.builder()
        .id(userDTO.getId())
        .name(userDTO.getName())
        .email(userDTO.getEmail())
        .phone(userDTO.getPhone())
        .dateOfBirth(userDTO.getDateOfBirth())
        .ageVerified(userDTO.isAgeVerified())
        .roles(userDTO.getRoles())
        .createdAt(userDTO.getCreatedAt())
        .updatedAt(userDTO.getUpdatedAt())
        .build();
  }

  public User toEntity(RegisterUserRequest request) {
    if (request == null) return null;
    
    return User.builder()
        .name(request.getName())
        .email(request.getEmail())
        .phone(request.getPhone())
        .dateOfBirth(request.getDateOfBirth())
        .ageVerified(false) // Default to not verified
        .build();
  }
}
