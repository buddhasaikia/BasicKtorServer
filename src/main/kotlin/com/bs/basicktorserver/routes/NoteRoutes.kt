package com.bs.basicktorserver.routes

import com.bs.basicktorserver.config.Config
import com.bs.basicktorserver.data.repository.NoteRepository
import com.bs.basicktorserver.data.repository.UserRepository
import com.bs.basicktorserver.model.NoteRequest
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.noteRouting() {
    authenticate(Config.JWT_NAME) {
        route("/notes") {
            post {
                val username = getUserNameFromToken(call)
                if (username == null) {
                    call.respond(HttpStatusCode.Unauthorized, "Invalid token: username claim missing")
                    return@post
                }
                val noteRequest = call.receive<NoteRequest>()
                // Atomic: user lookup + note insert in a single transaction
                val wasCreated = NoteRepository.createNoteForUsername(username, noteRequest.title, noteRequest.content)
                if (wasCreated) {
                    call.respond(HttpStatusCode.Created, "Note created successfully for user $username")
                } else {
                    call.respond(HttpStatusCode.Unauthorized, "User not found for the provided token")
                }
            }
            get {
                val username = getUserNameFromToken(call)
                if (username == null) {
                    call.respond(HttpStatusCode.Unauthorized, "Invalid token: username claim missing")
                    return@get
                }
                val userId = UserRepository.findIdByUsername(username)
                if (userId == null) {
                    call.respond(HttpStatusCode.Unauthorized, "User not found for the provided token")
                    return@get
                }
                val userNotes = NoteRepository.getNotesForUser(userId)
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
                val userId = UserRepository.findIdByUsername(username)
                if (userId == null) {
                    call.respond(HttpStatusCode.Unauthorized, "User not found for the provided token")
                    return@put
                }
                val wasUpdated = NoteRepository.updateNote(noteId, userId, noteRequest.title, noteRequest.content)
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
                val userId = UserRepository.findIdByUsername(username)
                if (userId == null) {
                    call.respond(HttpStatusCode.Unauthorized, "User not found for the provided token")
                    return@delete
                }
                val wasDeleted = NoteRepository.deleteNote(noteId, userId)
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
