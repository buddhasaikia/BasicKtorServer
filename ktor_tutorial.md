# Building a REST API with Ktor: A Complete Tutorial

Ktor 🌐 is a framework built from the ground up using Kotlin and coroutines. It is designed for creating asynchronous, high-performance client and server applications. What makes it stand out is its lightweight nature and how it lets you structure your application using Kotlin's DSL (Domain Specific Language) to create very readable, modular code.

---

## Table of Contents

1. [Your First Ktor Server](#1-your-first-ktor-server)
2. [Routing Basics](#2-routing-basics)
3. [Responding with JSON](#3-responding-with-json)
4. [Accepting Incoming JSON (POST Requests)](#4-accepting-incoming-json-post-requests)
5. [HTTP Status Codes](#5-http-status-codes)
6. [Database Integration with Exposed](#6-database-integration-with-exposed)
7. [Querying Data](#7-querying-data)
8. [Transactions & Atomicity](#8-transactions--atomicity)
9. [Update & Delete (PUT and DELETE)](#9-update--delete-put-and-delete)
10. [Modularizing Routes](#10-modularizing-routes)
11. [Application Modules](#11-application-modules)
12. [Exception Handling with StatusPages](#12-exception-handling-with-statuspages)
13. [Authentication with JWT](#13-authentication-with-jwt)
14. [Putting It All Together (IntelliJ IDEA Setup)](#14-putting-it-all-together-intellij-idea-setup)
15. [Testing with Postman](#15-testing-with-postman)

---

## 1. Your First Ktor Server

At its core, a Ktor backend receives incoming HTTP requests from clients (like a web browser or a mobile app) and sends back responses. To get a basic server up and running, you need two main components:

- **The Engine** 🚂: Handles the heavy lifting of network connections. Ktor supports several engines, but **Netty** is the most common default.
- **Routing** 🗺️: Tells the server how to respond when a user visits a specific URL path.

Here is a fully functioning Ktor server:

```kotlin
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun main() {
    embeddedServer(Netty, port = 8080) {
        routing {
            get("/") {
                call.respondText("Hello, Ktor!")
            }
        }
    }.start(wait = true)
}
```

We start an `embeddedServer` on port `8080`. Inside the `routing` block, we define what happens when a client makes a **GET** request to the root path (`"/"`). The server responds with the simple text `"Hello, Ktor!"`.

---

## 2. Routing Basics

Adding new routes follows the same pattern. To respond to the path `/about`:

```kotlin
routing {
    get("/about") {
        call.respondText("About our API")
    }
}
```

> **Note:** Kotlin is strictly case-sensitive. Use lowercase for `routing`, `get`, and `call`, and standard straight quotes (`"`) instead of smart quotes (`""`).

---

## 3. Responding with JSON

Most modern APIs communicate using **JSON** (JavaScript Object Notation). To make Ktor automatically convert Kotlin data into JSON, we use **Plugins**.

> **Plugins** are a core concept in Ktor that let you add functionality to your server — such as JSON serialization, authentication, or logging.

### Step 1: Define a Data Class

Use a Kotlin `data class` annotated with `@Serializable` to structure your data:

```kotlin
import kotlinx.serialization.Serializable

@Serializable
data class Profile(val name: String, val age: Int)
```

### Step 2: Use `call.respond()` Instead of `call.respondText()`

When you use `call.respond(myProfile)`, Ktor sees that the `Profile` class has the `@Serializable` tag and converts it to JSON automatically:

```kotlin
get("/profile") {
    val myProfile = Profile("Alex", 25)
    call.respond(myProfile) // Ktor handles the object → JSON conversion
}
```

### Step 3: Install the ContentNegotiation Plugin

This plugin is the translator that tells Ktor to format outgoing data as JSON. Install it **outside** the `routing` block, directly inside `embeddedServer`, so it applies to **all** routes:

```kotlin
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class Profile(val name: String, val age: Int)

fun main() {
    embeddedServer(Netty, port = 8080) {
        // 1. Install the plugin at the server level
        install(ContentNegotiation) {
            json()
        }

        // 2. Define our routes
        routing {
            get("/") {
                call.respondText("Hello, Ktor!")
            }
            get("/profile") {
                val myProfile = Profile("Alex", 25)
                call.respond(myProfile) // Automatically turns into JSON!
            }
        }
    }.start(wait = true)
}
```

Visiting `http://localhost:8080/profile` in your browser will display:

```json
{ "name": "Alex", "age": 25 }
```

---

## 4. Accepting Incoming JSON (POST Requests)

When a user submits data (like a registration form), their app typically sends a **POST** request.

### Step 1: Define the Data Class

```kotlin
@Serializable
data class RegistrationForm(val username: String, val email: String)
```

### Step 2: Use `call.receive<T>()` to Parse Incoming JSON

The method `call.receive<RegistrationForm>()` takes the incoming JSON and converts it into a Kotlin object:

```kotlin
post("/register") {
    // 1. Receive the incoming JSON and convert it to our data class
    val form = call.receive<RegistrationForm>()

    // 2. We can now use the data (e.g., save it to a database)
    println("Received registration for: ${form.username}")

    // 3. Send a response back to the user
    call.respondText("User ${form.username} registered successfully!")
}
```

If a user sends a POST request with `{"username": "Taylor", "email": "taylor@example.com"}`, Ktor automatically builds the `RegistrationForm` object.

---

## 5. HTTP Status Codes

A well-designed API sends back specific HTTP Status Codes to indicate the result:

| Code  | Meaning                              |
| ----- | ------------------------------------ |
| `200` | OK — request was successful          |
| `201` | Created — a new resource was created |
| `404` | Not Found                            |
| `500` | Internal Server Error                |

In Ktor, use the built-in `HttpStatusCode` class:

```kotlin
import io.ktor.http.*

post("/register") {
    val form = call.receive<RegistrationForm>()

    // Respond with the 201 Created status AND our message
    call.respond(HttpStatusCode.Created, "User ${form.username} registered!")
}
```

---

## 6. Database Integration with Exposed

Ktor is "unopinionated" — it doesn't force you to use a specific database. **Exposed** (by JetBrains) is a popular ORM that lets you write database queries using Kotlin code instead of raw SQL.

### Defining a Table

Use a singleton `object` that inherits from `Table`:

```kotlin
import org.jetbrains.exposed.sql.Table

object Users : Table() {
    val id = integer("id").autoIncrement()
    val username = varchar("username", length = 50)
    val email = varchar("email", length = 100)

    override val primaryKey = PrimaryKey(id)
}
```

### Inserting Data

The Exposed function `insert` maps directly from the SQL `INSERT INTO` command:

```kotlin
post("/register") {
    val form = call.receive<RegistrationForm>()

    // Add the new user to the database
    Users.insert { row ->
        row[username] = form.username
        row[email] = form.email
    }

    call.respond(HttpStatusCode.Created, "User registered successfully!")
}
```

Inside the `insert` block, the `row` variable represents the new database row. Match columns (`row[username]`) to incoming data (`form.username`).

---

## 7. Querying Data

Use `selectAll()` to retrieve every record from a table, then `map` the raw rows to Kotlin objects:

```kotlin
get("/users") {
    val allUsers = Users.selectAll().map { row ->
        RegistrationForm(
            username = row[Users.username],
            email = row[Users.email]
        )
    }

    // Ktor turns the list into a JSON array!
    call.respond(allUsers)
}
```

---

## 8. Transactions & Atomicity

**Atomicity** (the "A" in ACID) ensures that a series of database operations either **all succeed** together or **all fail** together.

In Exposed, wrap all database interactions inside a `transaction` block:

```kotlin
import org.jetbrains.exposed.sql.transactions.transaction

get("/users") {
    val allUsers = transaction {
        Users.selectAll().map { row ->
            RegistrationForm(
                username = row[Users.username],
                email = row[Users.email]
            )
        }
    }

    call.respond(allUsers)
}
```

This block automatically opens a database connection, executes the code, and safely closes/commits (or rolls back on error).

---

## 9. Update & Delete (PUT and DELETE)

### Dynamic Path Parameters

Capture dynamic values from URLs using curly braces `{id}`. Extract them with `call.parameters["id"]`:

### PUT — Update a User

```kotlin
put("/users/{id}") {
    val userId = call.parameters["id"]
    val updatedInfo = call.receive<RegistrationForm>()

    // Database update would happen here

    call.respond(HttpStatusCode.OK, "User $userId updated successfully!")
}
```

### DELETE — Remove a User

```kotlin
delete("/users/{id}") {
    val userId = call.parameters["id"]

    // Delete operation would happen here

    call.respond(HttpStatusCode.OK, "User $userId deleted successfully!")
}
```

---

## 10. Modularizing Routes

As an API grows, keeping all routes in a single `main` function becomes unmanageable. Ktor handles modularization using **Kotlin extension functions** on the `Route` class.

### Step 1: Create a Separate Routes File

```kotlin
// UserRoutes.kt
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.http.*

fun Route.userRouting() {

    // The 'route' block groups everything under a common base path
    route("/users") {

        get {
            // Logic to get all users
        }

        post("/register") {
            // Logic to register a new user
        }

        put("/{id}") {
            // Logic to update a user
        }

        delete("/{id}") {
            // Logic to delete a user
        }
    }
}
```

> **Note:** Using `route("/users")` automatically prefixes the endpoints inside it. So `post("/register")` becomes `/users/register`.

### Step 2: Plug Into the Main Server

```kotlin
fun main() {
    embeddedServer(Netty, port = 8080) {
        // ... plugins ...

        routing {
            userRouting() // All user routes are plugged in here!

            // As your app grows, add more modules:
            // productRouting()
            // orderRouting()
        }
    }.start(wait = true)
}
```

---

## 11. Application Modules

While extending `Route` organizes your **endpoints**, extending `Application` organizes your **entire server configuration**.

An Application Module is an extension function on the `Application` class:

```kotlin
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*

fun Application.module() {
    // 1. Install server-wide plugins
    install(ContentNegotiation) {
        json()
    }

    // 2. Register modularized routes
    routing {
        userRouting()
    }
}
```

Your server launch code becomes minimal:

```kotlin
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)
}
```

### Why Use Application Modules?

- **Testing**: Load the same `module()` into a test environment without spinning up a real network engine.
- **Configuration Files**: Production Ktor apps use `application.conf` that simply points to `Application.module` as the entry point.
- **Readability**: The file name `Application.kt` is a convention — Kotlin files can be named anything, but this signals intent to other developers.

---

## 12. Exception Handling with StatusPages

The **StatusPages** plugin acts as a global safety net 🥅. Instead of `try/catch` in every route, it catches exceptions across your entire server and lets you send clean JSON error responses.

### Step 1: Define an Error Response and Custom Exceptions

```kotlin
@Serializable
data class ErrorResponse(val error: String)

class UserNotFoundException(message: String) : Exception(message)
```

### Step 2: Install the Plugin

```kotlin
import io.ktor.server.plugins.statuspages.*
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.application.*

fun Application.module() {
    install(StatusPages) {
        // 1. Catch specific, predictable errors
        exception<UserNotFoundException> { call, cause ->
            call.respond(HttpStatusCode.NotFound, ErrorResponse(cause.message ?: "Not found"))
        }

        // 2. Catch everything else as a fallback
        exception<Throwable> { call, cause ->
            cause.printStackTrace()
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse("Something went wrong on our end!")
            )
        }
    }

    // ... routing goes here ...
}
```

Now, `throw UserNotFoundException("User 999 does not exist")` anywhere in your routes will return:

```json
{ "error": "User 999 does not exist" }
```

with a `404` status — instead of crashing the server.

---

## 13. Authentication with JWT

The **Authentication** plugin secures your routes using JSON Web Tokens (JWT).

### The JWT Analogy 🎫

Think of a JWT like a **wristband at a concert**:

1. **Identity**: User proves who they are (username + password).
2. **The Wristband**: Server verifies credentials and hands back a signed JWT.
3. **Access**: Future requests include this JWT in the header. The server verifies and grants access.

### Step 1: Create the Login Route (The "Ticket Booth")

This route stays **outside** the `authenticate` block — users need it to _get_ their token:

```kotlin
data class UserCredentials(val username: String, val password: String)

post("/login") {
    val credentials = call.receive<UserCredentials>()

    // Check the database (hardcoded for testing)
    if (credentials.username == "testuser" && credentials.password == "password123") {

        // Generate the JWT
        val token = JWT.create()
            .withAudience("my-api-audience")
            .withIssuer("my-api-issuer")
            .withClaim("username", credentials.username)
            .withExpiresAt(Date(System.currentTimeMillis() + 600000)) // 10 minutes
            .sign(Algorithm.HMAC256("my-super-secret-key"))

        call.respond(mapOf("token" to token))
    } else {
        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid credentials"))
    }
}
```

### Step 2: Configure the Authentication Plugin

Install this in your `Application.module()`. The verifier must **exactly match** the credentials used to generate the token:

```kotlin
install(Authentication) {
    jwt("auth-jwt") {

        // 1. Cryptographic check — proves the token came from your server
        verifier(
            JWT.require(Algorithm.HMAC256("my-super-secret-key"))
                .withAudience("my-api-audience")
                .withIssuer("my-api-issuer")
                .build()
        )

        // 2. Logical check — inspect the claims inside the token
        validate { credential ->
            if (credential.payload.getClaim("username").asString().isNotEmpty()) {
                JWTPrincipal(credential.payload) // Token accepted
            } else {
                null // Token rejected
            }
        }

        // 3. Handle unauthorized requests
        challenge { defaultScheme, realm ->
            call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Token is not valid or has expired"))
        }
    }
}
```

### Step 3: Protect Your Routes

Wrap protected routes inside `authenticate("auth-jwt")`:

```kotlin
authenticate("auth-jwt") {

    get("/users") {
        // Only requests with a valid Bearer token reach here
    }

    put("/users/{id}") {
        // Protected
    }

    delete("/users/{id}") {
        // Protected
    }
}
```

---

## 14. Putting It All Together (IntelliJ IDEA Setup)

### Project Dependencies (`build.gradle.kts`)

```kotlin
// Exposed Database Library
implementation("org.jetbrains.exposed:exposed-core:0.41.1")
implementation("org.jetbrains.exposed:exposed-jdbc:0.41.1")

// H2 In-Memory Database Driver
implementation("com.h2database:h2:2.1.214")

// Ktor Authentication & JWT
implementation("io.ktor:ktor-server-auth:$ktorVersion")
implementation("io.ktor:ktor-server-auth-jwt:$ktorVersion")

// Content Negotiation (for JSON parsing)
implementation("io.ktor:ktor-serialization-gson:$ktorVersion")
implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
```

### Project Structure

```
src/main/kotlin/com/bs/basicktorserver/
├── Application.kt              # Application module (plugins + routing)
├── data/
│   ├── DatabaseFactory.kt      # Database connection setup
│   └── models/
│       └── Users.kt            # Table definition
└── routes/
    └── UserRoutes.kt           # Login + User endpoints
```

### `data/models/Users.kt`

```kotlin
package com.bs.basicktorserver.data.models

import org.jetbrains.exposed.sql.Table

object Users : Table() {
    val id = integer("id").autoIncrement()
    val username = varchar("username", length = 50)
    val email = varchar("email", length = 100)

    override val primaryKey = PrimaryKey(id)
}
```

### `data/DatabaseFactory.kt`

```kotlin
package com.bs.basicktorserver.data

import com.bs.basicktorserver.data.models.Users
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init() {
        Database.connect(
            url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver"
        )

        transaction {
            SchemaUtils.create(Users)

            Users.insert {
                it[username] = "testuser"
                it[email] = "testuser@example.com"
            }
        }
    }
}
```

### `Application.kt`

```kotlin
package com.bs.basicktorserver

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.bs.basicktorserver.data.DatabaseFactory
import com.bs.basicktorserver.routes.userRouting
import io.ktor.http.*
import io.ktor.serialization.gson.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.module() {
    // 1. Initialize Database First
    DatabaseFactory.init()

    // 2. Install JSON Serialization
    install(ContentNegotiation) {
        gson { setPrettyPrinting() }
    }

    // 3. Install JWT Authentication
    install(Authentication) {
        jwt("auth-jwt") {
            verifier(
                JWT.require(Algorithm.HMAC256("my-super-secret-key"))
                    .withAudience("my-api-audience")
                    .withIssuer("my-api-issuer")
                    .build()
            )
            validate { credential ->
                if (credential.payload.getClaim("username").asString().isNotEmpty()) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            challenge { defaultScheme, realm ->
                call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("error" to "Token is not valid or has expired")
                )
            }
        }
    }

    // 4. Register Routes
    routing {
        userRouting()
    }
}
```

### `routes/UserRoutes.kt`

```kotlin
package com.bs.basicktorserver.routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.bs.basicktorserver.data.models.Users
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.Date

data class UserCredentials(val username: String, val password: String)

fun Route.userRouting() {

    // Public Route: Generates JWT
    post("/login") {
        val credentials = call.receive<UserCredentials>()

        if (credentials.username == "testuser" && credentials.password == "password123") {
            val token = JWT.create()
                .withAudience("my-api-audience")
                .withIssuer("my-api-issuer")
                .withClaim("username", credentials.username)
                .withExpiresAt(Date(System.currentTimeMillis() + 600000))
                .sign(Algorithm.HMAC256("my-super-secret-key"))

            call.respond(mapOf("token" to token))
        } else {
            call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid credentials"))
        }
    }

    // Protected Routes: Require valid JWT
    authenticate("auth-jwt") {
        route("/users") {
            get {
                val usersList = transaction {
                    Users.selectAll().map {
                        mapOf(
                            "id" to it[Users.id],
                            "username" to it[Users.username],
                            "email" to it[Users.email]
                        )
                    }
                }
                call.respond(usersList)
            }
        }
    }
}
```

---

## 15. Testing with Postman

### Step 1: Getting the Token

| Setting    | Value                         |
| ---------- | ----------------------------- |
| **Method** | `POST`                        |
| **URL**    | `http://localhost:8080/login` |
| **Body**   | Select `raw` → `JSON`         |

```json
{
  "username": "testuser",
  "password": "password123"
}
```

**Result**: A JSON response containing your JWT string.

> **Important:** Make sure the Body type is set to **raw** and the dropdown is changed from "Text" to **JSON**. This tells Postman to send the `Content-Type: application/json` header.

### Step 2: Accessing Protected Data

| Setting           | Value                                               |
| ----------------- | --------------------------------------------------- |
| **Method**        | `GET`                                               |
| **URL**           | `http://localhost:8080/users`                       |
| **Authorization** | Select `Bearer Token` → paste the token from Step 1 |

**Result**: A JSON array displaying the test user from the database.

---

## Quick Reference

| Concept                | Ktor Feature                                    |
| ---------------------- | ----------------------------------------------- |
| Start a server         | `embeddedServer(Netty, port = 8080)`            |
| Define routes          | `routing { get("/path") { ... } }`              |
| Send text              | `call.respondText("...")`                       |
| Send objects as JSON   | `call.respond(myObject)`                        |
| Receive incoming JSON  | `call.receive<MyClass>()`                       |
| Enable JSON            | `install(ContentNegotiation) { json() }`        |
| Extract path params    | `call.parameters["id"]`                         |
| Modularize routes      | `fun Route.myRouting() { ... }`                 |
| Application module     | `fun Application.module() { ... }`              |
| Install a plugin       | `install(PluginName) { ... }`                   |
| Handle errors globally | `install(StatusPages) { exception<T> { ... } }` |
| Protect routes         | `authenticate("config-name") { ... }`           |
| Database transaction   | `transaction { ... }`                           |
