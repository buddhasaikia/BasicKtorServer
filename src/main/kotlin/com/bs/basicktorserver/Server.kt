package com.bs.basicktorserver

import com.bs.basicktorserver.model.Profile
import com.bs.basicktorserver.routes.userRouting
import io.ktor.serialization.gson.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun main() {
    embeddedServer(Netty, port = 8080) {
        // Plugins to handle content negotiation and JSON serialization
        install(ContentNegotiation) {
            gson()
        }

        // Define routing for the application
        routing {
            get("/") {
                call.respondText("Hello, Ktor!")
            }
            get("/about") {
                call.respondText("About our API")
            }
            get("/profile") {
                val myProfile = Profile("Alex", 25)
                // Respond with the profile data as JSON
                call.respond(myProfile)
            }
            userRouting()
        }
    }.start(wait = true)
}
