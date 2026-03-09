package com.bs.basicktorserver.routes

import com.bs.basicktorserver.config.Config
import com.bs.basicktorserver.data.repository.UserRepository
import com.bs.basicktorserver.exceptions.isUniqueConstraintViolation
import com.bs.basicktorserver.model.RegisterRequest
import com.bs.basicktorserver.model.RegistrationForm
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.mindrot.jbcrypt.BCrypt

fun Route.userRouting() {
    route("/users") {
        post("/register") {
            val registerRequest = call.receive<RegisterRequest>()
            println("Received registration for username=${registerRequest.username}, email=${registerRequest.email}")
            if (UserRepository.isUsernameTaken(registerRequest.username)) {
                call.respond(HttpStatusCode.Conflict, "Username already exists")
                return@post
            }

            val hashPassword = BCrypt.hashpw(registerRequest.password, BCrypt.gensalt())

            try {
                UserRepository.createUser(registerRequest.username, registerRequest.email, hashPassword)
                call.respond(HttpStatusCode.Created, "Registration successful for ${registerRequest.username}")
            } catch (ex: ExposedSQLException) {
                if (ex.isUniqueConstraintViolation()) {
                    call.respond(HttpStatusCode.Conflict, "Username already exists")
                } else {
                    throw ex
                }
            }
        }

        authenticate(Config.JWT_NAME) {
            get {
                val allUsers = UserRepository.getAllUsers()
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
            if (UserRepository.isUsernameTaken(updatedInfo.username, excludeUserId = userId)) {
                call.respond(HttpStatusCode.Conflict, "Username '${updatedInfo.username}' is already taken")
                return@put
            }

            val wasUpdated = UserRepository.updateUser(userId, updatedInfo.username, updatedInfo.email)

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

                val wasDeleted = UserRepository.deleteUser(userId)

                if (wasDeleted) {
                    call.respond(HttpStatusCode.OK, "User $userId deleted successfully!")
                } else {
                    call.respond(HttpStatusCode.NotFound, "User $userId not found")
                }
            }
        }
    }
}
