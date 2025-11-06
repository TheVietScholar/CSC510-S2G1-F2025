package com.boozebuddies.exception;

/**
 * Exception thrown when validation fails (e.g., invalid role assignment, missing required data).
 * Results in HTTP 400 Bad Request.
 */
public class ValidationException extends RuntimeException {

  /**
   * Constructs a new ValidationException with the specified message.
   *
   * @param message the detail message
   */
  public ValidationException(String message) {
    super(message);
  }

  /**
   * Constructs a new ValidationException with the specified message and cause.
   *
   * @param message the detail message
   * @param cause the cause of the exception
   */
  public ValidationException(String message, Throwable cause) {
    super(message, cause);
  }
}
