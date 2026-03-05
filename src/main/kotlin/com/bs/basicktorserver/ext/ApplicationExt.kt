package com.bs.basicktorserver.ext

import com.bs.basicktorserver.exceptions.UserNotFoundException
import com.bs.basicktorserver.model.ErrorResponse
import io.ktor.http.*
import io.ktor.serialization.gson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.module() {
    // Install your server plugins here
    install(StatusPages) {
        exception<UserNotFoundException> { call, cause ->
            val errorResponse = ErrorResponse(cause.message ?: "User not found")
            call.respond(errorResponse)
        }
        exception<Throwable> { call, cause ->
            cause.printStackTrace()
            call.respond(
                status = HttpStatusCode.InternalServerError,
                message = ErrorResponse("An unexpected error occurred: ${cause.message}").toString()
            )
        }
    }
    install(ContentNegotiation) {
        gson()
    }

    // Register your modularized routes here
    routing {
        pagesRouting()
        userRouting()
    }
}