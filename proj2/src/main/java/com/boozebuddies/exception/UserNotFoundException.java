package com.boozebuddies.exception;

/** Exception thrown when a requested user cannot be found. */
public class UserNotFoundException extends RuntimeException {

  /**
   * Constructs a new UserNotFoundException with the specified message.
   *
   * @param message the detail message
   */
  public UserNotFoundException(String message) {
    super(message);
  }

  /**
   * Constructs a new UserNotFoundException with the specified message and cause.
   *
   * @param message the detail message
   * @param cause the cause of the exception
   */
  public UserNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}
