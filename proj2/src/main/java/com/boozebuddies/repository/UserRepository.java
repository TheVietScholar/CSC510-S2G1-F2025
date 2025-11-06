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

/**
 * Repository interface for performing database operations on {@link User} entities.
 *
 * <p>This interface provides methods for user authentication, role management, age verification,
 * account status filtering, and advanced search capabilities.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  // ==================== Authentication Methods ====================

  /**
   * Finds a user by their email address, ignoring case.
   *
   * <p>Used primarily for authentication and login operations.
   *
   * @param email the user's email address
   * @return an {@link Optional} containing the user if found
   */
  Optional<User> findByEmailIgnoreCase(String email);

  /**
   * Checks whether a user exists with the given email (case-insensitive).
   *
   * <p>Used to validate email uniqueness during registration.
   *
   * @param email the user's email address
   * @return true if a user exists with that email, false otherwise
   */
  boolean existsByEmailIgnoreCase(String email);

  /**
   * Finds a user by their refresh token.
   *
   * <p>Used for JWT token refresh operations.
   *
   * @param refreshToken the refresh token
   * @return an {@link Optional} containing the user if found
   */
  Optional<User> findByRefreshToken(String refreshToken);

  // ==================== Role-based Queries ====================

  /**
   * Finds users assigned to a specific {@link Role}.
   *
   * @param role the role to filter by
   * @param pageable pagination information
   * @return a {@link Page} of users with the specified role
   */
  @Query("SELECT u FROM User u JOIN u.roles r WHERE r = :role")
  Page<User> findByRole(@Param("role") Role role, Pageable pageable);

  /**
   * Retrieves all users assigned to a specific {@link Role}.
   *
   * @param role the role to filter by
   * @return a list of users with the specified role
   */
  @Query("SELECT u FROM User u JOIN u.roles r WHERE r = :role")
  java.util.List<User> findAllByRole(@Param("role") Role role);

  // ==================== Age Verification Queries ====================

  /**
   * Finds users who meet the legal drinking age requirement (21+).
   *
   * <p>Useful for compliance and reporting purposes.
   *
   * @param cutoffDob the cutoff date of birth representing legal age
   * @param pageable pagination information
   * @return a {@link Page} of users who are of legal age
   */
  @Query("SELECT u FROM User u WHERE u.dateOfBirth <= :cutoff")
  Page<User> findUsersOfLegalAge(@Param("cutoff") LocalDate cutoffDob, Pageable pageable);

  /**
   * Finds all users who have been age-verified.
   *
   * @param pageable pagination information
   * @return a {@link Page} of age-verified users
   */
  @Query("SELECT u FROM User u WHERE u.ageVerified = true")
  Page<User> findAgeVerified(Pageable pageable);

  /**
   * Counts the number of users who have been age-verified.
   *
   * @return the number of age-verified users
   */
  long countByAgeVerifiedTrue();

  // ==================== Account Status Queries ====================

  /**
   * Finds all active users.
   *
   * @param pageable pagination information
   * @return a {@link Page} of active users
   */
  Page<User> findByIsActiveTrue(Pageable pageable);

  /**
   * Finds all inactive or deactivated users.
   *
   * @param pageable pagination information
   * @return a {@link Page} of inactive users
   */
  Page<User> findByIsActiveFalse(Pageable pageable);

  /**
   * Finds users who have verified their email addresses.
   *
   * @param pageable pagination information
   * @return a {@link Page} of users with verified emails
   */
  Page<User> findByIsEmailVerifiedTrue(Pageable pageable);

  /**
   * Finds users who have not verified their email addresses.
   *
   * @param pageable pagination information
   * @return a {@link Page} of users with unverified emails
   */
  Page<User> findByIsEmailVerifiedFalse(Pageable pageable);

  /**
   * Finds users eligible to place orders — active, email verified, and age verified.
   *
   * @param pageable pagination information
   * @return a {@link Page} of eligible users
   */
  @Query(
      "SELECT u FROM User u WHERE u.isActive = true AND u.isEmailVerified = true AND u.ageVerified = true")
  Page<User> findEligibleUsers(Pageable pageable);

  // ==================== Search Queries ====================

  /**
   * Searches for users by name or email address (case-insensitive).
   *
   * @param searchTerm the keyword to search by
   * @param pageable pagination information
   * @return a {@link Page} of users matching the search term
   */
  @Query(
      "SELECT u FROM User u WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
  Page<User> searchUsers(@Param("searchTerm") String searchTerm, Pageable pageable);
}
