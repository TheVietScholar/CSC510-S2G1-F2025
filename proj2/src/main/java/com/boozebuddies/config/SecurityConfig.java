package com.boozebuddies.config;

import com.boozebuddies.security.JwtAuthenticationFilter;
import java.util.Arrays;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/** Security configuration for the application using JWT authentication. */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  /**
   * Constructor injection for JWT authentication filter.
   *
   * @param jwtAuthenticationFilter the JWT authentication filter
   */
  public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
  }

  /**
   * Configures the security filter chain with JWT authentication and role-based access control.
   *
   * @param http the HttpSecurity to configure
   * @return the configured SecurityFilterChain
   * @throws Exception if an error occurs during configuration
   */
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        // Disable CSRF since we're using JWT (stateless)
        .csrf(csrf -> csrf.disable())

        // Configure CORS
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))

        // Set session management to stateless (no sessions, JWT only)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

        // Configure authorization rules
        .authorizeHttpRequests(
            auth ->
                auth
                    // ==================== PUBLIC ENDPOINTS ====================
                    .requestMatchers("/api/auth/**")
                    .permitAll()
                    .requestMatchers("/actuator/health")
                    .permitAll()
                    .requestMatchers("/h2-console/**")
                    .permitAll()
                    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**")
                    .permitAll()

                    // ==================== USER ENDPOINTS ====================
                    // Users can view/update their own profile (enforced in controller)
                    .requestMatchers(HttpMethod.GET, "/api/users/me")
                    .authenticated()
                    .requestMatchers(HttpMethod.GET, "/api/users/{id}")
                    .authenticated()
                    .requestMatchers(HttpMethod.PUT, "/api/users/{id}")
                    .authenticated()

                    // Only ADMIN can view all users or delete users
                    .requestMatchers(HttpMethod.GET, "/api/users")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/users/**")
                    .hasRole("ADMIN")

                    // Age verification
                    .requestMatchers(HttpMethod.POST, "/api/users/{id}/verify-age")
                    .authenticated()

                    // Role management (ADMIN only)
                    .requestMatchers("/api/users/*/roles/**")
                    .hasRole("ADMIN")
                    .requestMatchers("/api/users/*/merchant")
                    .hasRole("ADMIN")

                    // ==================== MERCHANT ENDPOINTS ====================
                    // Anyone authenticated can browse/search merchants
                    .requestMatchers(HttpMethod.GET, "/api/merchants")
                    .authenticated()
                    .requestMatchers(HttpMethod.GET, "/api/merchants/search/**")
                    .authenticated()
                    .requestMatchers(HttpMethod.GET, "/api/merchants/by-distance")
                    .authenticated()
                    .requestMatchers(HttpMethod.GET, "/api/merchants/name/**")
                    .authenticated()

                    // Only ADMIN can view merchant by ID
                    .requestMatchers(HttpMethod.GET, "/api/merchants/{id}")
                    .hasRole("ADMIN")

                    // Only ADMIN can create/delete merchants
                    .requestMatchers(HttpMethod.POST, "/api/merchants")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/merchants/**")
                    .hasRole("ADMIN")

                    // ADMIN or MERCHANT_ADMIN can update merchants (ownership checked in
                    // controller)
                    .requestMatchers(HttpMethod.PUT, "/api/merchants/{id}")
                    .hasAnyRole("ADMIN", "MERCHANT_ADMIN")

                    // MERCHANT_ADMIN specific endpoints (their own merchant)
                    .requestMatchers("/api/merchants/my-merchant/**")
                    .hasRole("MERCHANT_ADMIN")

                    // ==================== PRODUCT ENDPOINTS ====================
                    .requestMatchers(HttpMethod.GET, "/api/products/**")
                    .authenticated()
                    .requestMatchers(HttpMethod.POST, "/api/products/**")
                    .hasAnyRole("ADMIN", "MERCHANT_ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/products/**")
                    .hasAnyRole("ADMIN", "MERCHANT_ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/products/**")
                    .hasAnyRole("ADMIN", "MERCHANT_ADMIN")

                    // ==================== ORDER ENDPOINTS ====================
                    // Users place and manage their own orders
                    .requestMatchers(HttpMethod.POST, "/api/orders")
                    .hasRole("USER")
                    .requestMatchers(HttpMethod.GET, "/api/orders/my-orders/**")
                    .hasRole("USER")
                    .requestMatchers(HttpMethod.PUT, "/api/orders/{id}/cancel")
                    .hasRole("USER")
                    .requestMatchers(HttpMethod.GET, "/api/orders/my-orders")
                    .hasRole("USER")

                    // MERCHANT_ADMIN can view orders for their merchant
                    .requestMatchers("/api/orders/merchant/**")
                    .hasRole("MERCHANT_ADMIN")

                    // DRIVER can view assigned orders
                    .requestMatchers("/api/orders/driver/**")
                    .hasRole("DRIVER")

                    // ADMIN can view all orders
                    .requestMatchers(HttpMethod.GET, "/api/orders")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/api/orders/{id}")
                    .authenticated()

                    // ==================== PAYMENT ENDPOINTS ====================
                    .requestMatchers("/api/payments/**")
                    .authenticated()

                    // ==================== DELIVERY ENDPOINTS ====================
                    // Drivers manage deliveries
                    .requestMatchers("/api/deliveries/driver/**")
                    .hasRole("DRIVER")
                    .requestMatchers(HttpMethod.POST, "/api/deliveries/{id}/pickup")
                    .hasRole("DRIVER")
                    .requestMatchers(HttpMethod.POST, "/api/deliveries/{id}/deliver")
                    .hasRole("DRIVER")
                    .requestMatchers(HttpMethod.POST, "/api/deliveries/{id}/verify-age")
                    .hasRole("DRIVER")
                    .requestMatchers(HttpMethod.PUT, "/api/deliveries/{id}/location")
                    .hasRole("DRIVER")

                    // Admin can view all deliveries
                    .requestMatchers(HttpMethod.GET, "/api/deliveries")
                    .hasRole("ADMIN")

                    // ==================== DRIVER MANAGEMENT ENDPOINTS ====================
                    // ADMIN manages driver certifications and views all drivers
                    .requestMatchers(HttpMethod.GET, "/api/drivers")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/api/drivers/available")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/api/drivers/{id}")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/drivers/{id}/certification")
                    .hasRole("ADMIN")

                    // Drivers manage their own profile
                    .requestMatchers("/api/drivers/my-profile/**")
                    .hasRole("DRIVER")

                    // ==================== ADMIN ENDPOINTS ====================
                    .requestMatchers("/api/admin/**")
                    .hasRole("ADMIN")

                    // ==================== DEFAULT ====================
                    // All other requests require authentication
                    .anyRequest()
                    .authenticated())

        // Add JWT filter before UsernamePasswordAuthenticationFilter
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

        // Disable HTTP Basic authentication (we're using JWT)
        .httpBasic(httpBasic -> httpBasic.disable())

        // Disable form login (we're using JWT)
        .formLogin(formLogin -> formLogin.disable());

    // Allow H2 console frames (only for development)
    http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

    return http.build();
  }

  /**
   * Configures CORS settings for cross-origin requests.
   *
   * @return the CORS configuration source
   */
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    // DEVELOPMENT: Allow localhost for frontend development
    // Uncomment these if you're building a React/Angular/Vue frontend
    // configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000",
    // "http://localhost:4200"));

    // PRODUCTION: Use specific domain
    // configuration.setAllowedOrigins(Arrays.asList("https://yourdomain.com",
    // "https://www.yourdomain.com"));

    // FOR NOW (API testing only): Allow all origins
    // This is fine if you're only testing with Postman/curl and have no frontend yet
    configuration.addAllowedOriginPattern("*");

    configuration.setAllowedMethods(
        Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setExposedHeaders(Arrays.asList("Authorization"));
    configuration.setAllowCredentials(true);
    configuration.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
