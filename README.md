# Auto Policy Management API

A RESTful API built with Spring Boot for managing insurance clients and their **auto (vehicle) policies**. This project is a practical reference for learning the core patterns of a Spring Boot application: layered architecture, JPA, dependency injection, DTOs, and REST controllers.

> **Studying Spring?** Start with [Spring & Spring Boot Fundamentals](#spring--spring-boot-fundamentals) for the mental model, then use the [Annotation Cheat Sheet](#annotation-cheat-sheet) as a quick reference.

---

## Table of Contents

1. [Tech Stack](#tech-stack)
2. [Spring & Spring Boot Fundamentals](#spring--spring-boot-fundamentals) — *the core concepts*
3. [Project Structure](#project-structure)
4. [The Request Lifecycle](#the-request-lifecycle)
5. [Data Model](#data-model)
6. [DTOs](#dtos-data-transfer-objects)
7. [Spring Concepts in This Project](#spring-concepts-in-this-project)
8. [Security](#spring-concepts-in-this-project) & [Authorization](#authorization)
9. [API Endpoints](#api-endpoints)
10. [Validation](#validation)
11. [Exception Handling](#exception-handling)
12. [API Documentation (Swagger / OpenAPI)](#api-documentation-swagger--openapi)
13. [Running the App](#running-the-app)
14. [Running with Docker](#running-with-docker)
15. [Deployment](#deployment) — *Docker & PaaS*
16. [Running Tests](#running-tests)
17. [Frontend](#frontend) — *React SPA*
18. [Annotation Cheat Sheet](#annotation-cheat-sheet) — *quick reference*

---

## Tech Stack

| Dependency | What it does |
|---|---|
| **Spring Boot 4.0.6** | The framework. Auto-configures everything and runs the app via an embedded Tomcat server — no WAR deployment needed. |
| **Spring Web** | Provides `@RestController`, `@GetMapping`, etc. to build REST endpoints. |
| **Spring Data JPA** | Abstracts database access. You define an interface and Spring generates the SQL queries for you. |
| **Spring Security** | Adds authentication/authorization. Configured with HTTP Basic Auth and a database-backed user store. |
| **Spring Validation** | Lets you annotate entity fields with rules like `@NotNull`, `@Size`, `@Email`, etc. |
| **Lombok** | Generates boilerplate Java (getters, setters, constructors) at compile time via annotations. |
| **H2** | An in-memory database used in development. Data is lost when the app stops. |
| **PostgreSQL** | The production database. |

---

## Spring & Spring Boot Fundamentals

This section is the mental model the rest of the README assumes. Read it once and the annotations everywhere else stop being magic.

### Spring vs. Spring Boot

- **Spring Framework** is the core: an *Inversion of Control (IoC) container* plus modules for web, data, security, etc. On its own, Spring needs a lot of manual configuration (XML or Java config) to wire everything together.
- **Spring Boot** sits on top of Spring and removes that ceremony. It adds **auto-configuration**, **starter dependencies**, and an **embedded server** so you can run a production-grade app with almost no boilerplate.

> Rule of thumb: *Spring gives you the building blocks; Spring Boot assembles them for you with sensible defaults.*

### Inversion of Control & Dependency Injection (the #1 concept)

Normally **your code** creates the objects it needs:

```java
ClientService service = new ClientService(new ClientRepository(...));  // you are in control
```

With Spring, you **invert** that control — you declare *what* you need, and the framework creates and supplies it:

```java
@Service
@RequiredArgsConstructor
public class ClientService {
    private final ClientRepository clientRepository;  // "I need this" — Spring provides it
}
```

This is **Inversion of Control (IoC)**, and the mechanism that delivers the objects is **Dependency Injection (DI)**. You never call `new` on a service, repository, or controller.

**Why bother?** Loose coupling. `ClientService` doesn't know *how* its repository is built — Spring can swap in a mock for tests, a different implementation, or a proxy (for transactions/security) without changing your code.

### The Container & Beans

- A **bean** is any object that Spring creates and manages.
- The **ApplicationContext** (the IoC *container*) is the registry that holds every bean and knows how to wire them together.
- At startup Spring **scans** your packages for classes marked as beans, instantiates them, resolves their dependencies, and builds a complete object graph. This is the **bean lifecycle**: *instantiate → inject dependencies → ready to use → destroy on shutdown.*

```
Startup
   ↓
@ComponentScan finds @Component/@Service/@Repository/@Controller classes
   ↓
ApplicationContext instantiates each as a bean (singleton by default)
   ↓
Constructor injection wires dependencies between beans
   ↓
Application is ready to serve requests
```

By default beans are **singletons** — one shared instance for the whole app.

### Stereotype Annotations (how a class becomes a bean)

These all tell Spring "manage me as a bean." They're functionally similar but express *intent*, which aids readability and lets Spring apply layer-specific behavior:

| Annotation | Layer | Purpose |
|---|---|---|
| `@Component` | generic | The base stereotype — any Spring-managed bean |
| `@Service` | business logic | A `@Component` that marks a service-layer class |
| `@Repository` | data access | A `@Component` that also translates DB exceptions into Spring's `DataAccessException` |
| `@RestController` | web | `@Controller` + `@ResponseBody` — returns data (JSON), not view names |
| `@Configuration` | config | A class that defines beans via `@Bean` methods |

In this project: `ClientService`/`PolicyService` are `@Service`, the controllers are `@RestController`, `SecurityConfig`/`DataInitializer` are `@Configuration`. Repositories don't even need `@Repository` — Spring Data JPA registers them automatically.

### Constructor Injection (what `@RequiredArgsConstructor` really does)

Spring recommends **constructor injection**: dependencies are `final` fields set by a constructor. Lombok's `@RequiredArgsConstructor` generates that constructor for all `final` fields, so this:

```java
@Service
@RequiredArgsConstructor
public class ClientService {
    private final ClientRepository clientRepository;
    private final AppUserRepository appUserRepository;
}
```

…is equivalent to writing the constructor by hand. When Spring builds `ClientService`, it sees the constructor needs those two beans and injects them. Constructor injection is preferred over field injection (`@Autowired` on a field) because it makes dependencies explicit, allows `final` (immutable) fields, and makes the class trivial to unit-test with plain `new`.

### `@SpringBootApplication` & Auto-Configuration

The entry point is tiny:

```java
@SpringBootApplication
public class PcApplication {
    public static void main(String[] args) {
        SpringApplication.run(PcApplication.class, args);
    }
}
```

`@SpringBootApplication` is actually **three annotations in one**:

| It includes | What it does |
|---|---|
| `@Configuration` | Marks this class as a source of bean definitions |
| `@ComponentScan` | Scans this package **and sub-packages** for beans (this is why your controllers/services are found automatically) |
| `@EnableAutoConfiguration` | The Spring Boot magic — inspects the classpath and auto-configures beans |

**Auto-configuration** means: because `spring-boot-starter-web` is on the classpath, Boot configures an embedded Tomcat, a `DispatcherServlet`, JSON conversion, etc. Because `spring-boot-starter-data-jpa` + H2 are present, it configures a `DataSource`, an `EntityManager`, and transaction management. You only override what you need (in `application.properties`).

**Starters** are curated dependency bundles. `spring-boot-starter-web` pulls in Spring MVC, Jackson (JSON), validation, and Tomcat in one line — no version-juggling.

---

## Project Structure

```
src/main/java/com/pc/pc/
│
├── PcApplication.java          # Entry point — contains main(), starts Spring Boot
│
├── config/
│   ├── SecurityConfig.java     # Spring Security configuration
│   └── DataInitializer.java    # Seeds the default admin user into the DB on startup
│
├── security/
│   └── AppUserDetailsService.java  # Loads users from the DB for Spring Security
│
├── dto/                        # Data Transfer Objects — what the API sends and receives
│   ├── ClientRequestDTO.java
│   ├── ClientResponseDTO.java
│   ├── PolicyRequestDTO.java
│   └── PolicyResponseDTO.java
│
├── entity/                     # JPA Entities — map directly to database tables
│   ├── AppUser.java
│   ├── Client.java
│   └── Policy.java
│
├── enums/
│   └── Status.java             # Enum for policy status values
│
├── exception/                  # Error handling
│   ├── ResourceNotFoundException.java  # Thrown when a Client or Policy ID doesn't exist
│   ├── ErrorResponse.java              # JSON body returned on every error
│   └── GlobalExceptionHandler.java     # @RestControllerAdvice — catches exceptions globally
│
├── repository/                 # Data access layer — interfaces that talk to the DB
│   ├── AppUserRepository.java
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
Controller   →   receives the RequestDTO, calls the service
    ↓
Service      →   maps DTO → Entity, calls the repository, maps Entity → ResponseDTO
    ↓
Repository   →   talks to the database
    ↓
Entity       →   represents a single row in a database table
```

Each layer only knows about the one directly below it. This keeps concerns separated and the code easier to test and maintain.

---

## The Request Lifecycle

When a request like `GET /clients/1` arrives, it passes through Spring's machinery before reaching your code. Understanding this pipeline explains *where* each annotation plugs in.

```
1. HTTP request hits the embedded Tomcat server
        ↓
2. Spring Security Filter Chain
   • authenticates the user (HTTP Basic → AppUserDetailsService)
   • populates SecurityContextHolder with the logged-in user
   • rejects with 401 if credentials are bad
        ↓
3. DispatcherServlet (Spring MVC's "front controller")
   • the single entry point for every request
        ↓
4. Handler Mapping
   • matches the URL + HTTP method to a controller method
   • GET /clients/{id} → ClientController.findById(...)
        ↓
5. @Valid runs (for POST/PUT) — fails fast with 400 if invalid
        ↓
6. Controller method executes → calls the Service
        ↓
7. Service runs business logic → calls the Repository
   • Spring Data JPA generates SQL, Hibernate executes it
        ↓
8. Entity → DTO mapping, value returned up the stack
        ↓
9. Jackson serializes the returned object to JSON
        ↓
10. HTTP response sent back to the client
```

If an exception is thrown anywhere in steps 6–8, the `@RestControllerAdvice` ([GlobalExceptionHandler](#exception-handling)) intercepts it and converts it to a clean JSON error response instead of a stack trace.

**Key insight:** you only write steps 6–8. Everything else — routing, JSON conversion, authentication, validation triggering, transaction boundaries — is provided by Spring and configured by annotations.

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
| `owner` | AppUser | The authenticated user who created this client (FK: `user_id`) |
| `policies` | Set\<Policy\> | Policies owned by this client (not exposed via API) |

### Policy — `policies` table

| Field | Type | Description |
|---|---|---|
| `id` | Long | Auto-generated primary key |
| `vehicleMake` | String | Vehicle manufacturer (e.g., "Toyota") |
| `vehicleModel` | String | Vehicle model (e.g., "Camry") |
| `vehicleYear` | Integer | Year the vehicle was manufactured |
| `vin` | String | Vehicle Identification Number |
| `licensePlate` | String | License plate number |
| `status` | Status | Current state: `ACTIVE`, `INACTIVE`, `PENDING`, or `CANCELLED` |
| `policyNumber` | String | Unique policy identifier |
| `premium` | Double | Monthly/annual cost |
| `startDate` | Date | Coverage start date |
| `endDate` | Date | Coverage end date |
| `client` | Client | The client this policy belongs to (FK: `client_id`) |

### Relationships

```
AppUser (1) ──────── (N) Client (1) ──────── (N) Policy
```

- `AppUser → Client`: one user can have many clients. `Client` stores `user_id` as a foreign key.
- `Client → Policy`: one client can have many policies. `Policy` stores `client_id` as a foreign key.
- In `Client.java`: `@ManyToOne` + `@JoinColumn(name = "user_id")` — "I belong to one user."
- In `Client.java`: `@OneToMany(mappedBy = "client")` — "I have many policies."
- In `Policy.java`: `@ManyToOne` + `@JoinColumn(name = "client_id")` — "I belong to one client."

---

## DTOs (Data Transfer Objects)

The API never exposes JPA entities directly. Instead it uses DTOs — plain classes that carry only the data needed for each operation. This avoids infinite recursion from the bidirectional `Client ↔ Policy` relationship and decouples the API contract from the database model.

### Request DTOs (what comes IN)

| DTO | Fields |
|---|---|
| `ClientRequestDTO` | `name`, `lastName`, `email`, `phone` |
| `PolicyRequestDTO` | `vehicleMake`, `vehicleModel`, `vehicleYear`, `vin`, `licensePlate`, `status`, `policyNumber`, `premium`, `startDate`, `endDate`, `clientId` |

No `id` on request DTOs — the database generates it on insert, and the URL provides it on update.

Request DTOs also carry validation constraints (see [Validation](#validation) below).

### Response DTOs (what goes OUT)

| DTO | Fields |
|---|---|
| `ClientResponseDTO` | `id`, `name`, `lastName`, `email`, `phone` |
| `PolicyResponseDTO` | `id`, `vehicleMake`, `vehicleModel`, `vehicleYear`, `vin`, `licensePlate`, `status`, `policyNumber`, `premium`, `startDate`, `endDate`, `clientId`, `clientName` |

`PolicyResponseDTO` flattens the client relationship into `clientId` and `clientName` instead of embedding the full `Client` object.

### How mapping works in the service

Each service has two private helper methods that handle the conversion:

```java
// DTO → Entity (used in save and update)
private Client toEntity(ClientRequestDTO dto) {
    Client client = new Client();
    client.setName(dto.getName());
    // ...
    return client;
}

// Entity → DTO (used before returning to the controller)
private ClientResponseDTO toResponse(Client client) {
    return new ClientResponseDTO(
        client.getId(), client.getName(), ...
    );
}
```

---

## Spring Concepts in This Project

> This section shows the [fundamentals](#spring--spring-boot-fundamentals) applied to actual code in the project. If a concept here feels unfamiliar, jump back up to the fundamentals first.

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
@Enumerated(EnumType.STRING)    // Stores enum as "ACTIVE" instead of 0, 1, 2...
```

### Lombok Annotations

```java
@Data                    // Generates getters, setters, equals, hashCode, and toString
@NoArgsConstructor       // Required by JPA — entities need a no-args constructor
@AllArgsConstructor      // Constructor with all fields — useful for creating DTOs
@RequiredArgsConstructor // Constructor for final fields — used for dependency injection
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

### Security

Authentication is backed by the database. When a request arrives, Spring Security calls `AppUserDetailsService`, which loads the user from the `users` table. The password stored in the DB is BCrypt-hashed — plain text is never stored.

**Why DB-backed instead of in-memory?**
`InMemoryUserDetailsManager` hardcodes credentials in source code — there is no way to add, remove, or update users without redeploying. A DB-backed approach lets users be managed at runtime and is the prerequisite for linking each authenticated user to their own policies.

**How the pieces connect:**

```
HTTP Request (username + password)
    ↓
Spring Security intercepts the request
    ↓
DaoAuthenticationProvider
    ↓ calls
AppUserDetailsService.loadUserByUsername(username)
    ↓ queries
AppUserRepository.findByUsername(username)   →  users table
    ↓ returns UserDetails
DaoAuthenticationProvider compares the BCrypt hash
    ↓ grants or rejects access
```

**`AppUser` entity** (`entity/AppUser.java`) — the `users` table:

```java
@Entity
@Table(name = "users")
public class AppUser {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;   // BCrypt hash — never plain text

    @Column(nullable = false)
    private String role;       // e.g. "ADMIN"
}
```

**`AppUserRepository`** (`repository/AppUserRepository.java`) — one derived query method is all that's needed:

```java
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);
    // Spring generates: SELECT * FROM users WHERE username = ?
}
```

**`AppUserDetailsService`** (`security/AppUserDetailsService.java`) — the bridge between Spring Security and the DB:

```java
@Service
public class AppUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) {
        return appUserRepository.findByUsername(username)
                .map(u -> new User(
                        u.getUsername(),
                        u.getPassword(),
                        List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole()))
                ))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}
```

**`SecurityConfig`** (`config/SecurityConfig.java`) — wires `DaoAuthenticationProvider` with the service and encoder:

```java
@Bean
public DaoAuthenticationProvider authenticationProvider(
        AppUserDetailsService userDetailsService,
        PasswordEncoder passwordEncoder) {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
    provider.setPasswordEncoder(passwordEncoder);
    return provider;
}

@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();  // hashes passwords — never store plain text
}
```

**`DataInitializer`** (`config/DataInitializer.java`) — seeds the default admin user at startup so the app is never locked out on a fresh DB:

```java
@Bean
CommandLineRunner seedUsers(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
    return args -> {
        if (userRepository.findByUsername("admin").isEmpty()) {
            userRepository.save(new AppUser(null, "admin", passwordEncoder.encode("admin123"), "ADMIN"));
        }
    };
}
```

The `if` check makes it idempotent — safe to run on every startup without creating duplicates.

---

## Authorization

Authentication answers "who are you?" Authorization answers "what are you allowed to see?"

Every client and policy is scoped to the authenticated user. A user can only read, create, update, or delete their own data — they can never access another user's records.

**Why this matters:** without authorization, any authenticated user could hit `GET /clients/1` and retrieve a client that belongs to someone else. With it, the service filters every query by the logged-in user, so IDs from other users return `404 Not Found`.

### How it works

Both `ClientService` and `PolicyService` call a private `getCurrentUser()` method on every operation:

```java
private AppUser getCurrentUser() {
    String username = SecurityContextHolder.getContext().getAuthentication().getName();
    return appUserRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
}
```

`SecurityContextHolder` is a thread-local store that Spring Security populates after authenticating each request. `.getAuthentication().getName()` returns the logged-in username, which is then looked up in the DB to get the full `AppUser`.

### Scoped repository queries

The repositories use Spring Data JPA derived query methods to filter by owner automatically:

**`ClientRepository`:**
```java
List<Client> findByOwner(AppUser owner);
// SELECT * FROM clients WHERE user_id = ?

Optional<Client> findByIdAndOwner(Long id, AppUser owner);
// SELECT * FROM clients WHERE id = ? AND user_id = ?

boolean existsByIdAndOwner(Long id, AppUser owner);
// SELECT COUNT(*) > 0 FROM clients WHERE id = ? AND user_id = ?
```

**`PolicyRepository`:**
```java
List<Policy> findByClientOwner(AppUser owner);
// SELECT * FROM policies JOIN clients ON ... WHERE clients.user_id = ?

Optional<Policy> findByIdAndClientOwner(Long id, AppUser owner);
// SELECT * FROM policies JOIN clients ON ... WHERE policies.id = ? AND clients.user_id = ?

boolean existsByIdAndClientOwner(Long id, AppUser owner);
// SELECT COUNT(*) > 0 FROM policies JOIN clients ON ... WHERE policies.id = ? AND clients.user_id = ?
```

The `findByClientOwner` naming tells Spring Data JPA to follow the `client` relationship on `Policy` and then match the `owner` field on `Client` — it generates the JOIN automatically.

### Behavior per operation

| Operation | How authorization is applied |
|---|---|
| `GET /clients` | Returns only clients where `user_id` = current user |
| `GET /clients/{id}` | Returns `404` if the client belongs to another user |
| `POST /clients` | Sets `owner` = current user on the new client |
| `PUT /clients/{id}` | Checks `existsByIdAndOwner` before saving — `404` if not owned |
| `DELETE /clients/{id}` | Checks `existsByIdAndOwner` before deleting — `404` if not owned |
| `GET /policies` | Returns only policies whose client belongs to current user |
| `GET /policies/{id}` | Returns `404` if the policy's client belongs to another user |
| `POST /policies` | Validates that the `clientId` in the request belongs to current user |
| `PUT /policies/{id}` | Checks `existsByIdAndClientOwner` before saving — `404` if not owned |
| `DELETE /policies/{id}` | Checks `existsByIdAndClientOwner` before deleting — `404` if not owned |

> Returning `404` instead of `403` for unauthorized access is intentional — it avoids leaking that a resource exists at that ID.

---

## API Endpoints

All endpoints require HTTP Basic Auth: **username** `admin`, **password** `admin123`.

### Clients — `/clients`

| Method | URL | Body | Status | Description |
|---|---|---|---|---|
| GET | `/clients` | — | `200 OK` | Get all clients |
| GET | `/clients/{id}` | — | `200 OK` / `404 Not Found` | Get a client by ID |
| POST | `/clients` | `ClientRequestDTO` | `201 Created` / `400 Bad Request` | Create a new client |
| PUT | `/clients/{id}` | `ClientRequestDTO` | `200 OK` / `400 Bad Request` | Update an existing client |
| DELETE | `/clients/{id}` | — | `204 No Content` | Delete a client by ID |

### Policies — `/policies`

| Method | URL | Body | Status | Description |
|---|---|---|---|---|
| GET | `/policies` | — | `200 OK` | Get all policies |
| GET | `/policies/{id}` | — | `200 OK` / `404 Not Found` | Get a policy by ID |
| POST | `/policies` | `PolicyRequestDTO` | `201 Created` / `400 Bad Request` | Create a new policy |
| PUT | `/policies/{id}` | `PolicyRequestDTO` | `200 OK` / `400 Bad Request` | Update an existing policy |
| DELETE | `/policies/{id}` | — | `204 No Content` | Delete a policy by ID |

### HTTP Status Codes

Controllers return `ResponseEntity` to give proper HTTP status codes on every response:

```java
// 200 OK — found (service throws ResourceNotFoundException if not found)
return ResponseEntity.ok(clientService.findById(id));

// 201 Created — new resource
return ResponseEntity.status(201).body(clientService.save(dto));

// 204 No Content — deleted, nothing to return
clientService.delete(id);
return ResponseEntity.noContent().build();
```

404 and 500 errors are handled globally — see [Exception Handling](#exception-handling) below.

### Example Requests

**Create a client:**
```http
POST /clients
Authorization: Basic YWRtaW46YWRtaW4xMjM=
Content-Type: application/json

{
  "name": "Victor",
  "lastName": "Tovar",
  "email": "victor@example.com",
  "phone": "555-1234"
}
```

**Response:**
```json
{
  "id": 1,
  "name": "Victor",
  "lastName": "Tovar",
  "email": "victor@example.com",
  "phone": "555-1234"
}
```

**Create an auto policy linked to client ID 1:**
```http
POST /policies
Authorization: Basic YWRtaW46YWRtaW4xMjM=
Content-Type: application/json

{
  "vehicleMake": "Toyota",
  "vehicleModel": "Camry",
  "vehicleYear": 2022,
  "vin": "1HGBH41JXMN109186",
  "licensePlate": "ABC-1234",
  "status": "ACTIVE",
  "policyNumber": "POL-001",
  "premium": 150.00,
  "startDate": "2026-01-01",
  "endDate": "2027-01-01",
  "clientId": 1
}
```

**Response:**
```json
{
  "id": 1,
  "vehicleMake": "Toyota",
  "vehicleModel": "Camry",
  "vehicleYear": 2022,
  "vin": "1HGBH41JXMN109186",
  "licensePlate": "ABC-1234",
  "status": "ACTIVE",
  "policyNumber": "POL-001",
  "premium": 150.00,
  "startDate": "2026-01-01",
  "endDate": "2027-01-01",
  "clientId": 1,
  "clientName": "Victor"
}
```

**With curl:**
```bash
curl -u admin:admin123 http://localhost:8080/clients
```

---

## Validation

Request DTOs are annotated with Jakarta Validation constraints. The controller methods use `@Valid` to trigger validation before the request reaches the service layer.

### `ClientRequestDTO`

| Field | Constraint | Rule |
|---|---|---|
| `name` | `@NotBlank` | Must not be null or empty |
| `lastName` | `@NotBlank` | Must not be null or empty |
| `email` | `@NotBlank` + `@Email` | Must not be empty and must be a valid email format |
| `phone` | `@NotBlank` | Must not be null or empty |

### `PolicyRequestDTO`

| Field | Constraint | Rule |
|---|---|---|
| `vehicleMake` | `@NotBlank` | Must not be null or empty |
| `vehicleModel` | `@NotBlank` | Must not be null or empty |
| `vehicleYear` | `@NotNull` | Must be provided |
| `vin` | `@NotBlank` | Must not be null or empty |
| `licensePlate` | `@NotBlank` | Must not be null or empty |
| `policyNumber` | `@NotBlank` | Must not be null or empty |
| `status` | `@NotNull` | Must be provided (`ACTIVE`, `INACTIVE`, `PENDING`, or `CANCELLED`) |
| `premium` | `@NotNull` | Must be provided |
| `startDate` | `@NotNull` | Must be provided |
| `endDate` | `@NotNull` | Must be provided |
| `clientId` | `@NotNull` | Must be provided |

### How it works

```java
// Controller — @Valid triggers constraint checks before the method body runs
@PostMapping
public ResponseEntity<ClientResponseDTO> save(@Valid @RequestBody ClientRequestDTO dto) { ... }
```

If any constraint fails, Spring throws `MethodArgumentNotValidException` before the service is called. `GlobalExceptionHandler` catches it and returns a `400 Bad Request` listing every failing field:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "email: must be a well-formed email address, name: must not be blank",
  "timestamp": "2026-05-14T10:00:00"
}
```

---

## Exception Handling

All errors return a consistent JSON body instead of stack traces. The three classes in `exception/` work together:

### `ResourceNotFoundException`

A custom unchecked exception thrown by the service layer when a lookup fails:

```java
// In PolicyService — when clientId doesn't exist
clientRepository.findById(dto.getClientId())
        .orElseThrow(() -> new ResourceNotFoundException("Client", id));
// message: "Client not found with id: 5"
```

### `ErrorResponse`

A Java record that defines the JSON shape of every error response:

```java
public record ErrorResponse(int status, String error, String message, LocalDateTime timestamp) {}
```

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Policy not found with id: 99",
  "timestamp": "2026-05-13T10:30:00"
}
```

### `GlobalExceptionHandler`

`@RestControllerAdvice` intercepts exceptions thrown anywhere in the controller layer and maps them to `ResponseEntity<ErrorResponse>`:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(404)
                .body(ErrorResponse.of(404, "Not Found", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.status(400)
                .body(ErrorResponse.of(400, "Bad Request", message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return ResponseEntity.status(500)
                .body(ErrorResponse.of(500, "Internal Server Error", "An unexpected error occurred"));
    }
}
```

The `MethodArgumentNotValidException` handler returns a `400` with all failing field names and messages. The catch-all `Exception` handler ensures unexpected errors never leak a stack trace to the client.

---

## API Documentation (Swagger / OpenAPI)

Interactive API docs are generated automatically from the controllers and DTOs by [springdoc-openapi](https://springdoc.org/). No manual spec maintenance — the contract always matches the code.

| URL | What it serves |
|---|---|
| `http://localhost:8080/swagger-ui.html` | **Swagger UI** — an interactive page to browse and call every endpoint. |
| `http://localhost:8080/v3/api-docs` | The raw **OpenAPI 3.1** spec as JSON (import into Postman, codegen tools, etc.). |

These paths are whitelisted in `SecurityConfig`, so the docs load without authentication. The endpoints themselves still require HTTP Basic auth — Swagger UI shows an **Authorize** button (the `basicAuth` scheme is declared in `OpenApiConfig`) so you can enter credentials and call secured endpoints directly from the browser.

The API title, description, version, and security scheme are configured in [`OpenApiConfig`](src/main/java/com/pc/pc/config/OpenApiConfig.java).

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

### CORS

Browser origins allowed to call the API are configured in `application.properties` via `app.cors.allowed-origins` (comma-separated). It defaults to the local dev servers (`http://localhost:5173`, `http://localhost:3000`) and is overridable per environment with the `APP_CORS_ALLOWED_ORIGINS` env var. The setting is wired into `SecurityConfig`'s `CorsConfigurationSource`, with credentials allowed so the browser can send the HTTP Basic header cross-origin.

---

## Running with Docker

`./mvnw spring-boot:run` is the quickest way to run the app, but it uses the in-memory H2 database. Running with Docker instead spins up the app **and a real PostgreSQL** together — the same setup as production — so you can catch prod-only issues before deploying.

### What's in the box

| File | Role |
|---|---|
| `Dockerfile` | Multi-stage build: stage 1 compiles the JAR with Maven, stage 2 runs it on a slim JRE as a non-root user |
| `docker-compose.yml` | Defines two containers — `app` (the API, `prod` profile) and `db` (PostgreSQL) — and wires them together |
| `.dockerignore` | Keeps the build context small (excludes `target/`, `.git/`, IDE files, etc.) |

### Prerequisites

You need **Docker Engine** and the **Docker Compose v2 plugin**.

On Ubuntu / Pop!_OS:

```bash
sudo apt install -y docker.io docker-compose-v2

# Run docker without sudo (then log out and back in to apply)
sudo usermod -aG docker $USER

# Verify
docker --version
docker compose version
```

> If `docker compose version` says "unknown command", the `docker-compose-v2` plugin isn't installed — `docker.io` alone does **not** include it.

### Build and run

From the project root:

```bash
docker compose up --build
```

- The **first** run takes a few minutes — it builds the image (downloading Maven dependencies inside the container). Later runs are cached and fast.
- The app is ready when you see `Started PcApplication in X seconds` in the logs.
- This terminal now streams the live logs from both containers. Leave it running.

### Test it

In a **second terminal**, or with a tool like Postman. The login is `admin` / `change-me-locally` (set in `docker-compose.yml` via `ADMIN_PASSWORD`):

```bash
# Health check (no auth required)
curl http://localhost:8080/actuator/health
# → {"status":"UP"}

# Create a client (HTTP Basic Auth)
curl -u admin:change-me-locally -X POST http://localhost:8080/clients \
  -H "Content-Type: application/json" \
  -d '{"name":"Victor","lastName":"Tovar","email":"victor@example.com","phone":"555-1234"}'
# → JSON with "id": 1

# List your clients
curl -u admin:change-me-locally http://localhost:8080/clients
```

**Using Postman?** Set the **Authorization** tab to **Basic Auth** (`admin` / `change-me-locally`) on each request. For the POST, use **Body → raw → JSON**.

### Stop it

In the logs terminal press `Ctrl+C`, then:

```bash
docker compose down       # stop and remove the containers
docker compose down -v     # also delete the PostgreSQL data volume (fresh DB next time)
```

---

## Deployment

The app is containerized and deploys to any platform that runs a Docker image (Railway, Render, Fly.io, etc.).

### Environment variables (production)

The `prod` profile reads all secrets and connection details from the environment — nothing sensitive is committed:

| Variable | Purpose | Example |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Selects the prod profile | `prod` |
| `SPRING_DATASOURCE_URL` | JDBC URL of the database | `jdbc:postgresql://db:5432/policydb` |
| `DB_USER` | Database username | `policy` |
| `DB_PASS` | Database password | `secret` |
| `ADMIN_USERNAME` | Seeded admin login | `admin` |
| `ADMIN_PASSWORD` | Seeded admin password | *(a real secret)* |
| `PORT` | Port to bind to (injected by most PaaS) | `8080` |

### Test the production build locally first

Before deploying, run the containerized app against a real PostgreSQL with `docker compose up --build` — see [Running with Docker](#running-with-docker) for the full walkthrough. It uses the same image and `prod` profile the PaaS will run, so if it works locally it'll work in the cloud.

### Deploy to a PaaS (Railway / Render / Fly.io)

The flow is the same on every platform:

1. **Push this repo to GitHub** (the platform builds from the `Dockerfile`).
2. **Provision a PostgreSQL database** on the platform — it gives you a connection URL, username, and password.
3. **Create the web service** pointing at your repo. The platform detects the `Dockerfile` and builds the image.
4. **Set the environment variables** from the table above (`SPRING_PROFILES_ACTIVE=prod`, the DB values, and a strong `ADMIN_PASSWORD`). Most platforms inject `PORT` automatically.
5. **Set the health check path** to `/actuator/health` so the platform knows when the app is ready.
6. **Deploy.** On first boot, `DataInitializer` seeds the admin user and Hibernate creates the tables.

### One-click on Render (Blueprint)

A [`render.yaml`](render.yaml) blueprint provisions all three pieces at once — the PostgreSQL database, the backend API (Docker), and the frontend (static site). In Render: **New → Blueprint**, point it at this repo, and apply.

Two URLs are cross-referenced between the services and can't be auto-wired, so after the first deploy set them in the dashboard and redeploy:

1. On **pc-frontend**, set `VITE_API_BASE_URL` to the backend URL (e.g. `https://pc-api.onrender.com`). Vite bakes this in at **build time**, so a redeploy is required after changing it.
2. On **pc-api**, set `APP_CORS_ALLOWED_ORIGINS` to the frontend URL (e.g. `https://pc-frontend.onrender.com`).

The frontend static site uses a rewrite rule (`/*` → `/index.html`) so React Router's client-side routes resolve on refresh. The database connection is assembled from the `DB_HOST`/`DB_PORT`/`DB_NAME`/`DB_USER`/`DB_PASS` values Render injects from the managed database.

> **Free-tier caveats:** the API sleeps after inactivity (~30–50s cold start on the next request), and free PostgreSQL databases expire after ~90 days.

### Production hardening checklist

What's done and what's left as you take this further:

- [x] Secrets read from environment variables (no credentials in source)
- [x] Admin password configurable per environment
- [x] Health endpoint for platform probes (`/actuator/health`)
- [x] Binds to the platform-injected `PORT`
- [x] Runs as a non-root user in the container
- [ ] Replace `ddl-auto=update` with **Flyway** migrations (versioned, reviewable schema changes)
- [ ] Add a real user-registration flow instead of a single seeded admin
- [ ] Restrict the H2 console / disable it entirely in prod
- [x] CORS configured for a browser frontend (`app.cors.allowed-origins`)

---

## Running Tests

```bash
./mvnw test
```

### Test Structure

```
src/test/java/com/pc/pc/
├── PcApplicationTests.java          # Context load smoke test
└── service/
    ├── ClientServiceTest.java       # Unit tests for ClientService
    └── PolicyServiceTest.java       # Unit tests for PolicyService
```

All service tests are pure unit tests using **Mockito** — no Spring context, no database. Repositories are mocked, so tests run fast and in isolation.

### `ClientServiceTest`

| Test | What it verifies |
|---|---|
| `findAll_returnsMappedList` | Returns a mapped list of `ClientResponseDTO` with correct field values |
| `findById_returnsResponse_whenFound` | Returns the correct response when the client exists |
| `findById_throwsException_whenNotFound` | Throws `ResourceNotFoundException` for a missing ID |
| `save_persistsAndReturnsResponse` | Calls `clientRepository.save()` and maps the result to a response DTO |
| `update_setsIdAndSaves` | Sets the ID on the entity before saving (ensures UPDATE, not INSERT) |
| `update_throwsException_whenNotFound` | Throws `ResourceNotFoundException` when the client ID doesn't exist |
| `delete_callsDeleteById` | Delegates to `clientRepository.deleteById()` |

### `PolicyServiceTest`

| Test | What it verifies |
|---|---|
| `findAll_returnsMappedList` | Returns a mapped list of `PolicyResponseDTO` with correct field values |
| `findById_returnsResponse_whenFound` | Returns the correct response when the policy exists |
| `findById_throwsException_whenNotFound` | Throws `ResourceNotFoundException` for a missing ID |
| `save_persistsAndReturnsResponse` | Calls `policyRepository.save()` and maps the result to a response DTO |
| `save_throwsException_whenClientNotFound` | Throws `ResourceNotFoundException` when the `clientId` doesn't exist |
| `update_setsIdAndSaves` | Sets the ID on the entity before saving (ensures UPDATE, not INSERT) |
| `update_throwsException_whenNotFound` | Throws `ResourceNotFoundException` when the policy ID doesn't exist |
| `delete_callsDeleteById` | Delegates to `policyRepository.deleteById()` |

### Test Dependencies

All test tooling comes bundled with `spring-boot-starter-test` — no extra dependencies needed:

| Tool | Role |
|---|---|
| **JUnit 5** | Test runner (`@Test`, `@BeforeEach`, `@ExtendWith`) |
| **Mockito** | Mocking repositories (`@Mock`, `@InjectMocks`, `when(...).thenReturn(...)`, `verify(...)`) |
| **AssertJ** | Fluent assertions (`assertThat(...)`, `assertThatThrownBy(...)`) |
| **spring-security-test** | Security context support for future controller-layer tests |

---

## Frontend

A single-page app in **React + TypeScript + Vite** lives in [`frontend/`](frontend/) and consumes this API. It's a separate npm project in the same repository (monorepo), so the backend and frontend are versioned together but built independently.

### Stack

| Tool | Role |
|---|---|
| **Vite + React 18 + TypeScript** | Build tooling and UI framework |
| **React Router** | Client-side routing (login, list and form pages) |
| **TanStack Query** | Server-state fetching, caching, and cache invalidation |
| **Axios** | HTTP client with an interceptor that attaches the HTTP Basic header |

### Features

- **Login** screen that validates credentials against the API before storing them.
- **HTTP Basic auth** persisted in `localStorage`; a response interceptor clears the session and redirects to `/login` on a `401`.
- **CRUD for clients and policies** — list, create, edit, and delete, mapped to the REST endpoints.
- TypeScript types that mirror the backend DTOs (`ClientResponseDTO`, `PolicyResponseDTO`, …).

### Running it

```bash
# 1) Start the backend (dev profile)
./mvnw spring-boot:run

# 2) Start the frontend dev server
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173` and log in with the seeded credentials (`admin` / `admin123` in dev).

> **CORS:** the dev origin `http://localhost:5173` is already whitelisted in `SecurityConfig`
> (see [Configuration Profiles](#configuration-profiles) — `app.cors.allowed-origins`). Add the
> production frontend origin there before deploying.

The API base URL is read from `VITE_API_BASE_URL` (`frontend/.env.development`, defaults to `http://localhost:8080`). See [`frontend/README.md`](frontend/README.md) for full details.

---

## Annotation Cheat Sheet

Every annotation used in this project, grouped by purpose. Use this as a quick reference while reading the code.

### Bootstrapping & Configuration
| Annotation | Where | What it does |
|---|---|---|
| `@SpringBootApplication` | `PcApplication` | Entry point; bundles `@Configuration` + `@ComponentScan` + `@EnableAutoConfiguration` |
| `@Configuration` | `SecurityConfig`, `DataInitializer` | Marks a class that defines beans |
| `@Bean` | inside `@Configuration` | Declares a single bean from a method's return value |
| `@EnableWebSecurity` | `SecurityConfig` | Turns on Spring Security's web support |

### Stereotypes (make a class a bean)
| Annotation | Where | What it does |
|---|---|---|
| `@RestController` | controllers | `@Controller` + `@ResponseBody` — methods return JSON |
| `@Service` | services | Marks a business-logic bean |
| `@Component` | (implicit base) | Generic Spring-managed bean |

### Web / REST
| Annotation | What it does |
|---|---|
| `@RequestMapping("/clients")` | Base URL path for a controller |
| `@GetMapping` / `@PostMapping` / `@PutMapping` / `@DeleteMapping` | Map an HTTP method + path to a handler |
| `@PathVariable` | Binds a URL segment (`/clients/{id}`) to a parameter |
| `@RequestBody` | Deserializes the JSON request body into an object |
| `@Valid` | Triggers Jakarta Validation on the request body |

### Persistence (JPA / Hibernate)
| Annotation | What it does |
|---|---|
| `@Entity` | Maps a class to a database table |
| `@Table(name = "...")` | Sets the table name explicitly |
| `@Id` | Marks the primary key field |
| `@GeneratedValue(strategy = IDENTITY)` | Auto-increment PK, managed by the DB |
| `@Column(unique, nullable)` | Configures a column's constraints |
| `@OneToMany(mappedBy = "...")` | The "one" side of a relationship (no FK column) |
| `@ManyToOne` | The "many" side (owns the FK column) |
| `@JoinColumn(name = "...")` | Names the foreign-key column |
| `@Enumerated(EnumType.STRING)` | Stores an enum as text instead of an ordinal int |

### Validation
| Annotation | Rule |
|---|---|
| `@NotBlank` | String must not be null or empty/whitespace |
| `@NotNull` | Value must not be null (use for non-Strings) |
| `@Email` | Must be a well-formed email address |

### Exception Handling
| Annotation | What it does |
|---|---|
| `@RestControllerAdvice` | Global exception handler returning JSON (`@ControllerAdvice` + `@ResponseBody`) |
| `@ExceptionHandler(X.class)` | Maps a specific exception type to a handler method |

### Lombok (compile-time code generation)
| Annotation | Generates |
|---|---|
| `@Data` | Getters, setters, `equals`, `hashCode`, `toString` |
| `@NoArgsConstructor` | Empty constructor (required by JPA) |
| `@AllArgsConstructor` | Constructor with every field |
| `@RequiredArgsConstructor` | Constructor for `final` fields → used for constructor injection |

### Testing
| Annotation | What it does |
|---|---|
| `@ExtendWith(MockitoExtension.class)` | Enables Mockito in a JUnit 5 test |
| `@Mock` | Creates a mock of a dependency |
| `@InjectMocks` | Builds the class under test with mocks injected |
| `@BeforeEach` / `@AfterEach` | Setup/teardown run before/after each test |
| `@Test` | Marks a test method |
