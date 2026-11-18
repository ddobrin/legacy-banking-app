package com.banking.cif.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload for updating an account's status.
 *
 * @param status new account status ({@code ACTIVE}, {@code FROZEN}, {@code CLOSED})
 */
public record UpdateAccountStatusRequest(
    @NotBlank(message = "Status is required") @Size(max = 20) String status) {}
