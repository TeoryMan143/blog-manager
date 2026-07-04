package com.teoryman.blogmanager.common.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.teoryman.blogmanager.common.response.ApiResponse;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ApiResponse<?>> handleResourceNotFound(ResourceNotFoundException e) {
    ApiResponse<?> response = new ApiResponse<>(null, e.getMessage(), LocalDateTime.now());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
  }

  @ExceptionHandler(ForbiddenException.class)
  public ResponseEntity<ApiResponse<?>> handleForbidden(ForbiddenException e) {
    ApiResponse<?> response = new ApiResponse<>(null, e.getMessage(), LocalDateTime.now());
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
  }

  @ExceptionHandler(DuplicateUserException.class)
  public ResponseEntity<ApiResponse<?>> handleDuplicateUser(DuplicateUserException e) {
    ApiResponse<?> response = new ApiResponse<>(null, e.getMessage(), LocalDateTime.now());
    return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
  }

  @ExceptionHandler(InvalidTokenException.class)
  public ResponseEntity<ApiResponse<?>> handleInvalidToken(InvalidTokenException e) {
    ApiResponse<?> response = new ApiResponse<>(null, e.getMessage(), LocalDateTime.now());
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
  }

  @ExceptionHandler(ExpiredTokenException.class)
  public ResponseEntity<ApiResponse<?>> handleExpiredToken(ExpiredTokenException e) {
    ApiResponse<?> response = new ApiResponse<>(null, e.getMessage(), LocalDateTime.now());
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ApiResponse<?>> handleAuthentication(AuthenticationException e) {
    ApiResponse<?> response = new ApiResponse<>(null, "Invalid username or password", LocalDateTime.now());
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
  }

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<ApiResponse<?>> handleApiException(ApiException e) {
    ApiResponse<?> response = new ApiResponse<>(null, e.getMessage(), LocalDateTime.now());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<ApiResponse<?>> handleIllegalState(IllegalStateException e) {
    ApiResponse<?> response = new ApiResponse<>(null, e.getMessage(), LocalDateTime.now());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ApiResponse<?>> handleDataIntegrity(DataIntegrityViolationException e) {
    ApiResponse<?> response = new ApiResponse<>(null, "Duplicate or invalid data", LocalDateTime.now());
    return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiResponse<?>> handleMalformedJson(HttpMessageNotReadableException e) {
    ApiResponse<?> response = new ApiResponse<>(null, "Malformed JSON request", LocalDateTime.now());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ApiResponse<?>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
    String message = "Invalid value for parameter '" + e.getName() + "'. Must be a valid UUID.";
    ApiResponse<?> response = new ApiResponse<>(null, message, LocalDateTime.now());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<?>> handleValidationErrors(MethodArgumentNotValidException e) {
    String errors = e.getBindingResult().getFieldErrors().stream()
            .map(err -> err.getField() + ": " + err.getDefaultMessage())
            .collect(Collectors.joining(", "));
    ApiResponse<?> response = new ApiResponse<>(null, errors, LocalDateTime.now());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }
}
