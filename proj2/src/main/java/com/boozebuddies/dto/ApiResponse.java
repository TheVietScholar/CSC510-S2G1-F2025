package com.boozebuddies.dto;

import java.util.List;
import lombok.*;

@Data
@NoArgsConstructor
@Builder
public class ApiResponse<T> {
  private boolean success;
  private String message;
  private T data;
  private List<String> errors;

  // Manual constructor for the static factory methods
  public ApiResponse(boolean success, String message, T data) {
    this.success = success;
    this.message = message;
    this.data = data;
  }

  public ApiResponse(boolean success, String message) {
    this.success = success;
    this.message = message;
  }

  public ApiResponse(boolean success, String message, T data, List<String> errors) {
    this.success = success;
    this.message = message;
    this.data = data;
    this.errors = errors;
  }

  // Static factory methods
  public static <T> ApiResponse<T> success(T data, String message) {
    return new ApiResponse<>(true, message, data);
  }

  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>(true, "Operation successful", data);
  }

  public static <T> ApiResponse<T> error(String message) {
    return new ApiResponse<>(false, message);
  }

  public static <T> ApiResponse<T> error(String message, List<String> errors) {
    return new ApiResponse<>(false, message, null, errors);
  }
}
