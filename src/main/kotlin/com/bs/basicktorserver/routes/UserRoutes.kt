package com.bs.basicktorserver.routes

import com.bs.basicktorserver.config.Config
import com.bs.basicktorserver.data.models.Users
import com.bs.basicktorserver.model.RegisterRequest
import com.bs.basicktorserver.model.RegistrationForm
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.neq
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt

fun Route.userRouting() {
    route("/users") {
        post("/register") {
            val registerRequest = call.receive<RegisterRequest>()
            println("Received registration for username=${registerRequest.username}, email=${registerRequest.email}")
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
            val userId = call.parameters["id"]?.toIntOrNull()
            if (userId == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid user ID")
                return@put
            }

            val updatedInfo = call.receive<RegistrationForm>()

            // Check if the new username is already taken by another user
            val isUsernameTaken = transaction {
                Users.select { (Users.username eq updatedInfo.username) and (Users.id neq userId) }
                    .singleOrNull() != null
            }
            if (isUsernameTaken) {
                call.respond(HttpStatusCode.Conflict, "Username '${updatedInfo.username}' is already taken")
                return@put
            }

            val wasUpdated = transaction {
                Users.update({ Users.id eq userId }) {
                    it[username] = updatedInfo.username
                    it[email] = updatedInfo.email
                } > 0
            }

            if (wasUpdated) {
                call.respond(HttpStatusCode.OK, "User $userId updated successfully!")
            } else {
                call.respond(HttpStatusCode.NotFound, "User $userId not found")
            }
        }

        authenticate(Config.JWT_NAME) {
            delete("/{id}") {
                val userId = call.parameters["id"]?.toIntOrNull()
                if (userId == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid user ID")
                    return@delete
                }

                val wasDeleted = transaction {
                    Users.deleteWhere { Users.id eq userId } > 0
                }

                if (wasDeleted) {
                    call.respond(HttpStatusCode.OK, "User $userId deleted successfully!")
                } else {
                    call.respond(HttpStatusCode.NotFound, "User $userId not found")
                }
            }
        }
    }
}
