package com.boozebuddies.exception;

/** Exception thrown when a JWT token is invalid or expired. */
public class InvalidTokenException extends RuntimeException {

  /**
   * Constructs a new InvalidTokenException with the specified message.
   *
   * @param message the detail message
   */
  public InvalidTokenException(String message) {
    super(message);
  }

  /**
   * Constructs a new InvalidTokenException with the specified message and cause.
   *
   * @param message the detail message
   * @param cause the cause of the exception
   */
  public InvalidTokenException(String message, Throwable cause) {
    super(message, cause);
  }
}
