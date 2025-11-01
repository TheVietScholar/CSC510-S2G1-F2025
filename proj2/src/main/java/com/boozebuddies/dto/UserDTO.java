package com.boozebuddies.dto;

import com.boozebuddies.model.Role;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.*;

/**
 * Data Transfer Object for User entity. Excludes sensitive information like password and refresh
 * tokens.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {

  private Long id;
  private String name;
  private String email;
  private String phone;
  private LocalDate dateOfBirth;
  private boolean ageVerified;
  private Set<Role> roles; // Changed from List<String> to Set<Role>
  private boolean isActive; // NEW - account active status
  private boolean isEmailVerified; // NEW - email verification status
  private LocalDateTime lastLoginAt; // NEW - last login timestamp
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private Double latitude;
  private Double longitude;

  // Note: passwordHash, refreshToken, and refreshTokenExpiryDate are intentionally excluded
  // for security reasons - never expose these in API responses
}
