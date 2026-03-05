package com.bs.basicktorserver.ext

import com.bs.basicktorserver.exposed.Users
import com.bs.basicktorserver.model.Profile
import com.bs.basicktorserver.model.RegistrationForm
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

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

fun Route.userRouting() {
    route("/users") {

        get {
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

        post("/register") {
            val from = call.receive<RegistrationForm>()
            println("Received registration form: $from")
            Users.insert {
                it[username] = from.username
                it[email] = from.email
            }
            call.respond(HttpStatusCode.Created, "Registration successful for ${from.username}")
        }

        put("/{id}") {
            // 1. Extract the ID from the URL path
            val userId = call.parameters["id"]

            // 2. Receive the new data from the user
            val updatedInfo = call.receive<RegistrationForm>()

            // (Conceptual) Database update would happen here using userId and updatedInfo

            // 3. Respond with a success status
            call.respond(HttpStatusCode.OK, "User $userId updated successfully!")
        }

        delete("/{id}") {
            val userId = call.parameters["id"]
            //Delete operation would happen here using userId
            call.respond(HttpStatusCode.OK, "User $userId deleted successfully!")
        }
    }
}