package com.boozebuddies.security;

import com.boozebuddies.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

  private final SecretKey key;
  private final long jwtExpirationMs;

  public JwtUtil(
      @Value("${jwt.secret:boozebuddies-super-secret-key-change-in-production-minimum-256-bits}")
          String secret,
      @Value("${jwt.expirationMs:900000}") long jwtExpirationMs) {
    // Ensure the secret is at least 256 bits (32 bytes) for HS256
    if (secret.length() < 32) {
      throw new IllegalArgumentException(
          "JWT secret must be at least 32 characters (256 bits) for HS256");
    }
    byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
    this.key = Keys.hmacShaKeyFor(secretBytes);
    this.jwtExpirationMs = jwtExpirationMs;
  }

  /** Generate JWT token for user */
  public String generateToken(User user) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + jwtExpirationMs);

    return Jwts.builder()
        .setSubject(user.getEmail())
        .claim("userId", user.getId())
        .claim("name", user.getName())
        .setIssuedAt(now)
        .setExpiration(expiry)
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  /** Extract claims from token - Returns null if invalid */
  private Claims getClaims(String token) {
    try {
      return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    } catch (ExpiredJwtException e) {
      // Token is expired but we can still get claims
      return e.getClaims();
    } catch (JwtException | IllegalArgumentException e) {
      // Invalid signature or malformed token
      return null;
    }
  }

  /** Extract username (email) from token */
  public String extractUsername(String token) {
    Claims claims = getClaims(token);
    return claims != null ? claims.getSubject() : null;
  }

  /** Extract user ID from token */
  public Long extractUserId(String token) {
    Claims claims = getClaims(token);
    if (claims == null) return null;
    Object userIdObj = claims.get("userId");
    if (userIdObj instanceof Integer) {
      return ((Integer) userIdObj).longValue();
    }
    return (Long) userIdObj;
  }

  /** Check if token is expired */
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

  /** Validate token against user */
  public boolean validateToken(String token, User user) {
    if (token == null || user == null) {
      return false;
    }

    try {
      Claims claims =
          Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();

      String username = claims.getSubject();
      Date expiration = claims.getExpiration();

      return username != null
          && username.equalsIgnoreCase(user.getEmail())
          && expiration != null
          && !expiration.before(new Date());

    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }

  /** Validate token without user check (just signature and expiration) */
  public boolean validateToken(String token) {
    try {
      Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }
}
