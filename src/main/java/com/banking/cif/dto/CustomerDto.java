package com.banking.cif.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response representation of a banking customer.
 *
 * @param customerId customer primary key
 * @param cifNumber unique CIF identifier
 * @param firstName first name
 * @param lastName last name
 * @param dateOfBirth date of birth
 * @param email unique email address
 * @param phoneNumber phone number
 * @param addressLine1 street address line 1
 * @param city city
 * @param state state or province
 * @param postalCode postal code
 * @param countryCode ISO-3166 alpha-2 country code
 * @param kycStatus KYC verification status
 * @param kycDocuments KYC documents JSON metadata
 * @param riskRating customer risk rating
 * @param createdAt creation timestamp
 * @param updatedAt last modification timestamp
 * @param accounts associated accounts when loaded for detail view
 * @param accountCount total account count when loaded for summary view
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CustomerDto(
    Integer customerId,
    String cifNumber,
    String firstName,
    String lastName,
    LocalDate dateOfBirth,
    String email,
    String phoneNumber,
    String addressLine1,
    String city,
    String state,
    String postalCode,
    String countryCode,
    String kycStatus,
    String kycDocuments,
    String riskRating,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    List<AccountDto> accounts,
    Integer accountCount) {}
