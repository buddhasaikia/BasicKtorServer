package com.bs.basicktorserver.data.repository

import com.bs.basicktorserver.data.models.Notes
import com.bs.basicktorserver.data.models.Users
import com.bs.basicktorserver.model.NoteResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object NoteRepository {

    /**
     * Looks up the user by username and creates a note in a single transaction,
     * ensuring atomicity (no race condition if the user is deleted concurrently).
     *
     * @return true if the note was created, false if the user was not found.
     */
    fun createNoteForUsername(username: String, title: String, content: String): Boolean {
        return transaction {
            val userId = Users.select { Users.username eq username }.singleOrNull()?.get(Users.id)
                ?: return@transaction false
            Notes.insert {
                it[Notes.userId] = userId
                it[Notes.title] = title
                it[Notes.content] = content
            }
            true
        }
    }

    fun createNote(userId: Int, title: String, content: String) {
        transaction {
            Notes.insert {
                it[Notes.userId] = userId
                it[Notes.title] = title
                it[Notes.content] = content
            }
        }
    }

    fun getNotesForUser(userId: Int, limit: Int = 10, offset: Long = 0): List<NoteResponse> {
        return transaction {
            Notes.select { Notes.userId eq userId }
                .limit(limit, offset)
                .map { row ->
                    NoteResponse(
                        id = row[Notes.id],
                        title = row[Notes.title],
                        content = row[Notes.content]
                    )
                }
        }
    }

    fun updateNote(noteId: Int, userId: Int, title: String, content: String): Boolean {
        return transaction {
            Notes.update({ (Notes.id eq noteId) and (Notes.userId eq userId) }) {
                it[Notes.title] = title
                it[Notes.content] = content
                it[Notes.updatedAt] = java.time.LocalDateTime.now()
            } > 0
        }
    }

    fun deleteNote(noteId: Int, userId: Int): Boolean {
        return transaction {
            Notes.deleteWhere { (Notes.id eq noteId) and (Notes.userId eq userId) } > 0
        }
    }
}
