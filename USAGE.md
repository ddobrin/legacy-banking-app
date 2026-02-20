# Banking CIF Microservice - Usage Guide

This project has been migrated from a legacy Struts 2 monolith to a modern, secure Spring Boot microservice. 

## Prerequisites
- Java 21
- Maven

## Building and Running

### 1. Build the project
To compile and package the project:
```bash
./mvnw clean package -DskipTests
```

### 2. Run Tests
The test suite utilizes **TestContainers** to boot up a real PostgreSQL instance and test against it. Ensure Docker is running locally.
```bash
./mvnw clean test
```

### 3. Start the Application
By default, the application runs on port `8080` using an embedded `H2` database for local development.

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

To run with PostgreSQL (Production Profile):
Set the environment variables before starting:
```bash
export DB_URL=jdbc:postgresql://localhost:5432/cifdb
export DB_USERNAME=postgres
export DB_PASSWORD=your_secure_password
./mvnw spring-boot:run
```

---

## API Endpoints

The API handles Customers, Accounts, and Transactions. It exposes secure DTOs and prevents legacy vulnerabilities like IDOR and DevMode exploits.

### Customers
- **Create Customer:** `POST /api/v1/customers`
- **Get Customer by ID:** `GET /api/v1/customers/{id}` (Include `Authorization-User-Id: {id}` header for IDOR protection).
- **Search Customers:** `GET /api/v1/customers/search?name={name}`
- **Update Customer:** `PUT /api/v1/customers/{id}`

### Accounts
- **Create Account:** `POST /api/v1/accounts`
- **Get Account:** `GET /api/v1/accounts/{id}`
- **Update Account Status:** `PATCH /api/v1/accounts/{id}/status`

### Transactions
- **Create Transaction (Deposit/Withdrawal):** `POST /api/v1/transactions`
  - Validates positive amounts.
  - Implements database-level pessimistic locking (`SELECT FOR UPDATE`) to prevent concurrent "lost update" bugs.
- **Get Transaction History:** `GET /api/v1/transactions/account/{accountId}`

## Security Improvements
- Removed Apache Struts 2 entirely (Addressing the DevMode RCE vulnerability).
- Extracted hardcoded database credentials into standard `application.yml` and environment variables.
- Handled Insecure Direct Object Reference (IDOR) with user context validation.
- Validated all negative transaction attempts with proper domain exceptions (`InsufficientFundsException`, `InvalidRequestException`).
- Added Cross-Site Scripting (XSS) escaping `<%- ... %>` to legacy Backbone frontend templates.
- Enforced unpredictable identifiers for CIF and Account numbers using secure generation.
