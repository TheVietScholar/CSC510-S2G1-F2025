package com.boozebuddies.security;

import com.boozebuddies.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Utility class for generating, parsing, and validating JSON Web Tokens (JWTs) used for stateless
 * authentication in the BoozeBuddies application.
 *
 * <p>This component handles:
 *
 * <ul>
 *   <li>Creating signed JWT tokens with user claims and expiration times
 *   <li>Extracting claims such as username, user ID, and roles
 *   <li>Validating tokens for integrity, signature, and expiration
 * </ul>
 *
 * <p>Uses the HS256 (HMAC-SHA256) signing algorithm and requires a minimum secret key length of 256
 * bits (32 characters).
 */
@Component
public class JwtUtil {

  private final SecretKey key;
  private final long jwtExpirationMs;

  /**
   * Constructs a {@code JwtUtil} instance with the provided secret key and token expiration period.
   *
   * @param secret the secret key used for signing JWTs (must be at least 32 characters)
   * @param jwtExpirationMs the duration in milliseconds before a token expires
   * @throws IllegalArgumentException if the secret key is shorter than 32 characters
   */
  public JwtUtil(
      @Value("${jwt.secret:boozebuddies-super-secret-key-change-in-production-minimum-256-bits}")
          String secret,
      @Value("${jwt.expirationMs:900000}") long jwtExpirationMs) {

    if (secret.length() < 32) {
      throw new IllegalArgumentException(
          "JWT secret must be at least 32 characters (256 bits) for HS256");
    }

    byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
    this.key = Keys.hmacShaKeyFor(secretBytes);
    this.jwtExpirationMs = jwtExpirationMs;
  }

  /**
   * Generates a new JWT for the given user, embedding essential user claims such as ID, email,
   * name, and roles.
   *
   * <p>The generated token is signed using the configured secret key and expires after the
   * configured duration.
   *
   * @param user the user for whom the token is to be generated
   * @return a signed JWT as a compact string
   */
  public String generateToken(User user) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + jwtExpirationMs);

    var roles =
        user.getRoles() != null
            ? user.getRoles().stream().map(Enum::name).toList()
            : Collections.emptyList();

    return Jwts.builder()
        .setSubject(user.getEmail())
        .claim("userId", user.getId())
        .claim("name", user.getName())
        .claim("roles", roles)
        .setIssuedAt(now)
        .setExpiration(expiry)
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  /**
   * Extracts and parses all claims from a given JWT.
   *
   * <p>If the token is expired, it still returns the claims. Returns {@code null} if the token is
   * malformed or has an invalid signature.
   *
   * @param token the JWT to parse
   * @return a {@link Claims} object containing the token’s payload, or {@code null} if invalid
   */
  private Claims getClaims(String token) {
    try {
      return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    } catch (ExpiredJwtException e) {
      // Token expired, but claims can still be used for limited verification
      return e.getClaims();
    } catch (JwtException | IllegalArgumentException e) {
      // Signature invalid or token malformed
      return null;
    }
  }

  /**
   * Extracts the username (email) from a given JWT.
   *
   * @param token the JWT to extract data from
   * @return the subject (email) if present, otherwise {@code null}
   */
  public String extractUsername(String token) {
    Claims claims = getClaims(token);
    return claims != null ? claims.getSubject() : null;
  }

  /**
   * Extracts the user ID claim from the given JWT.
   *
   * @param token the JWT to extract data from
   * @return the user ID as a {@link Long}, or {@code null} if missing or invalid
   */
  public Long extractUserId(String token) {
    Claims claims = getClaims(token);
    if (claims == null) return null;

    Object userIdObj = claims.get("userId");
    if (userIdObj instanceof Integer) {
      return ((Integer) userIdObj).longValue();
    }
    return (Long) userIdObj;
  }

  /**
   * Checks whether the provided JWT is expired.
   *
   * @param token the JWT to check
   * @return {@code true} if the token is expired or invalid, otherwise {@code false}
   */
  public boolean isTokenExpired(String token) {
    try {
      Claims claims =
          Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
      return claims.getExpiration().before(new Date());
    } catch (ExpiredJwtException e) {
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      return true;
    }
  }

  /**
   * Validates the JWT against a specific user by checking:
   *
   * <ul>
   *   <li>Signature integrity
   *   <li>Token expiration
   *   <li>Subject (username/email) matching the given user
   * </ul>
   *
   * @param token the JWT to validate
   * @param user the user to validate the token against
   * @return {@code true} if the token is valid and matches the user; otherwise {@code false}
   */
  public boolean validateToken(String token, User user) {
    if (token == null || user == null) {
      System.out.println(
          "VALIDATION FAILED: token=" + (token != null) + ", user=" + (user != null));
      return false;
    }

    try {
      Claims claims =
          Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();

      String username = claims.getSubject();
      Date expiration = claims.getExpiration();

      System.out.println("=== TOKEN VALIDATION ===");
      System.out.println("Token username: " + username);
      System.out.println("User email: " + user.getEmail());
      System.out.println(
          "Username match: " + (username != null && username.equalsIgnoreCase(user.getEmail())));
      System.out.println("Expiration: " + expiration);
      System.out.println("Expired: " + (expiration != null && expiration.before(new Date())));

      boolean valid =
          username != null
              && username.equalsIgnoreCase(user.getEmail())
              && expiration != null
              && !expiration.before(new Date());

      System.out.println("VALIDATION RESULT: " + valid);
      return valid;

    } catch (JwtException | IllegalArgumentException e) {
      System.out.println("VALIDATION EXCEPTION: " + e.getMessage());
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Validates the token’s integrity and expiration status without comparing to a user.
   *
   * <p>This method verifies the token’s signature and checks that it has not expired.
   *
   * @param token the JWT to validate
   * @return {@code true} if valid and unexpired; otherwise {@code false}
   */
  public boolean validateToken(String token) {
    try {
      Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }

  /**
   * Extracts user roles from the token claims.
   *
   * <p>Roles are stored as a list of strings under the {@code roles} claim.
   *
   * @param token the JWT to extract roles from
   * @return a set of role names (e.g., {@code ["ADMIN", "USER"]}); returns an empty set if none
   *     found
   */
  @SuppressWarnings("unchecked")
  public Set<String> extractRoles(String token) {
    Claims claims = getClaims(token);
    if (claims == null) return Collections.emptySet();

    Object rolesObj = claims.get("roles");
    if (rolesObj instanceof java.util.List<?>) {
      return ((java.util.List<?>) rolesObj)
          .stream()
              .filter(String.class::isInstance)
              .map(String.class::cast)
              .collect(Collectors.toSet());
    }
    return Collections.emptySet();
  }
}
