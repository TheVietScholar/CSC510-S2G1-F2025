package com.boozebuddies.exception;

/** Exception thrown when authentication fails due to invalid credentials. */
public class InvalidCredentialsException extends RuntimeException {

  /**
   * Constructs a new InvalidCredentialsException with the specified message.
   *
   * @param message the detail message
   */
  public InvalidCredentialsException(String message) {
    super(message);
  }

  /**
   * Constructs a new InvalidCredentialsException with the specified message and cause.
   *
   * @param message the detail message
   * @param cause the cause of the exception
   */
  public InvalidCredentialsException(String message, Throwable cause) {
    super(message, cause);
  }
}
