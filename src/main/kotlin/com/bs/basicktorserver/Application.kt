package com.bs.basicktorserver

import com.bs.basicktorserver.model.Profile
import com.bs.basicktorserver.model.RegistrationForm
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.gson.gson
import io.ktor.server.application.install
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
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
            post("/register") {
                val from = call.receive<RegistrationForm>()
                println("Received registration form: $from")
                call.respond(HttpStatusCode.Created, "Registration successful for ${from.username}")
            }
        }
    }.start(wait = true)
}
