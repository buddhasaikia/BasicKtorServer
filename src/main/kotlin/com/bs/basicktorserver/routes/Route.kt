package com.bs.basicktorserver.routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.bs.basicktorserver.config.Config
import com.bs.basicktorserver.data.models.Notes
import com.bs.basicktorserver.data.models.Users
import com.bs.basicktorserver.model.*
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.util.*

fun Route.pagesRouting() {
    get("/") {
        call.respondText("Hello, Ktor!")
    }

    get("/about") {
        call.respondText("About our API")
    }

    get("/profile") {
        val myProfile = Profile("Alex", 25)
        // Respond with the profile data as JSON
        call.respond(myProfile)
    }

    post("/login") {
        // 1. Receive the username and password from the user
        val credentials = call.receive<UserCredentials>()
        val isValidUser = isValidUser(credentials.username, credentials.password)
        if (!isValidUser) {
            call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
            return@post
        }
        // Generate the JWT
        val token = JWT.create()
            .withAudience(Config.JWT_AUDIENCE)
            .withIssuer(Config.JWT_ISSUER)
            .withClaim("username", credentials.username)
            // Tokens expire! Let's say in 600,000 milliseconds (10 minute) for testing
            .withExpiresAt(Date(System.currentTimeMillis() + 600000))
            .sign(Algorithm.HMAC256(Config.JWT_SECRET)) // Sign it securely

        call.respond<HashMap<String, String>>(hashMapOf("token" to token))
    }
}

fun Route.userRouting() {
    route("/users") {
        post("/register") {
            val registerRequest = call.receive<RegisterRequest>()
            println("Received registration form: $registerRequest")
            val isUsernameTaken = transaction {
                Users.select { Users.username eq registerRequest.username }.singleOrNull() != null
            }
            if (isUsernameTaken) {
                call.respond(HttpStatusCode.Conflict, "Username already exists")
                return@post
            }

            val hashPassword = BCrypt.hashpw(registerRequest.password, BCrypt.gensalt())

            transaction {
                Users.insert {
                    it[username] = registerRequest.username
                    it[email] = registerRequest.email
                    it[password] = hashPassword
                }
            }
            call.respond(HttpStatusCode.Created, "Registration successful for ${registerRequest.username}")
        }

        authenticate(Config.JWT_NAME) {
            get {
                val allUsers = transaction {
                    Users.selectAll().map { row ->
                        mapOf(
                            "id" to row[Users.id],
                            "username" to row[Users.username],
                            "email" to row[Users.email]
                        )
                    }
                }
                call.respond(allUsers)
            }
        }

        put("/{id}") {
            // 1. Extract the ID from the URL path
            val userId = call.parameters["id"]

            // 2. Receive the new data from the user
            val updatedInfo = call.receive<RegistrationForm>()

            // (Conceptual) Database update would happen here using userId and updatedInfo

            // 3. Respond with a success status
            call.respond(HttpStatusCode.OK, "User $userId updated successfully!")
        }

        delete("/{id}") {
            val userId = call.parameters["id"]
            //Delete operation would happen here using userId
            call.respond(HttpStatusCode.OK, "User $userId deleted successfully!")
        }
    }

    authenticate(Config.JWT_NAME) {
        post("/notes") {
            // 2. Extract the username claim from the JWT
            val username = getUserNameFromToken(call)
            if (username == null) {
                call.respond(HttpStatusCode.Unauthorized, "Invalid token: username claim missing")
                return@post
            }
            val noteRequest = call.receive<NoteRequest>()
            val wasCreated = transaction {
                val usrId = Users.select { Users.username eq username }.singleOrNull()?.get(Users.id)
                Notes.insert {
                    it[Notes.userId] = usrId ?: 0
                    it[Notes.title] = noteRequest.title
                    it[Notes.content] = noteRequest.content
                }
                return@transaction true
            }
            if (wasCreated) {
                call.respond(HttpStatusCode.Created, "Note created successfully for user $username")
            } else {
                call.respond(HttpStatusCode.InternalServerError, "Failed to create note for user $username")
            }
        }
        get("/notes") {
            val username = getUserNameFromToken(call)
            if (username == null) {
                call.respond(HttpStatusCode.Unauthorized, "Invalid token: username claim missing")
                return@get
            }
            val userNotes = transaction {
                val usrId = Users.select { Users.username eq username }.singleOrNull()?.get(Users.id)
                Notes.select { Notes.userId eq (usrId ?: 0) }.map { row ->
                    NoteResponse(
                        id = row[Notes.id],
                        title = row[Notes.title],
                        content = row[Notes.content]
                    )
                }
            }
            call.respond(userNotes)
        }
    }
}

fun getUserNameFromToken(call: RoutingCall): String? {
    val principal = call.principal<JWTPrincipal>()
    return principal?.payload?.getClaim("username")?.asString()
}

fun isValidUser(username: String, password: String): Boolean {
    val userRecord = transaction {
        Users.select { Users.username eq username }.singleOrNull()
    }
    if (userRecord == null) {
        return false
    }
    val storedHash = userRecord[Users.password]
    val isPasswordMatched = BCrypt.checkpw(password, storedHash)
    return isPasswordMatched
}