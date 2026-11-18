package com.banking.cif.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** JPA entity mapped to the {@code accounts} table. */
@Entity
@Table(name = "accounts")
public class AccountEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "account_id")
  private Integer accountId;

  @Column(name = "customer_id", nullable = false)
  private Integer customerId;

  @Column(name = "product_code", length = 50, nullable = false)
  private String productCode;

  @Column(name = "account_number", nullable = false, unique = true)
  private Integer accountNumber;

  @Column(name = "iban", length = 34)
  private String iban;

  @Column(name = "balance", precision = 15, scale = 2)
  private BigDecimal balance = BigDecimal.ZERO;

  @Column(name = "overdraft_limit", precision = 15, scale = 2)
  private BigDecimal overdraftLimit = BigDecimal.ZERO;

  @Column(name = "status", length = 20)
  private String status = "ACTIVE";

  @Column(name = "opened_at")
  private LocalDateTime openedAt;

  @Column(name = "closed_at")
  private LocalDateTime closedAt;

  @Column(name = "configurations", length = 4000)
  private String configurations = "{}";

  /** Creates an empty {@link AccountEntity}. */
  public AccountEntity() {}

  /** Populates default fields and opening timestamp prior to insertion. */
  @PrePersist
  public void onCreate() {
    if (openedAt == null) {
      openedAt = LocalDateTime.now();
    }
    if (balance == null) {
      balance = BigDecimal.ZERO;
    }
    if (overdraftLimit == null) {
      overdraftLimit = BigDecimal.ZERO;
    }
    if (status == null || status.isBlank()) {
      status = "ACTIVE";
    }
    if (configurations == null || configurations.isBlank()) {
      configurations = "{}";
    }
  }

  public Integer getAccountId() {
    return accountId;
  }

  public void setAccountId(Integer accountId) {
    this.accountId = accountId;
  }

  public Integer getCustomerId() {
    return customerId;
  }

  public void setCustomerId(Integer customerId) {
    this.customerId = customerId;
  }

  public String getProductCode() {
    return productCode;
  }

  public void setProductCode(String productCode) {
    this.productCode = productCode;
  }

  public Integer getAccountNumber() {
    return accountNumber;
  }

  public void setAccountNumber(Integer accountNumber) {
    this.accountNumber = accountNumber;
  }

  public String getIban() {
    return iban;
  }

  public void setIban(String iban) {
    this.iban = iban;
  }

  public BigDecimal getBalance() {
    return balance;
  }

  public void setBalance(BigDecimal balance) {
    this.balance = balance;
  }

  public BigDecimal getOverdraftLimit() {
    return overdraftLimit;
  }

  public void setOverdraftLimit(BigDecimal overdraftLimit) {
    this.overdraftLimit = overdraftLimit;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public LocalDateTime getOpenedAt() {
    return openedAt;
  }

  public void setOpenedAt(LocalDateTime openedAt) {
    this.openedAt = openedAt;
  }

  public LocalDateTime getClosedAt() {
    return closedAt;
  }

  public void setClosedAt(LocalDateTime closedAt) {
    this.closedAt = closedAt;
  }

  public String getConfigurations() {
    return configurations;
  }

  public void setConfigurations(String configurations) {
    this.configurations = configurations;
  }
}
