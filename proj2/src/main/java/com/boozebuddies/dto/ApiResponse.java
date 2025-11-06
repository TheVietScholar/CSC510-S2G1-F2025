package com.boozebuddies.dto;

import java.util.List;
import lombok.*;

/**
 * Generic API response wrapper for consistent response format.
 *
 * @param <T> the type of data being returned
 */
@Data
@NoArgsConstructor
@Builder
public class ApiResponse<T> {
  private boolean success;
  private String message;
  private T data;
  private List<String> errors;

  /**
   * Constructor for creating an API response with data.
   *
   * @param success whether the operation was successful
   * @param message the response message
   * @param data the response data
   */
  public ApiResponse(boolean success, String message, T data) {
    this.success = success;
    this.message = message;
    this.data = data;
  }

  /**
   * Constructor for creating an API response without data.
   *
   * @param success whether the operation was successful
   * @param message the response message
   */
  public ApiResponse(boolean success, String message) {
    this.success = success;
    this.message = message;
  }

  /**
   * Constructor for creating an API response with data and errors.
   *
   * @param success whether the operation was successful
   * @param message the response message
   * @param data the response data
   * @param errors list of error messages
   */
  public ApiResponse(boolean success, String message, T data, List<String> errors) {
    this.success = success;
    this.message = message;
    this.data = data;
    this.errors = errors;
  }

  /**
   * Creates a success response with data and message.
   *
   * @param <T> the type of data
   * @param data the response data
   * @param message the success message
   * @return a success API response
   */
  public static <T> ApiResponse<T> success(T data, String message) {
    return new ApiResponse<>(true, message, data);
  }

  /**
   * Creates a success response with data and default message.
   *
   * @param <T> the type of data
   * @param data the response data
   * @return a success API response
   */
  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>(true, "Operation successful", data);
  }

  /**
   * Creates an error response with a message.
   *
   * @param <T> the type of data
   * @param message the error message
   * @return an error API response
   */
  public static <T> ApiResponse<T> error(String message) {
    return new ApiResponse<>(false, message);
  }

  /**
   * Creates an error response with a message and error list.
   *
   * @param <T> the type of data
   * @param message the error message
   * @param errors list of error messages
   * @return an error API response
   */
  public static <T> ApiResponse<T> error(String message, List<String> errors) {
    return new ApiResponse<>(false, message, null, errors);
  }
}
