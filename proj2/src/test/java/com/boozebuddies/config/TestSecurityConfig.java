package com.boozebuddies.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Test security configuration to disable security and CSRF for controller tests. Placed under
 * src/test so it's only picked up during tests.
 */
@TestConfiguration
public class TestSecurityConfig {

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf()
        .disable()
        .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
        .httpBasic()
        .disable();
    return http.build();
  }
}
