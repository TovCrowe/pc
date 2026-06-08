# Auto Policy Management API — Workflow Diagrams

---

## 1. Full Request Lifecycle

Every HTTP request passes through these layers in order:

```mermaid
sequenceDiagram
    participant Client as HTTP Client
    participant Sec as SecurityFilter
    participant Ctrl as Controller
    participant Svc as Service
    participant Repo as Repository
    participant DB as Database

    Client->>Sec: HTTP Request + Basic Auth
    Sec-->>Client: 401 Unauthorized (if invalid credentials)
    Sec->>Ctrl: Authenticated request

    Ctrl->>Ctrl: @Valid — validate RequestDTO
    Ctrl-->>Client: 400 Bad Request (if validation fails)

    Ctrl->>Svc: call service method(RequestDTO)
    Svc->>Repo: findById / save / deleteById
    Repo->>DB: SQL query
    DB-->>Repo: result
    Repo-->>Svc: Entity / Optional<Entity>

    Svc->>Svc: map Entity → ResponseDTO
    Svc-->>Ctrl: ResponseDTO
    Ctrl-->>Client: 200 / 201 / 204 ResponseEntity
```

---

## 2. Layer Responsibilities

```mermaid
flowchart TD
    A[HTTP Request] --> B[Controller]
    B --> C[Service]
    C --> D[Repository]
    D --> E[(Database)]

    B:::layer
    C:::layer
    D:::layer
    E:::db

    subgraph Controller Layer
        B["Controller
        • Receives HTTP request
        • Validates RequestDTO with @Valid
        • Returns ResponseEntity with status code"]
    end

    subgraph Service Layer
        C["Service
        • Maps RequestDTO → Entity
        • Calls repository
        • Maps Entity → ResponseDTO
        • Throws ResourceNotFoundException if not found"]
    end

    subgraph Repository Layer
        D["Repository
        • Extends JpaRepository
        • No SQL needed for CRUD
        • Spring generates queries automatically"]
    end

    classDef layer fill:#dbeafe,stroke:#3b82f6
    classDef db fill:#dcfce7,stroke:#16a34a
```

---

## 3. Data Model

```mermaid
erDiagram
    CLIENT {
        Long id PK
        String name
        String lastName
        String email
        String phone
    }

    POLICY {
        Long id PK
        String vehicleMake
        String vehicleModel
        Integer vehicleYear
        String vin
        String licensePlate
        String status
        String policyNumber
        Double premium
        Date startDate
        Date endDate
        Long client_id FK
    }

    CLIENT ||--o{ POLICY : "has many"
```

---

## 4. Exception Handling Flow

```mermaid
flowchart TD
    A[Request] --> B[Controller]
    B --> C{Validation passes?}
    C -- No --> D[MethodArgumentNotValidException]
    C -- Yes --> E[Service]
    E --> F{Resource exists?}
    F -- No --> G[ResourceNotFoundException]
    F -- Yes --> H[Success Response]

    D --> I[GlobalExceptionHandler]
    G --> I
    J[Any other Exception] --> I

    I --> K{Exception type?}
    K -- ResourceNotFoundException --> L["404 Not Found
    message: 'Policy not found with id: X'"]
    K -- MethodArgumentNotValidException --> M["400 Bad Request
    message: 'field: must not be blank, ...'"]
    K -- Exception --> N["500 Internal Server Error
    message: 'An unexpected error occurred'"]
```

---

## 5. DTO Flow

```mermaid
flowchart LR
    A["PolicyRequestDTO
    ───────────────
    vehicleMake
    vehicleModel
    vehicleYear
    vin
    licensePlate
    status
    policyNumber
    premium
    startDate
    endDate
    clientId"] -->|toEntity| B["Policy Entity
    ───────────────
    id (generated)
    vehicleMake
    vehicleModel
    vehicleYear
    vin
    licensePlate
    status
    policyNumber
    premium
    startDate
    endDate
    client (object)"]

    B -->|toResponse| C["PolicyResponseDTO
    ───────────────
    id
    vehicleMake
    vehicleModel
    vehicleYear
    vin
    licensePlate
    status
    policyNumber
    premium
    startDate
    endDate
    clientId
    clientName"]
```