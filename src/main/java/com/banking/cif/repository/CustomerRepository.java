package com.banking.cif.repository;

import com.banking.cif.entity.CustomerEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Spring Data JPA repository for {@link CustomerEntity}. */
public interface CustomerRepository extends JpaRepository<CustomerEntity, Integer> {

  /**
   * Checks whether a customer with the given email already exists.
   *
   * @param email email address to check
   * @return {@code true} if a matching customer exists
   */
  boolean existsByEmailIgnoreCase(String email);

  /**
   * Checks whether a customer with the given CIF number already exists.
   *
   * @param cifNumber CIF number to check
   * @return {@code true} if a matching customer exists
   */
  boolean existsByCifNumber(String cifNumber);

  /**
   * Finds customers whose first or last name contains the given text, case-insensitively.
   *
   * @param name fragment to match against first or last name
   * @return matching customers ordered by last name then first name
   */
  @Query(
      "SELECT c FROM CustomerEntity c "
          + "WHERE LOWER(c.firstName) LIKE LOWER(CONCAT('%', :name, '%')) "
          + "OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :name, '%')) "
          + "ORDER BY c.lastName ASC, c.firstName ASC")
  List<CustomerEntity> searchByName(@Param("name") String name);

  /**
   * Retrieves all customers paired with their account counts, ordered by last and first name.
   *
   * @return list of {@code [CustomerEntity, Long]} pairs
   */
  @Query(
      "SELECT c, (SELECT COUNT(a) FROM AccountEntity a WHERE a.customerId = c.customerId) "
          + "FROM CustomerEntity c ORDER BY c.lastName ASC, c.firstName ASC")
  List<Object[]> findAllWithAccountCount();
}
