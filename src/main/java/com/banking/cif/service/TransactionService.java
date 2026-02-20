package com.banking.cif.service;

import com.banking.cif.dto.TransactionDTO;
import com.banking.cif.dto.TransactionRequest;
import com.banking.cif.exception.InsufficientFundsException;
import com.banking.cif.exception.InvalidRequestException;
import com.banking.cif.exception.ResourceNotFoundException;
import com.banking.cif.model.Account;
import com.banking.cif.model.Transaction;
import com.banking.cif.repository.AccountRepository;
import com.banking.cif.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service

public class TransactionService {
     @org.springframework.beans.factory.annotation.Autowired
    private TransactionRepository transactionRepository;
     @org.springframework.beans.factory.annotation.Autowired
    private AccountRepository accountRepository;

    @Transactional
    public TransactionDTO createTransaction(TransactionRequest request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidRequestException("Transaction amount must be strictly positive.");
        }

        // Use pessimistic locking to prevent lost updates
        Account account = accountRepository.findByIdForUpdate(request.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + request.getAccountId()));

        if ("FROZEN".equalsIgnoreCase(account.getStatus()) || "CLOSED".equalsIgnoreCase(account.getStatus())) {
            throw new InvalidRequestException("Cannot process transaction on " + account.getStatus() + " account.");
        }

        if ("WITHDRAWAL".equalsIgnoreCase(request.getType())) {
            BigDecimal availableFunds = account.getBalance().add(
                    account.getOverdraftLimit() != null ? account.getOverdraftLimit() : BigDecimal.ZERO
            );
            if (request.getAmount().compareTo(availableFunds) > 0) {
                throw new InsufficientFundsException("Insufficient funds for withdrawal.");
            }
            account.setBalance(account.getBalance().subtract(request.getAmount()));
        } else if ("DEPOSIT".equalsIgnoreCase(request.getType())) {
            account.setBalance(account.getBalance().add(request.getAmount()));
        } else {
            throw new InvalidRequestException("Invalid transaction type: " + request.getType());
        }

        accountRepository.save(account);

        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setTransactionType(request.getType().toUpperCase());
        transaction.setAmount(request.getAmount());
        transaction.setDescription(request.getDescription());
        transaction.setBalanceAfter(account.getBalance());

        Transaction savedTransaction = transactionRepository.save(transaction);
        return mapToDTO(savedTransaction);
    }

    public List<TransactionDTO> getTransactionHistory(Integer accountId) {
        if (!accountRepository.existsById(accountId)) {
            throw new ResourceNotFoundException("Account not found with id: " + accountId);
        }

        return transactionRepository.findByAccount_AccountIdOrderByTransactionDateDesc(accountId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private TransactionDTO mapToDTO(Transaction transaction) {
        TransactionDTO dto = new TransactionDTO();
        dto.setTransactionId(transaction.getTransactionId());
        dto.setAccountId(transaction.getAccount().getAccountId());
        dto.setTransactionType(transaction.getTransactionType());
        dto.setAmount(transaction.getAmount());
        dto.setBalanceAfter(transaction.getBalanceAfter());
        dto.setDescription(transaction.getDescription());
        dto.setTransactionDate(transaction.getTransactionDate());
        return dto;
    }
}
