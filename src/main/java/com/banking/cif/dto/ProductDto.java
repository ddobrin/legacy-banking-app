package com.banking.cif.dto;

import java.math.BigDecimal;

/**
 * Response representation of a banking product.
 *
 * @param productCode unique product code
 * @param name display name
 * @param category product category
 * @param interestRate nominal interest rate
 * @param active whether the product is active
 */
public record ProductDto(
    String productCode,
    String name,
    String category,
    BigDecimal interestRate,
    boolean active) {}
