# Policy Management API

API REST para la gestión de pólizas de seguros y sus clientes, desarrollada con **Spring Boot 4** y **Java 21**.

---

## Tecnologías usadas

| Tecnología | Versión | Rol en el proyecto |
|---|---|---|
| Java | 21 | Lenguaje principal |
| Spring Boot | 4.0.6 | Framework base de la aplicación |
| Spring Data JPA | — | Acceso a la base de datos sin SQL manual |
| Spring Security | — | Autenticación y autorización de endpoints |
| Spring Validation | — | Validación de datos de entrada |
| Hibernate | — | ORM: mapea clases Java ↔ tablas SQL |
| H2 | — | Base de datos en memoria (desarrollo) |
| PostgreSQL | — | Base de datos (producción) |
| Lombok | — | Elimina código repetitivo (getters, setters, constructores) |
| Maven | — | Gestor de dependencias (`pom.xml`) |

---

## Estructura del proyecto

```
src/main/java/com/pc/pc/
├── entity/
│   ├── Client.java          → Entidad cliente (tabla "clients")
│   └── Policy.java          → Entidad póliza (tabla "policies")
├── enums/
│   └── Status.java          → Estados posibles de una póliza
├── repository/
│   └── PolicyRepository.java → Acceso a datos de pólizas
├── service/
│   └── PolicyService.java   → Lógica de negocio de pólizas
└── PcApplication.java       → Punto de entrada de la aplicación

src/main/resources/
├── application.properties       → Configuración base (activa el perfil activo)
├── application-dev.properties   → Config para desarrollo (H2 en memoria)
└── application-prod.properties  → Config para producción (PostgreSQL)
```

---

## Modelo de datos

### Client

Representa a un cliente que puede tener múltiples pólizas.

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | Long | Identificador único (autoincremental) |
| `name` | String | Nombre del cliente |
| `lastName` | String | Apellido del cliente |
| `email` | String | Correo electrónico |
| `phone` | String | Teléfono de contacto |
| `policies` | Set\<Policy\> | Pólizas asociadas al cliente |

### Policy

Representa una póliza de seguro asociada a un cliente.

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | Long | Identificador único (autoincremental) |
| `policyNumber` | String | Número de póliza |
| `type` | String | Tipo de póliza (vida, auto, hogar, etc.) |
| `status` | Status | Estado actual de la póliza |
| `premium` | Double | Monto de la prima |
| `startDate` | Date | Fecha de inicio de vigencia |
| `endDate` | Date | Fecha de fin de vigencia |
| `client` | Client | Cliente al que pertenece la póliza (FK: `client_id`) |

### Status (enum)

Los posibles estados de una póliza son:

- `ACTIVE` — Póliza vigente
- `INACTIVE` — Póliza inactiva
- `PENDING` — Póliza pendiente de activación
- `CANCELLED` — Póliza cancelada

### Relación entre entidades

```
Client (1) ──────────────── (N) Policy
         Un cliente puede tener
         múltiples pólizas.
         La FK "client_id" vive en la tabla "policies".
```

---

## Cómo funciona Spring en este proyecto

### Inyección de dependencias

Spring crea y gestiona los objetos automáticamente. Cuando ve una anotación como `@Service` o `@Repository`, instancia la clase y la inyecta donde sea necesaria usando el constructor generado por `@RequiredArgsConstructor`.

```java
@Service
@RequiredArgsConstructor
public class PolicyService {
    private final PolicyRepository policyRepository; // Spring inyecta esto
}
```

### Capa de repositorio (JPA)

`PolicyRepository` extiende `JpaRepository`, lo que provee operaciones CRUD sin escribir SQL:

```java
policyRepository.findAll()       // SELECT * FROM policies
policyRepository.findById(id)    // SELECT * WHERE id = ?
policyRepository.save(policy)    // INSERT o UPDATE
policyRepository.delete(policy)  // DELETE
```

### Capa de servicio

`PolicyService` orquesta la lógica de negocio llamando al repositorio. Es el lugar correcto para agregar validaciones, reglas de negocio o transformaciones de datos antes de persistir.

---

## Configuración por entornos

El perfil activo se define en `application.properties`:

```properties
spring.profiles.active=dev
```

### Desarrollo (`application-dev.properties`)

- Base de datos **H2 en memoria** — no requiere instalación
- Se crea al iniciar y se destruye al detener la app
- Consola web disponible en: `http://localhost:8080/h2-console`
  - JDBC URL: `jdbc:h2:mem:policydb`
  - Usuario: `sa` / Contraseña: *(vacía)*
- Esquema recreado en cada inicio (`ddl-auto=create-drop`)

### Producción (`application-prod.properties`)

- Base de datos **PostgreSQL**
- Requiere configurar credenciales antes de desplegar

---

## Cómo ejecutar el proyecto

**Requisitos:** Java 21, Maven

```bash
# Clonar el repositorio
git clone <url-del-repo>
cd pc

# Ejecutar en modo desarrollo
./mvnw spring-boot:run

# Compilar y empaquetar
./mvnw clean package
java -jar target/pc-0.0.1-SNAPSHOT.jar
```

La API estará disponible en `http://localhost:8080`.

---

## Estado actual y próximos pasos

El proyecto tiene implementadas las capas de **entidad**, **repositorio** y **servicio**. Falta:

- [ ] `ClientRepository` y `ClientService`
- [ ] `PolicyController` — endpoints REST para pólizas (`@RestController`)
- [ ] `ClientController` — endpoints REST para clientes
- [ ] Validaciones en las entidades (`@NotNull`, `@NotBlank`, `@Email`)
- [ ] Configuración de Spring Security (roles, JWT, etc.)
- [ ] Configurar credenciales de PostgreSQL en `application-prod.properties`
- [ ] Tests unitarios e de integración