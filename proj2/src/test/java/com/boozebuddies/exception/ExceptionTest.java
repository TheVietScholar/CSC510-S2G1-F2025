package com.boozebuddies.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Exception Classes Tests")
public class ExceptionTest {

  // ==================== DriverNotFoundException Tests ====================

  @Test
  @DisplayName("DriverNotFoundException with message")
  void driverNotFoundException_withMessage() {
    String message = "Driver not found";
    DriverNotFoundException exception = new DriverNotFoundException(message);

    assertEquals(message, exception.getMessage());
    assertNull(exception.getCause());
  }

  @Test
  @DisplayName("DriverNotFoundException with message and cause")
  void driverNotFoundException_withMessageAndCause() {
    String message = "Driver not found";
    Throwable cause = new RuntimeException("Root cause");
    DriverNotFoundException exception = new DriverNotFoundException(message, cause);

    assertEquals(message, exception.getMessage());
    assertEquals(cause, exception.getCause());
  }

  // ==================== UserNotFoundException Tests ====================

  @Test
  @DisplayName("UserNotFoundException with message")
  void userNotFoundException_withMessage() {
    String message = "User not found";
    UserNotFoundException exception = new UserNotFoundException(message);

    assertEquals(message, exception.getMessage());
    assertNull(exception.getCause());
  }

  @Test
  @DisplayName("UserNotFoundException with message and cause")
  void userNotFoundException_withMessageAndCause() {
    String message = "User not found";
    Throwable cause = new RuntimeException("Root cause");
    UserNotFoundException exception = new UserNotFoundException(message, cause);

    assertEquals(message, exception.getMessage());
    assertEquals(cause, exception.getCause());
  }

  // ==================== UserAlreadyExistsException Tests ====================

  @Test
  @DisplayName("UserAlreadyExistsException with message")
  void userAlreadyExistsException_withMessage() {
    String message = "User already exists";
    UserAlreadyExistsException exception = new UserAlreadyExistsException(message);

    assertEquals(message, exception.getMessage());
    assertNull(exception.getCause());
  }

  @Test
  @DisplayName("UserAlreadyExistsException with message and cause")
  void userAlreadyExistsException_withMessageAndCause() {
    String message = "User already exists";
    Throwable cause = new RuntimeException("Root cause");
    UserAlreadyExistsException exception = new UserAlreadyExistsException(message, cause);

    assertEquals(message, exception.getMessage());
    assertEquals(cause, exception.getCause());
  }

  // ==================== InvalidCredentialsException Tests ====================

  @Test
  @DisplayName("InvalidCredentialsException with message")
  void invalidCredentialsException_withMessage() {
    String message = "Invalid credentials";
    InvalidCredentialsException exception = new InvalidCredentialsException(message);

    assertEquals(message, exception.getMessage());
    assertNull(exception.getCause());
  }

  @Test
  @DisplayName("InvalidCredentialsException with message and cause")
  void invalidCredentialsException_withMessageAndCause() {
    String message = "Invalid credentials";
    Throwable cause = new RuntimeException("Root cause");
    InvalidCredentialsException exception = new InvalidCredentialsException(message, cause);

    assertEquals(message, exception.getMessage());
    assertEquals(cause, exception.getCause());
  }

  // ==================== InvalidTokenException Tests ====================

  @Test
  @DisplayName("InvalidTokenException with message")
  void invalidTokenException_withMessage() {
    String message = "Invalid token";
    InvalidTokenException exception = new InvalidTokenException(message);

    assertEquals(message, exception.getMessage());
    assertNull(exception.getCause());
  }

  @Test
  @DisplayName("InvalidTokenException with message and cause")
  void invalidTokenException_withMessageAndCause() {
    String message = "Invalid token";
    Throwable cause = new RuntimeException("Root cause");
    InvalidTokenException exception = new InvalidTokenException(message, cause);

    assertEquals(message, exception.getMessage());
    assertEquals(cause, exception.getCause());
  }

  // ==================== UnauthorizedException Tests ====================

  @Test
  @DisplayName("UnauthorizedException with message")
  void unauthorizedException_withMessage() {
    String message = "Unauthorized";
    UnauthorizedException exception = new UnauthorizedException(message);

    assertEquals(message, exception.getMessage());
    assertNull(exception.getCause());
  }

  @Test
  @DisplayName("UnauthorizedException with message and cause")
  void unauthorizedException_withMessageAndCause() {
    String message = "Unauthorized";
    Throwable cause = new RuntimeException("Root cause");
    UnauthorizedException exception = new UnauthorizedException(message, cause);

    assertEquals(message, exception.getMessage());
    assertEquals(cause, exception.getCause());
  }

  // ==================== ValidationException Tests ====================

  @Test
  @DisplayName("ValidationException with message")
  void validationException_withMessage() {
    String message = "Validation failed";
    ValidationException exception = new ValidationException(message);

    assertEquals(message, exception.getMessage());
    assertNull(exception.getCause());
  }

  @Test
  @DisplayName("ValidationException with message and cause")
  void validationException_withMessageAndCause() {
    String message = "Validation failed";
    Throwable cause = new RuntimeException("Root cause");
    ValidationException exception = new ValidationException(message, cause);

    assertEquals(message, exception.getMessage());
    assertEquals(cause, exception.getCause());
  }
}
