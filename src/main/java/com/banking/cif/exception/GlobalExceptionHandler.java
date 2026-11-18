package com.banking.cif.exception;

import com.banking.cif.dto.ApiErrorResponse;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/** Centralized REST exception handler producing standardized {@link ApiErrorResponse} payloads. */
@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * Handles missing resource errors with HTTP 404.
   *
   * @param ex exception instance
   * @return 404 response entity
   */
  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException ex) {
    return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
  }

  /**
   * Handles business rule violations with HTTP 400.
   *
   * @param ex exception instance
   * @return 400 response entity
   */
  @ExceptionHandler({BusinessValidationException.class, IllegalArgumentException.class})
  public ResponseEntity<ApiErrorResponse> handleBusinessValidation(RuntimeException ex) {
    return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
  }

  /**
   * Handles Jakarta Bean Validation failures with HTTP 400.
   *
   * @param ex validation exception
   * @return 400 response entity
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex) {
    String details =
        ex.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining("; "));
    String message = details.isBlank() ? "Validation failed" : details;
    return buildResponse(HttpStatus.BAD_REQUEST, message);
  }

  /**
   * Handles malformed JSON payloads or path/query parameter type mismatches with HTTP 400.
   *
   * @param ex exception instance
   * @return 400 response entity
   */
  @ExceptionHandler({
    HttpMessageNotReadableException.class,
    MethodArgumentTypeMismatchException.class
  })
  public ResponseEntity<ApiErrorResponse> handleMalformedRequest(Exception ex) {
    return buildResponse(HttpStatus.BAD_REQUEST, "Invalid request payload or parameter");
  }

  /**
   * Handles unexpected server errors with HTTP 500.
   *
   * @param ex exception instance
   * @return 500 response entity
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiErrorResponse> handleGenericException(Exception ex) {
    return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
  }

  private ResponseEntity<ApiErrorResponse> buildResponse(HttpStatus status, String message) {
    ApiErrorResponse body =
        new ApiErrorResponse(
            status.value(), status.getReasonPhrase(), message, LocalDateTime.now());
    return ResponseEntity.status(status).body(body);
  }
}
