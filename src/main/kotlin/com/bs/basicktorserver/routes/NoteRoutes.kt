package com.bs.basicktorserver.routes

import com.bs.basicktorserver.config.Config
import com.bs.basicktorserver.data.models.Notes
import com.bs.basicktorserver.data.models.Users
import com.bs.basicktorserver.model.NoteRequest
import com.bs.basicktorserver.model.NoteResponse
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.noteRouting() {
    authenticate(Config.JWT_NAME) {
        route("/notes") {
            post {
                // 2. Extract the username claim from the JWT
                val username = getUserNameFromToken(call)
                if (username == null) {
                    call.respond(HttpStatusCode.Unauthorized, "Invalid token: username claim missing")
                    return@post
                }
                val noteRequest = call.receive<NoteRequest>()
                val wasCreated = transaction {
                    val usrId = Users.select { Users.username eq username }.singleOrNull()?.get(Users.id)
                    if (usrId == null) return@transaction false
                    Notes.insert {
                        it[Notes.userId] = usrId
                        it[Notes.title] = noteRequest.title
                        it[Notes.content] = noteRequest.content
                    }
                    return@transaction true
                }
                if (wasCreated) {
                    call.respond(HttpStatusCode.Created, "Note created successfully for user $username")
                } else {
                    call.respond(HttpStatusCode.InternalServerError, "Failed to create note for user $username")
                }
            }
            get {
                val username = getUserNameFromToken(call)
                if (username == null) {
                    call.respond(HttpStatusCode.Unauthorized, "Invalid token: username claim missing")
                    return@get
                }
                val userNotes = transaction {
                    val usrId = Users.select { Users.username eq username }.singleOrNull()?.get(Users.id)
                    if (usrId == null) return@transaction false
                    Notes.select { Notes.userId eq usrId }.map { row ->
                        NoteResponse(
                            id = row[Notes.id],
                            title = row[Notes.title],
                            content = row[Notes.content]
                        )
                    }
                }
                call.respond(userNotes)
            }

            put("/{id}") {
                val noteId = call.parameters["id"]?.toIntOrNull()
                if (noteId == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid note ID")
                    return@put
                }
                val username = getUserNameFromToken(call)
                if (username == null) {
                    call.respond(HttpStatusCode.Unauthorized, "Invalid token: username claim missing")
                    return@put
                }
                val noteRequest = call.receive<NoteRequest>()
                val wasUpdated = transaction {
                    val usrId = Users.select { Users.username eq username }.singleOrNull()?.get(Users.id)
                    if (usrId == null) return@transaction false
                    Notes.update({ (Notes.id eq noteId) and (Notes.userId eq usrId) }) {
                        it[title] = noteRequest.title
                        it[content] = noteRequest.content
                    } > 0
                }
                if (wasUpdated) {
                    call.respond(HttpStatusCode.OK, "Note $noteId updated successfully for user $username")
                } else {
                    call.respond(HttpStatusCode.NotFound, "Note $noteId not found for user $username")
                }
            }

            delete("/{id}") {
                val noteId = call.parameters["id"]?.toIntOrNull()
                if (noteId == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid note ID")
                    return@delete
                }
                val username = getUserNameFromToken(call)
                if (username == null) {
                    call.respond(HttpStatusCode.Unauthorized, "Invalid token: username claim missing")
                    return@delete
                }
                val wasDeleted = transaction {
                    val usrId = Users.select { Users.username eq username }.singleOrNull()?.get(Users.id)
                    if (usrId == null) return@transaction false
                    Notes.deleteWhere { (Notes.id eq noteId) and (Notes.userId eq usrId) } > 0
                }
                if (wasDeleted) {
                    call.respond(HttpStatusCode.OK, "Note $noteId deleted successfully for user $username")
                } else {
                    call.respond(HttpStatusCode.NotFound, "Note $noteId not found for user $username")
                }
            }
        }
    }
}

fun getUserNameFromToken(call: RoutingCall): String? {
    val principal = call.principal<JWTPrincipal>()
    return principal?.payload?.getClaim("username")?.asString()
}
