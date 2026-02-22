package com.banking.cif.dto;

import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.math.BigDecimal;


public class TransactionRequest {
    public TransactionRequest() {}

    @NotNull
    private Integer accountId;
    
    @NotBlank
    @Pattern(regexp = "DEPOSIT|WITHDRAWAL", message = "Type must be DEPOSIT or WITHDRAWAL")
    @JsonProperty("transactionType")
    private String type;
    
    @NotNull
    @Positive(message = "Transaction amount must be strictly positive.")
    private BigDecimal amount;
    
    private String description;
    public Integer getAccountId() { return accountId; }
    public void setAccountId(Integer accountId) { this.accountId = accountId; }
    @JsonProperty("transactionType")
    public String getType() { return type; }
    @JsonProperty("transactionType")
    public void setType(String type) { this.type = type; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
