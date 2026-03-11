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
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlin.time.Duration.Companion.minutes

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

    // Install CORS with configurable allowed origins
    val corsAllowedHosts = environment.config.propertyOrNull("ktor.cors.allowHosts")?.getString()
        ?.split(',')
        ?.map { it.trim() }
        ?.filter { it.isNotEmpty() }
        ?.takeIf { it.isNotEmpty() }
        ?: listOf("http://localhost:8080", "http://localhost:3000")
    
    install(CORS) {
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Options)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        
        corsAllowedHosts.forEach { origin ->
            val parts = origin.split("://", limit = 2)
            if (parts.size == 2) {
                val scheme = parts[0]
                val hostPort = parts[1]
                allowHost(hostPort, listOf(scheme))
            } else {
                allowHost(origin, listOf("http", "https"))
            }
        }
    }

    // Install rate limiting
    install(RateLimit) {
        global {
            rateLimiter(limit = 1000, refillPeriod = 1.minutes)
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