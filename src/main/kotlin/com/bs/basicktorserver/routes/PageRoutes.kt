package com.bs.basicktorserver.routes

import com.bs.basicktorserver.config.Config
import com.bs.basicktorserver.data.models.Users
import com.bs.basicktorserver.data.repository.UserRepository
import com.bs.basicktorserver.model.UserResponse
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.pagesRouting() {
    get("/") {
        call.respondText("Hello, Ktor!")
    }

    get("/about") {
        call.respondText("About our API")
    }

    authenticate(Config.JWT_NAME) {
        get("/profile") {
            val result = getAuthenticatedUser(call)
            if (result !is AuthResult.Success) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    (result as? AuthResult.MissingClaim)?.message
                        ?: (result as AuthResult.UserNotFound).message
                )
                return@get
            }

            val userRow = UserRepository.findByUsername(result.user.username)
            if (userRow == null) {
                call.respond(HttpStatusCode.NotFound, "User not found")
                return@get
            }

            call.respond(
                UserResponse(
                    id = userRow[Users.id],
                    username = userRow[Users.username],
                    email = userRow[Users.email]
                )
            )
        }
    }
}
