package com.bs.basicktorserver

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun main() {
    embeddedServer(Netty, port = 8080) {
        // Define routing for the application
        routing {
            get("/") {
                call.respondText("Hello, Ktor!")
            }
        }
    }.start(wait = true)
}
