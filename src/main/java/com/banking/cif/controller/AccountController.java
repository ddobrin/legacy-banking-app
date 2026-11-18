package com.banking.cif.controller;

import com.banking.cif.dto.AccountDto;
import com.banking.cif.dto.CreateAccountRequest;
import com.banking.cif.dto.UpdateAccountStatusRequest;
import com.banking.cif.service.AccountService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for managing bank accounts. */
@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

  private final AccountService accountService;

  /**
   * Creates a new {@link AccountController}.
   *
   * @param accountService account service
   */
  public AccountController(AccountService accountService) {
    this.accountService = accountService;
  }

  /**
   * Lists all accounts, optionally filtered by customer ID.
   *
   * @param customerId optional customer ID filter
   * @return list of accounts
   */
  @GetMapping
  public List<AccountDto> getAccounts(
      @RequestParam(name = "customerId", required = false) Integer customerId) {
    return accountService.getAccounts(customerId);
  }

  /**
   * Retrieves a single account by ID.
   *
   * @param id account ID
   * @return account details
   */
  @GetMapping("/{id}")
  public AccountDto getAccount(@PathVariable("id") String id) {
    return accountService.getAccountById(id);
  }

  /**
   * Opens a new bank account.
   *
   * @param request account creation payload
   * @return HTTP 201 with created account
   */
  @PostMapping
  public ResponseEntity<AccountDto> createAccount(
      @Valid @RequestBody CreateAccountRequest request) {
    AccountDto created = accountService.createAccount(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  /**
   * Updates an account's status via PUT.
   *
   * @param id account ID
   * @param request status update payload
   * @return updated account details
   */
  @PutMapping("/{id}")
  public AccountDto updateAccountPut(
      @PathVariable("id") Integer id, @Valid @RequestBody UpdateAccountStatusRequest request) {
    return accountService.updateAccountStatus(id, request);
  }

  /**
   * Updates an account's status via PATCH.
   *
   * @param id account ID
   * @param request status update payload
   * @return updated account details
   */
  @PatchMapping("/{id}")
  public AccountDto updateAccountPatch(
      @PathVariable("id") Integer id, @Valid @RequestBody UpdateAccountStatusRequest request) {
    return accountService.updateAccountStatus(id, request);
  }

  /**
   * Updates an account's status via explicit {@code /status} subresource.
   *
   * @param id account ID
   * @param request status update payload
   * @return updated account details
   */
  @PatchMapping("/{id}/status")
  public AccountDto updateAccountStatus(
      @PathVariable("id") Integer id, @Valid @RequestBody UpdateAccountStatusRequest request) {
    return accountService.updateAccountStatus(id, request);
  }
}
