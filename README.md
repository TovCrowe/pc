# Policy Management API

A RESTful API built with Spring Boot for managing insurance clients and their policies. This project is a practical reference for learning the core patterns of a Spring Boot application: layered architecture, JPA, dependency injection, DTOs, and REST controllers.

---

## Tech Stack

| Dependency | What it does |
|---|---|
| **Spring Boot 4.0.6** | The framework. Auto-configures everything and runs the app via an embedded Tomcat server — no WAR deployment needed. |
| **Spring Web** | Provides `@RestController`, `@GetMapping`, etc. to build REST endpoints. |
| **Spring Data JPA** | Abstracts database access. You define an interface and Spring generates the SQL queries for you. |
| **Spring Security** | Adds authentication/authorization. Configured with HTTP Basic Auth and an in-memory user. |
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
├── config/
│   └── SecurityConfig.java     # Spring Security configuration
│
├── dto/                        # Data Transfer Objects — what the API sends and receives
│   ├── ClientRequestDTO.java
│   ├── ClientResponseDTO.java
│   ├── PolicyRequestDTO.java
│   └── PolicyResponseDTO.java
│
├── entity/                     # JPA Entities — map directly to database tables
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

## Data Model

### Client — `clients` table

| Field | Type | Description |
|---|---|---|
| `id` | Long | Auto-generated primary key |
| `name` | String | First name |
| `lastName` | String | Last name |
| `email` | String | Contact email |
| `phone` | String | Phone number |
| `policies` | Set\<Policy\> | Policies owned by this client (not exposed via API) |

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

## DTOs (Data Transfer Objects)

The API never exposes JPA entities directly. Instead it uses DTOs — plain classes that carry only the data needed for each operation. This avoids infinite recursion from the bidirectional `Client ↔ Policy` relationship and decouples the API contract from the database model.

### Request DTOs (what comes IN)

| DTO | Fields |
|---|---|
| `ClientRequestDTO` | `name`, `lastName`, `email`, `phone` |
| `PolicyRequestDTO` | `type`, `status`, `policyNumber`, `premium`, `startDate`, `endDate`, `clientId` |

No `id` on request DTOs — the database generates it on insert, and the URL provides it on update.

### Response DTOs (what goes OUT)

| DTO | Fields |
|---|---|
| `ClientResponseDTO` | `id`, `name`, `lastName`, `email`, `phone` |
| `PolicyResponseDTO` | `id`, `type`, `status`, `policyNumber`, `premium`, `startDate`, `endDate`, `clientId`, `clientName` |

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

### Security (`SecurityConfig.java`)

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())           // REST APIs don't need CSRF protection
            .headers(h -> h.frameOptions(...))      // Allow H2 console (runs in an iframe)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/h2-console/**").permitAll()
                .anyRequest().authenticated()       // everything else requires login
            )
            .httpBasic(basic -> {});                // use HTTP Basic Auth
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        // in-memory user — replace with DB-backed UserDetailsService later
        var user = User.builder()
            .username("admin")
            .password(encoder.encode("admin123"))
            .roles("ADMIN")
            .build();
        return new InMemoryUserDetailsManager(user);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();  // hashes passwords — never store plain text
    }
}
```

---

## API Endpoints

All endpoints require HTTP Basic Auth: **username** `admin`, **password** `admin123`.

### Clients — `/clients`

| Method | URL | Body | Status | Description |
|---|---|---|---|---|
| GET | `/clients` | — | `200 OK` | Get all clients |
| GET | `/clients/{id}` | — | `200 OK` / `404 Not Found` | Get a client by ID |
| POST | `/clients` | `ClientRequestDTO` | `201 Created` | Create a new client |
| PUT | `/clients/{id}` | `ClientRequestDTO` | `200 OK` | Update an existing client |
| DELETE | `/clients/{id}` | — | `204 No Content` | Delete a client by ID |

### Policies — `/policies`

| Method | URL | Body | Status | Description |
|---|---|---|---|---|
| GET | `/policies` | — | `200 OK` | Get all policies |
| GET | `/policies/{id}` | — | `200 OK` / `404 Not Found` | Get a policy by ID |
| POST | `/policies` | `PolicyRequestDTO` | `201 Created` | Create a new policy |
| PUT | `/policies/{id}` | `PolicyRequestDTO` | `200 OK` | Update an existing policy |
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

**Create a policy linked to client ID 1:**
```http
POST /policies
Authorization: Basic YWRtaW46YWRtaW4xMjM=
Content-Type: application/json

{
  "type": "auto",
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
  "type": "auto",
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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return ResponseEntity.status(500)
                .body(ErrorResponse.of(500, "Internal Server Error", "An unexpected error occurred"));
    }
}
```

The catch-all `Exception` handler ensures unexpected errors never leak a stack trace to the client.

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

---

## Running Tests

```bash
./mvnw test
```
