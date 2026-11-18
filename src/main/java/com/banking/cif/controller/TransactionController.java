package com.banking.cif.controller;

import com.banking.cif.dto.CreateTransactionRequest;
import com.banking.cif.dto.TransactionDto;
import com.banking.cif.service.TransactionService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for processing and querying financial transactions. */
@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

  private final TransactionService transactionService;

  /**
   * Creates a new {@link TransactionController}.
   *
   * @param transactionService transaction service
   */
  public TransactionController(TransactionService transactionService) {
    this.transactionService = transactionService;
  }

  /**
   * Lists transactions when {@code accountId} is provided, or returns an empty list otherwise.
   *
   * @param accountId optional account ID filter
   * @return list of transactions
   */
  @GetMapping
  public List<TransactionDto> listTransactions(
      @RequestParam(name = "accountId", required = false) Integer accountId) {
    if (accountId == null) {
      return List.of();
    }
    return transactionService.getTransactionsByAccountId(accountId);
  }

  /**
   * Lists transactions for the specified account ID.
   *
   * @param accountId account ID
   * @return list of transactions ordered newest first
   */
  @GetMapping("/{accountId}")
  public List<TransactionDto> getTransactionsByAccount(
      @PathVariable("accountId") Integer accountId) {
    return transactionService.getTransactionsByAccountId(accountId);
  }

  /**
   * Lists transactions for the specified account ID via the explicit {@code /account} path.
   *
   * @param accountId account ID
   * @return list of transactions ordered newest first
   */
  @GetMapping("/account/{accountId}")
  public List<TransactionDto> getTransactionsByAccountAlias(
      @PathVariable("accountId") Integer accountId) {
    return transactionService.getTransactionsByAccountId(accountId);
  }

  /**
   * Processes a deposit, withdrawal, or transfer transaction.
   *
   * @param request transaction payload
   * @return HTTP 201 with created transaction
   */
  @PostMapping
  public ResponseEntity<TransactionDto> createTransaction(
      @Valid @RequestBody CreateTransactionRequest request) {
    TransactionDto created = transactionService.processTransaction(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }
}
