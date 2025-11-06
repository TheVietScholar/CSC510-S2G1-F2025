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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * A Spring Security filter that validates and processes JWTs (JSON Web Tokens) from the {@code
 * Authorization} header on each HTTP request.
 *
 * <p>This filter runs once per request, before any authorization logic executes. It extracts a JWT,
 * validates it, and if valid, sets the corresponding authenticated {@link
 * org.springframework.security.core.Authentication} object into the {@link SecurityContextHolder}.
 *
 * <p>Public endpoints (such as login or registration) are automatically excluded from filtering for
 * efficiency.
 *
 * <p>Typical request flow:
 *
 * <ol>
 *   <li>Client sends an HTTP request with {@code Authorization: Bearer <token>}
 *   <li>The filter extracts and validates the token
 *   <li>If valid, the user is authenticated and the SecurityContext is populated
 *   <li>If invalid, the request proceeds unauthenticated (secured endpoints will reject it)
 * </ol>
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String BEARER_PREFIX = "Bearer ";
  private static final int BEARER_PREFIX_LENGTH = 7;

  private final JwtUtil jwtUtil;
  private final UserService userService;

  /**
   * Constructs a {@code JwtAuthenticationFilter} with the required dependencies.
   *
   * @param jwtUtil utility class for JWT generation, extraction, and validation
   * @param userService service for retrieving user details from the database
   */
  public JwtAuthenticationFilter(JwtUtil jwtUtil, UserService userService) {
    this.jwtUtil = jwtUtil;
    this.userService = userService;
  }

  /**
   * Core filtering logic that executes once per request.
   *
   * <p>Extracts the JWT from the Authorization header, validates it, and if valid, builds an
   * authentication object to set in the security context. If the token is missing or invalid, the
   * filter simply passes the request along without setting authentication (allowing public
   * endpoints to function).
   *
   * @param request the current HTTP request
   * @param response the current HTTP response
   * @param filterChain the filter chain to continue execution
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an input/output error occurs
   */
  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
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
   * Extracts the JWT token from the {@code Authorization} header.
   *
   * <p>Expected header format: {@code Authorization: Bearer <token>}
   *
   * @param request the current HTTP request
   * @return the extracted JWT token, or {@code null} if not present or improperly formatted
   */
  private String extractJwtFromRequest(HttpServletRequest request) {
    String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
    if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
      return bearerToken.substring(BEARER_PREFIX_LENGTH);
    }
    return null;
  }

  /**
   * Validates a JWT and sets the authentication context if valid.
   *
   * <p>This method extracts the username and roles from the token, optionally checks the user
   * record in the database for activeness, and validates the token's signature. Upon success, an
   * authenticated {@link UsernamePasswordAuthenticationToken} is placed into the {@link
   * SecurityContextHolder}.
   *
   * @param token the JWT to authenticate
   * @param request the current HTTP request
   */
  private void authenticateToken(String token, HttpServletRequest request) {
    String username = jwtUtil.extractUsername(token);
    if (username == null) {
      log.debug("Cannot extract username from token");
      return;
    }

    // Extract roles from token claims
    Set<String> roles = jwtUtil.extractRoles(token);
    log.debug("Extracted roles from token: {}", roles);

    // Map role names to Spring Security authorities
    Set<SimpleGrantedAuthority> authorities =
        roles.stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
            .collect(Collectors.toSet());

    // Verify the user still exists / active
    User user = userService.findByEmail(username).orElse(null);
    if (user == null) {
      log.warn("User not found for email: {}", username);
      return;
    }

    if (!user.isActive()) {
      log.warn("User is inactive: {}", username);
      return;
    }

    if (!jwtUtil.validateToken(token, user)) {
      log.warn("Token validation failed for user: {}", username);
      return;
    }

    // Use roles from user entity if token roles are empty (fallback)
    if (authorities.isEmpty() && user.getRoles() != null && !user.getRoles().isEmpty()) {
      authorities = buildAuthorities(user);
      log.debug("Using roles from user entity: {}", authorities);
    }

    // Build authentication token and set security context
    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(user, null, authorities);
    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
    SecurityContextHolder.getContext().setAuthentication(authentication);
    log.debug("Authentication set for user: {} with authorities: {}", username, authorities);
  }

  /**
   * Constructs a set of {@link SimpleGrantedAuthority} objects from the user’s roles.
   *
   * @param user the {@link User} entity containing assigned roles
   * @return a set of authorities prefixed with {@code ROLE_}, or an empty set if none
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
   * Determines whether the filter should skip processing for the given request.
   *
   * <p>Used to bypass JWT validation for public or system endpoints such as login, registration,
   * health checks, and database consoles.
   *
   * @param request the current HTTP request
   * @return {@code true} if the request path matches a public endpoint; otherwise {@code false}
   */
  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    // Skip filter for public authentication and monitoring endpoints
    return path.startsWith("/api/auth/login")
        || path.startsWith("/api/auth/register")
        || path.startsWith("/api/auth/refresh")
        || path.startsWith("/actuator/")
        || path.startsWith("/h2-console/");
  }
}
