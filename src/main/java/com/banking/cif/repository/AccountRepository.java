package com.banking.cif.repository;

import com.banking.cif.entity.AccountEntity;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Spring Data JPA repository for {@link AccountEntity}. */
public interface AccountRepository extends JpaRepository<AccountEntity, Integer> {

  /**
   * Finds all accounts owned by the specified customer.
   *
   * @param customerId customer ID
   * @return list of accounts ordered by account ID
   */
  List<AccountEntity> findByCustomerIdOrderByAccountIdAsc(Integer customerId);

  /**
   * Checks whether an account number is already in use.
   *
   * @param accountNumber generated or supplied account number
   * @return {@code true} if the account number exists
   */
  boolean existsByAccountNumber(Integer accountNumber);

  /**
   * Counts accounts belonging to a customer.
   *
   * @param customerId customer ID
   * @return number of accounts
   */
  long countByCustomerId(Integer customerId);

  /**
   * Loads an account using a pessimistic write lock to prevent concurrent balance lost updates.
   *
   * @param accountId account ID
   * @return locked account entity if present
   */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT a FROM AccountEntity a WHERE a.accountId = :accountId")
  Optional<AccountEntity> findByIdForUpdate(@Param("accountId") Integer accountId);
}
