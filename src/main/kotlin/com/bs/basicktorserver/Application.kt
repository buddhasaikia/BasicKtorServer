package com.bs.basicktorserver

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.bs.basicktorserver.config.Config
import com.bs.basicktorserver.data.DatabaseFactory
import com.bs.basicktorserver.exceptions.UserNotFoundException
import com.bs.basicktorserver.model.ErrorResponse
import com.bs.basicktorserver.routes.authRouting
import com.bs.basicktorserver.routes.noteRouting
import com.bs.basicktorserver.routes.pagesRouting
import com.bs.basicktorserver.routes.userRouting
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.module() {
    // Initialize your database connection here
    DatabaseFactory.init(environment.config)

    install(Authentication) {
        jwt(Config.JWT_NAME) {
            verifier(
                JWT.require(Algorithm.HMAC256(Config.JWT_SECRET))
                    .withAudience(Config.JWT_AUDIENCE)
                    .withIssuer(Config.JWT_ISSUER)
                    .build()
            )
            validate { credential ->
                if (credential.payload.getClaim("username").asString().isNotEmpty()) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            challenge { defaultScheme, realm ->
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorResponse("Token is not valid or has expired")
                )
            }
        }
    }

    // Install your server plugins here
    install(ContentNegotiation) {
        json()
    }

    // Global error handling
    install(StatusPages) {
        exception<UserNotFoundException> { call, cause ->
            call.respond(
                status = HttpStatusCode.NotFound,
                message = ErrorResponse(cause.message ?: "User not found")
            )
        }
        exception<Throwable> { call, cause ->
            // Log the detailed exception server-side
            call.application.environment.log.error("Unhandled exception: ${cause.message}", cause)
            call.respond(
                status = HttpStatusCode.InternalServerError,
                message = ErrorResponse("An unexpected error occurred. Please try again later.")
            )
        }
    }

    // Register your modularized routes here
    routing {
        pagesRouting()
        route("/v1") {
            authRouting()
            userRouting()
            noteRouting()
        }
    }
}