package com.banking.cif.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** JPA entity mapped to the {@code customers} table. */
@Entity
@Table(name = "customers")
public class CustomerEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "customer_id")
  private Integer customerId;

  @Column(name = "cif_number", length = 20, nullable = false, unique = true)
  private String cifNumber;

  @Column(name = "first_name", length = 100, nullable = false)
  private String firstName;

  @Column(name = "last_name", length = 100, nullable = false)
  private String lastName;

  @Column(name = "date_of_birth", nullable = false)
  private LocalDate dateOfBirth;

  @Column(name = "email", length = 150, nullable = false, unique = true)
  private String email;

  @Column(name = "phone_number", length = 20)
  private String phoneNumber;

  @Column(name = "address_line1", length = 255)
  private String addressLine1;

  @Column(name = "city", length = 100)
  private String city;

  @Column(name = "state", length = 100)
  private String state;

  @Column(name = "postal_code", length = 20)
  private String postalCode;

  @Column(name = "country_code", length = 2, columnDefinition = "char(2)")
  private String countryCode;

  @Column(name = "kyc_status", length = 20)
  private String kycStatus = "PENDING";

  @Column(name = "kyc_documents", length = 4000)
  private String kycDocuments = "{}";

  @Column(name = "risk_rating", length = 10)
  private String riskRating = "LOW";

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  /** Creates an empty {@link CustomerEntity}. */
  public CustomerEntity() {}

  /** Populates default fields and timestamps prior to insertion. */
  @PrePersist
  public void onCreate() {
    LocalDateTime now = LocalDateTime.now();
    if (createdAt == null) {
      createdAt = now;
    }
    updatedAt = now;
    if (kycStatus == null || kycStatus.isBlank()) {
      kycStatus = "PENDING";
    }
    if (kycDocuments == null || kycDocuments.isBlank()) {
      kycDocuments = "{}";
    }
    if (riskRating == null || riskRating.isBlank()) {
      riskRating = "LOW";
    }
  }

  /** Updates the modification timestamp prior to update. */
  @PreUpdate
  public void onUpdate() {
    updatedAt = LocalDateTime.now();
  }

  public Integer getCustomerId() {
    return customerId;
  }

  public void setCustomerId(Integer customerId) {
    this.customerId = customerId;
  }

  public String getCifNumber() {
    return cifNumber;
  }

  public void setCifNumber(String cifNumber) {
    this.cifNumber = cifNumber;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public LocalDate getDateOfBirth() {
    return dateOfBirth;
  }

  public void setDateOfBirth(LocalDate dateOfBirth) {
    this.dateOfBirth = dateOfBirth;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public String getAddressLine1() {
    return addressLine1;
  }

  public void setAddressLine1(String addressLine1) {
    this.addressLine1 = addressLine1;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getPostalCode() {
    return postalCode;
  }

  public void setPostalCode(String postalCode) {
    this.postalCode = postalCode;
  }

  public String getCountryCode() {
    return countryCode;
  }

  public void setCountryCode(String countryCode) {
    this.countryCode = countryCode;
  }

  public String getKycStatus() {
    return kycStatus;
  }

  public void setKycStatus(String kycStatus) {
    this.kycStatus = kycStatus;
  }

  public String getKycDocuments() {
    return kycDocuments;
  }

  public void setKycDocuments(String kycDocuments) {
    this.kycDocuments = kycDocuments;
  }

  public String getRiskRating() {
    return riskRating;
  }

  public void setRiskRating(String riskRating) {
    this.riskRating = riskRating;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}
