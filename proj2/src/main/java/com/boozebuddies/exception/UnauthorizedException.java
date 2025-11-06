package com.boozebuddies.exception;

/**
 * Exception thrown when user lacks required authorization (e.g., missing role, insufficient
 * permissions). Results in HTTP 403 Forbidden.
 */
public class UnauthorizedException extends RuntimeException {

  /**
   * Constructs a new UnauthorizedException with the specified message.
   *
   * @param message the detail message
   */
  public UnauthorizedException(String message) {
    super(message);
  }

  /**
   * Constructs a new UnauthorizedException with the specified message and cause.
   *
   * @param message the detail message
   * @param cause the cause of the exception
   */
  public UnauthorizedException(String message, Throwable cause) {
    super(message, cause);
  }
}
