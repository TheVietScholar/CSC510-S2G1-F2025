package com.boozebuddies.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
  @Column(name = "role")
  @Builder.Default
  private List<String> roles = new ArrayList<>();

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
}
