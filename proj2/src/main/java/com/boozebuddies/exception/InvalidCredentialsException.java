package com.boozebuddies.exception;

/** Exception thrown when authentication fails due to invalid credentials. */
public class InvalidCredentialsException extends RuntimeException {

  public InvalidCredentialsException(String message) {
    super(message);
  }

  public InvalidCredentialsException(String message, Throwable cause) {
    super(message, cause);
  }
}
