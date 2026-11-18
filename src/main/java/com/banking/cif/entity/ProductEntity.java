package com.banking.cif.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/** JPA entity mapped to the {@code products} table. */
@Entity
@Table(name = "products")
public class ProductEntity {

  @Id
  @Column(name = "product_code", length = 50, nullable = false)
  private String productCode;

  @Column(name = "name", length = 100, nullable = false)
  private String name;

  @Column(name = "category", length = 50, nullable = false)
  private String category;

  @Column(name = "interest_rate", precision = 5, scale = 4)
  private BigDecimal interestRate;

  @Column(name = "is_active")
  private Boolean active = Boolean.TRUE;

  /** Creates an empty {@link ProductEntity}. */
  public ProductEntity() {}

  public String getProductCode() {
    return productCode;
  }

  public void setProductCode(String productCode) {
    this.productCode = productCode;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public BigDecimal getInterestRate() {
    return interestRate;
  }

  public void setInterestRate(BigDecimal interestRate) {
    this.interestRate = interestRate;
  }

  public Boolean getActive() {
    return active;
  }

  public void setActive(Boolean active) {
    this.active = active;
  }
}
