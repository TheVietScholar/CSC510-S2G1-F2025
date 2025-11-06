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

/** Entity representing a user in the system. */
@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class User {
  /** The unique user ID */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** The user's name */
  @Column(nullable = false)
  private String name;

  /** The user's email address */
  @Column(nullable = false, unique = true)
  private String email;

  /** The user's hashed password */
  @Column(nullable = false)
  private String passwordHash;

  /** The user's phone number */
  private String phone;

  /** The user's date of birth */
  @Column(name = "date_of_birth")
  private LocalDate dateOfBirth;

  /** Whether the user's age has been verified */
  @Builder.Default
  @Column(name = "age_verified")
  private boolean ageVerified = false;

  /** The latitude coordinate of the user's location */
  @Column(name = "latitude")
  private Double latitude;

  /** The longitude coordinate of the user's location */
  @Column(name = "longitude")
  private Double longitude;

  // Roles
  /** The set of roles assigned to the user */
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

  /** Whether the user account is active */
  @Builder.Default
  @Column(name = "is_active")
  private boolean isActive = true;

  /** Whether the user's email has been verified */
  @Builder.Default
  @Column(name = "is_email_verified")
  private boolean isEmailVerified = false;

  /** When the user last logged in */
  @Column(name = "last_login_at")
  private LocalDateTime lastLoginAt;

  /** The user's refresh token for authentication */
  @Column(name = "refresh_token", length = 512)
  private String refreshToken;

  /** When the refresh token expires */
  @Column(name = "refresh_token_expiry")
  private LocalDateTime refreshTokenExpiryDate;

  // ==================== RELATIONSHIPS ====================

  /** Placeholder for address - replace with @ManyToOne Address when ready */
  @Transient private Object address;

  /** When the user account was created */
  @Builder.Default
  @Column(name = "created_at")
  private LocalDateTime createdAt = LocalDateTime.now();

  /** When the user account was last updated */
  @Builder.Default
  @Column(name = "updated_at")
  private LocalDateTime updatedAt = LocalDateTime.now();

  /** The list of orders placed by this user */
  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
  @Builder.Default
  private List<Order> orders = new ArrayList<>();

  /** The list of ratings submitted by this user */
  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
  @Builder.Default
  private List<Rating> ratings = new ArrayList<>();

  // ==================== LIFECYCLE ====================

  /** Updates the updatedAt timestamp before persisting changes. */
  @PreUpdate
  public void preUpdate() {
    this.updatedAt = LocalDateTime.now();
  }

  // ==================== HELPER METHODS ====================

  /**
   * Checks if the user account is active.
   *
   * @return true if the account is active, false otherwise
   */
  public boolean isActive() {
    return isActive;
  }

  /**
   * Checks if the user's email has been verified.
   *
   * @return true if email is verified, false otherwise
   */
  public boolean isEmailVerified() {
    return isEmailVerified;
  }

  /**
   * Checks if the user's age has been verified.
   *
   * @return true if age is verified, false otherwise
   */
  public boolean isAgeVerified() {
    return ageVerified;
  }

  /**
   * Check if user has a specific role.
   *
   * @param role the role to check
   * @return true if user has the role, false otherwise
   */
  public boolean hasRole(Role role) {
    return roles != null && roles.contains(role);
  }

  /**
   * Check if user has any of the specified roles.
   *
   * @param roles the roles to check
   * @return true if user has at least one of the roles, false otherwise
   */
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

  /**
   * Check if user has all of the specified roles.
   *
   * @param roles the roles to check
   * @return true if user has all the roles, false otherwise
   */
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

  /**
   * Add a role to the user.
   *
   * @param role the role to add
   */
  public void addRole(Role role) {
    if (this.roles == null) {
      this.roles = new HashSet<>();
    }
    this.roles.add(role);
  }

  /**
   * Remove a role from the user.
   *
   * @param role the role to remove
   */
  public void removeRole(Role role) {
    if (this.roles != null) {
      this.roles.remove(role);
    }
  }

  /**
   * Check if user is a merchant admin.
   *
   * @return true if user has MERCHANT_ADMIN role and has a merchant ID, false otherwise
   */
  public boolean isMerchantAdmin() {
    return hasRole(Role.MERCHANT_ADMIN) && merchantId != null;
  }

  /**
   * Check if user is a driver.
   *
   * @return true if user has DRIVER role and has a driver entity, false otherwise
   */
  public boolean isDriver() {
    return hasRole(Role.DRIVER) && driver != null;
  }

  /**
   * Check if user is an admin.
   *
   * @return true if user has ADMIN role, false otherwise
   */
  public boolean isAdmin() {
    return hasRole(Role.ADMIN);
  }

  /**
   * Check if user owns/manages a specific merchant.
   *
   * @param merchantId the ID of the merchant to check
   * @return true if user is a merchant admin and owns the specified merchant, false otherwise
   */
  public boolean ownsMerchant(Long merchantId) {
    return isMerchantAdmin() && this.merchantId != null && this.merchantId.equals(merchantId);
  }
}
