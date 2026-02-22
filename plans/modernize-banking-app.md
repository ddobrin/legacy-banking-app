# Implementation Plan - Modernize Legacy Banking App

## 1. 🔍 Analysis & Context
*   **Objective:** Modernize the legacy banking app by upgrading to Spring Boot 4.0.2, migrating the frontend to Vaadin 25, and adopting a modern look and feel using Vaadin's default Aura theme.
*   **Affected Files:** 
    *   `pom.xml` (Dependencies and plugins update)
    *   `src/main/webapp/*` (Legacy Backbone.js UI to be removed)
    *   `src/main/java/com/banking/cif/ui/*` (New Vaadin Flow Java views to be created)
*   **Key Dependencies:** `spring-boot-starter-parent` (4.0.2), `vaadin-spring-boot-starter` (25.x).
*   **Risks/Unknowns:** 
    *   Potential changes in Spring Boot 4.0.x autoconfiguration and compatibility.
    *   The frontend requires a complete rewrite from Backbone.js to Vaadin Java Components (Flow).
    *   Existing REST controllers (`@RestController`) will be preserved for API clients, but the Vaadin UI will directly consume the underlying `@Service` layer for performance and type safety.

## 2. 📋 Checklist
- [x] Step 1: Update Build Dependencies & Spring Boot
- [x] Step 2: Set Up Vaadin 25 Framework
- [x] Step 3: Scaffold Vaadin Main Layout & Theme (Aura)
- [x] Step 4: Implement Customer Views (List, Detail) in Vaadin
- [x] Step 5: Implement Account & Transaction Views in Vaadin
- [x] Step 6: Remove Legacy Webapp Resources
- [ ] Verification

## 3. 📝 Step-by-Step Implementation Details

### Step 1: Update Build Dependencies & Spring Boot
*   **Goal:** Upgrade the project baseline to Spring Boot 4.0.2 and ensure Java compatibility.
*   **Action:**
    *   Modify `pom.xml`: Change `<parent><version>` from `3.2.3` to `4.0.2`.
    *   Ensure `<java.version>` remains `21` (already set and required by Vaadin 25).
*   **Verification:** Run `./mvnw clean compile` and resolve any compile errors related to Spring 4.x API changes.

### Step 2: Set Up Vaadin 25 Framework
*   **Goal:** Introduce Vaadin 25 to the Spring Boot project.
*   **Action:**
    *   Modify `pom.xml`: Add `<dependencyManagement>` for `vaadin-bom` version `25.0.0` (or latest available 25.x).
    *   Add dependency: `vaadin-spring-boot-starter` in the `<dependencies>` block.
    *   Add plugin: `vaadin-maven-plugin` in the `<build><plugins>` block to handle frontend optimization.
*   **Verification:** Run `./mvnw compile` to ensure Vaadin dependencies download correctly.

### Step 3: Scaffold Vaadin Main Layout & Theme (Aura)
*   **Goal:** Create the main application shell using the Vaadin 25 Aura theme.
*   **Action:**
    *   Create `src/main/java/com/banking/cif/ui/MainLayout.java` extending Vaadin's `AppLayout`.
    *   Configure the application header and side navigation using Vaadin's `SideNav` component.
    *   The application will automatically use the modern `Aura` theme (default in Vaadin 25).
*   **Verification:** Run the application (`./mvnw spring-boot:run`), navigate to `http://localhost:8080/`, and verify the AppLayout renders correctly with the new styling.

### Step 4: Implement Customer Views (List, Detail) in Vaadin
*   **Goal:** Replace the Backbone.js customer views with server-side Vaadin Java components.
*   **Action:**
    *   Create `src/main/java/com/banking/cif/ui/view/CustomerListView.java` annotated with `@Route(value = "customers", layout = MainLayout.class)`. Use Vaadin `Grid` to display `CustomerDTO` lists fetched directly from `CustomerService`.
    *   Create `src/main/java/com/banking/cif/ui/view/CustomerFormView.java` for creating/editing customers using Vaadin `FormLayout` and `Binder` for validation.
*   **Verification:** Test CRUD operations for Customers entirely through the new Vaadin interface.

### Step 5: Implement Account & Transaction Views in Vaadin
*   **Goal:** Replace the Backbone.js account and transaction interfaces.
*   **Action:**
    *   Create `src/main/java/com/banking/cif/ui/view/AccountListView.java` using Vaadin `Grid`.
    *   Create `src/main/java/com/banking/cif/ui/view/TransactionListView.java` to display recent transactions.
    *   Link these views to the side navigation in `MainLayout`.
*   **Verification:** Ensure users can navigate to and interact with Accounts and Transactions data.

### Step 6: Remove Legacy Webapp Resources
*   **Goal:** Clean up the legacy Backbone.js application.
*   **Action:**
    *   Delete the entire `src/main/webapp/` directory (including `app.js`, `index.html`, `css/`, `images/`, and JS libraries).
*   **Verification:** Run the application and confirm the legacy UI is gone and the Vaadin UI successfully serves from the root URL.

## 4. 🧪 Testing Strategy
*   **Unit Tests:** Verify that `CustomerService`, `AccountService`, and `TransactionService` business logic remains completely untouched and passing.
*   **Integration Tests:** Verify that Vaadin views correctly bind to the backend services. Use standard Spring `@SpringBootTest` to verify application context loads correctly.
*   **Manual Verification:** Launch the application, navigate through all views, create a new customer, view their account, perform a transaction, and ensure the Aura theme provides a responsive and accessible experience across various screen sizes.

## 5. ✅ Success Criteria
*   The application builds and starts successfully with Spring Boot 4.0.2.
*   The Vaadin 25 AppLayout loads as the default UI, completely replacing the legacy `index.html`.
*   The modern `Aura` theme is visibly applied and responsive.
*   All legacy webapp files (`src/main/webapp`) are removed from the project.
*   Users can seamlessly manage customers, accounts, and transactions via the new Vaadin Flow UI.