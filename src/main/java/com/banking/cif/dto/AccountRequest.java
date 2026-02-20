package com.banking.cif.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;


public class AccountRequest {
    public AccountRequest() {}

    @NotNull
    private Integer customerId;
    
    @NotBlank
    private String productCode;

    private BigDecimal balance;

    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
}
