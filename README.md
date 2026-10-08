# Spring Boot + React Starter

A full-stack starter project for building web applications. The **backend** is a
Spring Boot REST API using a layered (MVC) architecture with JWT authentication.
The **frontend** is a minimal React (Vite) application that consumes the API.
Clone it, rename the base package, and start building your own features.

The database is **MySQL**. You can later switch to PostgreSQL, Supabase, or
MongoDB; see [Switching databases](#switching-databases).

## Contents

- [Project structure](#project-structure)
- [Prerequisites and installation](#prerequisites-and-installation)
- [Getting started](#getting-started)
- [API reference](#api-reference)
- [Frontend overview](#frontend-overview)
- [Switching databases](#switching-databases)
- [Troubleshooting](#troubleshooting)
- [Building for production](#building-for-production)

## Project structure

```
Springboot-Boilerplate/
├── backend/      Spring Boot REST API (Java 17, Maven) - see backend/README.md
└── frontend/     React + Vite single-page application
```

The backend is organized **by layer** (routes, controller, service, repository,
model, dto, mapper, security, config, exception). `routes/` is a catalog of every
URL path, so students coming from Node/Express see a familiar routes + controller
+ model split. The full package layout and a step-by-step explanation of how a
request flows through the layers are in the **[backend README](backend/README.md)**.

```
frontend/src/
├── api/          fetch() wrapper (attaches the JWT automatically)
├── auth/         AuthContext (login / register / logout state)
├── components/   NavBar, ProtectedRoute
└── pages/        Login, Register, Tasks
```

## Prerequisites and installation

| Tool    | Version | Purpose                 | Check          |
|---------|---------|-------------------------|----------------|
| JDK     | 17+     | Compile and run backend | `java -version`|
| Maven   | 3.8+    | Build the backend       | `mvn -version` |
| Node.js | 18+     | Run the frontend        | `node -v`      |
| MySQL   | 8+      | Database                | `mysql --version` |

### Download pages

- JDK (Temurin / Adoptium): https://adoptium.net/temurin/releases/
- Apache Maven: https://maven.apache.org/download.cgi
- Node.js (LTS): https://nodejs.org/en/download
- MySQL Community Server: https://dev.mysql.com/downloads/mysql/

### Install commands

**Linux (Debian / Ubuntu)**

```bash
sudo apt update
sudo apt install -y openjdk-17-jdk maven mysql-server
# Node.js 20 LTS (via NodeSource):
curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
sudo apt install -y nodejs
```

**macOS (Homebrew)** — install Homebrew first from https://brew.sh

```bash
brew install openjdk@17 maven node mysql
brew services start mysql        # start the MySQL server
```

**Windows (winget)** — or use the download pages above

```powershell
winget install EclipseAdoptium.Temurin.17.JDK
winget install Apache.Maven
winget install OpenJS.NodeJS.LTS
winget install Oracle.MySQL
```

After installing, open a new terminal and confirm each `Check` command above
prints a version.

## Getting started

### 1. Backend

Make sure MySQL is installed and running (see
[Prerequisites](#prerequisites-and-installation)), then create the database and
an application user:

```bash
sudo mysql          # on Windows/macOS: mysql -u root -p
```

```sql
CREATE DATABASE app_db;
CREATE USER 'appuser'@'localhost' IDENTIFIED BY 'apppass';
GRANT ALL PRIVILEGES ON app_db.* TO 'appuser'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

Then start the backend with those credentials:

```bash
cd backend
DB_USERNAME=appuser DB_PASSWORD=apppass mvn spring-boot:run
```

The backend starts on http://localhost:8080. Swagger UI (try the API in the
browser) is at http://localhost:8080/swagger-ui.html.

> Instead of passing the environment variables each time, you can put your
> username and password directly in `src/main/resources/application.properties`.

### 2. Frontend

In a second terminal:

```bash
cd frontend
npm install
npm run dev
```

The frontend starts on http://localhost:5173. Open it, register an account, and
manage your tasks. The Vite dev server proxies `/api/*` to the backend, so you
do not deal with CORS during development.

## API reference

| Method | Endpoint             | Auth | Description               |
|--------|----------------------|------|---------------------------|
| POST   | `/api/auth/register` | No   | Create an account, get JWT|
| POST   | `/api/auth/login`    | No   | Log in, get a JWT         |
| GET    | `/api/tasks`         | Yes  | List the current user's tasks |
| GET    | `/api/tasks/{id}`    | Yes  | Get one task              |
| POST   | `/api/tasks`         | Yes  | Create a task             |
| PUT    | `/api/tasks/{id}`    | Yes  | Update a task             |
| DELETE | `/api/tasks/{id}`    | Yes  | Delete a task             |

### Example with curl

```bash
# Register (returns { "token": "...", ... })
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Ada","email":"ada@example.com","password":"secret123"}'

# Save the token, then create a task:
TOKEN=paste-the-token-here
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"title":"Finish assignment","description":"Due Friday","completed":false}'

# List your tasks:
curl http://localhost:8080/api/tasks -H "Authorization: Bearer $TOKEN"
```

A detailed explanation of the backend request flow and authentication is in the
[backend README](backend/README.md).

## Frontend overview

The frontend is intentionally small so it is easy to read:

- `api/client.js` - a thin wrapper around `fetch` that sets the base URL, JSON
  headers, and the `Authorization: Bearer <token>` header automatically.
- `auth/AuthContext.jsx` - React context holding the logged-in user; exposes
  `login`, `register`, and `logout`. The token is kept in `localStorage`.
- `components/ProtectedRoute.jsx` - redirects to `/login` when there is no user.
- `pages/` - `LoginPage`, `RegisterPage`, and `TasksPage` (full CRUD demo).

To point the built frontend at a deployed backend, copy `.env.example` to `.env`
and set `VITE_API_URL`.

## Switching databases

The project uses MySQL, but the architecture does not depend on it. In most
cases you only change `application.properties` and the driver dependency in
`pom.xml`.

### PostgreSQL

1. Add the driver to `backend/pom.xml`:

   ```xml
   <dependency>
     <groupId>org.postgresql</groupId>
     <artifactId>postgresql</artifactId>
     <scope>runtime</scope>
   </dependency>
   ```

2. Update `application.properties`:

   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/app_db
   spring.datasource.username=postgres
   spring.datasource.password=your-password
   spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
   ```

### Supabase (hosted PostgreSQL)

Supabase is PostgreSQL, so follow the PostgreSQL steps and use the connection
string from the Supabase dashboard
(Project → Settings → Database → Connection string → JDBC):

```properties
spring.datasource.url=jdbc:postgresql://db.<project-ref>.supabase.co:5432/postgres
spring.datasource.username=postgres
spring.datasource.password=your-supabase-password
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

Keep the password in an environment variable, not in the file.

### MongoDB (different data model, more than a config change)

MongoDB is a document database and does not use JPA:

1. Replace `spring-boot-starter-data-jpa` with `spring-boot-starter-data-mongodb`
   in `pom.xml`.
2. Entities use `@Document` (not `@Entity`) and `@Id` from
   `org.springframework.data.annotation.Id`; ids are usually `String`.
3. Repositories extend `MongoRepository<Note, String>` instead of `JpaRepository`.
4. Configure the connection:

   ```properties
   spring.data.mongodb.uri=mongodb://localhost:27017/app_db
   ```

The controller, service, and DTO layers stay almost identical, which is the
benefit of keeping business logic out of the data layer.

## Troubleshooting

| Problem | Fix |
|---------|-----|
| `Communications link failure` connecting to MySQL | MySQL is not running, or the database/credentials in `application.properties` are wrong. Start MySQL and verify the database and user exist. |
| `Access denied for user` | Wrong username/password, or the user lacks privileges on `app_db`. Re-check the `CREATE USER` / `GRANT` step. |
| Port 8080 already in use | Change `server.port` in `application.properties`. |
| `npm run dev` fails | Run `npm install` first; Node 18+ is required. |
| Tasks return 401 | The token is missing or expired - log in again. Tokens last 24h by default. |
| CORS error (only if not using the proxy) | Add your frontend origin to `app.cors.allowed-origins`. |

## Building for production

```bash
# Backend -> runnable jar in backend/target/
cd backend && mvn clean package
java -jar target/app-0.0.1-SNAPSHOT.jar

# Frontend -> static files in frontend/dist/
cd frontend && npm run build
```

Before deploying: set a strong `JWT_SECRET`, provide database credentials via
environment variables, and set `spring.jpa.hibernate.ddl-auto=validate`, managing
schema changes with a migration tool such as Flyway instead of `update`.
