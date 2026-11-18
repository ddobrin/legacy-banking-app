package com.banking.cif.dto;

import java.time.LocalDateTime;

/**
 * Standardized REST API error payload.
 *
 * @param status HTTP status code
 * @param error HTTP reason phrase
 * @param message detailed error message
 * @param timestamp time the error occurred
 */
public record ApiErrorResponse(
    int status, String error, String message, LocalDateTime timestamp) {}
