package com.banking.cif.dto;

import lombok.Data;
import java.math.BigDecimal;


public class AccountDTO {
    public AccountDTO() {}

    private Integer accountId;
    private Integer customerId;
    private String productCode;
    private Integer accountNumber;
    private BigDecimal balance;
    private String status;
    public Integer getAccountId() { return accountId; }
    public void setAccountId(Integer accountId) { this.accountId = accountId; }
    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public Integer getAccountNumber() { return accountNumber; }
    public void setAccountNumber(Integer accountNumber) { this.accountNumber = accountNumber; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
