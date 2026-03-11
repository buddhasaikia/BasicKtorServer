package com.bs.basicktorserver.routes

import com.bs.basicktorserver.config.Config
import com.bs.basicktorserver.data.repository.UserRepository
import com.bs.basicktorserver.exceptions.isUniqueConstraintViolation
import com.bs.basicktorserver.model.RegisterRequest
import com.bs.basicktorserver.model.RegistrationForm
import com.bs.basicktorserver.model.ErrorResponse
import com.bs.basicktorserver.validation.InputValidator
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
            
            val usernameValidation = InputValidator.validateUsername(registerRequest.username)
            val emailValidation = InputValidator.validateEmail(registerRequest.email)
            val passwordValidation = InputValidator.validatePassword(registerRequest.password)
            
            val allErrors = usernameValidation.errors + emailValidation.errors + passwordValidation.errors
            if (allErrors.isNotEmpty()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("Validation failed: ${allErrors.joinToString(", ")}")
                )
                return@post
            }
            
            if (UserRepository.isUsernameTaken(registerRequest.username)) {
                call.respond(HttpStatusCode.Conflict, ErrorResponse("Username already exists"))
                return@post
            }

            val hashPassword = BCrypt.hashpw(registerRequest.password, BCrypt.gensalt())

            try {
                UserRepository.createUser(registerRequest.username, registerRequest.email, hashPassword)
                call.respond(HttpStatusCode.Created, "Registration successful for ${registerRequest.username}")
            } catch (ex: ExposedSQLException) {
                if (ex.isUniqueConstraintViolation()) {
                    call.respond(HttpStatusCode.Conflict, ErrorResponse("Username already exists"))
                } else {
                    throw ex
                }
            }
        }

        authenticate(Config.JWT_NAME) {
            get {
                val limitParam = call.request.queryParameters["limit"]
                val offsetParam = call.request.queryParameters["offset"]

                val parsedLimit = limitParam?.toIntOrNull()
                val parsedOffset = offsetParam?.toLongOrNull()

                if (parsedLimit != null && (parsedLimit <= 0 || parsedLimit > 100)) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("Query parameter 'limit' must be between 1 and 100")
                    )
                    return@get
                }

                if (parsedOffset != null && parsedOffset < 0L) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("Query parameter 'offset' must be greater than or equal to 0")
                    )
                    return@get
                }

                val limit = parsedLimit ?: 10
                val offset = parsedOffset ?: 0L
                val allUsers = UserRepository.getAllUsers(limit, offset)
                call.respond(allUsers)
            }

            put("/{id}") {
                val userId = call.parameters["id"]?.toIntOrNull()
                if (userId == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("Invalid user ID")
                    )
                    return@put
                }

                val updatedInfo = call.receive<RegistrationForm>()
                
                val usernameValidation = InputValidator.validateUsername(updatedInfo.username)
                val emailValidation = InputValidator.validateEmail(updatedInfo.email)
                
                val allErrors = usernameValidation.errors + emailValidation.errors
                if (allErrors.isNotEmpty()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("Validation failed: ${allErrors.joinToString(", ")}")
                    )
                    return@put
                }

                if (UserRepository.isUsernameTaken(updatedInfo.username, excludeUserId = userId)) {
                    call.respond(
                        HttpStatusCode.Conflict,
                        ErrorResponse("Username '${updatedInfo.username}' is already taken")
                    )
                    return@put
                }

                val wasUpdated = UserRepository.updateUser(userId, updatedInfo.username, updatedInfo.email)

                if (wasUpdated) {
                    call.respond(HttpStatusCode.OK, "User $userId updated successfully!")
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse("User $userId not found")
                    )
                }
            }

            delete("/{id}") {
                val userId = call.parameters["id"]?.toIntOrNull()
                if (userId == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("Invalid user ID")
                    )
                    return@delete
                }

                val wasDeleted = UserRepository.deleteUser(userId)

                if (wasDeleted) {
                    call.respond(HttpStatusCode.OK, "User $userId deleted successfully!")
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse("User $userId not found")
                    )
                }
            }
        }
    }
}
