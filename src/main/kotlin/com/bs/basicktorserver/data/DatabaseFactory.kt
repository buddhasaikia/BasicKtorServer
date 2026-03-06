package com.bs.basicktorserver.data

import com.bs.basicktorserver.data.models.Users
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init() {
        // Establish a connection to an in-memory H2 database
        Database.connect(
            url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;",
            driver = "org.h2.Driver"
        )
        // This will create the Users table in the database if it doesn't exist
        transaction {
            SchemaUtils.create(Users)
            Users.insert {
                it[username] = "testuser"
                it[email] = "testuser@gmailcom"
            }
        }
    }
}