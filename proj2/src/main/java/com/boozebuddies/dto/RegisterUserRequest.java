package com.boozebuddies.dto;

import lombok.*;
import java.time.LocalDate;

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