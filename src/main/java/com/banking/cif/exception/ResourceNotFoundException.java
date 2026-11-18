package com.banking.cif.exception;

/** Exception thrown when a requested banking resource does not exist. */
public class ResourceNotFoundException extends RuntimeException {

  /**
   * Creates a new {@link ResourceNotFoundException}.
   *
   * @param message descriptive error message
   */
  public ResourceNotFoundException(String message) {
    super(message);
  }
}
