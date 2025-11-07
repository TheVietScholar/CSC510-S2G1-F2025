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

  /** The unique user ID */
  private Long id;

  /** The user's name */
  private String name;

  /** The user's email address */
  private String email;

  /** The user's phone number */
  private String phone;

  /** The user's date of birth */
  private LocalDate dateOfBirth;

  /** Whether the user's age has been verified */
  private boolean ageVerified;

  /** The set of roles assigned to the user */
  private Set<Role> roles; // Changed from List<String> to Set<Role>

  /** Whether the user account is active */
  private boolean isActive; // NEW - account active status

  /** Whether the user's email has been verified */
  private boolean isEmailVerified; // NEW - email verification status

  /** When the user last logged in */
  private LocalDateTime lastLoginAt; // NEW - last login timestamp

  /** When the user account was created */
  private LocalDateTime createdAt;

  /** When the user account was last updated */
  private LocalDateTime updatedAt;

  /** The latitude coordinate of the user's location */
  private Double latitude;

  /** The longitude coordinate of the user's location */
  private Double longitude;

  // Note: passwordHash, refreshToken, and refreshTokenExpiryDate are intentionally excluded
  // for security reasons - never expose these in API responses
}
