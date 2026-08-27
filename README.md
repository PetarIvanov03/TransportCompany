# Transport Company

A console-based Java application for managing a transport company's clients, employees, vehicle fleet, and transport orders, backed by a relational database via Hibernate.

## Features

- **Companies** — create, edit, delete; view by ID with its vehicles or its employees; list sorted by name or by revenue
- **Clients** — create, edit, delete, view by ID, list all
- **Vehicles** (Bus / Truck / Tanker) — create, edit, delete, view by ID, list all, with type-specific fields (seat capacity, max load, tank capacity + permitted cargo type)
- **Employees & Drivers** — create, edit, delete, view by ID with company; list sorted by salary; filter drivers by qualification (hazardous cargo, 12+ passenger, oversized load)
- **Transports** — create, edit, delete, view by ID with full details (client/vehicle/driver); search by destination or by date range; cargo type (people/goods) and payment status tracking
- **Reports** — total transport count, total revenue, transport count and revenue per driver, company revenue for a given period, and CSV export of transports (filtered by destination, date range, or all)

## Tech Stack

- Java 21
- Hibernate ORM 6.4.4.Final (JPA/Hibernate mapping, `hbm2ddl.auto=update`)
- Microsoft SQL Server (via `mssql-jdbc` 12.6.1.jre11)
- Hibernate Validator 8.0.1.Final (Jakarta Bean Validation) + Expressly 5.0.0
- Lombok 1.18.32
- SLF4J Simple 2.0.13
- JUnit Jupiter 5.10.2, Mockito 5.11.0, H2 2.2.224 (tests only)
- Maven (`maven-compiler-plugin` 3.13.0)

## Architecture

The app follows a layered design:

```
entity → dao → service → ui
```

- **entity** — JPA-annotated domain classes (`Vehicle`/`Employee` use `JOINED` inheritance for `Bus`/`Truck`/`Tanker` and `Driver`)
- **dao** — Hibernate `Session`-based data access (`GenericDAO` + per-entity interfaces/implementations)
- **service** — business logic, validation, and transaction boundaries; each service owns its `SessionFactory` and opens/closes `Session`s per call
- **ui** — console menus (`ConsoleApp` + one `*Menu` per domain area), driving the services
- **util** — cross-cutting concerns: `HibernateUtil` (session factory bootstrap from `db.properties`), `TransactionUtil` (commit/rollback wrapper around a `Session`-consuming action), `ValidationUtil` (Bean Validation entry point)

Session/transaction management lives at the service layer: read operations open a short-lived session, while writes go through `TransactionUtil.execute`, which commits on success and rolls back on any exception.

`hbm2ddl.auto=update` is used deliberately to keep schema management simple for this project's scope; it auto-creates/updates tables on startup instead of relying on migration scripts.

## Setup

1. **Clone the repository**
   ```
   git clone <repository-url>
   cd TransportCompany/TransportCompanyApp
   ```

2. **Configure the database connection**

   Copy the example properties file and fill in your SQL Server credentials:
   ```
   cp src/main/resources/db.properties.example src/main/resources/db.properties
   ```
   Edit `db.properties`:
   ```
   db.url=jdbc:sqlserver://HOST:PORT;databaseName=DB_NAME;encrypt=true;trustServerCertificate=true
   db.username=YOUR_USERNAME
   db.password=YOUR_PASSWORD
   ```
   The schema is created/updated automatically on startup (`hbm2ddl.auto=update`).

3. **Build**
   ```
   mvn clean install
   ```

4. **Run**

   Run `org.transport.ui.ConsoleApp` — this is the application's entry point and launches the interactive console menu (from your IDE, or on the classpath produced by Maven).

   `org.transport.Main` is a separate, minimal utility that only verifies the DB connection and schema sync; it does not launch the UI.

## Tests

```
mvn test
```

Unit tests (`TransportServiceTest`, `ValidationUtilTest`) use Mockito to isolate the service/validation logic. `TransportDAOIntegrationTest` runs against a real H2 in-memory database (`hibernate-test.cfg.xml`, `create-drop` schema) rather than mocks, exercising the DAO layer through actual Hibernate sessions.

## Data Model

Core entities: `TransportCompany`, `Client`, `Employee` (with `Driver` as a subtype holding qualifications), `Vehicle` (abstract, with `Bus`, `Truck`, `Tanker` subtypes), and `Transport`, which links a `Client`, `Vehicle`, and `Driver` for a single trip.

---

*Developed as a course project for CSCB525.*
