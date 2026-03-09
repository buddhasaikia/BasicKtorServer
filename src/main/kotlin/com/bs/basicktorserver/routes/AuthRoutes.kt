package com.bs.basicktorserver.routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.bs.basicktorserver.config.Config
import com.bs.basicktorserver.data.models.Users
import com.bs.basicktorserver.data.repository.UserRepository
import com.bs.basicktorserver.model.TokenResponse
import com.bs.basicktorserver.model.UserCredentials
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.mindrot.jbcrypt.BCrypt
import java.util.*

fun Route.authRouting() {
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

        call.respond(TokenResponse(token = token))
    }
}

private fun isValidUser(username: String, password: String): Boolean {
    val userRecord = UserRepository.findByUsername(username) ?: return false
    val storedHash = userRecord[Users.password]
    return BCrypt.checkpw(password, storedHash)
}
