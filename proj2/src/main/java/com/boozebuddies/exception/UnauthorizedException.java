package com.boozebuddies.exception;

/**
 * Exception thrown when user lacks required authorization (e.g., missing role, insufficient permissions).
 * Results in HTTP 403 Forbidden.
 */
public class UnauthorizedException extends RuntimeException {

  public UnauthorizedException(String message) {
    super(message);
  }

  public UnauthorizedException(String message, Throwable cause) {
    super(message, cause);
  }
}
