package com.bs.basicktorserver.routes

import com.bs.basicktorserver.model.Profile
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.pagesRouting() {
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
}
