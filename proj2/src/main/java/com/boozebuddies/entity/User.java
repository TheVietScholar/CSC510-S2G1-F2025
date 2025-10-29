package com.boozebuddies.entity;

import com.boozebuddies.model.Role;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.*;

@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  private String passwordHash;

  private String phone;

  @Column(name = "date_of_birth")
  private LocalDate dateOfBirth;

  @Builder.Default
  @Column(name = "age_verified")
  private boolean ageVerified = false;

  // Changed from List<String> to Set<Role> enum
  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
  @Enumerated(EnumType.STRING)
  @Column(name = "role")
  @Builder.Default
  private Set<Role> roles = new HashSet<>();

  // New fields for authentication
  @Builder.Default
  @Column(name = "is_active")
  private boolean isActive = true;

  @Builder.Default
  @Column(name = "is_email_verified")
  private boolean isEmailVerified = false;

  @Column(name = "last_login_at")
  private LocalDateTime lastLoginAt;

  @Column(name = "refresh_token", length = 512)
  private String refreshToken;

  @Column(name = "refresh_token_expiry")
  private LocalDateTime refreshTokenExpiryDate;

  // Address can be added later when you create the Address entity
  @Transient private Object address; // Placeholder - replace with @ManyToOne Address when ready

  @Builder.Default
  @Column(name = "created_at")
  private LocalDateTime createdAt = LocalDateTime.now();

  @Builder.Default
  @Column(name = "updated_at")
  private LocalDateTime updatedAt = LocalDateTime.now();

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
  @Builder.Default
  private List<Order> orders = new ArrayList<>();

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
  @Builder.Default
  private List<Rating> ratings = new ArrayList<>();

  @PreUpdate
  public void preUpdate() {
    this.updatedAt = LocalDateTime.now();
  }

  // Helper method to check if user is active
  public boolean isActive() {
    return isActive;
  }

  // Helper method to check if email is verified
  public boolean isEmailVerified() {
    return isEmailVerified;
  }

  // Helper method to check if age is verified
  public boolean isAgeVerified() {
    return ageVerified;
  }
}
