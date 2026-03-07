package com.bs.basicktorserver.routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.bs.basicktorserver.config.Config
import com.bs.basicktorserver.data.models.Users
import com.bs.basicktorserver.model.Profile
import com.bs.basicktorserver.model.RegisterRequest
import com.bs.basicktorserver.model.RegistrationForm
import com.bs.basicktorserver.model.UserCredentials
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
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

fun isValidUser(username: String, password: String): Boolean {
    val isValid = transaction {
        Users.select { Users.username eq username and (Users.password eq password) }.singleOrNull() != null
    }
    return isValid
}

fun Route.userRouting() {
    route("/users") {
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

            transaction {
                Users.insert {
                    it[username] = registerRequest.username
                    it[email] = registerRequest.email
                    it[password] = registerRequest.password
                }
            }
            call.respond(HttpStatusCode.Created, "Registration successful for ${registerRequest.username}")
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
}