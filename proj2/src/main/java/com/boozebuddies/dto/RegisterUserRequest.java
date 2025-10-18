package com.boozebuddies.dto;

import java.time.LocalDate;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterUserRequest {
  private String name;
  private String email;
  private String password;
  private String phone;
  private LocalDate dateOfBirth;
}
