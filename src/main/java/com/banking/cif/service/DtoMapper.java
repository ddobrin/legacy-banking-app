package com.banking.cif.service;

import com.banking.cif.dto.AccountDto;
import com.banking.cif.dto.CustomerDto;
import com.banking.cif.dto.ProductDto;
import com.banking.cif.dto.TransactionDto;
import com.banking.cif.entity.AccountEntity;
import com.banking.cif.entity.CustomerEntity;
import com.banking.cif.entity.ProductEntity;
import com.banking.cif.entity.TransactionEntity;
import java.util.List;

/** Utility class for mapping JPA entities to API DTOs. */
final class DtoMapper {

  private DtoMapper() {}

  static ProductDto toProductDto(ProductEntity entity) {
    return new ProductDto(
        entity.getProductCode(),
        entity.getName(),
        entity.getCategory(),
        entity.getInterestRate(),
        Boolean.TRUE.equals(entity.getActive()));
  }

  static AccountDto toAccountDto(AccountEntity entity) {
    return new AccountDto(
        entity.getAccountId(),
        entity.getCustomerId(),
        entity.getProductCode(),
        entity.getAccountNumber() != null ? String.valueOf(entity.getAccountNumber()) : null,
        entity.getIban(),
        entity.getBalance(),
        entity.getOverdraftLimit(),
        entity.getStatus(),
        entity.getOpenedAt(),
        entity.getClosedAt(),
        entity.getConfigurations());
  }

  static CustomerDto toCustomerDto(
      CustomerEntity entity, List<AccountDto> accounts, Integer accountCount) {
    return new CustomerDto(
        entity.getCustomerId(),
        entity.getCifNumber(),
        entity.getFirstName(),
        entity.getLastName(),
        entity.getDateOfBirth(),
        entity.getEmail(),
        entity.getPhoneNumber(),
        entity.getAddressLine1(),
        entity.getCity(),
        entity.getState(),
        entity.getPostalCode(),
        entity.getCountryCode(),
        entity.getKycStatus(),
        entity.getKycDocuments(),
        entity.getRiskRating(),
        entity.getCreatedAt(),
        entity.getUpdatedAt(),
        accounts,
        accountCount);
  }

  static TransactionDto toTransactionDto(TransactionEntity entity) {
    return new TransactionDto(
        entity.getTransactionId(),
        entity.getAccountId(),
        entity.getReferenceCode(),
        entity.getTransactionType(),
        entity.getAmount(),
        entity.getDescription(),
        entity.getTransactionDate(),
        entity.getBalanceAfter());
  }
}
