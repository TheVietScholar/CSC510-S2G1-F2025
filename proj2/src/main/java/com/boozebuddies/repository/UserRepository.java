package com.boozebuddies.repository;

import com.boozebuddies.entity.User;
import com.boozebuddies.model.Role;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  // ==================== Authentication Methods ====================

  /** Find user by email (case-insensitive). Used for login and authentication. */
  Optional<User> findByEmailIgnoreCase(String email);

  /** Check if email exists (case-insensitive). Used for registration validation. */
  boolean existsByEmailIgnoreCase(String email);

  /** Find user by refresh token. Used for JWT token refresh flow. */
  Optional<User> findByRefreshToken(String refreshToken);

  /** Find user by refresh token id (used when refresh tokens are stored as hashed secrets + id). */
  Optional<User> findByRefreshTokenId(String refreshTokenId);

  // ==================== Role-based Queries ====================

  /** Find users by specific role. Updated to work with Role enum. */
  @Query("SELECT u FROM User u JOIN u.roles r WHERE r = :role")
  Page<User> findByRole(@Param("role") Role role, Pageable pageable);

  /** Find all users with a specific role (returns list). */
  @Query("SELECT u FROM User u JOIN u.roles r WHERE r = :role")
  java.util.List<User> findAllByRole(@Param("role") Role role);

  // ==================== Age Verification Queries ====================

  /** Find users of legal drinking age (21+). For compliance reports. */
  @Query("SELECT u FROM User u WHERE u.dateOfBirth <= :cutoff")
  Page<User> findUsersOfLegalAge(@Param("cutoff") LocalDate cutoffDob, Pageable pageable);

  /** Find all age-verified users. */
  @Query("SELECT u FROM User u WHERE u.ageVerified = true")
  Page<User> findAgeVerified(Pageable pageable);

  /** Count age-verified users. */
  long countByAgeVerifiedTrue();

  // ==================== Account Status Queries ====================

  /** Find all active users. */
  Page<User> findByIsActiveTrue(Pageable pageable);

  /** Find all inactive/deactivated users. */
  Page<User> findByIsActiveFalse(Pageable pageable);

  /** Find users with verified emails. */
  Page<User> findByIsEmailVerifiedTrue(Pageable pageable);

  /** Find users with unverified emails. */
  Page<User> findByIsEmailVerifiedFalse(Pageable pageable);

  /** Find users who can place orders (active, email verified, age verified). */
  @Query(
      "SELECT u FROM User u WHERE u.isActive = true AND u.isEmailVerified = true AND u.ageVerified = true")
  Page<User> findEligibleUsers(Pageable pageable);

  // ==================== Search Queries ====================

  /** Search users by name or email (case-insensitive). */
  @Query(
      "SELECT u FROM User u WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
  Page<User> searchUsers(@Param("searchTerm") String searchTerm, Pageable pageable);
}
