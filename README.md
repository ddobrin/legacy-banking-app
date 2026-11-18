# Legacy Banking CIF Application (Modernized)

A modernized enterprise Customer Information File (CIF), Account, and Transaction banking application built on **Java 21**, **Spring Boot 4.0.2**, **Jackson 3**, **Spring Data JPA**, and **Vaadin 25 Hilla**.

## Architecture & Tech Stack

- **Language:** Java 21
- **Backend:** Spring Boot 4.0.2 (modular starters: `spring-boot-starter-webmvc`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`, `spring-boot-starter-jackson`)
- **Frontend:** Vaadin 25.0.7 + Hilla (Material Design UI)
- **JSON Processing:** Jackson 3 (`tools.jackson.*`)
- **Database & Profiles:**
  - `dev` (default): In-memory H2 database (`MODE=PostgreSQL`) with `legacy_schema.sql` and idempotent `data.sql` seed data, plus H2 Console at `/h2-console`
  - `test`: PostgreSQL via Testcontainers `1.20.4` (`PostgreSQLContainer`) initialized with `legacy_schema.sql` and `data.sql`
  - `prod`: External PostgreSQL database initialized with `legacy_schema.sql` and `data.sql`

## Prerequisites

- **Java 21** (e.g., JDK 21)
- **Maven Wrapper** (`./mvnw` included in repository)
- **Node.js** (automatically managed or used by Vaadin 25 during frontend build)
- **Docker** (or Docker-compatible container runtime such as Podman with Docker socket) for running the `test` profile Testcontainers PostgreSQL suite

---

## Local Development & Testing (`dev` Profile with H2)

The `dev` profile is active by default (`spring.profiles.default: dev`). It starts an in-memory H2 database configured in PostgreSQL compatibility mode, applies `src/main/resources/legacy_schema.sql`, and loads seed data from `src/main/resources/data.sql`.

### 1. Start the Application Locally

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Once started:
- **Web UI:** `http://localhost:8080`
- **H2 Console:** `http://localhost:8080/h2-console`
  - JDBC URL: `jdbc:h2:mem:bankingdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false`
  - Username: `sa`
  - Password: *(empty)*

### 2. Smoke-Test the REST API Locally

Pre-seeded data includes 2 products (`CHK-STD`, `SAV-HYS`), 3 customers (`John Smith`, `Jane Doe`, `Frodo Baggins`), 9 accounts, and 90 transactions.

```bash
# List active banking products
curl -s http://localhost:8080/api/v1/products

# List all customers with account counts
curl -s http://localhost:8080/api/v1/customers

# Search customers by name or fetch by ID
curl -s "http://localhost:8080/api/v1/customers/search?name=Smith"
curl -s http://localhost:8080/api/v1/customers/1

# Create a new customer
curl -s -X POST http://localhost:8080/api/v1/customers \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Alice",
    "lastName": "Wonder",
    "email": "alice.wonder@example.com",
    "dateOfBirth": "1990-05-15"
  }'

# Open an account for customer 1
curl -s -X POST http://localhost:8080/api/v1/accounts \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "productCode": "CHK-STD",
    "balance": 250.00
  }'

# Post a deposit, withdrawal, or transfer
curl -s -X POST http://localhost:8080/api/v1/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "accountId": 1,
    "transactionType": "DEPOSIT",
    "amount": 100.00,
    "description": "Local smoke test deposit"
  }'

# Fetch account transactions (newest first)
curl -s http://localhost:8080/api/v1/transactions/account/1
```

### 3. Run Unit, Slice, and H2 Integration Tests

```bash
./mvnw test
```

This executes:
- `BankingServiceTest` — unit tests for customer, account, transaction (including transfer and validation), and product services
- `BankingControllerWebMvcTest` — `@WebMvcTest` controller, Jackson 3 serialization, Bean Validation, and `GlobalExceptionHandler` slice tests
- `BankingRepositoryTest` — `@DataJpaTest` repository queries against H2 (`dev` profile)
- `BankingApiTest` — `@SpringBootTest` + `MockMvc` full-stack integration tests against H2 (`dev` profile), including concurrent deposit locking verification

---

## PostgreSQL Integration Testing (`test` Profile with Testcontainers 1.20.4)

`PostgresContainerIntegrationTest` runs full-stack `@SpringBootTest` + `MockMvc` tests under `@ActiveProfiles("test")` against an ephemeral PostgreSQL 16 container managed by **Testcontainers 1.20.4** (`@ServiceConnection`).

### Run with Docker Available

Ensure Docker (or a Docker-compatible daemon) is running, then run:

```bash
# Run the full test suite including Testcontainers PostgreSQL tests
./mvnw test

# Or run only the PostgreSQL Testcontainers integration suite
./mvnw test -Dtest=PostgresContainerIntegrationTest
```

> **Note:** `PostgresContainerIntegrationTest` is annotated with `@Testcontainers(disabledWithoutDocker = true)`. When Docker is unavailable, it is cleanly skipped so local unit/H2 test runs do not fail.

If using Podman on macOS instead of Docker Desktop, start a Podman machine and export `DOCKER_HOST` before running Maven:

```bash
podman machine start
export DOCKER_HOST="unix://$(podman machine inspect --format '{{.ConnectionInfo.PodmanSocket.Path}}')"
./mvnw test -Dtest=PostgresContainerIntegrationTest
```

---

## Cloud Deployment & Production Testing (`prod` Profile with PostgreSQL)

In production (`prod` profile), the application connects to PostgreSQL using `src/main/resources/application-prod.yml` and automatically ensures the schema (`legacy_schema.sql`) and initial seed data (`data.sql`, idempotent) are applied.

### 1. Environment Variables (`prod` Profile)

| Variable | Description | Default |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Active Spring profile (set to `prod`) | `dev` |
| `SERVER_PORT` | HTTP listening port | `8080` |
| `DB_URL` | Full JDBC PostgreSQL URL (overrides host/port/name) | `jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:bankingdb}` |
| `DB_HOST` | PostgreSQL hostname (used if `DB_URL` is unset) | `localhost` |
| `DB_PORT` | PostgreSQL port (used if `DB_URL` is unset) | `5432` |
| `DB_NAME` | PostgreSQL database name (used if `DB_URL` is unset) | `bankingdb` |
| `DB_USERNAME` | PostgreSQL username | `postgres` |
| `DB_PASSWORD` | PostgreSQL password | *(empty)* |

### 2. Build a Production Package Locally

Use the Maven `production` profile to compile the Vaadin 25 Hilla frontend production bundle and package the executable JAR:

```bash
./mvnw clean package -Pproduction -DskipTests
```

Run the packaged JAR against a PostgreSQL database:

```bash
SPRING_PROFILES_ACTIVE=prod \
DB_URL="jdbc:postgresql://<host>:5432/<db_name>" \
DB_USERNAME="<username>" \
DB_PASSWORD="<password>" \
java -jar target/banking-app.jar
```

### 3. Deploy to Google Cloud Run

Ensure your PostgreSQL instance (e.g., Cloud SQL for PostgreSQL) is reachable from Cloud Run and deploy with `SPRING_PROFILES_ACTIVE=prod` and `GOOGLE_BUILD_ARGS="-Pproduction"`:

```bash
gcloud run deploy legacy-banking-app \
  --source . \
  --project <your-gcp-project> \
  --region us-central1 \
  --set-env-vars SPRING_PROFILES_ACTIVE=prod,SERVER_PORT=8080,DB_URL="jdbc:postgresql://<db-host>:5432/<db-name>",DB_USERNAME="<db-user>",DB_PASSWORD="<db-password>" \
  --set-build-env-vars GOOGLE_BUILD_ARGS="-Pproduction" \
  --memory 2Gi \
  --cpu 2 \
  --min-instances 0 \
  --max-instances 1 \
  --allow-unauthenticated
```

### 4. Verify the Deployed Cloud Instance

After deployment, verify both the REST API and Vaadin UI against the Cloud Run service URL:

```bash
SERVICE_URL=$(gcloud run services describe legacy-banking-app \
  --project <your-gcp-project> \
  --region us-central1 \
  --format='value(status.url)')

# Verify products and seeded customers from PostgreSQL (legacy_schema.sql + data.sql)
curl -fsS "${SERVICE_URL}/api/v1/products"
curl -fsS "${SERVICE_URL}/api/v1/customers"
curl -fsS "${SERVICE_URL}/api/v1/accounts/1"

# Verify a write/read round-trip against PostgreSQL
curl -fsS -X POST "${SERVICE_URL}/api/v1/customers" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Cloud",
    "lastName": "Verifier",
    "email": "cloud.verifier@example.com",
    "dateOfBirth": "1991-07-20"
  }'
```

Open `${SERVICE_URL}` in a browser to confirm the Vaadin 25 Hilla Material Design UI loads and communicates with `/api/v1/*`.
