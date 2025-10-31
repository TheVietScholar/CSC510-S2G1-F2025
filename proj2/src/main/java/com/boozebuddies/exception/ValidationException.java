package com.boozebuddies.exception;

/**
 * Exception thrown when validation fails (e.g., invalid role assignment, missing required data).
 * Results in HTTP 400 Bad Request.
 */
public class ValidationException extends RuntimeException {

  public ValidationException(String message) {
    super(message);
  }

  public ValidationException(String message, Throwable cause) {
    super(message, cause);
  }
}
