package com.boozebuddies.exception;

/** Exception thrown when a driver is not found. */
public class DriverNotFoundException extends RuntimeException {
  /**
   * Constructs a new DriverNotFoundException with the specified message.
   *
   * @param message the detail message
   */
  public DriverNotFoundException(String message) {
    super(message);
  }

  /**
   * Constructs a new DriverNotFoundException with the specified message and cause.
   *
   * @param message the detail message
   * @param cause the cause of the exception
   */
  public DriverNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}
