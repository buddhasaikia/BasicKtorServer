package com.bs.basicktorserver.data

import com.bs.basicktorserver.data.models.Notes
import com.bs.basicktorserver.data.models.Users
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt

object DatabaseFactory {
    fun init() {
        // Establish a connection to an in-memory H2 database
        Database.connect(
            url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;",
            driver = "org.h2.Driver"
        )
        // This will create the Users table in the database if it doesn't exist
        transaction {
            SchemaUtils.create(Users, Notes)
            val hashPassword = BCrypt.hashpw("password123", BCrypt.gensalt())
            Users.insert {
                it[username] = "buddha"
                it[email] = "bsaikia.dev@gmailcom"
                it[password] = hashPassword
            }
        }
    }
}