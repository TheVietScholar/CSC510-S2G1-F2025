package com.boozebuddies.dto;

import java.time.LocalDate;

public class RegisterUserRequest {
    private String name;
    private String email;
    private String password;
    private String phone;
    private LocalDate dateOfBirth;

    // Constructors
    public RegisterUserRequest() {}
    
    public RegisterUserRequest(String name, String email, String password, String phone, LocalDate dateOfBirth) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.dateOfBirth = dateOfBirth;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
}