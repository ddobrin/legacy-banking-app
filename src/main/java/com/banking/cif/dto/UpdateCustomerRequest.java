package com.banking.cif.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Request payload for updating an existing customer profile.
 *
 * @param firstName updated first name
 * @param lastName updated last name
 * @param email updated email address
 * @param dateOfBirth updated date of birth
 * @param phoneNumber updated phone number
 * @param addressLine1 updated street address
 * @param city updated city
 * @param state updated state or province
 * @param postalCode updated postal code
 * @param countryCode updated 2-letter country code
 * @param kycStatus updated KYC status
 * @param riskRating updated risk rating
 */
public record UpdateCustomerRequest(
    @Size(max = 100) String firstName,
    @Size(max = 100) String lastName,
    @Email(message = "Email must be valid") @Size(max = 150) String email,
    LocalDate dateOfBirth,
    @Size(max = 20) String phoneNumber,
    @Size(max = 255) String addressLine1,
    @Size(max = 100) String city,
    @Size(max = 100) String state,
    @Size(max = 20) String postalCode,
    @Size(max = 2) String countryCode,
    @Size(max = 20) String kycStatus,
    @Size(max = 10) String riskRating) {}
