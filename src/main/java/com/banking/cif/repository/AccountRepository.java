package com.banking.cif.repository;

import com.banking.cif.model.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Integer> {
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.accountId = :id")
    Optional<Account> findByIdForUpdate(@Param("id") Integer id);

    Optional<Account> findByAccountNumber(Integer accountNumber);

    boolean existsByAccountNumber(Integer accountNumber);

    long countByCustomer_CustomerId(Integer customerId);
    
    java.util.List<Account> findByCustomer_CustomerId(Integer customerId);
}
