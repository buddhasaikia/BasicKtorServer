package com.bs.basicktorserver.routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.bs.basicktorserver.config.Config
import com.bs.basicktorserver.data.models.Users
import com.bs.basicktorserver.data.repository.UserRepository
import com.bs.basicktorserver.model.TokenResponse
import com.bs.basicktorserver.model.UserCredentials
import com.bs.basicktorserver.model.ErrorResponse
import com.bs.basicktorserver.validation.InputValidator
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.mindrot.jbcrypt.BCrypt
import java.util.*

fun Route.authRouting() {
    post("/login") {
        val credentials = call.receive<UserCredentials>()
        
        val usernameValidation = InputValidator.validateUsername(credentials.username)
        val passwordValidation = InputValidator.validatePassword(credentials.password)
        
        if (!usernameValidation.isValid || !passwordValidation.isValid) {
            val errors = usernameValidation.errors + passwordValidation.errors
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("Validation failed: ${errors.joinToString(", ")}")
            )
            return@post
        }
        
        val isValidUser = isValidUser(credentials.username, credentials.password)
        if (!isValidUser) {
            call.respond(
                HttpStatusCode.Unauthorized,
                ErrorResponse("Invalid credentials")
            )
            return@post
        }
        
        val token = JWT.create()
            .withAudience(Config.JWT_AUDIENCE)
            .withIssuer(Config.JWT_ISSUER)
            .withClaim("username", credentials.username)
            .withExpiresAt(Date(System.currentTimeMillis() + 600000))
            .sign(Algorithm.HMAC256(Config.JWT_SECRET))

        call.respond(TokenResponse(token = token))
    }
}

private fun isValidUser(username: String, password: String): Boolean {
    val userRecord = UserRepository.findByUsername(username) ?: return false
    val storedHash = userRecord[Users.password]
    return BCrypt.checkpw(password, storedHash)
}
