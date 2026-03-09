package com.bs.basicktorserver.data.repository

import com.bs.basicktorserver.data.models.Notes
import com.bs.basicktorserver.model.NoteResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object NoteRepository {

    fun createNote(userId: Int, title: String, content: String) {
        transaction {
            Notes.insert {
                it[Notes.userId] = userId
                it[Notes.title] = title
                it[Notes.content] = content
            }
        }
    }

    fun getNotesForUser(userId: Int): List<NoteResponse> {
        return transaction {
            Notes.select { Notes.userId eq userId }.map { row ->
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
            } > 0
        }
    }

    fun deleteNote(noteId: Int, userId: Int): Boolean {
        return transaction {
            Notes.deleteWhere { (Notes.id eq noteId) and (Notes.userId eq userId) } > 0
        }
    }
}
