package com.bs.basicktorserver.routes

import com.bs.basicktorserver.data.repository.UserRepository
import io.ktor.server.auth.jwt.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*

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
 * Convenience: extracts the username from the JWT and resolves
 * the corresponding database user ID in one call.
 *
 * @return a [Pair] of (username, userId), or null if the token is
 *         invalid or the user no longer exists in the database.
 */
fun getAuthenticatedUser(call: RoutingCall): Pair<String, Int>? {
    val username = getUserNameFromToken(call) ?: return null
    val userId = UserRepository.findIdByUsername(username) ?: return null
    return username to userId
}
