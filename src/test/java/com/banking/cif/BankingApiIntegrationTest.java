package com.banking.cif;

import com.banking.cif.model.Product;
import com.banking.cif.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Transactional
public class BankingApiIntegrationTest {

    @Container
    public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    public void setup() {
        if (!productRepository.existsById("SAV-HYS")) {
            Product product = new Product();
            product.setProductCode("SAV-HYS");
            product.setName("High Yield Savings");
            product.setCategory("SAVINGS");
            product.setInterestRate(new BigDecimal("0.05"));
            product.setIsActive(true);
            productRepository.save(product);
        }
    }

    @Test
    public void testCustomerFlow() throws Exception {
        // Create Customer
        String customerJson = "{\"firstName\":\"Alice\",\"lastName\":\"Smith\",\"email\":\"alice.smith@example.com\",\"dateOfBirth\":\"1990-01-01\"}";
        MvcResult result = mockMvc.perform(post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(customerJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email", is("alice.smith@example.com")))
                .andExpect(jsonPath("$.kycStatus", is("PENDING")))
                .andReturn();

        String responseStr = result.getResponse().getContentAsString();
        Integer customerId = com.jayway.jsonpath.JsonPath.read(responseStr, "$.customerId");

        // Validation Error Missing Email
        mockMvc.perform(post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"Bob\",\"lastName\":\"Jones\",\"dateOfBirth\":\"1990-01-01\"}"))
                .andExpect(status().isBadRequest());

        // Get Customer Found
        mockMvc.perform(get("/api/v1/customers/{id}", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("Alice")));

        // Get Customer Invalid ID Type
        mockMvc.perform(get("/api/v1/customers/alice"))
                .andExpect(status().isBadRequest());

        // Search Customer
        mockMvc.perform(get("/api/v1/customers/search?name=Alice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        // Get Customer Unauthorized (IDOR)
        mockMvc.perform(get("/api/v1/customers/{id}", customerId)
                .header("Authorization-User-Id", 999))
                .andExpect(status().isForbidden());

        // Create Account
        String accountJson = "{\"customerId\":" + customerId + ",\"productCode\":\"SAV-HYS\"}";
        MvcResult accountResult = mockMvc.perform(post("/api/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(accountJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("ACTIVE")))
                .andExpect(jsonPath("$.balance", is(0)))
                .andReturn();

        Integer accountId = com.jayway.jsonpath.JsonPath.read(accountResult.getResponse().getContentAsString(), "$.accountId");

        // Create Account Invalid Product
        mockMvc.perform(post("/api/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"customerId\":" + customerId + ",\"productCode\":\"INVALID-CODE\"}"))
                .andExpect(status().isBadRequest()); // should be InvalidRequestException -> 400

        // Update Account Status
        mockMvc.perform(patch("/api/v1/accounts/{id}/status", accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"FROZEN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("FROZEN")));
                
        // Reset status for transaction test
        mockMvc.perform(patch("/api/v1/accounts/{id}/status", accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"ACTIVE\"}"));

        // Deposit Success
        mockMvc.perform(post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":" + accountId + ",\"transactionType\":\"DEPOSIT\",\"amount\":500.00,\"description\":\"Opening Deposit\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount", is(500.0)))
                .andExpect(jsonPath("$.transactionType", is("DEPOSIT")))
                .andExpect(jsonPath("$.balanceAfter", is(500.0)));

        // Deposit Negative Amount
        mockMvc.perform(post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":" + accountId + ",\"transactionType\":\"DEPOSIT\",\"amount\":-100.00,\"description\":\"Hacker Attempt\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Bad Request")));

        // Withdrawal Insufficient Funds
        mockMvc.perform(post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":" + accountId + ",\"transactionType\":\"WITHDRAWAL\",\"amount\":10000.00}"))
                .andExpect(status().isUnprocessableEntity());

        // Get Account Transactions
        mockMvc.perform(get("/api/v1/transactions/account/{id}", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }
}
