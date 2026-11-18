package com.banking.cif.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Request payload for creating a new customer.
 *
 * @param firstName customer first name
 * @param lastName customer last name
 * @param email customer email address
 * @param dateOfBirth date of birth
 * @param cifNumber optional CIF number (auto-generated when omitted)
 * @param phoneNumber optional phone number
 * @param addressLine1 optional street address
 * @param city optional city
 * @param state optional state or province
 * @param postalCode optional postal code
 * @param countryCode optional 2-letter country code
 * @param riskRating optional risk rating
 */
public record CreateCustomerRequest(
    @NotBlank(message = "First name is required") @Size(max = 100) String firstName,
    @NotBlank(message = "Last name is required") @Size(max = 100) String lastName,
    @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 150)
        String email,
    @NotNull(message = "Date of birth is required") LocalDate dateOfBirth,
    @Size(max = 20) String cifNumber,
    @Size(max = 20) String phoneNumber,
    @Size(max = 255) String addressLine1,
    @Size(max = 100) String city,
    @Size(max = 100) String state,
    @Size(max = 20) String postalCode,
    @Size(max = 2) String countryCode,
    @Size(max = 10) String riskRating) {}
