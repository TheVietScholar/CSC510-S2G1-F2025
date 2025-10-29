package com.boozebuddies.config;

import com.boozebuddies.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  // Constructor injection - Spring will automatically create JwtAuthenticationFilter
  public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        // Disable CSRF since we're using JWT (stateless)
        .csrf(csrf -> csrf.disable())
        
        // Configure CORS
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        
        // Set session management to stateless (no sessions, JWT only)
        .sessionManagement(session -> 
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        
        // Configure authorization rules
        .authorizeHttpRequests(auth -> auth
            // Public endpoints - no authentication required
            .requestMatchers("/api/auth/**").permitAll()
            .requestMatchers("/api/auth/register").permitAll()
            .requestMatchers("/api/auth/login").permitAll()
            .requestMatchers("/api/auth/refresh").permitAll()
            
            // Health check endpoints (Spring Actuator)
            .requestMatchers("/actuator/health").permitAll()
            
            // H2 Console (only for development)
            .requestMatchers("/h2-console/**").permitAll()
            
            // Swagger/OpenAPI (if you add it later)
            .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
            
            // User endpoints - require authentication
            .requestMatchers(HttpMethod.GET, "/api/users/**").authenticated()
            .requestMatchers(HttpMethod.PUT, "/api/users/**").authenticated()
            .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasRole("ADMIN")
            
            // Merchant endpoints - require MERCHANT_ADMIN role
            .requestMatchers("/api/merchants/**").hasAnyRole("MERCHANT_ADMIN", "ADMIN")
            
            // Driver endpoints - require DRIVER role
            .requestMatchers("/api/drivers/**").hasAnyRole("DRIVER", "ADMIN")
            
            // Order endpoints - require authentication
            .requestMatchers("/api/orders/**").authenticated()
            
            // Admin endpoints - require ADMIN or SUPER_ADMIN role
            .requestMatchers("/api/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
            
            // All other requests require authentication
            .anyRequest().authenticated()
        )
        
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
   * CORS configuration for cross-origin requests
   * IMPORTANT: Configure this properly for production!
   */
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    
    // DEVELOPMENT: Allow localhost for frontend development
    // Uncomment these if you're building a React/Angular/Vue frontend
    // configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:4200"));
    
    // PRODUCTION: Use specific domain
    // configuration.setAllowedOrigins(Arrays.asList("https://yourdomain.com", "https://www.yourdomain.com"));
    
    // FOR NOW (API testing only): Allow all origins
    // This is fine if you're only testing with Postman/curl and have no frontend yet
    configuration.addAllowedOriginPattern("*");
    
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setExposedHeaders(Arrays.asList("Authorization"));
    configuration.setAllowCredentials(true);
    configuration.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}