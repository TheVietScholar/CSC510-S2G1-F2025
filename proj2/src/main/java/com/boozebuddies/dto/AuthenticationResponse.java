package com.boozebuddies.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthenticationResponse {
  private String token;
  private UserDTO user;
  private String message;
}
