package com.banking.cif.dto;

import jakarta.validation.constraints.*;
import lombok.Data;


public class AccountRequest {
    public AccountRequest() {}

    @NotNull
    private Integer customerId;
    
    @NotBlank
    private String productCode;
    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
}
