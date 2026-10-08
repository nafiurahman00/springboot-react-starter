# Backend (Spring Boot REST API)

A REST API built with Spring Boot using a layered (MVC) architecture and JWT
authentication. This document explains how the code is organized and how a
request flows through the layers. For installation and how to run the whole
project, see the [root README](../README.md).

## Technology

- Java 17, Spring Boot 3.3 (Maven)
- Spring Web (REST controllers)
- Spring Data JPA + Hibernate (database access)
- Spring Security + JWT (authentication)
- Bean Validation (request validation)
- MySQL
- springdoc-openapi (Swagger UI)

## Package structure

The code is organized **by layer** (technical role), so every class of a given
kind lives together:

```
com.example.app
├── Application.java          Entry point (main method)
│
├── routes/                   The route catalog: every URL path in one place
│   └── ApiRoutes               path constants the controllers reference
│
├── controller/               Web layer: request handlers (@RestController)
│   ├── AuthController          POST /api/auth/register, /login
│   └── TaskController          CRUD for /api/tasks
│
├── service/                  Business logic (@Service), transactions
│   ├── AuthService
│   └── TaskService
│
├── repository/               Data access (Spring Data JPA interfaces)
│   ├── UserRepository
│   └── TaskRepository
│
├── model/                    JPA entities = database tables (@Entity)
│   ├── User
│   ├── Role                    enum (USER, ADMIN)
│   └── Task
│
├── dto/                      Data Transfer Objects (request/response shapes)
│   ├── RegisterRequest, LoginRequest, AuthResponse
│   └── TaskRequest, TaskResponse
│
├── mapper/                   Converts entities <-> DTOs
│   └── TaskMapper
│
├── security/                 JWT creation + the request filter
│   ├── JwtService              builds and validates tokens
│   ├── JwtAuthenticationFilter reads the token on each request
│   └── RestAuthenticationEntryPoint returns JSON 401 when unauthenticated
│
├── config/                   Spring configuration (@Configuration)
│   ├── SecurityConfig          security rules, CORS, filter chain
│   ├── ApplicationConfig       UserDetailsService, PasswordEncoder, AuthManager
│   └── OpenApiConfig           Swagger UI + "Authorize" button
│
└── exception/                Error handling
    ├── GlobalExceptionHandler  turns exceptions into JSON (@RestControllerAdvice)
    ├── ApiError                the JSON error shape
    ├── ResourceNotFoundException
    └── EmailAlreadyExistsException
```

### What each layer is responsible for

| Layer        | Responsibility                                                            | Must not do                              |
|--------------|---------------------------------------------------------------------------|------------------------------------------|
| Routes       | Declare the URL paths of the API in one catalog (`ApiRoutes`)             | Contain any logic                        |
| Controller   | Receive HTTP requests, validate input, return DTOs/status codes           | Contain business logic or database code  |
| Service      | Business rules, orchestration, transactions                               | Know about HTTP (request/response)       |
| Repository   | Read/write the database                                                   | Contain business logic                   |
| Model        | Represent database tables                                                 | Be exposed directly over the API         |
| DTO          | Define exactly what goes in/out over HTTP                                 | Hold persistence or business logic       |

Keeping these boundaries is the whole point of the architecture: each layer can
change independently, and the code is easy to find, test, and reason about.

### If you come from Node / Express

The layers map onto the Express structure you may already know:

| Express            | Here                                             |
|--------------------|--------------------------------------------------|
| `routes/`          | `routes/ApiRoutes` (the list of URL paths)       |
| `controllers/`     | `controller/` (`@RestController` handler classes)|
| `models/`          | `model/` + `repository/`                          |

One difference: in Express a route file wires a path to a handler function
(`router.post('/login', authController.login)`). In Spring that wiring is done
by the annotation on the controller method (`@PostMapping(ApiRoutes.LOGIN)`),
so the path lives in `routes/` and the handler lives in `controller/`, joined by
the annotation. `SecurityConfig` also reads its public paths from `ApiRoutes`,
so every URL in the app has a single source of truth.

## Request flow

Every request travels down through the layers and the response travels back up.

```
Client (browser / curl / React)
   │  HTTP request (JSON)
   ▼
[ Security filters ]  JwtAuthenticationFilter validates the token (if present)
   │
   ▼
Controller   @RestController   parse + validate the request body (@Valid)
   │          calls ↓
   ▼
Service      @Service          business logic, runs inside a @Transactional
   │          calls ↓
   ▼
Repository   JpaRepository     Spring Data generates the SQL
   │          maps to ↓
   ▼
Database     (MySQL)           rows are read/written
   ▲
   │  Entity objects come back up; the Service/Mapper converts them to a DTO
   ▼
Controller returns the DTO  ->  Spring serializes it to JSON  ->  Client
```

### Worked example: `POST /api/tasks`

1. `JwtAuthenticationFilter` reads `Authorization: Bearer <token>`, validates it
   with `JwtService`, and puts the user into Spring Security's context. If the
   token is missing/invalid, `RestAuthenticationEntryPoint` returns a JSON 401.
2. `TaskController.create()` receives the JSON body as a `TaskRequest`. `@Valid`
   triggers validation; if it fails, `GlobalExceptionHandler` returns a 400 with
   per-field messages.
3. The controller reads the logged-in user via `@AuthenticationPrincipal` and
   calls `TaskService.create(request, email)`.
4. `TaskService` loads the owning `User` (via `UserRepository`), builds a `Task`
   entity, and saves it with `TaskRepository.save(...)` inside a transaction.
5. `TaskMapper` converts the saved `Task` entity into a `TaskResponse` DTO.
6. The controller returns the DTO with status `201 Created`; Spring serializes
   it to JSON.

### How authentication works

1. Register or log in → the server verifies credentials and returns a **JWT**.
2. The client sends that token on every request: `Authorization: Bearer <token>`.
3. `JwtAuthenticationFilter` validates the signature and identifies the user for
   that single request. No session is stored on the server ("stateless" auth).
4. Passwords are hashed with **BCrypt**; raw passwords are never stored.

## Configuration

All settings live in `src/main/resources/application.properties`. Key values:

| Property                     | Meaning                                  |
|------------------------------|------------------------------------------|
| `app.jwt.secret`             | Signing key for JWTs (min 32 chars)      |
| `app.jwt.expiration-ms`      | Token lifetime (default 24h)             |
| `app.cors.allowed-origins`   | Frontend origins allowed to call the API |
| `spring.jpa.hibernate.ddl-auto` | How Hibernate manages the schema      |

Secrets and credentials should come from environment variables in real
deployments (`JWT_SECRET`, `DB_USERNAME`, `DB_PASSWORD`), not be committed.

## Run and test

MySQL must be running for all of these (including the tests, which use the same
database). Pass `-DskipTests` to build without running tests.

```bash
# From the backend/ directory:
mvn spring-boot:run                         # start the app
mvn test                                    # run tests (needs MySQL)
mvn clean package                           # build a runnable jar
java -jar target/app-0.0.1-SNAPSHOT.jar     # run the jar
```

- Swagger UI: http://localhost:8080/swagger-ui.html

## Adding a new feature

The Task feature is a complete template. To add, for example, a `Note` feature,
add one class in each layer and keep the same responsibilities:

1. `routes/ApiRoutes` — add the path constants, e.g. `NOTES = "/api/notes"`.
2. `model/Note.java` — the `@Entity`.
3. `repository/NoteRepository.java` — `extends JpaRepository<Note, Long>`.
4. `dto/NoteRequest.java` and `dto/NoteResponse.java` — with validation.
5. `mapper/NoteMapper.java` — entity to DTO.
6. `service/NoteService.java` — business logic.
7. `controller/NoteController.java` — `@RequestMapping(ApiRoutes.NOTES)`, exposes `/api/notes`.

New endpoints under `/api/**` automatically require a valid JWT (see
`SecurityConfig`).

## Extending the backend further

This starter covers the essentials. As a project grows, these are the common
Spring Boot additions, roughly in order of how often they are needed:

- **Database migrations** — replace `ddl-auto=update` with
  [Flyway](https://flywaydb.org/) or
  [Liquibase](https://www.liquibase.org/) so schema changes are versioned and
  repeatable. Recommended before any real deployment.
- **Pagination and sorting** — accept `Pageable` in repository/controller
  methods to return pages instead of entire tables
  (`Page<Task> findByOwnerId(Long id, Pageable pageable)`).
- **Role-based authorization** — enable `@EnableMethodSecurity` and guard
  endpoints with `@PreAuthorize("hasRole('ADMIN')")`. The `Role` enum and
  `ROLE_` authorities are already in place.
- **Refresh tokens** — issue a short-lived access token plus a long-lived
  refresh token, with a `/api/auth/refresh` endpoint, instead of one 24h token.
- **Spring Boot Actuator** — add `spring-boot-starter-actuator` for health
  checks (`/actuator/health`), metrics, and readiness/liveness probes.
- **Caching** — `spring-boot-starter-cache` with `@Cacheable` (optionally backed
  by Redis) to avoid repeated expensive lookups.
- **Sending email** — `spring-boot-starter-mail` for verification emails,
  password resets, notifications.
- **Scheduled jobs** — `@EnableScheduling` + `@Scheduled` for recurring tasks
  (cleanup, reports, reminders).
- **File uploads** — accept `MultipartFile` and store files on disk or a service
  like Amazon S3.
- **Richer testing** — slice tests with `@WebMvcTest` (controllers) and
  `@DataJpaTest` (repositories), plus
  [Testcontainers](https://testcontainers.com/) to run tests against a real
  database in Docker.
- **Rate limiting** — a filter or a library such as
  [Bucket4j](https://bucket4j.com/) to throttle abusive clients.
- **Structured logging & profiles** — environment-specific
  `application-{profile}.properties` (dev/prod) and JSON logging for production.
- **Containerization** — a `Dockerfile` (or `mvn spring-boot:build-image`) and a
  `docker-compose.yml` to run the API and database together.
- **API documentation groups** — organize the existing springdoc/Swagger output,
  add examples, and publish the OpenAPI spec.

Add these incrementally, only when a project actually needs them.
