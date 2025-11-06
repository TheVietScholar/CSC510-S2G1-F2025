package com.boozebuddies.dto;

import lombok.*;

/** DTO for authentication responses containing tokens and user information. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthenticationResponse {
  /** The JWT access token. */
  private String token;

  /** The refresh token for obtaining new access tokens. */
  private String refreshToken;

  /** The authenticated user's information. */
  private UserDTO user;

  /** The response message. */
  private String message;
}
