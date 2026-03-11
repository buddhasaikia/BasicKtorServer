package com.bs.basicktorserver.routes

import com.bs.basicktorserver.config.Config
import com.bs.basicktorserver.data.repository.NoteRepository
import com.bs.basicktorserver.model.NoteRequest
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.noteRouting() {
    authenticate(Config.JWT_NAME) {
        route("/notes") {
            post {
                // POST uses createNoteForUsername() for atomicity, so only needs the username
                val username = getUserNameFromToken(call)
                if (username == null) {
                    call.respond(HttpStatusCode.Unauthorized, com.bs.basicktorserver.model.ErrorResponse("Invalid token: username claim missing"))
                    return@post
                }
                val noteRequest = call.receive<NoteRequest>()
                val wasCreated = NoteRepository.createNoteForUsername(username, noteRequest.title, noteRequest.content)
                if (wasCreated) {
                    call.respond(HttpStatusCode.Created, "Note created successfully for user $username")
                } else {
                    call.respond(HttpStatusCode.Unauthorized, com.bs.basicktorserver.model.ErrorResponse("User not found for the provided token"))
                }
            }

            get {
                val result = getAuthenticatedUser(call)
                if (result !is AuthResult.Success) {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        com.bs.basicktorserver.model.ErrorResponse(
                            (result as? AuthResult.MissingClaim)?.message
                                ?: (result as AuthResult.UserNotFound).message
                        )
                    )
                    return@get
                }

                val limitParam = call.request.queryParameters["limit"]
                val offsetParam = call.request.queryParameters["offset"]

                val parsedLimit = limitParam?.toIntOrNull()
                val parsedOffset = offsetParam?.toLongOrNull()

                if (parsedLimit != null && (parsedLimit <= 0 || parsedLimit > 100)) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        com.bs.basicktorserver.model.ErrorResponse("Query parameter 'limit' must be between 1 and 100")
                    )
                    return@get
                }

                if (parsedOffset != null && parsedOffset < 0L) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        com.bs.basicktorserver.model.ErrorResponse("Query parameter 'offset' must be greater than or equal to 0")
                    )
                    return@get
                }

                val limit = parsedLimit ?: 10
                val offset = parsedOffset ?: 0L
                val userNotes = NoteRepository.getNotesForUser(result.user.userId, limit, offset)
                call.respond(userNotes)
            }

            put("/{id}") {
                val noteId = call.parameters["id"]?.toIntOrNull()
                if (noteId == null) {
                    call.respond(HttpStatusCode.BadRequest, com.bs.basicktorserver.model.ErrorResponse("Invalid note ID"))
                    return@put
                }
                val result = getAuthenticatedUser(call)
                if (result !is AuthResult.Success) {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        com.bs.basicktorserver.model.ErrorResponse(
                            (result as? AuthResult.MissingClaim)?.message
                                ?: (result as AuthResult.UserNotFound).message
                        )
                    )
                    return@put
                }
                val user = result.user
                val noteRequest = call.receive<NoteRequest>()
                val wasUpdated = NoteRepository.updateNote(noteId, user.userId, noteRequest.title, noteRequest.content)
                if (wasUpdated) {
                    call.respond(HttpStatusCode.OK, "Note $noteId updated successfully for user ${user.username}")
                } else {
                    call.respond(HttpStatusCode.NotFound, com.bs.basicktorserver.model.ErrorResponse("Note $noteId not found for user ${user.username}"))
                }
            }

            delete("/{id}") {
                val noteId = call.parameters["id"]?.toIntOrNull()
                if (noteId == null) {
                    call.respond(HttpStatusCode.BadRequest, com.bs.basicktorserver.model.ErrorResponse("Invalid note ID"))
                    return@delete
                }
                val result = getAuthenticatedUser(call)
                if (result !is AuthResult.Success) {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        com.bs.basicktorserver.model.ErrorResponse(
                            (result as? AuthResult.MissingClaim)?.message
                                ?: (result as AuthResult.UserNotFound).message
                        )
                    )
                    return@delete
                }
                val user = result.user
                val wasDeleted = NoteRepository.deleteNote(noteId, user.userId)
                if (wasDeleted) {
                    call.respond(HttpStatusCode.OK, "Note $noteId deleted successfully for user ${user.username}")
                } else {
                    call.respond(HttpStatusCode.NotFound, com.bs.basicktorserver.model.ErrorResponse("Note $noteId not found for user ${user.username}"))
                }
            }
        }
    }
}
