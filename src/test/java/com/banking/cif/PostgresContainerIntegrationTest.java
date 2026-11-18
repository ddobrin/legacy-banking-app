package com.banking.cif;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.banking.cif.dto.AccountDto;
import com.banking.cif.dto.CreateAccountRequest;
import com.banking.cif.dto.CreateCustomerRequest;
import com.banking.cif.dto.CreateTransactionRequest;
import com.banking.cif.dto.CustomerDto;
import com.banking.cif.dto.UpdateAccountStatusRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.ObjectMapper;

/**
 * End-to-end integration tests running against a real PostgreSQL instance via Testcontainers
 * 1.20.4 using the {@code test} profile and {@code legacy_schema.sql}.
 */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PostgresContainerIntegrationTest {

  @Container @ServiceConnection
  static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @Test
  void postgresSchemaAndSeedData_initializeAndSupportFullBankingFlow() throws Exception {
    assertThat(POSTGRES.isRunning()).isTrue();

    // Verify seeded data loaded via legacy_schema.sql and data.sql
    mockMvc
        .perform(get("/api/v1/customers/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.cifNumber").value("CIF-100001"))
        .andExpect(jsonPath("$.accounts.length()").value(3));

    mockMvc
        .perform(get("/api/v1/transactions/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(10));

    // Create a new customer on PostgreSQL
    CreateCustomerRequest customerRequest =
        new CreateCustomerRequest(
            "Pg",
            "Tester",
            "pg.tester@example.com",
            LocalDate.of(1992, 5, 14),
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null);
    MvcResult customerResult =
        mockMvc
            .perform(
                post("/api/v1/customers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(customerRequest)))
            .andExpect(status().isCreated())
            .andReturn();
    CustomerDto customer =
        objectMapper.readValue(
            customerResult.getResponse().getContentAsString(), CustomerDto.class);

    // Open two accounts
    MvcResult acc1Result =
        mockMvc
            .perform(
                post("/api/v1/accounts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            new CreateAccountRequest(
                                customer.customerId(), "CHK-STD", BigDecimal.ZERO, null, null))))
            .andExpect(status().isCreated())
            .andReturn();
    AccountDto acc1 =
        objectMapper.readValue(acc1Result.getResponse().getContentAsString(), AccountDto.class);

    MvcResult acc2Result =
        mockMvc
            .perform(
                post("/api/v1/accounts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            new CreateAccountRequest(
                                customer.customerId(), "SAV-HYS", BigDecimal.ZERO, null, null))))
            .andExpect(status().isCreated())
            .andReturn();
    AccountDto acc2 =
        objectMapper.readValue(acc2Result.getResponse().getContentAsString(), AccountDto.class);

    // Deposit, transfer, and freeze on PostgreSQL (exercises SELECT ... FOR UPDATE)
    mockMvc
        .perform(
            post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        new CreateTransactionRequest(
                            acc1.accountId(),
                            "DEPOSIT",
                            new BigDecimal("300.00"),
                            "PG Deposit",
                            null,
                            null))))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.balanceAfter").value(300.00));

    mockMvc
        .perform(
            post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        new CreateTransactionRequest(
                            acc1.accountId(),
                            "TRANSFER",
                            new BigDecimal("125.00"),
                            "PG Transfer",
                            null,
                            acc2.accountId()))))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.balanceAfter").value(175.00));

    mockMvc
        .perform(
            patch("/api/v1/accounts/" + acc1.accountId() + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(new UpdateAccountStatusRequest("FROZEN"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("FROZEN"));
  }
}
