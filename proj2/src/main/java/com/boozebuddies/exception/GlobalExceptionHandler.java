package com.boozebuddies.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

/**
 * Global exception handler for the application. Catches exceptions and returns appropriate HTTP
 * responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(UserAlreadyExistsException.class)
  public ResponseEntity<Object> handleUserAlreadyExists(
      UserAlreadyExistsException ex, WebRequest request) {
    return buildErrorResponse(ex.getMessage(), HttpStatus.CONFLICT, request);
  }

  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<Object> handleUserNotFound(UserNotFoundException ex, WebRequest request) {
    return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND, request);
  }

  @ExceptionHandler(InvalidCredentialsException.class)
  public ResponseEntity<Object> handleInvalidCredentials(
      InvalidCredentialsException ex, WebRequest request) {
    return buildErrorResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED, request);
  }

  @ExceptionHandler(InvalidTokenException.class)
  public ResponseEntity<Object> handleInvalidToken(InvalidTokenException ex, WebRequest request) {
    return buildErrorResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED, request);
  }

  // ==================== NEW HANDLERS FOR ROLE MANAGEMENT ====================

  @ExceptionHandler(ValidationException.class)
  public ResponseEntity<Object> handleValidation(ValidationException ex, WebRequest request) {
    return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST, request);
  }

  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<Object> handleUnauthorized(UnauthorizedException ex, WebRequest request) {
    return buildErrorResponse(ex.getMessage(), HttpStatus.FORBIDDEN, request);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
    return buildErrorResponse(
        "Access denied: " + ex.getMessage(), HttpStatus.FORBIDDEN, request);
  }

  // ==================== GENERIC HANDLERS ====================

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Object> handleIllegalArgument(
      IllegalArgumentException ex, WebRequest request) {
    return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST, request);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<Object> handleHttpMessageNotReadable(
      HttpMessageNotReadableException ex, WebRequest request) {
    return buildErrorResponse("Invalid or missing request body", HttpStatus.BAD_REQUEST, request);
  }

  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ResponseEntity<Object> handleMediaTypeNotSupported(
      HttpMediaTypeNotSupportedException ex, WebRequest request) {
    return buildErrorResponse("Content type not supported", HttpStatus.BAD_REQUEST, request);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Object> handleGlobalException(Exception ex, WebRequest request) {
    return buildErrorResponse(
        "An unexpected error occurred: " + ex.getMessage(),
        HttpStatus.INTERNAL_SERVER_ERROR,
        request);
  }

  /** Builds a standardized error response. */
  private ResponseEntity<Object> buildErrorResponse(
      String message, HttpStatus status, WebRequest request) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("timestamp", LocalDateTime.now());
    body.put("status", status.value());
    body.put("error", status.getReasonPhrase());
    body.put("message", message);
    body.put("path", request.getDescription(false).replace("uri=", ""));

    return new ResponseEntity<>(body, status);
  }
}