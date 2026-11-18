package com.banking.cif.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response representation of a financial transaction.
 *
 * @param transactionId transaction primary key
 * @param accountId account ID
 * @param referenceCode optional reference code
 * @param transactionType transaction type ({@code DEPOSIT}, {@code WITHDRAWAL}, {@code TRANSFER})
 * @param amount monetary amount
 * @param description transaction description
 * @param transactionDate timestamp of posting
 * @param balanceAfter resulting account balance after the transaction
 */
public record TransactionDto(
    Integer transactionId,
    Integer accountId,
    String referenceCode,
    String transactionType,
    BigDecimal amount,
    String description,
    LocalDateTime transactionDate,
    BigDecimal balanceAfter) {}
