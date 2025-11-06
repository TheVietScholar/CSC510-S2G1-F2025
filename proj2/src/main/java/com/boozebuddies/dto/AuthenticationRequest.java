package com.boozebuddies.dto;

import lombok.*;

/** DTO for user authentication requests. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthenticationRequest {
  /** The user's email address. */
  private String email;

  /** The user's password. */
  private String password;
}
