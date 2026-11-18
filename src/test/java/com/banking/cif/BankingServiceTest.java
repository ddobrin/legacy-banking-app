package com.banking.cif;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.banking.cif.dto.AccountDto;
import com.banking.cif.dto.CreateAccountRequest;
import com.banking.cif.dto.CreateCustomerRequest;
import com.banking.cif.dto.CreateTransactionRequest;
import com.banking.cif.dto.CustomerDto;
import com.banking.cif.dto.ProductDto;
import com.banking.cif.dto.TransactionDto;
import com.banking.cif.dto.UpdateAccountStatusRequest;
import com.banking.cif.entity.AccountEntity;
import com.banking.cif.entity.CustomerEntity;
import com.banking.cif.entity.ProductEntity;
import com.banking.cif.entity.TransactionEntity;
import com.banking.cif.exception.BusinessValidationException;
import com.banking.cif.exception.ResourceNotFoundException;
import com.banking.cif.repository.AccountRepository;
import com.banking.cif.repository.CustomerRepository;
import com.banking.cif.repository.ProductRepository;
import com.banking.cif.repository.TransactionRepository;
import com.banking.cif.service.AccountService;
import com.banking.cif.service.CustomerService;
import com.banking.cif.service.ProductService;
import com.banking.cif.service.TransactionService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for the banking service layer covering customers, accounts, transactions, and
 * products.
 */
@ExtendWith(MockitoExtension.class)
class BankingServiceTest {

  @Mock private CustomerRepository customerRepository;
  @Mock private AccountRepository accountRepository;
  @Mock private ProductRepository productRepository;
  @Mock private TransactionRepository transactionRepository;

  private CustomerService customerService;
  private AccountService accountService;
  private TransactionService transactionService;
  private ProductService productService;

  @BeforeEach
  void setUp() {
    customerService = new CustomerService(customerRepository, accountRepository);
    accountService = new AccountService(accountRepository, customerRepository, productRepository);
    transactionService = new TransactionService(accountRepository, transactionRepository);
    productService = new ProductService(productRepository);
  }

  @Test
  void createCustomer_generatesCifAndDefaultsKycAndRisk() {
    CreateCustomerRequest request =
        new CreateCustomerRequest(
            "Alice",
            "Smith",
            "alice.service@example.com",
            LocalDate.of(1990, 1, 1),
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null);

    when(customerRepository.existsByEmailIgnoreCase("alice.service@example.com")).thenReturn(false);
    when(customerRepository.existsByCifNumber(anyString())).thenReturn(false);
    when(customerRepository.save(any(CustomerEntity.class)))
        .thenAnswer(
            invocation -> {
              CustomerEntity entity = invocation.getArgument(0);
              entity.setCustomerId(10);
              return entity;
            });

    CustomerDto created = customerService.createCustomer(request);

    assertThat(created.customerId()).isEqualTo(10);
    assertThat(created.firstName()).isEqualTo("Alice");
    assertThat(created.cifNumber()).startsWith("CIF-");
    assertThat(created.kycStatus()).isEqualTo("PENDING");
    assertThat(created.riskRating()).isEqualTo("LOW");
    assertThat(created.accountCount()).isZero();
  }

  @Test
  void createCustomer_duplicateEmail_throwsValidationException() {
    CreateCustomerRequest request =
        new CreateCustomerRequest(
            "Bob",
            "Jones",
            "bob@example.com",
            LocalDate.of(1990, 1, 1),
            "CIF-999001",
            null,
            null,
            null,
            null,
            null,
            null,
            null);
    when(customerRepository.existsByEmailIgnoreCase("bob@example.com")).thenReturn(true);

    assertThatThrownBy(() -> customerService.createCustomer(request))
        .isInstanceOf(BusinessValidationException.class)
        .hasMessage("Email already exists");
  }

  @Test
  void createCustomer_duplicateCifNumber_throwsValidationException() {
    CreateCustomerRequest request =
        new CreateCustomerRequest(
            "Bob",
            "Jones",
            "bob@example.com",
            LocalDate.of(1990, 1, 1),
            "CIF-100001",
            null,
            null,
            null,
            null,
            null,
            null,
            null);
    when(customerRepository.existsByEmailIgnoreCase("bob@example.com")).thenReturn(false);
    when(customerRepository.existsByCifNumber("CIF-100001")).thenReturn(true);

    assertThatThrownBy(() -> customerService.createCustomer(request))
        .isInstanceOf(BusinessValidationException.class)
        .hasMessage("CIF number already exists");
  }

  @Test
  void getCustomerByIdOrName_numericId_returnsCustomerWithAccounts() {
    CustomerEntity customer = buildCustomer(1, "CIF-100001", "John", "Smith");
    AccountEntity account = buildAccount(1, 1, "CHK-STD", new BigDecimal("1500.00"), "ACTIVE");
    when(customerRepository.findById(1)).thenReturn(Optional.of(customer));
    when(accountRepository.findByCustomerIdOrderByAccountIdAsc(1)).thenReturn(List.of(account));

    CustomerDto result = customerService.getCustomerByIdOrName("1.json");

    assertThat(result.customerId()).isEqualTo(1);
    assertThat(result.accounts()).hasSize(1);
    assertThat(result.accountCount()).isEqualTo(1);
  }

  @Test
  void getCustomerByIdOrName_nameFragment_returnsFirstMatch() {
    CustomerEntity customer = buildCustomer(2, "CIF-100002", "Jane", "Doe");
    when(customerRepository.searchByName("Jane")).thenReturn(List.of(customer));
    when(accountRepository.findByCustomerIdOrderByAccountIdAsc(2)).thenReturn(List.of());

    CustomerDto result = customerService.getCustomerByIdOrName("Jane");

    assertThat(result.customerId()).isEqualTo(2);
    assertThat(result.firstName()).isEqualTo("Jane");
  }

  @Test
  void getCustomerByIdOrName_notFound_throwsResourceNotFound() {
    when(customerRepository.findById(999)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> customerService.getCustomerByIdOrName("999"))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("Customer not found");
  }

  @Test
  void deleteCustomer_withExistingAccounts_throwsValidationException() {
    CustomerEntity customer = buildCustomer(1, "CIF-100001", "John", "Smith");
    when(customerRepository.findById(1)).thenReturn(Optional.of(customer));
    when(accountRepository.countByCustomerId(1)).thenReturn(2L);

    assertThatThrownBy(() -> customerService.deleteCustomer(1))
        .isInstanceOf(BusinessValidationException.class)
        .hasMessage("Cannot delete customer with existing accounts");
    verify(customerRepository, never()).delete(any());
  }

  @Test
  void createAccount_validCustomerAndProduct_defaultsActiveAndZeroBalance() {
    ProductEntity product = buildProduct("CHK-STD", "Standard Checking", true);
    when(productRepository.findById("CHK-STD")).thenReturn(Optional.of(product));
    when(customerRepository.existsById(1)).thenReturn(true);
    when(accountRepository.existsByAccountNumber(anyInt())).thenReturn(false);
    when(accountRepository.save(any(AccountEntity.class)))
        .thenAnswer(
            invocation -> {
              AccountEntity entity = invocation.getArgument(0);
              entity.setAccountId(12);
              return entity;
            });

    AccountDto created =
        accountService.createAccount(new CreateAccountRequest(1, "CHK-STD", null, null, null));

    assertThat(created.accountId()).isEqualTo(12);
    assertThat(created.status()).isEqualTo("ACTIVE");
    assertThat(created.balance()).isEqualByComparingTo(BigDecimal.ZERO);
  }

  @Test
  void createAccount_invalidProduct_throwsValidationException() {
    when(productRepository.findById("BAD-CODE")).thenReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                accountService.createAccount(
                    new CreateAccountRequest(1, "BAD-CODE", null, null, null)))
        .isInstanceOf(BusinessValidationException.class)
        .hasMessage("Invalid Product Code");
  }

  @Test
  void updateAccountStatus_closedSetsClosedAt_activeClearsClosedAt() {
    AccountEntity account = buildAccount(5, 2, "SAV-HYS", new BigDecimal("500.00"), "ACTIVE");
    when(accountRepository.findById(5)).thenReturn(Optional.of(account));
    when(accountRepository.save(any(AccountEntity.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    AccountDto closed =
        accountService.updateAccountStatus(5, new UpdateAccountStatusRequest("CLOSED"));
    assertThat(closed.status()).isEqualTo("CLOSED");
    assertThat(closed.closedAt()).isNotNull();

    AccountDto reopened =
        accountService.updateAccountStatus(5, new UpdateAccountStatusRequest("ACTIVE"));
    assertThat(reopened.status()).isEqualTo("ACTIVE");
    assertThat(reopened.closedAt()).isNull();
  }

  @Test
  void processTransaction_depositAndWithdrawal_updatesBalance() {
    AccountEntity account = buildAccount(1, 1, "CHK-STD", BigDecimal.ZERO, "ACTIVE");
    when(accountRepository.findByIdForUpdate(1)).thenReturn(Optional.of(account));
    when(accountRepository.save(any(AccountEntity.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(transactionRepository.save(any(TransactionEntity.class)))
        .thenAnswer(
            invocation -> {
              TransactionEntity txn = invocation.getArgument(0);
              txn.setTransactionId(100);
              txn.setTransactionDate(LocalDateTime.now());
              return txn;
            });

    TransactionDto deposit =
        transactionService.processTransaction(
            new CreateTransactionRequest(
                1, "DEPOSIT", new BigDecimal("100.00"), "Initial", null, null));
    assertThat(deposit.balanceAfter()).isEqualByComparingTo("100.00");

    TransactionDto withdrawal =
        transactionService.processTransaction(
            new CreateTransactionRequest(
                1, "WITHDRAW", new BigDecimal("40.00"), "ATM", null, null));
    assertThat(withdrawal.transactionType()).isEqualTo("WITHDRAWAL");
    assertThat(withdrawal.balanceAfter()).isEqualByComparingTo("60.00");
  }

  @Test
  void processTransaction_withdrawalInsufficientFunds_throwsValidationException() {
    AccountEntity account = buildAccount(1, 1, "CHK-STD", new BigDecimal("25.00"), "ACTIVE");
    when(accountRepository.findByIdForUpdate(1)).thenReturn(Optional.of(account));

    assertThatThrownBy(
            () ->
                transactionService.processTransaction(
                    new CreateTransactionRequest(
                        1, "WITHDRAWAL", new BigDecimal("100.00"), null, null, null)))
        .isInstanceOf(BusinessValidationException.class)
        .hasMessage("Insufficient funds");
    verify(transactionRepository, never()).save(any());
  }

  @Test
  void processTransaction_nonPositiveAmount_throwsValidationException() {
    assertThatThrownBy(
            () ->
                transactionService.processTransaction(
                    new CreateTransactionRequest(
                        1, "DEPOSIT", BigDecimal.ZERO, null, null, null)))
        .isInstanceOf(BusinessValidationException.class)
        .hasMessage("Amount must be greater than zero");
  }

  @Test
  void processTransaction_frozenAccount_throwsValidationException() {
    AccountEntity frozen = buildAccount(1, 1, "CHK-STD", new BigDecimal("100.00"), "FROZEN");
    when(accountRepository.findByIdForUpdate(1)).thenReturn(Optional.of(frozen));

    assertThatThrownBy(
            () ->
                transactionService.processTransaction(
                    new CreateTransactionRequest(
                        1, "DEPOSIT", new BigDecimal("10.00"), null, null, null)))
        .isInstanceOf(BusinessValidationException.class)
        .hasMessage("Account is not active");
  }

  @Test
  void processTransaction_transfer_debitsSourceAndCreditsTarget() {
    AccountEntity source = buildAccount(1, 1, "CHK-STD", new BigDecimal("200.00"), "ACTIVE");
    AccountEntity target = buildAccount(2, 1, "SAV-HYS", new BigDecimal("50.00"), "ACTIVE");
    when(accountRepository.findByIdForUpdate(1)).thenReturn(Optional.of(source));
    when(accountRepository.findByIdForUpdate(2)).thenReturn(Optional.of(target));
    when(accountRepository.save(any(AccountEntity.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(transactionRepository.save(any(TransactionEntity.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    TransactionDto result =
        transactionService.processTransaction(
            new CreateTransactionRequest(1, "TRANSFER", new BigDecimal("75.00"), null, "REF1", 2));

    assertThat(result.transactionType()).isEqualTo("TRANSFER");
    assertThat(result.balanceAfter()).isEqualByComparingTo("125.00");
    assertThat(target.getBalance()).isEqualByComparingTo("125.00");
    verify(transactionRepository, times(2)).save(any(TransactionEntity.class));
  }

  @Test
  void processTransaction_transferSameAccount_throwsValidationException() {
    assertThatThrownBy(
            () ->
                transactionService.processTransaction(
                    new CreateTransactionRequest(
                        1, "TRANSFER", new BigDecimal("10.00"), null, null, 1)))
        .isInstanceOf(BusinessValidationException.class)
        .hasMessage("Source and destination accounts must differ");
  }

  @Test
  void getActiveProducts_returnsMappedProducts() {
    ProductEntity checking = buildProduct("CHK-STD", "Standard Checking", true);
    ProductEntity savings = buildProduct("SAV-HYS", "High Yield Savings", true);
    when(productRepository.findByActiveTrueOrderByProductCodeAsc())
        .thenReturn(List.of(checking, savings));

    List<ProductDto> products = productService.getActiveProducts();

    assertThat(products).extracting(ProductDto::productCode).containsExactly("CHK-STD", "SAV-HYS");
  }

  private static CustomerEntity buildCustomer(
      Integer id, String cifNumber, String firstName, String lastName) {
    CustomerEntity customer = new CustomerEntity();
    customer.setCustomerId(id);
    customer.setCifNumber(cifNumber);
    customer.setFirstName(firstName);
    customer.setLastName(lastName);
    customer.setDateOfBirth(LocalDate.of(1980, 1, 1));
    customer.setEmail(firstName.toLowerCase() + "@example.com");
    customer.setKycStatus("COMPLETED");
    customer.setRiskRating("LOW");
    return customer;
  }

  private static AccountEntity buildAccount(
      Integer id, Integer customerId, String productCode, BigDecimal balance, String status) {
    AccountEntity account = new AccountEntity();
    account.setAccountId(id);
    account.setCustomerId(customerId);
    account.setProductCode(productCode);
    account.setAccountNumber(10000000 + id);
    account.setBalance(balance);
    account.setOverdraftLimit(BigDecimal.ZERO);
    account.setStatus(status);
    return account;
  }

  private static ProductEntity buildProduct(String code, String name, boolean active) {
    ProductEntity product = new ProductEntity();
    product.setProductCode(code);
    product.setName(name);
    product.setCategory("CHECKING");
    product.setInterestRate(BigDecimal.ZERO);
    product.setActive(active);
    return product;
  }
}
