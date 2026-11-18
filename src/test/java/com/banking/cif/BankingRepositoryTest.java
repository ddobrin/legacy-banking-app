package com.banking.cif;

import static org.assertj.core.api.Assertions.assertThat;

import com.banking.cif.entity.AccountEntity;
import com.banking.cif.entity.CustomerEntity;
import com.banking.cif.entity.ProductEntity;
import com.banking.cif.entity.TransactionEntity;
import com.banking.cif.repository.AccountRepository;
import com.banking.cif.repository.CustomerRepository;
import com.banking.cif.repository.ProductRepository;
import com.banking.cif.repository.TransactionRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

/**
 * JPA repository slice tests verifying {@code legacy_schema.sql} and {@code data.sql} mappings
 * against the {@code dev} H2 profile.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("dev")
class BankingRepositoryTest {

  @Autowired private CustomerRepository customerRepository;
  @Autowired private AccountRepository accountRepository;
  @Autowired private TransactionRepository transactionRepository;
  @Autowired private ProductRepository productRepository;

  @Test
  void findAllWithAccountCount_returnsPreSeededCustomersOrderedByName() {
    List<Object[]> rows = customerRepository.findAllWithAccountCount();

    assertThat(rows).hasSizeGreaterThanOrEqualTo(3);
    CustomerEntity first = (CustomerEntity) rows.get(0)[0];
    assertThat(first.getLastName()).isEqualTo("Baggins");
    assertThat(((Number) rows.get(0)[1]).intValue()).isEqualTo(4);
  }

  @Test
  void searchByName_matchesCaseInsensitiveSubstring() {
    List<CustomerEntity> matches = customerRepository.searchByName("smith");

    assertThat(matches).hasSize(1);
    assertThat(matches.get(0).getCifNumber()).isEqualTo("CIF-100001");
  }

  @Test
  void findByCustomerIdAndLockForUpdate_returnExpectedAccounts() {
    List<AccountEntity> johnAccounts = accountRepository.findByCustomerIdOrderByAccountIdAsc(1);
    assertThat(johnAccounts).hasSize(3);

    Optional<AccountEntity> locked = accountRepository.findByIdForUpdate(1);
    assertThat(locked).isPresent();
    assertThat(locked.get().getProductCode()).isEqualTo("CHK-STD");
  }

  @Test
  void findByAccountIdOrderByTransactionDateDesc_returnsTenPreSeededTransactions() {
    List<TransactionEntity> transactions =
        transactionRepository.findByAccountIdOrderByTransactionDateDescTransactionIdDesc(1);

    assertThat(transactions).hasSize(10);
  }

  @Test
  void findByActiveTrueOrderByProductCodeAsc_returnsSeededProducts() {
    List<ProductEntity> products = productRepository.findByActiveTrueOrderByProductCodeAsc();

    assertThat(products)
        .extracting(ProductEntity::getProductCode)
        .containsExactly("CHK-STD", "SAV-HYS");
  }
}
