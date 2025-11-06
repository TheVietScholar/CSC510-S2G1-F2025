package com.boozebuddies.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Configuration class for password encoding. */
@Configuration
public class PasswordEncoderConfig {

  /**
   * Creates a BCrypt password encoder bean for the application.
   *
   * @return a BCryptPasswordEncoder instance
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
