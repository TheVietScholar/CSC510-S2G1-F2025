package com.boozebuddies.security;

import com.boozebuddies.entity.User;
import com.boozebuddies.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filter that validates JWT from the Authorization header and sets the SecurityContext.
 * Runs once per request before Spring Security checks authorization.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String BEARER_PREFIX = "Bearer ";
  private static final int BEARER_PREFIX_LENGTH = 7;

  private final JwtUtil jwtUtil;
  private final UserService userService;

  public JwtAuthenticationFilter(JwtUtil jwtUtil, UserService userService) {
    this.jwtUtil = jwtUtil;
    this.userService = userService;
  }

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    try {
      String jwt = extractJwtFromRequest(request);

      if (jwt != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        authenticateToken(jwt, request);
      }
    } catch (Exception ex) {
      log.error("Cannot set user authentication: {}", ex.getMessage());
      // Continue filter chain even if authentication fails
      // Secured endpoints will reject unauthenticated requests
    }

    filterChain.doFilter(request, response);
  }

  /**
   * Extract JWT token from Authorization header
   */
  private String extractJwtFromRequest(HttpServletRequest request) {
    String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
    if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
      return bearerToken.substring(BEARER_PREFIX_LENGTH);
    }
    return null;
  }

  /**
   * Authenticate the JWT token and set SecurityContext
   */
  private void authenticateToken(String token, HttpServletRequest request) {
    String username = jwtUtil.extractUsername(token);

    if (username == null) {
      log.debug("JWT token does not contain username");
      return;
    }

    User user = userService.findByEmail(username).orElse(null);

    if (user == null) {
      log.debug("User not found for username: {}", username);
      return;
    }

    if (!user.isActive()) {
      log.debug("User account is deactivated: {}", username);
      return;
    }

    if (!jwtUtil.validateToken(token, user)) {
      log.debug("JWT token validation failed for user: {}", username);
      return;
    }

    // Build authorities from user roles
    Set<SimpleGrantedAuthority> authorities = buildAuthorities(user);

    // Create authentication token
    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(user, null, authorities);

    // Set additional details (IP address, session ID, etc.)
    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

    // Set authentication in SecurityContext
    SecurityContextHolder.getContext().setAuthentication(authentication);

    log.debug("User authenticated successfully: {}", username);
  }

  /**
   * Build Spring Security authorities from user roles
   */
  private Set<SimpleGrantedAuthority> buildAuthorities(User user) {
    if (user.getRoles() == null || user.getRoles().isEmpty()) {
      return Collections.emptySet();
    }

    return user.getRoles().stream()
        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
        .collect(Collectors.toSet());
  }

  /**
   * Skip JWT filter for public endpoints (optional optimization)
   */
  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    // Skip filter for public auth endpoints
    return path.startsWith("/api/auth/login")
        || path.startsWith("/api/auth/register")
        || path.startsWith("/api/auth/refresh")
        || path.startsWith("/actuator/")
        || path.startsWith("/h2-console/");
  }
}