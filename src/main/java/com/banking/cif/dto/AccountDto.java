package com.banking.cif.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response representation of a customer bank account.
 *
 * @param accountId account primary key
 * @param customerId owning customer ID
 * @param productCode product code
 * @param accountNumber unique 8-digit account number
 * @param iban optional IBAN
 * @param balance current account balance
 * @param overdraftLimit configured overdraft limit
 * @param status account status ({@code ACTIVE}, {@code FROZEN}, {@code CLOSED})
 * @param openedAt timestamp when opened
 * @param closedAt timestamp when closed, if applicable
 * @param configurations JSON configuration string
 */
public record AccountDto(
    Integer accountId,
    Integer customerId,
    String productCode,
    String accountNumber,
    String iban,
    BigDecimal balance,
    BigDecimal overdraftLimit,
    String status,
    LocalDateTime openedAt,
    LocalDateTime closedAt,
    String configurations) {}
