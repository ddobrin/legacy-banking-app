package com.banking.cif.service;

import com.banking.cif.dto.AccountDto;
import com.banking.cif.dto.CreateAccountRequest;
import com.banking.cif.dto.UpdateAccountStatusRequest;
import com.banking.cif.entity.AccountEntity;
import com.banking.cif.entity.ProductEntity;
import com.banking.cif.exception.BusinessValidationException;
import com.banking.cif.exception.ResourceNotFoundException;
import com.banking.cif.repository.AccountRepository;
import com.banking.cif.repository.CustomerRepository;
import com.banking.cif.repository.ProductRepository;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service layer managing bank accounts and account lifecycle state. */
@Service
public class AccountService {

  private static final int ACCOUNT_NUMBER_BASE = 11_111_111;
  private static final int ACCOUNT_NUMBER_BOUND = 88_888_889;
  private static final Set<String> VALID_STATUSES = Set.of("ACTIVE", "FROZEN", "CLOSED");

  private final AccountRepository accountRepository;
  private final CustomerRepository customerRepository;
  private final ProductRepository productRepository;
  private final SecureRandom secureRandom = new SecureRandom();

  /**
   * Creates a new {@link AccountService}.
   *
   * @param accountRepository account repository
   * @param customerRepository customer repository
   * @param productRepository product repository
   */
  public AccountService(
      AccountRepository accountRepository,
      CustomerRepository customerRepository,
      ProductRepository productRepository) {
    this.accountRepository = accountRepository;
    this.customerRepository = customerRepository;
    this.productRepository = productRepository;
  }

  /**
   * Lists accounts, optionally filtered by customer ID.
   *
   * @param customerId optional customer ID filter
   * @return list of accounts
   */
  @Transactional(readOnly = true)
  public List<AccountDto> getAccounts(Integer customerId) {
    List<AccountEntity> entities =
        customerId != null
            ? accountRepository.findByCustomerIdOrderByAccountIdAsc(customerId)
            : accountRepository.findAll();
    return entities.stream().map(DtoMapper::toAccountDto).toList();
  }

  /**
   * Retrieves an account by string ID, matching legacy validation behavior.
   *
   * @param rawId account ID string
   * @return account details
   */
  @Transactional(readOnly = true)
  public AccountDto getAccountById(String rawId) {
    String cleanId = stripOptionalJsonExtension(rawId);
    if (cleanId.isEmpty()) {
      throw new BusinessValidationException("Account ID is missing");
    }

    int accountId;
    try {
      accountId = Integer.parseInt(cleanId);
    } catch (NumberFormatException ex) {
      throw new BusinessValidationException("Invalid Account ID format: " + cleanId);
    }

    AccountEntity account =
        accountRepository
            .findById(accountId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Account not found for ID: " + cleanId));
    return DtoMapper.toAccountDto(account);
  }

  /**
   * Opens a new bank account for an existing customer and valid product.
   *
   * @param request account creation request
   * @return created account details
   */
  @Transactional
  public AccountDto createAccount(CreateAccountRequest request) {
    ProductEntity product =
        productRepository
            .findById(request.productCode().trim())
            .orElseThrow(() -> new BusinessValidationException("Invalid Product Code"));
    if (!Boolean.TRUE.equals(product.getActive())) {
      throw new BusinessValidationException("Product is not active");
    }

    if (!customerRepository.existsById(request.customerId())) {
      throw new BusinessValidationException("Customer not found");
    }

    BigDecimal initialBalance =
        request.balance() != null ? request.balance() : BigDecimal.ZERO;
    if (initialBalance.compareTo(BigDecimal.ZERO) < 0) {
      throw new BusinessValidationException("Opening balance cannot be negative");
    }

    BigDecimal overdraftLimit =
        request.overdraftLimit() != null ? request.overdraftLimit() : BigDecimal.ZERO;

    AccountEntity entity = new AccountEntity();
    entity.setCustomerId(request.customerId());
    entity.setProductCode(product.getProductCode());
    entity.setAccountNumber(generateUniqueAccountNumber());
    entity.setIban(
        request.iban() != null && !request.iban().isBlank() ? request.iban().trim() : null);
    entity.setBalance(initialBalance);
    entity.setOverdraftLimit(overdraftLimit);
    entity.setStatus("ACTIVE");

    AccountEntity saved = accountRepository.save(entity);
    return DtoMapper.toAccountDto(saved);
  }

  /**
   * Updates an account's operational status.
   *
   * @param accountId account primary key
   * @param request status update payload
   * @return updated account details
   */
  @Transactional
  public AccountDto updateAccountStatus(Integer accountId, UpdateAccountStatusRequest request) {
    AccountEntity entity =
        accountRepository
            .findById(accountId)
            .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

    String normalizedStatus = request.status().trim().toUpperCase();
    if (!VALID_STATUSES.contains(normalizedStatus)) {
      throw new BusinessValidationException("Invalid account status: " + request.status());
    }

    entity.setStatus(normalizedStatus);
    if ("CLOSED".equals(normalizedStatus)) {
      entity.setClosedAt(LocalDateTime.now());
    } else if ("ACTIVE".equals(normalizedStatus)) {
      entity.setClosedAt(null);
    }

    AccountEntity saved = accountRepository.save(entity);
    return DtoMapper.toAccountDto(saved);
  }

  private Integer generateUniqueAccountNumber() {
    int candidate;
    do {
      candidate = ACCOUNT_NUMBER_BASE + secureRandom.nextInt(ACCOUNT_NUMBER_BOUND);
    } while (accountRepository.existsByAccountNumber(candidate));
    return candidate;
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
}
