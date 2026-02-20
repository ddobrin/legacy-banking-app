# Migrated Banking CIF Application Specification v1.0.0

## Overview

The Migrated Banking CIF (Customer Information Facility) application is a modern microservice designed to replace the legacy Struts 2 / Backbone.js monolith. It manages core banking entities: Customers (Persons), Accounts (Financial Applications), and Transactions. It provides a secure, stateless RESTful API to perform CRUD operations, specifically addressing historical architectural flaws and security vulnerabilities present in the legacy system.

## Design Principles & Migration Goals

1.  **Stateless REST:** All API interactions are stateless. Endpoints are no longer overloaded (e.g., separating ID lookup from name search).
2.  **DTO Pattern:** Expose Data Transfer Objects (DTOs), not JPA Entities directly, to prevent over-posting and accidental data exposure.
3.  **Global Error Handling:** Consistent error responses using `@ControllerAdvice`. Domain-specific exceptions (e.g., `InsufficientFundsException`, `AccountNotFoundException`) must replace generic `Exception` throwing.
4.  **Security First:** 
    - Fix Insecure Direct Object Reference (IDOR) by implementing robust authentication/authorization.
    - Remove hardcoded credentials; use environment variables or configuration properties.
    - Validate all inputs strictly (e.g., transaction amounts must be strictly positive).
5.  **Concurrency Control:** Use row-level pessimistic locking (`SELECT ... FOR UPDATE`) or optimistic locking on accounts when processing transactions to prevent "lost updates".
6.  **Separation of Concerns:** Business logic must reside in the `Service` layer, not in `Controllers`.

---

## Data Model (Summary)

The application maps to the existing legacy PostgreSQL DDL (`legacy_schema.sql`):

-   **Customer:** Represents the identity (`customers` table).
    -   Key attributes: `customer_id` (Integer/SERIAL), `cif_number` (String, must be generated securely), `first_name`, `last_name`, `email`, `kyc_status`.
-   **Product:** Represents the product catalog (`products` table).
    -   Key attributes: `product_code` (String), `name`, `category`, `interest_rate`.
-   **Account:** Represents a product instance held by a customer (`accounts` table).
    -   Key attributes: `account_id` (Integer/SERIAL), `customer_id`, `product_code`, `account_number` (Integer, must be securely generated), `balance`, `status`.
-   **Transaction:** Represents a financial movement (`transactions` table).
    -   Key attributes: `transaction_id` (Integer/SERIAL), `account_id`, `transaction_type`, `amount`, `balance_after`.

---

## API Specification

All endpoints are prefixed with `/api/v1`.

### 1. Customers

**Base URL:** `/api/v1/customers`

#### Create Customer
-   **Method:** `POST /`
-   **Body:**
    ```json
    {
      "firstName": "John",
      "lastName": "Doe",
      "email": "john.doe@example.com",
      "dateOfBirth": "1985-05-20"
    }
    ```
-   **Notes:** The system must securely generate the `cifNumber` (do not use `System.currentTimeMillis()`).
-   **Response:** `201 Created` with created Customer DTO.

#### Get Customer by ID
-   **Method:** `GET /{id}`
-   **Response:** `200 OK` with Customer DTO.
-   **Error:** `404 Not Found` if ID does not exist, or `400 Bad Request` if ID is not an integer.

#### Search Customers by Name
-   **Method:** `GET /search?name={name}`
-   **Response:** `200 OK` with a List of Customer DTOs. 

#### Update Customer
-   **Method:** `PUT /{id}`
-   **Body:** Fields to update (e.g., address, email).
-   **Response:** `200 OK` with updated Customer DTO.

---

### 2. Accounts

**Base URL:** `/api/v1/accounts`

#### Create Account
-   **Method:** `POST /`
-   **Body:**
    ```json
    {
      "customerId": 101,
      "productCode": "SAV-HYS"
    }
    ```
-   **Notes:** The `accountNumber` must be securely generated (do not use `java.util.Random`).
-   **Response:** `201 Created` with Account DTO.

#### Get Account Details
-   **Method:** `GET /{id}`
-   **Response:** `200 OK` with Account DTO.

#### Update Account Status
-   **Method:** `PATCH /{id}/status`
-   **Body:** `{ "status": "FROZEN" }`
-   **Response:** `200 OK` with updated Account DTO.

---

### 3. Transactions

**Base URL:** `/api/v1/transactions`

#### Create Transaction (Deposit/Withdrawal)
-   **Method:** `POST /`
-   **Body:**
    ```json
    {
      "accountId": 505,
      "type": "DEPOSIT",
      "amount": 100.00,
      "description": "ATM Deposit"
    }
    ```
-   **Notes:** 
    - `amount` MUST be strictly > 0 (Positive amount validation).
    - Database row-level locking MUST be used to prevent concurrency issues.
-   **Response:** `201 Created` with Transaction DTO (including `balanceAfter`).
-   **Error:** `400 Bad Request` or `422 Unprocessable Entity` for Insufficient Funds or negative amounts.

#### Get Transaction History
-   **Method:** `GET /account/{accountId}`
-   **Response:** `200 OK` with List of Transaction DTOs.

---

## Error Handling

Errors return a standard JSON structure to prevent exposing internal stack traces or generic messages:

```json
{
  "timestamp": "2026-02-20T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Transaction amount must be strictly positive.",
  "path": "/api/v1/transactions"
}
```

| HTTP Status | Condition |
|-------------|-----------|
| 400 | Validation failure (e.g., missing field, negative amount, non-integer ID) |
| 401/403 | Authentication/Authorization failure (Addresses IDOR) |
| 404 | Resource not found (Customer/Account ID) |
| 409 | Conflict (e.g., Duplicate CIF or Email) |
| 422 | Unprocessable Entity (e.g., Insufficient Funds) |
| 500 | Internal Server Error |
