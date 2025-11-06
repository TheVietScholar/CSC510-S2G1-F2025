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

  /**
   * Handles UserAlreadyExistsException.
   *
   * @param ex the exception
   * @param request the web request
   * @return error response with HTTP 409 CONFLICT
   */
  @ExceptionHandler(UserAlreadyExistsException.class)
  public ResponseEntity<Object> handleUserAlreadyExists(
      UserAlreadyExistsException ex, WebRequest request) {
    return buildErrorResponse(ex.getMessage(), HttpStatus.CONFLICT, request);
  }

  /**
   * Handles UserNotFoundException.
   *
   * @param ex the exception
   * @param request the web request
   * @return error response with HTTP 404 NOT FOUND
   */
  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<Object> handleUserNotFound(UserNotFoundException ex, WebRequest request) {
    return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND, request);
  }

  /**
   * Handles InvalidCredentialsException.
   *
   * @param ex the exception
   * @param request the web request
   * @return error response with HTTP 401 UNAUTHORIZED
   */
  @ExceptionHandler(InvalidCredentialsException.class)
  public ResponseEntity<Object> handleInvalidCredentials(
      InvalidCredentialsException ex, WebRequest request) {
    return buildErrorResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED, request);
  }

  /**
   * Handles InvalidTokenException.
   *
   * @param ex the exception
   * @param request the web request
   * @return error response with HTTP 401 UNAUTHORIZED
   */
  @ExceptionHandler(InvalidTokenException.class)
  public ResponseEntity<Object> handleInvalidToken(InvalidTokenException ex, WebRequest request) {
    return buildErrorResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED, request);
  }

  // ==================== NEW HANDLERS FOR ROLE MANAGEMENT ====================

  /**
   * Handles ValidationException.
   *
   * @param ex the exception
   * @param request the web request
   * @return error response with HTTP 400 BAD REQUEST
   */
  @ExceptionHandler(ValidationException.class)
  public ResponseEntity<Object> handleValidation(ValidationException ex, WebRequest request) {
    return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST, request);
  }

  /**
   * Handles UnauthorizedException.
   *
   * @param ex the exception
   * @param request the web request
   * @return error response with HTTP 403 FORBIDDEN
   */
  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<Object> handleUnauthorized(UnauthorizedException ex, WebRequest request) {
    return buildErrorResponse(ex.getMessage(), HttpStatus.FORBIDDEN, request);
  }

  /**
   * Handles AccessDeniedException.
   *
   * @param ex the exception
   * @param request the web request
   * @return error response with HTTP 403 FORBIDDEN
   */
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
    return buildErrorResponse("Access denied: " + ex.getMessage(), HttpStatus.FORBIDDEN, request);
  }

  // ==================== GENERIC HANDLERS ====================

  /**
   * Handles IllegalArgumentException.
   *
   * @param ex the exception
   * @param request the web request
   * @return error response with HTTP 400 BAD REQUEST
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Object> handleIllegalArgument(
      IllegalArgumentException ex, WebRequest request) {
    return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST, request);
  }

  /**
   * Handles HttpMessageNotReadableException.
   *
   * @param ex the exception
   * @param request the web request
   * @return error response with HTTP 400 BAD REQUEST
   */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<Object> handleHttpMessageNotReadable(
      HttpMessageNotReadableException ex, WebRequest request) {
    return buildErrorResponse("Invalid or missing request body", HttpStatus.BAD_REQUEST, request);
  }

  /**
   * Handles HttpMediaTypeNotSupportedException.
   *
   * @param ex the exception
   * @param request the web request
   * @return error response with HTTP 400 BAD REQUEST
   */
  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ResponseEntity<Object> handleMediaTypeNotSupported(
      HttpMediaTypeNotSupportedException ex, WebRequest request) {
    return buildErrorResponse("Content type not supported", HttpStatus.BAD_REQUEST, request);
  }

  /**
   * Handles all other exceptions not specifically handled.
   *
   * @param ex the exception
   * @param request the web request
   * @return error response with HTTP 500 INTERNAL SERVER ERROR
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Object> handleGlobalException(Exception ex, WebRequest request) {
    return buildErrorResponse(
        "An unexpected error occurred: " + ex.getMessage(),
        HttpStatus.INTERNAL_SERVER_ERROR,
        request);
  }

  /**
   * Builds a standardized error response.
   *
   * @param message the error message
   * @param status the HTTP status
   * @param request the web request
   * @return the error response entity
   */
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
