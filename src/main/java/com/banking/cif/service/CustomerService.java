package com.banking.cif.service;

import com.banking.cif.dto.AccountDto;
import com.banking.cif.dto.CreateCustomerRequest;
import com.banking.cif.dto.CustomerDto;
import com.banking.cif.dto.UpdateCustomerRequest;
import com.banking.cif.entity.CustomerEntity;
import com.banking.cif.exception.BusinessValidationException;
import com.banking.cif.exception.ResourceNotFoundException;
import com.banking.cif.repository.AccountRepository;
import com.banking.cif.repository.CustomerRepository;
import java.security.SecureRandom;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service layer managing customer CIF profiles and lookups. */
@Service
public class CustomerService {

  private static final int CIF_RANDOM_BOUND = 900_000;
  private static final int CIF_RANDOM_BASE = 100_000;

  private final CustomerRepository customerRepository;
  private final AccountRepository accountRepository;
  private final SecureRandom secureRandom = new SecureRandom();

  /**
   * Creates a new {@link CustomerService}.
   *
   * @param customerRepository customer repository
   * @param accountRepository account repository
   */
  public CustomerService(
      CustomerRepository customerRepository, AccountRepository accountRepository) {
    this.customerRepository = customerRepository;
    this.accountRepository = accountRepository;
  }

  /**
   * Retrieves all customers ordered by last and first name, including account counts.
   *
   * @return list of customer summaries
   */
  @Transactional(readOnly = true)
  public List<CustomerDto> getAllCustomers() {
    return customerRepository.findAllWithAccountCount().stream()
        .map(
            row -> {
              CustomerEntity customer = (CustomerEntity) row[0];
              int accountCount = ((Number) row[1]).intValue();
              return DtoMapper.toCustomerDto(customer, null, accountCount);
            })
        .toList();
  }

  /**
   * Searches customers by first or last name fragment.
   *
   * @param name search query string
   * @return matching customers with account counts
   */
  @Transactional(readOnly = true)
  public List<CustomerDto> searchCustomersByName(String name) {
    if (name == null || name.isBlank()) {
      return getAllCustomers();
    }
    String trimmed = name.trim();
    return customerRepository.searchByName(trimmed).stream()
        .map(
            customer -> {
              int count = (int) accountRepository.countByCustomerId(customer.getCustomerId());
              return DtoMapper.toCustomerDto(customer, null, count);
            })
        .toList();
  }

  /**
   * Retrieves a customer profile with associated accounts by numeric ID or name lookup.
   *
   * @param idOrName numeric customer ID or name search string
   * @return customer details with accounts
   */
  @Transactional(readOnly = true)
  public CustomerDto getCustomerByIdOrName(String idOrName) {
    String cleanId = stripOptionalJsonExtension(idOrName);
    if (cleanId.isEmpty()) {
      throw new BusinessValidationException("Customer ID is missing");
    }

    CustomerEntity customer;
    try {
      Integer customerId = Integer.parseInt(cleanId);
      customer =
          customerRepository
              .findById(customerId)
              .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
    } catch (NumberFormatException ex) {
      List<CustomerEntity> matches = customerRepository.searchByName(cleanId);
      if (matches.isEmpty()) {
        throw new ResourceNotFoundException("Customer not found by ID or Name: " + cleanId);
      }
      customer = matches.get(0);
    }

    List<AccountDto> accounts =
        accountRepository.findByCustomerIdOrderByAccountIdAsc(customer.getCustomerId()).stream()
            .map(DtoMapper::toAccountDto)
            .toList();
    return DtoMapper.toCustomerDto(customer, accounts, accounts.size());
  }

  /**
   * Creates and persists a new customer CIF record.
   *
   * @param request customer creation request
   * @return created customer details
   */
  @Transactional
  public CustomerDto createCustomer(CreateCustomerRequest request) {
    String normalizedEmail = request.email().trim();
    if (customerRepository.existsByEmailIgnoreCase(normalizedEmail)) {
      throw new BusinessValidationException("Email already exists");
    }

    String cifNumber = resolveUniqueCifNumber(request.cifNumber());

    CustomerEntity entity = new CustomerEntity();
    entity.setCifNumber(cifNumber);
    entity.setFirstName(request.firstName().trim());
    entity.setLastName(request.lastName().trim());
    entity.setDateOfBirth(request.dateOfBirth());
    entity.setEmail(normalizedEmail);
    entity.setPhoneNumber(trimToNull(request.phoneNumber()));
    entity.setAddressLine1(trimToNull(request.addressLine1()));
    entity.setCity(trimToNull(request.city()));
    entity.setState(trimToNull(request.state()));
    entity.setPostalCode(trimToNull(request.postalCode()));
    entity.setCountryCode(trimToNull(request.countryCode()));
    entity.setKycStatus("PENDING");
    entity.setRiskRating(
        request.riskRating() != null && !request.riskRating().isBlank()
            ? request.riskRating().trim()
            : "LOW");

    CustomerEntity saved = customerRepository.save(entity);
    return DtoMapper.toCustomerDto(saved, List.of(), 0);
  }

  /**
   * Updates an existing customer profile.
   *
   * @param customerId customer primary key
   * @param request fields to update
   * @return updated customer details
   */
  @Transactional
  public CustomerDto updateCustomer(Integer customerId, UpdateCustomerRequest request) {
    CustomerEntity entity =
        customerRepository
            .findById(customerId)
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

    if (request.email() != null && !request.email().isBlank()) {
      String updatedEmail = request.email().trim();
      if (!updatedEmail.equalsIgnoreCase(entity.getEmail())
          && customerRepository.existsByEmailIgnoreCase(updatedEmail)) {
        throw new BusinessValidationException("Email already exists");
      }
      entity.setEmail(updatedEmail);
    }
    if (request.firstName() != null && !request.firstName().isBlank()) {
      entity.setFirstName(request.firstName().trim());
    }
    if (request.lastName() != null && !request.lastName().isBlank()) {
      entity.setLastName(request.lastName().trim());
    }
    if (request.dateOfBirth() != null) {
      entity.setDateOfBirth(request.dateOfBirth());
    }
    if (request.phoneNumber() != null) {
      entity.setPhoneNumber(trimToNull(request.phoneNumber()));
    }
    if (request.addressLine1() != null) {
      entity.setAddressLine1(trimToNull(request.addressLine1()));
    }
    if (request.city() != null) {
      entity.setCity(trimToNull(request.city()));
    }
    if (request.state() != null) {
      entity.setState(trimToNull(request.state()));
    }
    if (request.postalCode() != null) {
      entity.setPostalCode(trimToNull(request.postalCode()));
    }
    if (request.countryCode() != null) {
      entity.setCountryCode(trimToNull(request.countryCode()));
    }
    if (request.kycStatus() != null && !request.kycStatus().isBlank()) {
      entity.setKycStatus(request.kycStatus().trim().toUpperCase());
    }
    if (request.riskRating() != null && !request.riskRating().isBlank()) {
      entity.setRiskRating(request.riskRating().trim().toUpperCase());
    }

    CustomerEntity saved = customerRepository.save(entity);
    List<AccountDto> accounts =
        accountRepository.findByCustomerIdOrderByAccountIdAsc(customerId).stream()
            .map(DtoMapper::toAccountDto)
            .toList();
    return DtoMapper.toCustomerDto(saved, accounts, accounts.size());
  }

  /**
   * Deletes a customer record if no accounts are attached.
   *
   * @param customerId customer primary key
   */
  @Transactional
  public void deleteCustomer(Integer customerId) {
    CustomerEntity entity =
        customerRepository
            .findById(customerId)
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
    if (accountRepository.countByCustomerId(customerId) > 0) {
      throw new BusinessValidationException("Cannot delete customer with existing accounts");
    }
    customerRepository.delete(entity);
  }

  private String resolveUniqueCifNumber(String providedCif) {
    if (providedCif != null && !providedCif.isBlank()) {
      String trimmed = providedCif.trim();
      if (customerRepository.existsByCifNumber(trimmed)) {
        throw new BusinessValidationException("CIF number already exists");
      }
      return trimmed;
    }

    String generated;
    do {
      generated = "CIF-" + (CIF_RANDOM_BASE + secureRandom.nextInt(CIF_RANDOM_BOUND));
    } while (customerRepository.existsByCifNumber(generated));
    return generated;
  }

  private static String stripOptionalJsonExtension(String value) {
    if (value == null) {
      return "";
    }
    String clean = value.trim();
    if (clean.endsWith(".json")) {
      return clean.substring(0, clean.length() - 5);
    }
    return clean;
  }

  private static String trimToNull(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return value.trim();
  }
}
