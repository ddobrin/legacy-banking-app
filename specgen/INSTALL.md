# Migrating the Legacy Banking CIF Application

This document provides instructions for an AI coding agent (or a human developer) to migrate the legacy Struts 2 banking application to a modern Spring Boot microservice. 

## Agent Prompt

Copy and paste the following prompt into your coding agent (e.g., Gemini, Claude, Cursor):

```text
You are an expert Java developer migrating a legacy Struts 2 banking application to a modern, secure Spring Boot 4 microservice.

1. **Analyze the Requirements & Constraints:**
   - Read `migrate/Techstack.md` for the correct technology versions (Java 21, Spring Boot 4.0.2).
   - Read `specgen/SPEC.md` to understand the architecture, API endpoints, and design principles for the migration.
   - Review `migrate/Architecture_Overview.md` and `migrate/SECURITY_REPORT.md` to deeply understand the critical flaws in the legacy system.
   - Read `specgen/test.yaml` to see the API test scenarios your new implementation must pass.

2. **Setup the Project:**
   - Initialize a Spring Boot 4.0.2 project.
   - Configure dependencies for Web, Data JPA, PostgreSQL Driver, Validation, and TestContainers.
   - Explicitly avoid Struts 2 (remove all `struts.xml` references, fixing the devMode RCE vulnerability).
   - Use Jackson 3 for JSON processing.

3. **Implement the Data Layer:**
   - The PostgreSQL schema must map to the tables defined in `migrate/legacy_schema.sql` (`customers`, `accounts`, `products`, `transactions`).
   - Note that primary keys are `SERIAL` integers, not UUIDs.
   - Ensure you implement pessimistic locking (`@Lock(LockModeType.PESSIMISTIC_WRITE)`) in the Account Repository when processing transactions to fix the critical "Lost Update" concurrency bug.

4. **Refactor and Secure Business Logic (Services):**
   - Create Service classes (`CustomerService`, `AccountService`, `TransactionService`).
   - Move all business logic out of the legacy controllers into these services.
   - Validate that transaction amounts are strictly positive (`amount > 0`) to prevent unauthorized fund manipulation.
   - Ensure the database connection pooling is handled efficiently by Spring Boot's default HikariCP, resolving the legacy `DriverManager` bottleneck.
   - Remove hardcoded database credentials (found previously in `DBConnection.java`). Use `application.yml` or `application-dev.yml` with environment variable fallbacks.

5. **Implement the API Layer (Controllers):**
   - Create REST Controllers matching the endpoints in `specgen/SPEC.md`.
   - Separate the Customer ID lookup (`/{id}`) from the Customer name search (`/search?name=...`) to fix the overloaded endpoint bug.
   - Use DTOs for request/response bodies. Do not expose JPA Entities directly.
   - Implement `@ControllerAdvice` to provide standardized error JSON responses instead of generic exceptions.
   - Ensure endpoints are secured against Insecure Direct Object Reference (IDOR) by adding authentication/authorization contexts (e.g., validating the current user owns the account).

6. **Address Predictable Identifiers:**
   - Ensure `cifNumber` (Customer) and `accountNumber` (Account) generation uses `SecureRandom`, UUIDs, or database sequences, resolving the predictable `currentTimeMillis` and `java.util.Random` vulnerability.

7. **Frontend XSS Mitigation (Optional/If Applicable):**
   - If migrating the frontend templates alongside the API, ensure all Underscore.js template interpolations are changed from `<%= variable %>` to `<%- variable %>` to prevent Stored and Reflected XSS. (If this is a backend-only microservice, ensure output JSON is clean and strictly `application/json`).

8. **Testing:**
   - Create integration tests (using `@SpringBootTest`, `MockMvc`, and `TestContainers` for PostgreSQL) that verify the scenarios in `specgen/test.yaml`.
   - Specifically test for concurrency issues, negative transaction amounts, and IDOR prevention.

9. **Documentation:**
   - Generate a `usage.md` describing how to run the new Spring Boot app and call the new APIs.
```

## Manual Installation

If you are performing this migration manually:

1.  Initialize a new Java 21 / Spring Boot 4.0.2 project.
2.  Set up a local PostgreSQL database and execute `migrate/legacy_schema.sql` to create the schema.
3.  Implement the application code by referring to `specgen/SPEC.md`.
4.  Actively review `migrate/Architecture_Overview.md` and `migrate/SECURITY_REPORT.md` to ensure you do not port over legacy bugs (like missing locking on transactions, generic exception handling, missing input validation, or XSS).
5.  Run integration tests validating the behavior detailed in `specgen/test.yaml`.
