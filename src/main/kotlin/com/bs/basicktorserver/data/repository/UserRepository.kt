package com.bs.basicktorserver.data.repository

import com.bs.basicktorserver.data.models.Users
import com.bs.basicktorserver.model.UserResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object UserRepository {

    fun findByUsername(username: String): ResultRow? {
        return transaction {
            Users.select { Users.username eq username }.singleOrNull()
        }
    }

    fun findIdByUsername(username: String): Int? {
        return transaction {
            Users.select { Users.username eq username }.singleOrNull()?.get(Users.id)
        }
    }

    fun isUsernameTaken(username: String, excludeUserId: Int? = null): Boolean {
        return transaction {
            val query = if (excludeUserId != null) {
                Users.select { (Users.username eq username) and (Users.id neq excludeUserId) }
            } else {
                Users.select { Users.username eq username }
            }
            query.singleOrNull() != null
        }
    }

    fun createUser(username: String, email: String, hashedPassword: String) {
        transaction {
            Users.insert {
                it[Users.username] = username
                it[Users.email] = email
                it[Users.password] = hashedPassword
            }
        }
    }

    fun getAllUsers(limit: Int = 10, offset: Long = 0): List<UserResponse> {
        return transaction {
            Users.selectAll()
                .limit(limit, offset)
                .map { row ->
                    UserResponse(
                        id = row[Users.id],
                        username = row[Users.username],
                        email = row[Users.email]
                    )
                }
        }
    }

    fun updateUser(id: Int, username: String, email: String): Boolean {
        return transaction {
            Users.update({ Users.id eq id }) {
                it[Users.username] = username
                it[Users.email] = email
                it[Users.updatedAt] = java.time.LocalDateTime.now()
            } > 0
        }
    }

    fun deleteUser(id: Int): Boolean {
        return transaction {
            Users.deleteWhere { Users.id eq id } > 0
        }
    }
}
