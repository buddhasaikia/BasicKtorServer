package com.bs.basicktorserver.routes

import com.bs.basicktorserver.data.repository.UserRepository
import io.ktor.server.auth.jwt.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*

/**
 * Holds the resolved identity of an authenticated user.
 */
data class AuthenticatedUser(val username: String, val userId: Int)

/**
 * Represents the outcome of attempting to resolve the authenticated user.
 */
sealed class AuthResult {
    data class Success(val user: AuthenticatedUser) : AuthResult()
    data class MissingClaim(val message: String = "Invalid token: username claim missing") : AuthResult()
    data class UserNotFound(val message: String = "User not found for the provided token") : AuthResult()
}

/**
 * Extracts the username from the JWT token in the current request.
 *
 * @return the username claim, or null if the token/claim is missing.
 */
fun getUserNameFromToken(call: RoutingCall): String? {
    val principal = call.principal<JWTPrincipal>()
    return principal?.payload?.getClaim("username")?.asString()
}

/**
 * Extracts the username from the JWT and resolves the corresponding
 * database user ID, returning a typed [AuthResult] that preserves
 * the distinct failure cases for proper error reporting.
 */
fun getAuthenticatedUser(call: RoutingCall): AuthResult {
    val username = getUserNameFromToken(call)
        ?: return AuthResult.MissingClaim()
    val userId = UserRepository.findIdByUsername(username)
        ?: return AuthResult.UserNotFound()
    return AuthResult.Success(AuthenticatedUser(username, userId))
}
