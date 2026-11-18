package com.banking.cif.repository;

import com.banking.cif.entity.TransactionEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data JPA repository for {@link TransactionEntity}. */
public interface TransactionRepository extends JpaRepository<TransactionEntity, Integer> {

  /**
   * Finds all transactions for an account ordered from newest to oldest.
   *
   * @param accountId account ID
   * @return list of transactions
   */
  List<TransactionEntity> findByAccountIdOrderByTransactionDateDescTransactionIdDesc(
      Integer accountId);
}
