# Modernized Banking CIF Application

## Project Overview
This project is a modernized Customer Information File (CIF) system for a banking application. It provides a RESTful API for managing customers, accounts, and transactions, with a web-based frontend. The backend has been completely migrated from a legacy Struts 2 architecture to a modern Spring Boot microservice.

### Technologies
- **Backend:** Java 21, Maven
- **Framework:** Spring Boot 3.2.3 (Spring Web, Spring Data JPA, Spring Validation)
- **Database:** PostgreSQL (production), H2 (embedded/testing)
- **Frontend:** Backbone.js, jQuery, Underscore.js (Legacy UI preserved)
- **Testing:** JUnit 5, Testcontainers

## Building and Running

### Build
To compile the project:
```bash
./mvnw compile
```

### Test
To run the unit and integration tests (requires Docker for Testcontainers):
```bash
./mvnw test
```

### Run
To start the application locally:
```bash
./mvnw spring-boot:run
```
The application will be available at `http://localhost:8080/`.

## Development Conventions

### Architecture
- **Controllers:** Located in `src/main/java/com/banking/cif/controller/`. These classes use Spring `@RestController` and `@RequestMapping` to expose API endpoints.
- **Service Layer:** Located in `src/main/java/com/banking/cif/service/`. Contains business logic and transactional boundaries (e.g., `@Transactional` methods in `CustomerService`).
- **Data Access:** Located in `src/main/java/com/banking/cif/repository/`. Uses Spring Data JPA interfaces (e.g., `CustomerRepository`) for database interactions, replacing legacy JDBC logic.
- **Data Transfer Objects (DTOs):** DTOs (like `CustomerDTO`) and request objects are defined in `com.banking.cif.dto/` to ensure safe API data bounding and separate database logic from API responses.

### API Design
- Endpoints follow RESTful conventions (e.g., `GET /api/v1/customers/{id}`).
- JSON is the default exchange format.
- Validation is handled automatically via Spring Boot Validation and `@Valid` annotations on `@RequestBody`.

### Frontend
- The application is a Single Page Application (SPA).
- Backbone.js is used for routing, models, and views.
- Templates are defined in `src/main/webapp/index.html` using Underscore.js syntax.
- Static assets are located in `src/main/webapp/css/`, `js/`, and `images/`.

### Database
- Persistence relies on JPA/Hibernate.
- Handled through `application.yml` profiles: `validate` for production (PostgreSQL), and `create-drop` with H2 for the `dev` profile.
