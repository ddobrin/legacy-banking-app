package com.banking.cif.dto;

import jakarta.validation.constraints.*;
import lombok.Data;


public class AccountStatusRequest {
    public AccountStatusRequest() {}

    @NotBlank
    private String status;
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
