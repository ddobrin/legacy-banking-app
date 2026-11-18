package com.banking.cif.service;

import com.banking.cif.dto.CreateTransactionRequest;
import com.banking.cif.dto.TransactionDto;
import com.banking.cif.entity.AccountEntity;
import com.banking.cif.entity.TransactionEntity;
import com.banking.cif.exception.BusinessValidationException;
import com.banking.cif.exception.ResourceNotFoundException;
import com.banking.cif.repository.AccountRepository;
import com.banking.cif.repository.TransactionRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service layer executing atomic account transactions and transaction history queries. */
@Service
public class TransactionService {

  private final AccountRepository accountRepository;
  private final TransactionRepository transactionRepository;

  /**
   * Creates a new {@link TransactionService}.
   *
   * @param accountRepository account repository
   * @param transactionRepository transaction repository
   */
  public TransactionService(
      AccountRepository accountRepository, TransactionRepository transactionRepository) {
    this.accountRepository = accountRepository;
    this.transactionRepository = transactionRepository;
  }

  /**
   * Retrieves transactions for a specific account ordered from newest to oldest.
   *
   * @param accountId account ID
   * @return list of transactions
   */
  @Transactional(readOnly = true)
  public List<TransactionDto> getTransactionsByAccountId(Integer accountId) {
    if (!accountRepository.existsById(accountId)) {
      throw new ResourceNotFoundException("Account not found for ID: " + accountId);
    }
    return transactionRepository
        .findByAccountIdOrderByTransactionDateDescTransactionIdDesc(accountId)
        .stream()
        .map(DtoMapper::toTransactionDto)
        .toList();
  }

  /**
   * Atomically executes a deposit, withdrawal, or transfer using pessimistic row locking.
   *
   * @param request transaction request
   * @return persisted transaction record for the source account
   */
  @Transactional
  public TransactionDto processTransaction(CreateTransactionRequest request) {
    if (request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) <= 0) {
      throw new BusinessValidationException("Amount must be greater than zero");
    }

    String type = request.transactionType().trim().toUpperCase();
    return switch (type) {
      case "DEPOSIT" -> processDeposit(request);
      case "WITHDRAWAL", "WITHDRAW" -> processWithdrawal(request);
      case "TRANSFER" -> processTransfer(request);
      default ->
          throw new BusinessValidationException(
              "Unsupported transaction type: " + request.transactionType());
    };
  }

  private TransactionDto processDeposit(CreateTransactionRequest request) {
    AccountEntity account = lockActiveAccount(request.accountId());
    BigDecimal newBalance = account.getBalance().add(request.amount());
    account.setBalance(newBalance);
    accountRepository.save(account);

    TransactionEntity saved =
        saveTransaction(
            account.getAccountId(),
            "DEPOSIT",
            request.amount(),
            request.description(),
            request.referenceCode(),
            newBalance);
    return DtoMapper.toTransactionDto(saved);
  }

  private TransactionDto processWithdrawal(CreateTransactionRequest request) {
    AccountEntity account = lockActiveAccount(request.accountId());
    if (account.getBalance().compareTo(request.amount()) < 0) {
      throw new BusinessValidationException("Insufficient funds");
    }

    BigDecimal newBalance = account.getBalance().subtract(request.amount());
    account.setBalance(newBalance);
    accountRepository.save(account);

    TransactionEntity saved =
        saveTransaction(
            account.getAccountId(),
            "WITHDRAWAL",
            request.amount(),
            request.description(),
            request.referenceCode(),
            newBalance);
    return DtoMapper.toTransactionDto(saved);
  }

  private TransactionDto processTransfer(CreateTransactionRequest request) {
    Integer sourceId = request.accountId();
    Integer targetId = request.targetAccountId();
    if (targetId == null) {
      throw new BusinessValidationException(
          "Destination account ID is required for transfer");
    }
    if (sourceId.equals(targetId)) {
      throw new BusinessValidationException("Source and destination accounts must differ");
    }

    // Lock in deterministic ID order to avoid concurrent transfer deadlocks.
    AccountEntity firstLocked = lockActiveAccount(Math.min(sourceId, targetId));
    AccountEntity secondLocked = lockActiveAccount(Math.max(sourceId, targetId));
    AccountEntity source = sourceId < targetId ? firstLocked : secondLocked;
    AccountEntity target = sourceId < targetId ? secondLocked : firstLocked;

    if (source.getBalance().compareTo(request.amount()) < 0) {
      throw new BusinessValidationException("Insufficient funds");
    }

    BigDecimal sourceNewBalance = source.getBalance().subtract(request.amount());
    BigDecimal targetNewBalance = target.getBalance().add(request.amount());
    source.setBalance(sourceNewBalance);
    target.setBalance(targetNewBalance);
    accountRepository.save(source);
    accountRepository.save(target);

    String debitDescription =
        request.description() != null && !request.description().isBlank()
            ? request.description().trim()
            : "Transfer to account " + targetId;
    String creditDescription =
        request.description() != null && !request.description().isBlank()
            ? request.description().trim()
            : "Transfer from account " + sourceId;

    TransactionEntity debitTxn =
        saveTransaction(
            sourceId,
            "TRANSFER",
            request.amount(),
            debitDescription,
            request.referenceCode(),
            sourceNewBalance);
    saveTransaction(
        targetId,
        "DEPOSIT",
        request.amount(),
        creditDescription,
        request.referenceCode(),
        targetNewBalance);
    return DtoMapper.toTransactionDto(debitTxn);
  }

  private AccountEntity lockActiveAccount(Integer accountId) {
    AccountEntity account =
        accountRepository
            .findByIdForUpdate(accountId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Account not found for ID: " + accountId));
    String status = account.getStatus();
    if (!"ACTIVE".equalsIgnoreCase(status) && !"OPEN".equalsIgnoreCase(status)) {
      throw new BusinessValidationException("Account is not active");
    }
    return account;
  }

  private TransactionEntity saveTransaction(
      Integer accountId,
      String type,
      BigDecimal amount,
      String description,
      String referenceCode,
      BigDecimal balanceAfter) {
    TransactionEntity entity = new TransactionEntity();
    entity.setAccountId(accountId);
    entity.setTransactionType(type);
    entity.setAmount(amount);
    entity.setDescription(
        description != null && !description.isBlank() ? description.trim() : null);
    entity.setReferenceCode(
        referenceCode != null && !referenceCode.isBlank() ? referenceCode.trim() : null);
    entity.setBalanceAfter(balanceAfter);
    return transactionRepository.save(entity);
  }
}
