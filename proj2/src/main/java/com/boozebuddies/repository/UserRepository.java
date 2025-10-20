package com.boozebuddies.repository;

import com.boozebuddies.entity.User;
import java.time.LocalDate;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByEmailIgnoreCase(String email);
  boolean existsByEmailIgnoreCase(String email);

  // Simple role filter (roles is ElementCollection<String>)
  @Query("SELECT u FROM User u JOIN u.roles r WHERE LOWER(r) = LOWER(:role)")
  Page<User> findByRole(@Param("role") String role, Pageable pageable);

  // Age-based (for compliance reports)
  @Query("SELECT u FROM User u WHERE u.dateOfBirth <= :cutoff")
  Page<User> findUsersOfLegalAge(@Param("cutoff") LocalDate cutoffDob, Pageable pageable);

  @Query("SELECT u FROM User u WHERE u.ageVerified = true")
  Page<User> findAgeVerified(Pageable pageable);
}
