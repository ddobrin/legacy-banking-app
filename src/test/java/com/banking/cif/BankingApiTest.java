package com.banking.cif;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.banking.cif.dto.AccountDto;
import com.banking.cif.dto.CreateAccountRequest;
import com.banking.cif.dto.CreateCustomerRequest;
import com.banking.cif.dto.CreateTransactionRequest;
import com.banking.cif.dto.CustomerDto;
import com.banking.cif.dto.TransactionDto;
import com.banking.cif.dto.UpdateAccountStatusRequest;
import com.banking.cif.dto.UpdateCustomerRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

/**
 * Full-stack Spring Boot 4 integration tests against the {@code dev} profile in-memory H2
 * database, preserving all legacy banking scenarios and validating modernized endpoints.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class BankingApiTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @Test
  void preSeededProductsCustomersAccountsAndTransactions_areAccessible() throws Exception {
    mockMvc
        .perform(get("/api/v1/products"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].productCode").value("CHK-STD"))
        .andExpect(jsonPath("$[1].productCode").value("SAV-HYS"));

    mockMvc
        .perform(get("/api/v1/customers"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(3)));

    mockMvc
        .perform(get("/api/v1/customers/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.customerId").value(1))
        .andExpect(jsonPath("$.cifNumber").value("CIF-100001"))
        .andExpect(jsonPath("$.accounts.length()").value(3));

    mockMvc
        .perform(get("/api/v1/accounts/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accountId").value(1))
        .andExpect(jsonPath("$.customerId").value(1));

    mockMvc
        .perform(get("/api/v1/transactions/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(10));
  }

  @Test
  void createSearchUpdateAndDeleteCustomer_lifecycle() throws Exception {
    String suffix = uniqueSuffix();
    String email = "alice." + suffix + "@example.com";
    CreateCustomerRequest createRequest =
        new CreateCustomerRequest(
            "Alice",
            "Wonder",
            email,
            LocalDate.of(1990, 1, 1),
            null,
            "555-0100",
            "100 Main St",
            "Toronto",
            "ON",
            "M5V 1A1",
            "CA",
            null);

    MvcResult createResult =
        mockMvc
            .perform(
                post("/api/v1/customers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.customerId").isNumber())
            .andExpect(jsonPath("$.cifNumber").value(org.hamcrest.Matchers.startsWith("CIF-")))
            .andExpect(jsonPath("$.kycStatus").value("PENDING"))
            .andReturn();

    CustomerDto created =
        objectMapper.readValue(createResult.getResponse().getContentAsString(), CustomerDto.class);

    // Search by name endpoint
    mockMvc
        .perform(get("/api/v1/customers/search").param("name", "Wonder"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].email").value(email));

    // Legacy overloaded lookup by name
    mockMvc
        .perform(get("/api/v1/customers/Wonder"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.customerId").value(created.customerId()));

    // Update customer
    UpdateCustomerRequest updateRequest =
        new UpdateCustomerRequest(
            "Alice",
            "Updated",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            "COMPLETED",
            "MEDIUM");

    mockMvc
        .perform(
            put("/api/v1/customers/" + created.customerId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.lastName").value("Updated"))
        .andExpect(jsonPath("$.kycStatus").value("COMPLETED"))
        .andExpect(jsonPath("$.riskRating").value("MEDIUM"));

    // Delete customer with no accounts succeeds
    mockMvc
        .perform(delete("/api/v1/customers/" + created.customerId()))
        .andExpect(status().isNoContent());

    mockMvc
        .perform(get("/api/v1/customers/" + created.customerId()))
        .andExpect(status().isNotFound());
  }

  @Test
  void createCustomer_validationAndDuplicateEmail_return400() throws Exception {
    CreateCustomerRequest missingEmail =
        new CreateCustomerRequest(
            "Bob", "Jones", "", LocalDate.of(1990, 1, 1), "CIF-VAL-1", null, null, null, null, null,
            null, null);

    mockMvc
        .perform(
            post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(missingEmail)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400));

    CreateCustomerRequest duplicateEmail =
        new CreateCustomerRequest(
            "John",
            "Clone",
            "john.smith@example.com",
            LocalDate.of(1990, 1, 1),
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null);

    mockMvc
        .perform(
            post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateEmail)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Email already exists"));
  }

  @Test
  void deleteCustomerWithAccounts_returns400() throws Exception {
    mockMvc
        .perform(delete("/api/v1/customers/1"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Cannot delete customer with existing accounts"));
  }

  @Test
  void createRetrieveAndUpdateAccountStatus_lifecycle() throws Exception {
    CustomerDto customer = createCustomerHelper("Charlie", "charlie." + uniqueSuffix() + "@ex.com");
    AccountDto account = createAccountHelper(customer.customerId(), "CHK-STD", BigDecimal.ZERO);

    assertThat(account.accountId()).isNotNull();
    assertThat(account.status()).isEqualTo("ACTIVE");

    mockMvc
        .perform(get("/api/v1/accounts/" + account.accountId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accountId").value(account.accountId()));

    mockMvc
        .perform(
            put("/api/v1/accounts/" + account.accountId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(new UpdateAccountStatusRequest("FROZEN"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("FROZEN"));

    mockMvc
        .perform(
            patch("/api/v1/accounts/" + account.accountId() + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(new UpdateAccountStatusRequest("CLOSED"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("CLOSED"))
        .andExpect(jsonPath("$.closedAt").isNotEmpty());

    mockMvc
        .perform(get("/api/v1/accounts/not-a-number"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void depositWithdrawalTransferAndListTransactions_flow() throws Exception {
    CustomerDto customer = createCustomerHelper("Dave", "dave." + uniqueSuffix() + "@ex.com");
    AccountDto checking = createAccountHelper(customer.customerId(), "CHK-STD", BigDecimal.ZERO);
    AccountDto savings = createAccountHelper(customer.customerId(), "SAV-HYS", BigDecimal.ZERO);

    // Deposit 500.00
    CreateTransactionRequest deposit =
        new CreateTransactionRequest(
            checking.accountId(),
            "DEPOSIT",
            new BigDecimal("500.00"),
            "Opening deposit",
            "DEP-1",
            null);
    mockMvc
        .perform(
            post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(deposit)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.balanceAfter").value(500.00));

    // Withdrawal 100.00
    CreateTransactionRequest withdrawal =
        new CreateTransactionRequest(
            checking.accountId(), "WITHDRAWAL", new BigDecimal("100.00"), "ATM", "WDR-1", null);
    mockMvc
        .perform(
            post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(withdrawal)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.balanceAfter").value(400.00));

    // Insufficient funds withdrawal 900.00 -> 400
    CreateTransactionRequest overdraftAttempt =
        new CreateTransactionRequest(
            checking.accountId(), "WITHDRAWAL", new BigDecimal("900.00"), "Too much", null, null);
    mockMvc
        .perform(
            post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(overdraftAttempt)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Insufficient funds"));

    // Negative / zero amount rejected -> 400
    CreateTransactionRequest zeroAmount =
        new CreateTransactionRequest(
            checking.accountId(), "DEPOSIT", BigDecimal.ZERO, "Zero", null, null);
    mockMvc
        .perform(
            post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(zeroAmount)))
        .andExpect(status().isBadRequest());

    // Transfer 150.00 to savings
    CreateTransactionRequest transfer =
        new CreateTransactionRequest(
            checking.accountId(),
            "TRANSFER",
            new BigDecimal("150.00"),
            "Move to savings",
            "TRF-1",
            savings.accountId());
    mockMvc
        .perform(
            post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transfer)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.balanceAfter").value(250.00));

    // Verify savings credited and transaction list endpoints
    mockMvc
        .perform(get("/api/v1/accounts/" + savings.accountId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.balance").value(150.00));

    mockMvc
        .perform(get("/api/v1/transactions/account/" + checking.accountId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(3));

    mockMvc
        .perform(
            get("/api/v1/transactions").param("accountId", String.valueOf(savings.accountId())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1));
  }

  @Test
  void concurrentDeposits_preserveAtomicBalanceWithoutLostUpdates() throws Exception {
    CustomerDto customer =
        createCustomerHelper("Concur", "concur." + uniqueSuffix() + "@ex.com");
    AccountDto account = createAccountHelper(customer.customerId(), "CHK-STD", BigDecimal.ZERO);

    int threadCount = 8;
    BigDecimal depositAmount = new BigDecimal("25.00");
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    CountDownLatch readyLatch = new CountDownLatch(threadCount);
    CountDownLatch startLatch = new CountDownLatch(1);
    List<Future<Integer>> futures = new ArrayList<>();

    for (int i = 0; i < threadCount; i++) {
      final int idx = i;
      Callable<Integer> task =
          () -> {
            readyLatch.countDown();
            startLatch.await();
            CreateTransactionRequest req =
                new CreateTransactionRequest(
                    account.accountId(),
                    "DEPOSIT",
                    depositAmount,
                    "Concurrent " + idx,
                    "C-" + idx,
                    null);
            return mockMvc
                .perform(
                    post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andReturn()
                .getResponse()
                .getStatus();
          };
      futures.add(executor.submit(task));
    }

    readyLatch.await();
    startLatch.countDown();
    for (Future<Integer> future : futures) {
      assertThat(future.get()).isEqualTo(201);
    }
    executor.shutdown();

    MvcResult accountResult =
        mockMvc
            .perform(get("/api/v1/accounts/" + account.accountId()))
            .andExpect(status().isOk())
            .andReturn();
    AccountDto updated =
        objectMapper.readValue(accountResult.getResponse().getContentAsString(), AccountDto.class);
    assertThat(updated.balance()).isEqualByComparingTo("200.00");

    MvcResult txnsResult =
        mockMvc
            .perform(get("/api/v1/transactions/" + account.accountId()))
            .andExpect(status().isOk())
            .andReturn();
    TransactionDto[] txns =
        objectMapper.readValue(
            txnsResult.getResponse().getContentAsString(), TransactionDto[].class);
    assertThat(txns).hasSize(threadCount);
  }

  private CustomerDto createCustomerHelper(String firstName, String email) throws Exception {
    CreateCustomerRequest request =
        new CreateCustomerRequest(
            firstName,
            "Helper",
            email,
            LocalDate.of(1990, 1, 1),
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null);
    MvcResult result =
        mockMvc
            .perform(
                post("/api/v1/customers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn();
    return objectMapper.readValue(result.getResponse().getContentAsString(), CustomerDto.class);
  }

  private AccountDto createAccountHelper(
      Integer customerId, String productCode, BigDecimal initialBalance) throws Exception {
    CreateAccountRequest request =
        new CreateAccountRequest(customerId, productCode, initialBalance, null, null);
    MvcResult result =
        mockMvc
            .perform(
                post("/api/v1/accounts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn();
    return objectMapper.readValue(result.getResponse().getContentAsString(), AccountDto.class);
  }

  private static String uniqueSuffix() {
    return UUID.randomUUID().toString().substring(0, 8);
  }
}
