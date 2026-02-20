package com.banking.cif.controller;

import com.banking.cif.dto.TransactionDTO;
import com.banking.cif.dto.TransactionRequest;
import com.banking.cif.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")

public class TransactionController {

     @org.springframework.beans.factory.annotation.Autowired
    private TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionDTO> createTransaction(@Valid @RequestBody TransactionRequest request) {
        TransactionDTO created = transactionService.createTransaction(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<TransactionDTO>> getHistory(@PathVariable Integer accountId) {
        List<TransactionDTO> history = transactionService.getTransactionHistory(accountId);
        return ResponseEntity.ok(history);
    }
}
