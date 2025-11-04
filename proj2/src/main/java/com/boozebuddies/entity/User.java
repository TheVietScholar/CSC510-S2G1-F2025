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

  @Column(name = "latitude")
  private Double latitude;

  @Column(name = "longitude")
  private Double longitude;

  // Roles
  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
  @Enumerated(EnumType.STRING)
  @Column(name = "role")
  @Builder.Default
  private Set<Role> roles = new HashSet<>();

  // ==================== ROLE-SPECIFIC FIELDS ====================

  /** For MERCHANT_ADMIN role: The merchant this admin manages. Null for other roles. */
  @Column(name = "merchant_id", nullable = true)
  private Long merchantId;

  /**
   * For DRIVER role: Link to Driver entity with certification and vehicle details. Null for other
   * roles.
   */
  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private Driver driver;

  // ==================== AUTHENTICATION FIELDS ====================

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

  // ==================== RELATIONSHIPS ====================

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

  // ==================== LIFECYCLE ====================

  @PreUpdate
  public void preUpdate() {
    this.updatedAt = LocalDateTime.now();
  }

  // ==================== HELPER METHODS ====================

  public boolean isActive() {
    return isActive;
  }

  public boolean isEmailVerified() {
    return isEmailVerified;
  }

  public boolean isAgeVerified() {
    return ageVerified;
  }

  /** Check if user has a specific role. */
  public boolean hasRole(Role role) {
    return roles != null && roles.contains(role);
  }

  /** Check if user has any of the specified roles. */
  public boolean hasAnyRole(Role... roles) {
    if (this.roles == null || roles == null) {
      return false;
    }
    for (Role role : roles) {
      if (this.roles.contains(role)) {
        return true;
      }
    }
    return false;
  }

  /** Check if user has all of the specified roles. */
  public boolean hasAllRoles(Role... roles) {
    if (this.roles == null || roles == null) {
      return false;
    }
    for (Role role : roles) {
      if (!this.roles.contains(role)) {
        return false;
      }
    }
    return true;
  }

  /** Add a role to the user. */
  public void addRole(Role role) {
    if (this.roles == null) {
      this.roles = new HashSet<>();
    }
    this.roles.add(role);
  }

  /** Remove a role from the user. */
  public void removeRole(Role role) {
    if (this.roles != null) {
      this.roles.remove(role);
    }
  }

  /** Check if user is a merchant admin. */
  public boolean isMerchantAdmin() {
    return hasRole(Role.MERCHANT_ADMIN) && merchantId != null;
  }

  /** Check if user is a driver. */
  public boolean isDriver() {
    return hasRole(Role.DRIVER) && driver != null;
  }

  /** Check if user is an admin. */
  public boolean isAdmin() {
    return hasRole(Role.ADMIN);
  }

  /** Check if user owns/manages a specific merchant. */
  public boolean ownsMerchant(Long merchantId) {
    return isMerchantAdmin() && this.merchantId != null && this.merchantId.equals(merchantId);
  }
}
