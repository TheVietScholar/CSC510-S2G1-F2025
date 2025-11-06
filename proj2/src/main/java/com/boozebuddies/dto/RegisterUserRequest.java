package com.boozebuddies.dto;

import java.time.LocalDate;
import lombok.*;

/** Data transfer object for user registration. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterUserRequest {
  /** The user's name */
  private String name;

  /** The user's email address */
  private String email;

  /** The user's password */
  private String password;

  /** The user's phone number */
  private String phone;

  /** The user's date of birth */
  private LocalDate dateOfBirth;
}
