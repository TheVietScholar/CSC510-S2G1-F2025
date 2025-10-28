package com.boozebuddies.exception;

/**
 * Exception thrown when a requested user cannot be found.
 */
public class UserNotFoundException extends RuntimeException {
  
  public UserNotFoundException(String message) {
    super(message);
  }
  
  public UserNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}