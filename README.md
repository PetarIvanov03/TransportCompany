# Transport Company

Console-based Java/Hibernate application for managing a transport company's clients, employees, vehicle fleet, and transport orders against a relational database.

![Build](https://github.com/PetarIvanov03/TransportCompany/actions/workflows/build.yml/badge.svg)
![Java](https://img.shields.io/badge/Java-21-orange)
![Hibernate](https://img.shields.io/badge/Hibernate_ORM-6.4.4.Final-59666C)
![Maven](https://img.shields.io/badge/Build-Maven-C71A36)
![SQL Server](https://img.shields.io/badge/DB-SQL_Server-CC2927)
![JUnit5](https://img.shields.io/badge/Tests-JUnit_5_%2B_Mockito-25A162)

## Architecture

```
entity → dao → service → ui
```

| Layer | Responsibility |
|---|---|
| `entity` | JPA-annotated domain classes. `Vehicle` and `Employee` use `JOINED` inheritance (`Vehicle` → `Bus`/`Truck`/`Tanker`, `Employee` → `Driver`). |
| `dao` | Hibernate `Session`-based data access: `GenericDAO<T, ID>` for generic CRUD, per-entity interfaces/implementations for custom JPQL queries (fetch-joins, filters, sorts). |
| `service` | Business logic, Bean Validation calls, and transaction boundaries. |
| `ui` | Console menus — `ConsoleApp` dispatches to one `*Menu` class per domain area. |
| `util` | `HibernateUtil` (`SessionFactory` bootstrap from `db.properties`), `TransactionUtil` (commit/rollback wrapper), `ValidationUtil` (Bean Validation entry point). |

Session/transaction management lives at the service layer. Read methods open a short-lived `Session` per call (e.g. `ClientService.getById`, `ClientService.java:32-36`). Writes go through `TransactionUtil.execute(sessionFactory, action)` (`util/TransactionUtil.java:14-27`), which opens a session, begins a transaction, commits on success, and rolls back on any exception.

Because each session closes right after its query, DAO methods that need related entities use explicit fetch-joins (`CompanyDAOImpl.findByIdWithVehicles`, `EmployeeDAOImpl.findByIdWithCompany`, `TransportDAOImpl.findByIdWithDetails`), and the UI layer keeps separate "summary" vs "detail" print methods to match what was actually loaded — touching a lazy association outside its session throws `LazyInitializationException`, and this is called out inline at the print methods (e.g. `TransportMenu.java:204-221`, `VehicleMenu.java:183-186`).

## Design decisions

**1. Session-per-call, no Open-Session-in-View**
Every service method opens and closes its own `Session`; there's no shared session spanning a whole UI interaction. This fits a console app — there's no request lifecycle to hang a session on, and each method's data lifetime is explicit. The cost is the fetch-join/summary-vs-detail split described above. A web app would more likely use Spring's declarative `@Transactional` with OSIV, or explicit DTO projections instead of returning entities at all.

**2. `JOINED` inheritance for `Vehicle` and `Employee`**
`Bus`/`Truck`/`Tanker` and `Employee`/`Driver` have genuinely different required columns (`seatCapacity` vs `maxLoadKg` vs `capacityLiters` + `permittedCargoType`, `entity/Bus.java:20-23`, `entity/Truck.java:22-25`, `entity/Tanker.java:25-33`). `JOINED` (`Vehicle.java:23`, `Employee.java:17`) keeps each subtype's own columns `NOT NULL` in its own table instead of collapsing everything into one table with nullable columns per subtype (`SINGLE_TABLE`). The trade-off is an extra join on every load of a concrete subtype.

**3. `hbm2ddl.auto=update`, no migration tool**
`hibernate.cfg.xml:19` lets Hibernate create/alter tables from the entity mappings on startup. It fits a single-developer, iterating-fast project scope. It has no schema history, no reviewable diffs, and no safe rollback — a team or production setting would need Flyway or Liquibase instead.

**4. Constructor-injected `SessionFactory`/DAO only in `TransportService`**
`TransportService` has both a no-arg constructor (wires the real `HibernateUtil.getSessionFactory()` and `TransportDAOImpl`) and a second constructor taking `SessionFactory`/`TransportDAO` directly (`service/TransportService.java:22-29`), which is what lets `TransportServiceTest` mock both (`TransportServiceTest.java:28-48`). `ClientService`, `CompanyService`, `EmployeeService`, and `VehicleService` all instead `new` their DAO field and call the static `HibernateUtil.getSessionFactory()` directly — none of them can be unit-tested with mocks. This is an inconsistency across the service layer, not a deliberate pattern, and it's the reason only one service has a unit test.

**5. Placeholder-entity foreign keys in the UI layer**
Given just an ID typed at the console, `TransportMenu.idReference` builds a bare entity (e.g. `new Truck()` regardless of the vehicle's actual subtype) with only the ID set, and hands it to the service (`TransportMenu.java:226-235`). Because these associations carry no cascade, Hibernate reads only the ID off the object to populate the foreign-key column. This avoids a round trip to load the full entity before every association, but it also means a typo'd ID isn't caught upfront — it surfaces as a foreign-key constraint violation at commit time instead of a validation message.

## Testing

```
mvn test
```

| Test class | Level | What it covers |
|---|---|---|
| `TransportDAOIntegrationTest` | Integration | Persists a `TransportCompany`/`Bus`/`Client`/`Driver`/`Transport` graph against a real in-memory H2 database (`create-drop` schema, `hibernate-test.cfg.xml`) inside an actual transaction, then asserts `TransportDAOImpl.findByDestination` returns the correct row through a real Hibernate session and JPQL query — no mocks. |
| `TransportServiceTest` | Unit (Mockito) | Mocks `TransportDAO`/`SessionFactory`/`Session`/`Transaction` and tests `TransportService`'s validation rules in isolation: `GOODS` cargo without a weight throws, `PEOPLE` cargo with a weight throws, arrival not after departure throws, and valid input reaches `transportDAO.save`. |
| `ValidationUtilTest` | Unit | Asserts `ValidationUtil.validate` throws `IllegalArgumentException` for a `Client` with a blank name (`@NotBlank` via Bean Validation). |

`TestHibernateUtil` is test support, not a test — it builds the H2 `SessionFactory` used by `TransportDAOIntegrationTest`.

## Features

- **Companies** — create, edit, delete; view by ID with its vehicles or its employees; list sorted by name or by revenue
- **Clients** — create, edit, delete, view by ID, list all
- **Vehicles** (Bus / Truck / Tanker) — create, edit, delete, view by ID, list all, with type-specific fields (seat capacity, max load, tank capacity + permitted cargo type)
- **Employees & Drivers** — create, edit, delete, view by ID with company; list sorted by salary; filter drivers by qualification (hazardous cargo, 12+ passenger, oversized load)
- **Transports** — create, edit, delete, view by ID with full details (client/vehicle/driver); search by destination or by date range; cargo type (people/goods) and payment status tracking
- **Reports** — total transport count, total revenue, transport count and revenue per driver, company revenue for a given period, and CSV export of transports (filtered by destination, date range, or all)

## Data model

- `TransportCompany` — has many `Employee` and `Vehicle` (`cascade = PERSIST` only, `TransportCompany.java:32-36`)
- `Employee` — belongs to a `TransportCompany`; `Driver` extends `Employee` with a `Set<DriverQualification>` (`@ElementCollection`, table `driver_qualifications`)
- `Vehicle` (abstract) — belongs to a `TransportCompany`; subtypes `Bus`, `Truck`, `Tanker`
- `Client` — has many `Transport`
- `Transport` — a single trip, `@ManyToOne` to `Client`, `Vehicle`, and `Driver` (all required, `Transport.java:59-69`)

## Tech stack

| Component | Version |
|---|---|
| Java | 21 |
| Hibernate ORM | 6.4.4.Final |
| Microsoft SQL Server JDBC driver (`mssql-jdbc`) | 12.6.1.jre11 |
| Hibernate Validator (Jakarta Bean Validation) | 8.0.1.Final |
| Expressly | 5.0.0 |
| Lombok | 1.18.32 |
| SLF4J Simple | 2.0.13 |
| JUnit Jupiter | 5.10.2 |
| Mockito (`mockito-junit-jupiter`) | 5.11.0 |
| H2 (test only) | 2.2.224 |
| maven-compiler-plugin | 3.13.0 |

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

   Run `org.transport.ui.ConsoleApp` — the application's entry point, launches the interactive console menu.

   `org.transport.Main` is a separate, minimal utility that only verifies the DB connection and schema sync; it does not launch the UI.
