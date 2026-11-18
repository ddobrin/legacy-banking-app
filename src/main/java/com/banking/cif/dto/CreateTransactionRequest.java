package com.banking.cif.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Request payload for posting a financial transaction.
 *
 * @param accountId source account ID
 * @param transactionType operation type ({@code DEPOSIT}, {@code WITHDRAWAL}, {@code TRANSFER})
 * @param amount strictly positive monetary amount
 * @param description optional transaction description
 * @param referenceCode optional reference code
 * @param targetAccountId destination account ID required for {@code TRANSFER} operations
 */
public record CreateTransactionRequest(
    @NotNull(message = "Account ID is required") Integer accountId,
    @NotBlank(message = "Transaction type is required") @Size(max = 20) String transactionType,
    @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
        BigDecimal amount,
    @Size(max = 1000) String description,
    @Size(max = 50) String referenceCode,
    Integer targetAccountId) {}
