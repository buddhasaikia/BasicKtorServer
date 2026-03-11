# Copilot Instructions for BasicKtorServer

## Project Overview

BasicKtorServer is a Kotlin-based REST API built with **Ktor 3.4.0**, featuring JWT authentication, database persistence with Exposed ORM, and H2 in-memory database. The project uses Gradle for build management and includes database migrations via Flyway.

**Key Technologies:**
- **Framework**: Ktor 3.4.0 (Netty engine)
- **Language**: Kotlin 2.3.0 (JVM 21)
- **ORM**: Jetbrains Exposed 0.41.1
- **Database**: H2 (migrations via Flyway)
- **Auth**: JWT with jbcrypt password hashing
- **Port**: 8080

## Build & Test Commands

### Running the Server
```bash
./gradlew run                    # Start the dev server on port 8080
./gradlew runDocker             # Run via Docker (requires buildImage first)
```

### Building
```bash
./gradlew build                  # Build the project
./gradlew buildFatJar            # Create executable JAR with dependencies
./gradlew buildImage             # Build Docker image
```

### Testing
```bash
./gradlew test                   # Run all tests
./gradlew test --tests "*ClassName*" # Run single test class
```

### Other
```bash
./gradlew clean                  # Clean build artifacts
./gradlew check                  # Run quality checks (lint, tests)
```

## Project Structure & Architecture

### Core Layers

**src/main/kotlin/com/bs/basicktorserver/**

- **`Application.kt`** - Main entry point. Sets up:
  - JWT authentication with HS256 algorithm
  - Content negotiation (JSON serialization/deserialization)
  - Global error handling via StatusPages plugin
  - Route registration

- **`config/Config.kt`** - JWT configuration constants (audience, issuer, secret, auth scheme name)

- **`data/`** - Data access layer:
  - `DatabaseFactory.kt` - Initializes database, runs Flyway migrations, seeds test user
  - `models/` - Exposed ORM table definitions (Users, Notes)
  - `repository/` - Data access objects (UserRepository, NoteRepository)

- **`routes/`** - HTTP endpoint definitions:
  - `PageRoutes.kt` - Public endpoints (home, about, protected profile)
  - `AuthRoutes.kt` - Login/registration endpoints
  - `UserRoutes.kt` - User management endpoints
  - `NoteRoutes.kt` - Note CRUD operations
  - `AuthenticatedUserHelper.kt` - JWT extraction and user lookup utilities

- **`model/`** - Request/response DTOs:
  - `*Request.kt` / `*Response.kt` - Serializable data classes for API contracts
  - `ErrorResponse.kt` - Standardized error format

- **`exceptions/`** - Custom exceptions:
  - `UserNotFoundException.kt` - Thrown when user lookup fails
  - `ExceptionExt.kt` - Extension functions for exception handling

### Configuration & Database

- **`src/main/resources/application.yaml`** - Ktor server and database configuration
  - Database driver, connection URL, credentials
  - Server port and module setup

- **`src/main/resources/db/migration/V1__Initial_Schema.sql`** - Flyway migration script
  - Creates `Users` table with username/email/password
  - Creates `Notes` table with foreign key to Users
  - Default test user seed in DatabaseFactory.init()

### Gradle Configuration

- **`build.gradle.kts`** - Dependency management and plugin configuration
  - Kotlin compiler settings (JVM 21 toolchain, serialization plugin)
  - Ktor server and plugins (auth-jwt, content-negotiation, status-pages)
  - Exposed ORM, Flyway, H2, jbcrypt
  - Docker and fat JAR build tasks

- **`gradle.properties`** - Version pins for dependencies (kotlinVersion, ktorVersion, etc.)

## Key Conventions & Patterns

### API Structure
- **Version prefix**: All API routes live under `/v1/` (auth, user, note endpoints)
- **Public routes**: `/` (home), `/about`, non-auth endpoints
- **Protected routes**: Require `Authorization: Bearer <token>` header
- **Error responses**: Standardized `ErrorResponse` object with message field

### JWT & Authentication
- Auth scheme name: `"auth-jwt"` (defined in Config.JWT_NAME)
- Token validation: Uses HS256 with secret from Config
- Claims validation: Checks for non-empty `username` claim
- Failed auth returns 401 with ErrorResponse

### Database & ORM
- **Framework**: Jetbrains Exposed (table-based DSL, not entity-based)
- **Tables**: Defined as Kotlin objects (e.g., `object Users : Table()`) in `data/models/`
- **Transactions**: All DB operations wrapped in `transaction { }` blocks
- **Migrations**: Flyway auto-runs on startup; versioned SQL files in `src/main/resources/db/migration/`
- **Connection pooling**: HikariCP with max pool size of 3
- **Default schema**: H2 in-memory DB at `./db/data`

### Routing Organization
- Routes are modularized as extension functions on `Route` (e.g., `fun Route.authRouting()`)
- Called in main Application.kt: `routing { pagesRouting(); route("/v1") { ... } }`
- Each route module handles a domain (auth, users, notes)

### Data Flow
1. HTTP request → Route handler
2. Extract request body or JWT principal
3. Delegate to repository methods
4. Repository executes transaction with Exposed DSL
5. Response DTO serialized to JSON and returned

### Error Handling
- Custom exceptions (e.g., UserNotFoundException) caught by StatusPages
- Unhandled exceptions logged server-side and return generic 500 response
- All error responses wrapped in ErrorResponse DTO

### Serialization
- Uses `kotlinx.serialization` (not Jackson)
- Data classes annotated with `@Serializable`
- ContentNegotiation plugin handles JSON marshalling

## Testing
- Test framework: JUnit (Gradle configured with `useJUnit()`)
- Test discovery: Classpath scanning in `src/test/kotlin/`
- No dedicated test fixtures yet—see `src/test/kotlin/com/bs/basicktorserver/client/ApiClient.kt` for HTTP client utilities

## Database Seed Data
- Default test user created on first startup:
  - **username**: `testuser`
  - **password**: `password123` (hashed with jbcrypt)
  - **email**: `email@domain.com`
  - Skipped if user already exists (idempotent seeding)

## Common Tasks

### Adding a New Endpoint
1. Create request/response DTOs in `src/main/kotlin/.../model/`
2. Add repository method in `src/main/kotlin/.../data/repository/`
3. Add route handler in `src/main/kotlin/.../routes/`
4. If protected, use `authenticate(Config.JWT_NAME) { ... }`

### Adding a Database Table
1. Create new Exposed table definition in `src/main/kotlin/.../data/models/`
2. Add SQL migration in `src/main/resources/db/migration/V<N>__Description.sql`
3. Create repository if needed in `src/main/kotlin/.../data/repository/`
4. Restart server (Flyway auto-runs migrations)

### Debugging
- Server logs output to console with timestamps
- Database queries logged if Exposed debug mode enabled
- Check `src/main/resources/logback.xml` for logging configuration
- Unhandled exceptions logged with full stack trace before returning generic 500

## MCP Server Configuration

To enhance Copilot's capabilities for this project, configure the following MCP servers:

### 1. Database Inspector (for H2/SQL introspection)
Useful for exploring schema, running queries, and understanding data models.

**Setup:**
```bash
# Via Copilot CLI config file (~/.copilot/config.json or similar):
{
  "mcp_servers": [
    {
      "name": "sqlite",
      "command": "npx",
      "args": ["@modelcontextprotocol/server-sqlite", "db/data.mv.db"]
    }
  ]
}
```

**Alternative: Direct SQL queries**
- Use `./gradlew` to start the server, then H2 console at `http://localhost:8081` (if enabled)
- Or query via Exposed within the application

### 2. Gradle Build Tool (for build introspection)
Useful for understanding dependencies, running specific tasks, and checking build health.

**Setup:**
Enable if your Copilot CLI supports MCP servers for Gradle. Configuration varies by CLI version—check [GitHub Copilot CLI docs](https://github.com/github/copilot-cli).

**Manual alternative:**
- Use `./gradlew dependencies` to inspect dependency tree
- Use `./gradlew tasks` to list all available tasks

## External Resources
- [Ktor Documentation](https://ktor.io/docs/home.html)
- [Exposed ORM Guide](https://github.com/jetbrains/exposed)
- [GitHub Copilot CLI Docs](https://github.com/github/copilot-cli)
- Tutorial included: `ktor_tutorial.md` (covers routing, JSON, database, auth, testing)
