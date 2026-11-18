package com.banking.cif;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.banking.cif.controller.AccountController;
import com.banking.cif.controller.CustomerController;
import com.banking.cif.controller.ProductController;
import com.banking.cif.controller.TransactionController;
import com.banking.cif.dto.AccountDto;
import com.banking.cif.dto.CreateAccountRequest;
import com.banking.cif.dto.CreateCustomerRequest;
import com.banking.cif.dto.CreateTransactionRequest;
import com.banking.cif.dto.CustomerDto;
import com.banking.cif.dto.ProductDto;
import com.banking.cif.dto.TransactionDto;
import com.banking.cif.dto.UpdateAccountStatusRequest;
import com.banking.cif.dto.UpdateCustomerRequest;
import com.banking.cif.exception.BusinessValidationException;
import com.banking.cif.exception.GlobalExceptionHandler;
import com.banking.cif.exception.ResourceNotFoundException;
import com.banking.cif.service.AccountService;
import com.banking.cif.service.CustomerService;
import com.banking.cif.service.ProductService;
import com.banking.cif.service.TransactionService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

/**
 * WebMvc slice tests verifying REST controllers, Jackson 3 serialization, Bean Validation, and
 * {@link GlobalExceptionHandler} responses.
 */
@WebMvcTest(
    controllers = {
      CustomerController.class,
      AccountController.class,
      TransactionController.class,
      ProductController.class
    })
@Import(GlobalExceptionHandler.class)
class BankingControllerWebMvcTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private CustomerService customerService;
  @MockitoBean private AccountService accountService;
  @MockitoBean private TransactionService transactionService;
  @MockitoBean private ProductService productService;

  @Test
  void getAllCustomers_returnsOkJsonArray() throws Exception {
    CustomerDto john =
        new CustomerDto(
            1,
            "CIF-100001",
            "John",
            "Smith",
            LocalDate.of(1980, 1, 1),
            "john.smith@example.com",
            null,
            null,
            null,
            null,
            null,
            null,
            "COMPLETED",
            "{}",
            "LOW",
            null,
            null,
            null,
            3);
    when(customerService.getAllCustomers()).thenReturn(List.of(john));

    mockMvc
        .perform(get("/api/v1/customers"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].customerId").value(1))
        .andExpect(jsonPath("$[0].cifNumber").value("CIF-100001"))
        .andExpect(jsonPath("$[0].accountCount").value(3));
  }

  @Test
  void createCustomer_validPayload_returns201() throws Exception {
    CreateCustomerRequest request =
        new CreateCustomerRequest(
            "Alice",
            "Smith",
            "alice@example.com",
            LocalDate.of(1990, 1, 1),
            "CIF-API-001",
            null,
            null,
            null,
            null,
            null,
            null,
            null);
    CustomerDto response =
        new CustomerDto(
            10,
            "CIF-API-001",
            "Alice",
            "Smith",
            LocalDate.of(1990, 1, 1),
            "alice@example.com",
            null,
            null,
            null,
            null,
            null,
            null,
            "PENDING",
            "{}",
            "LOW",
            LocalDateTime.now(),
            LocalDateTime.now(),
            List.of(),
            0);
    when(customerService.createCustomer(any(CreateCustomerRequest.class))).thenReturn(response);

    mockMvc
        .perform(
            post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.customerId").value(10))
        .andExpect(jsonPath("$.kycStatus").value("PENDING"));
  }

  @Test
  void createCustomer_missingRequiredFields_returns400ApiError() throws Exception {
    CreateCustomerRequest invalidRequest =
        new CreateCustomerRequest(
            "Bob", "Jones", "", null, "CIF-API-002", null, null, null, null, null, null, null);

    mockMvc
        .perform(
            post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.error").value("Bad Request"))
        .andExpect(jsonPath("$.message").isNotEmpty());
  }

  @Test
  void updateAndDeleteCustomer_returnExpectedStatuses() throws Exception {
    UpdateCustomerRequest updateRequest =
        new UpdateCustomerRequest(
            "Alice", "Updated", null, null, null, null, null, null, null, null, null, null);
    CustomerDto updated =
        new CustomerDto(
            10,
            "CIF-API-001",
            "Alice",
            "Updated",
            LocalDate.of(1990, 1, 1),
            "alice@example.com",
            null,
            null,
            null,
            null,
            null,
            null,
            "PENDING",
            "{}",
            "LOW",
            null,
            null,
            List.of(),
            0);
    when(customerService.updateCustomer(eq(10), any(UpdateCustomerRequest.class)))
        .thenReturn(updated);
    doNothing().when(customerService).deleteCustomer(10);

    mockMvc
        .perform(
            put("/api/v1/customers/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.lastName").value("Updated"));

    mockMvc.perform(delete("/api/v1/customers/10")).andExpect(status().isNoContent());
  }

  @Test
  void getAccount_notFound_returns404ApiError() throws Exception {
    when(accountService.getAccountById("999"))
        .thenThrow(new ResourceNotFoundException("Account not found for ID: 999"));

    mockMvc
        .perform(get("/api/v1/accounts/999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.error").value("Not Found"))
        .andExpect(jsonPath("$.message").value("Account not found for ID: 999"));
  }

  @Test
  void createAndPatchAccountStatus_returnExpectedJson() throws Exception {
    CreateAccountRequest createReq = new CreateAccountRequest(1, "CHK-STD", null, null, null);
    AccountDto created =
        new AccountDto(
            15,
            1,
            "CHK-STD",
            "12345678",
            null,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            "ACTIVE",
            LocalDateTime.now(),
            null,
            "{}");
    AccountDto frozen =
        new AccountDto(
            15,
            1,
            "CHK-STD",
            "12345678",
            null,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            "FROZEN",
            LocalDateTime.now(),
            null,
            "{}");

    when(accountService.createAccount(any(CreateAccountRequest.class))).thenReturn(created);
    when(accountService.updateAccountStatus(eq(15), any(UpdateAccountStatusRequest.class)))
        .thenReturn(frozen);

    mockMvc
        .perform(
            post("/api/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.status").value("ACTIVE"));

    mockMvc
        .perform(
            patch("/api/v1/accounts/15/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdateAccountStatusRequest("FROZEN"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("FROZEN"));
  }

  @Test
  void createTransaction_insufficientFunds_returns400() throws Exception {
    CreateTransactionRequest request =
        new CreateTransactionRequest(1, "WITHDRAWAL", new BigDecimal("9999.00"), null, null, null);
    doThrow(new BusinessValidationException("Insufficient funds"))
        .when(transactionService)
        .processTransaction(any(CreateTransactionRequest.class));

    mockMvc
        .perform(
            post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Insufficient funds"));
  }

  @Test
  void getTransactionsAndProducts_returnOk() throws Exception {
    TransactionDto txn =
        new TransactionDto(
            1,
            1,
            null,
            "DEPOSIT",
            new BigDecimal("100.00"),
            "Paycheck",
            LocalDateTime.now(),
            new BigDecimal("100.00"));
    when(transactionService.getTransactionsByAccountId(1)).thenReturn(List.of(txn));
    when(productService.getActiveProducts())
        .thenReturn(
            List.of(
                new ProductDto(
                    "CHK-STD", "Standard Checking", "CHECKING", BigDecimal.ZERO, true)));

    mockMvc
        .perform(get("/api/v1/transactions/account/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].transactionType").value("DEPOSIT"));

    mockMvc
        .perform(get("/api/v1/products"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].productCode").value("CHK-STD"));
  }
}
