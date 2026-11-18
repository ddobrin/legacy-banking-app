package com.banking.cif.exception;

/** Exception thrown when a request violates banking business rules or input constraints. */
public class BusinessValidationException extends RuntimeException {

  /**
   * Creates a new {@link BusinessValidationException}.
   *
   * @param message descriptive error message
   */
  public BusinessValidationException(String message) {
    super(message);
  }
}
