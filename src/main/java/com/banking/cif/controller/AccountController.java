package com.banking.cif.controller;

import com.banking.cif.dto.AccountDTO;
import com.banking.cif.dto.AccountRequest;
import com.banking.cif.dto.AccountStatusRequest;
import com.banking.cif.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/accounts")

public class AccountController {

     @org.springframework.beans.factory.annotation.Autowired
    private AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountDTO> createAccount(@Valid @RequestBody AccountRequest request) {
        AccountDTO created = accountService.createAccount(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountDTO> getAccount(@PathVariable Integer id, @RequestHeader(value="Authorization-User-Id", required=false) Integer authUserId) {
        AccountDTO account = accountService.getAccountById(id);
        
        // Simple simulated IDOR protection - check if requesting user matches account's customer
        if (authUserId != null && !authUserId.equals(account.getCustomerId())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        
        return ResponseEntity.ok(account);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<AccountDTO> updateStatus(@PathVariable Integer id, @Valid @RequestBody AccountStatusRequest request) {
        AccountDTO updated = accountService.updateAccountStatus(id, request);
        return ResponseEntity.ok(updated);
    }
}
