package com.boozebuddies.dto;

import lombok.*;

/** Data transfer object for refresh token requests. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshTokenRequest {
  /** The refresh token used to obtain a new access token */
  private String refreshToken;
}
