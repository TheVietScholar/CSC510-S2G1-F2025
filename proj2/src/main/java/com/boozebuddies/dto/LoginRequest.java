package com.boozebuddies.dto;

import lombok.*;

/** Data transfer object for user login credentials. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {
  /** The user's email address */
  private String email;

  /** The user's password */
  private String password;
}
