package com.banking.cif.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Request payload for opening a new customer account.
 *
 * @param customerId owning customer ID
 * @param productCode banking product code (e.g. {@code CHK-STD}, {@code SAV-HYS})
 * @param balance optional non-negative opening deposit balance
 * @param overdraftLimit optional non-negative overdraft limit
 * @param iban optional IBAN
 */
public record CreateAccountRequest(
    @NotNull(message = "Customer ID is required") Integer customerId,
    @NotBlank(message = "Product code is required") @Size(max = 50) String productCode,
    @DecimalMin(value = "0.00", message = "Opening balance cannot be negative") BigDecimal balance,
    @DecimalMin(value = "0.00", message = "Overdraft limit cannot be negative")
        BigDecimal overdraftLimit,
    @Size(max = 34) String iban) {}
