package com.boozebuddies;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the BoozeBuddies Spring Boot application.
 *
 * <p>This class bootstraps the application using Spring Boot's auto-configuration. The protected
 * constructor prevents instantiation of this class.
 */
@SpringBootApplication
public class BoozeBuddiesApplication {

  /** Protected constructor to prevent instantiation of the application class. */
  protected BoozeBuddiesApplication() {
    // Prevent instantiation
  }

  /**
   * Main method that starts the Spring Boot application.
   *
   * @param args command-line arguments passed to the application
   */
  public static void main(String[] args) {
    SpringApplication.run(BoozeBuddiesApplication.class, args);
  }
}
