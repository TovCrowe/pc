# Policy Management API

A RESTful API built with Spring Boot for managing insurance clients and their policies. This project is a practical reference for learning the core patterns of a Spring Boot application: layered architecture, JPA, dependency injection, and REST controllers.

---

## Tech Stack

| Dependency | What it does |
|---|---|
| **Spring Boot 4.0.6** | The framework. Auto-configures everything and runs the app via an embedded Tomcat server — no WAR deployment needed. |
| **Spring Web** | Provides `@RestController`, `@GetMapping`, etc. to build REST endpoints. |
| **Spring Data JPA** | Abstracts database access. You define an interface and Spring generates the SQL queries for you. |
| **Spring Security** | Adds authentication/authorization. Currently included but not customized — all endpoints require HTTP Basic Auth by default. |
| **Spring Validation** | Lets you annotate entity fields with rules like `@NotNull`, `@Size`, `@Email`, etc. |
| **Lombok** | Generates boilerplate Java (getters, setters, constructors) at compile time via annotations. |
| **H2** | An in-memory database used in development. Data is lost when the app stops. |
| **PostgreSQL** | The production database. |

---

## Project Structure

```
src/main/java/com/pc/pc/
│
├── PcApplication.java          # Entry point — contains main(), starts Spring Boot
│
├── entity/                     # JPA Entities — map directly to database tables
│   ├── Client.java
│   └── Policy.java
│
├── enums/
│   └── Status.java             # Enum for policy status values
│
├── repository/                 # Data access layer — interfaces that talk to the DB
│   ├── ClientRepository.java
│   └── PolicyRepository.java
│
├── service/                    # Business logic layer
│   ├── ClientService.java
│   └── PolicyService.java
│
└── controller/                 # HTTP layer — receives requests, returns responses
    ├── ClientController.java
    └── PolicyController.java
```

### How the layers connect

```
HTTP Request
    ↓
Controller   →   receives the request, calls the service
    ↓
Service      →   contains business logic, calls the repository
    ↓
Repository   →   talks to the database
    ↓
Entity       →   represents a single row in a database table
```

Each layer only knows about the one directly below it. This keeps concerns separated and the code easier to test and maintain.

---

## Data Model

### Client — `clients` table

| Field | Type | Description |
|---|---|---|
| `id` | Long | Auto-generated primary key |
| `name` | String | First name |
| `lastName` | String | Last name |
| `email` | String | Contact email |
| `phone` | String | Phone number |
| `policies` | Set\<Policy\> | Policies owned by this client |

### Policy — `policies` table

| Field | Type | Description |
|---|---|---|
| `id` | Long | Auto-generated primary key |
| `type` | String | Type of insurance (e.g., "life", "auto") |
| `status` | Status | Current state: `ACTIVE`, `INACTIVE`, `PENDING`, or `CANCELLED` |
| `policyNumber` | String | Unique policy identifier |
| `premium` | Double | Monthly/annual cost |
| `startDate` | Date | Coverage start date |
| `endDate` | Date | Coverage end date |
| `client` | Client | The client this policy belongs to (FK: `client_id`) |

### Relationship

`Client` and `Policy` have a **one-to-many** relationship: one client can have many policies.

```
Client (1) ──────── (N) Policy
```

- In `Client.java`: `@OneToMany(mappedBy = "client")` — "I have many policies; the `client` field in Policy owns the foreign key column."
- In `Policy.java`: `@ManyToOne` + `@JoinColumn(name = "client_id")` — "I belong to one client and I store the foreign key as `client_id`."

---

## Spring Concepts Explained

### Dependency Injection

Spring manages object creation. Instead of writing `new ClientService()`, you declare a dependency and Spring injects it automatically:

```java
@RequiredArgsConstructor       // Lombok: generates a constructor for all final fields
public class ClientController {
    private final ClientService clientService;  // Spring injects this at startup
}
```

Spring builds a graph of all beans (objects it manages) and wires them together. You never call `new` on services or repositories.

### Entities and JPA Annotations

```java
@Entity                         // Marks this class as a JPA entity (maps to a DB table)
@Table(name = "clients")        // Sets the table name explicitly
@Id                             // Marks the primary key field
@GeneratedValue(strategy = GenerationType.IDENTITY)  // Auto-increment PK (DB-managed)
@OneToMany(mappedBy = "client") // One client → many policies
@ManyToOne                      // Many policies → one client
@JoinColumn(name = "client_id") // The FK column name in the policies table
```

### Lombok Annotations

```java
@Data            // Shortcut: generates getters, setters, equals, hashCode, and toString
@NoArgsConstructor   // Required by JPA — entities need a no-args constructor
@AllArgsConstructor  // Constructor with all fields
@RequiredArgsConstructor  // Constructor for final fields — used for dependency injection
```

### Repositories (Spring Data JPA)

```java
public interface ClientRepository extends JpaRepository<Client, Long> { }
```

Extending `JpaRepository<Entity, IdType>` gives you these methods for free — no SQL needed:

| Method | What it does |
|---|---|
| `findAll()` | `SELECT * FROM clients` |
| `findById(id)` | `SELECT * FROM clients WHERE id = ?` |
| `save(entity)` | `INSERT` if new, `UPDATE` if `id` already exists |
| `deleteById(id)` | `DELETE FROM clients WHERE id = ?` |

You can also add **derived query methods** by naming them correctly:

```java
Optional<Policy> findByPolicyNumber(String policyNumber);
// Spring generates: SELECT * FROM policies WHERE policy_number = ?

List<Policy> findByClient(Client client);
// Spring generates: SELECT * FROM policies WHERE client_id = ?
```

### Services

```java
@Service                // Marks this as a Spring-managed service bean
@RequiredArgsConstructor
public class ClientService {
    private final ClientRepository clientRepository;

    public List<Client> findAll() {
        return clientRepository.findAll();  // delegates to JPA
    }
}
```

Services are where business logic lives. Right now they delegate directly to the repository, but this is where you'd add things like: "don't delete a client that has active policies."

### Controllers (REST)

```java
@RestController              // Combines @Controller + @ResponseBody; methods return JSON
@RequestMapping("/clients")  // Base URL path for all methods in this controller
public class ClientController {

    @GetMapping              // Handles: GET /clients
    public List<Client> findAll() { ... }

    @GetMapping("/{id}")     // Handles: GET /clients/{id}
    public Optional<Client> findById(@PathVariable Long id) { ... }
    //                                ↑ binds the {id} in the URL to this parameter

    @PostMapping             // Handles: POST /clients
    public Client save(@RequestBody Client client) { ... }
    //                 ↑ deserializes the JSON body into a Client object

    @DeleteMapping("/{id}")  // Handles: DELETE /clients/{id}
    public void delete(@PathVariable long id) { ... }
}
```

---

## API Endpoints

### Clients — `/clients`

| Method | URL | Description |
|---|---|---|
| GET | `/clients` | Get all clients |
| GET | `/clients/{id}` | Get a client by ID |
| POST | `/clients` | Create a new client |
| DELETE | `/clients/{id}` | Delete a client by ID |

### Policies — `/policies`

| Method | URL | Description |
|---|---|---|
| GET | `/policies` | Get all policies |
| GET | `/policies/{id}` | Get a policy by ID |
| POST | `/policies` | Create a new policy |
| DELETE | `/policies/{id}` | Delete a policy by ID |

### Example Requests

**Create a client:**
```http
POST /clients
Content-Type: application/json

{
  "name": "Victor",
  "lastName": "Tovar",
  "email": "victor@example.com",
  "phone": "555-1234"
}
```

**Create a policy linked to client ID 1:**
```http
POST /policies
Content-Type: application/json

{
  "type": "auto",
  "status": "ACTIVE",
  "policyNumber": "POL-001",
  "premium": 150.00,
  "startDate": "2026-01-01",
  "endDate": "2027-01-01",
  "client": { "id": 1 }
}
```

---

## Running the App

**Prerequisites:** Java 21, Maven (or use the included `./mvnw` wrapper — no Maven installation needed)

### Development mode (H2 in-memory database)

The app starts in `dev` profile by default (`spring.profiles.active=dev` in `application.properties`).

```bash
./mvnw spring-boot:run
```

- API: `http://localhost:8080`
- H2 Console (browser): `http://localhost:8080/h2-console`
  - JDBC URL: `jdbc:h2:mem:policydb`
  - Username: `sa` | Password: *(leave empty)*

> **Note:** Spring Security is active. On startup, look for a line like:
> `Using generated security password: <some-uuid>`
> Use `user` as the username and that UUID as the password for requests, or disable security for now.

### Production mode (PostgreSQL)

```bash
export DB_USER=your_db_user
export DB_PASS=your_db_password
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

Make sure a PostgreSQL database named `policydb` exists on `localhost:5432`.

---

## Configuration Profiles

Spring profiles let you have different configs per environment without changing code.

| File | Profile | Database | Schema behavior |
|---|---|---|---|
| `application.properties` | (base) | — | Sets active profile |
| `application-dev.properties` | `dev` | H2 in-memory | Recreated on each start (`create-drop`) |
| `application-prod.properties` | `prod` | PostgreSQL | Migrated on start (`update`) |

---

## Things to Improve (Learning Notes)

These are real issues in the current code worth fixing as next steps:

1. **Spring Security is blocking everything** — Security is included but not configured. You need to either add a `SecurityFilterChain` bean to define your rules, or temporarily disable it with `spring.security.enabled=false` in `application-dev.properties`.

2. **Missing PUT endpoint** — Neither controller has an update method. In JPA, `save(entity)` handles both insert and update: if the entity has an `id`, it updates; otherwise it inserts. Adding `@PutMapping("/{id}")` is the next step.

3. **Return `ResponseEntity` for proper HTTP status codes** — Right now controllers return raw objects. Wrapping in `ResponseEntity` lets you return `201 Created` on POST, `404 Not Found` when nothing is found, and `204 No Content` on DELETE.

4. **Use DTOs (Data Transfer Objects)** — The controllers expose entities directly. The bidirectional `Client ↔ Policy` relationship (`Client` has `policies`, `Policy` has `client`) will cause **infinite recursion** during JSON serialization. A DTO breaks the cycle and gives you control over what the API exposes.

5. **`@Enumerated(EnumType.STRING)` on Status** — Without this annotation, JPA stores enum values as integers (0, 1, 2...) in the DB. Add `@Enumerated(EnumType.STRING)` to `Policy.status` so it stores `"ACTIVE"` instead of `0`.

6. **`PolicyRepository.delete(Long id)` is incorrect** — `JpaRepository.delete()` takes an entity object, not an ID. This method signature won't work as expected. Remove it and use `deleteById(Long id)`, which is already provided by `JpaRepository` and used correctly in `PolicyService`.

---

## Running Tests

```bash
./mvnw test
```
