# Legacy Banking CIF Application - Architectural Analysis

## 1. System Overview
The Legacy Banking CIF Application is a Single Page Application (SPA) driven by a **Backbone.js** frontend and a **Java 8** backend utilizing the **Struts 2 framework** (REST, Convention, and JSON plugins). It interacts with a PostgreSQL/H2 database via raw JDBC Data Access Objects (DAOs).

## 2. Component Breakdown

### A. Frontend Layer (Backbone.js & jQuery)
* **Location:** `src/main/webapp/js/app.js`
* **Role:** Manages the client-side state, UI rendering, and routing. 
* **Key Patterns:** MVC (Model-View-Collection) via Backbone, RESTful communication with the backend APIs.

### B. API / Controller Layer (Struts 2 REST)
* **Location:** `src/main/java/com/banking/cif/action/` (e.g., `AccountsController.java`)
* **Role:** Intercepts HTTP requests, validates input, and maps them to Service/DAO layers. Uses Struts 2 `ModelDriven` approach to serialize/deserialize JSON.

### C. Service / Business Logic Layer
* **Location:** `src/main/java/com/banking/cif/service/BankingService.java`
* **Role:** Contains core business rules (e.g., transaction processing). Note: Some business logic is currently leaking into the controllers (e.g., `AccountsController.create()`).

### D. Data Access Layer (JDBC DAOs)
* **Location:** `src/main/java/com/banking/cif/dao/`
* **Role:** Raw JDBC implementations wrapping SQL queries for Models like `Customer`, `Account`, and `Transaction`. No ORM (like Hibernate) is used.

## 3. Data Flow Example (View Account)
1. User clicks an account link -> `app.js` Router catches `accounts/:id`.
2. Backbone `Account` model invokes a `fetch()` (GET `/api/v1/accounts/{id}`).
3. Struts 2 intercepts and routes to `AccountsController.show()`.
4. Controller calls `BankingService.getAccount(id)` or `AccountDAO.findById(id)`.
5. Java model is returned, serialized to JSON by Struts, and Backbone renders `AccountView`.

## 4. In-Depth Component Analysis

### Area 1: Customer Management Module
**Scope:** Customer data model, CRUD operations, validation logic.
**Focus:** Data integrity, access control, API consistency.

**Findings:**
1. **API Consistency (The ID/Name Overload):** In `CustomersController.java` (`show()` method), the endpoint `/api/v1/customers/{id}` is overloaded. If it fails to parse `{id}` as an Integer, it falls back to querying by customer name. This violates REST API conventions; a name search should explicitly use query parameters (e.g., `/customers?name=xyz`).
2. **Business Logic Leakage:** Core validation and logic, such as checking if an email exists and generating a new `CIFNumber`, are hardcoded directly into the `CustomersController.create()` method rather than being delegated to the `BankingService`.
3. **Layer Bypassing:** The `CustomersController.index()` method bypasses the service layer entirely, making a direct call to `customerDAO.findAllWithAccountCount()`.

### Area 2: Account Services Layer
**Scope:** Account creation, transaction processing, balance management.
**Focus:** Business logic, transaction atomicity, error handling.

**Findings:**
1. **Transactional Integrity:** The `BankingService.processTransaction()` method correctly handles atomicity using manual JDBC connection controls (`conn.setAutoCommit(false)`, `conn.commit()`, and `conn.rollback()`). This ensures a transaction is completely successfully or fully reverted on error.
2. **Concurrency / Race Condition Risks:** Balance calculations are performed in-memory inside Java (`newBalance.subtract(transaction.getAmount())`) before updating the database. In a highly concurrent environment, this approach can lead to "lost updates." A safer approach would be utilizing row-level database locking (e.g., `SELECT ... FOR UPDATE`) or letting the database compute the balance (`UPDATE accounts SET balance = balance - ?`).
3. **Generic Exception Handling:** The service layer throws generic `Exception` types instead of defining domain-specific exceptions (like `InsufficientFundsException` or `AccountNotFoundException`), making error parsing brittle.

### Area 3: Data Access Layer (DAO)
**Scope:** Database interactions, query optimization, connection management.
**Focus:** Performance, security, resource utilization.

**Findings:**
1. **Lack of Connection Pooling:** `DBConnection.java` utilizes `DriverManager.getConnection()` to open a fresh database connection on every request. This lacks a connection pool (like HikariCP or C3P0), which will severely degrade performance under production load.
2. **Security & Syntax Practices:** The DAOs (like `CustomerDAO.java`) consistently use `PreparedStatement` to bind parameters, successfully mitigating SQL Injection risks. They also correctly utilize Java's *try-with-resources* for proper memory cleanup.
3. **Query Optimization Bottlenecks:**
   * `CustomerDAO.findAllWithAccountCount()` uses a subquery `(SELECT COUNT(*) FROM accounts a WHERE a.customer_id = c.customer_id)` in the SELECT clause. A `LEFT JOIN` combined with `GROUP BY` would likely perform better on large datasets.
   * `CustomerDAO.findByName()` relies on `LOWER(first_name) LIKE LOWER(?)`, which prevents the database from using standard indexes and results in full table scans unless specific functional/expression indexes are created in PostgreSQL.
