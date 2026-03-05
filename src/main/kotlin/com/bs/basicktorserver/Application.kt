package com.bs.basicktorserver

import com.bs.basicktorserver.exposed.Users
import com.bs.basicktorserver.model.Profile
import com.bs.basicktorserver.model.RegistrationForm
import io.ktor.http.*
import io.ktor.serialization.gson.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

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
                Users.insert {
                    it[username] = from.username
                    it[email] = from.email
                }
                call.respond(HttpStatusCode.Created, "Registration successful for ${from.username}")
            }
            get("/users") {
                val allUsers = transaction {
                    Users.selectAll().map { row ->
                        RegistrationForm(
                            username = row[Users.username],
                            email = row[Users.email]
                        )

                    }
                }
                call.respond(allUsers)
            }
            put("/users/{id}") {
                // 1. Extract the ID from the URL path
                val userId = call.parameters["id"]

                // 2. Receive the new data from the user
                val updatedInfo = call.receive<RegistrationForm>()

                // (Conceptual) Database update would happen here using userId and updatedInfo

                // 3. Respond with a success status
                call.respond(HttpStatusCode.OK, "User $userId updated successfully!")
            }

            delete("/users/{id}") {
                val userId = call.parameters["id"]
                //Delete operation would happen here using userId
                call.respond(HttpStatusCode.OK, "User $userId deleted successfully!")
            }
        }
    }.start(wait = true)
}
