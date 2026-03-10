package com.bs.basicktorserver.data

import com.bs.basicktorserver.data.models.Notes
import com.bs.basicktorserver.data.models.Users
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.*
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import javax.sql.DataSource

object DatabaseFactory {
    fun init(config: ApplicationConfig) {
        val driverClassName = config.property("database.driver").getString()
        val jdbcUrl = config.property("database.url").getString()
        val user = config.property("database.user").getString()
        val password = config.property("database.password").getString()

        val dataSource = createHikariDataSource(jdbcUrl, driverClassName, user, password)
        
        // Run migrations
        runFlyway(dataSource)

        Database.connect(dataSource)

        // Seed data if needed
        transaction {
            val hashPassword = BCrypt.hashpw("password123", BCrypt.gensalt())
            // Check if test user exists to avoid duplicate seed
            val userExists = !Users.select { Users.username eq "testuser" }.empty()
            if (!userExists) {
                Users.insert {
                    it[Users.username] = "testuser"
                    it[Users.email] = "email@domain.com"
                    it[Users.password] = hashPassword
                }
            }
        }
    }

    private fun createHikariDataSource(
        url: String,
        driver: String,
        user: String,
        pass: String
    ): DataSource {
        val config = HikariConfig().apply {
            driverClassName = driver
            jdbcUrl = url
            username = user
            password = pass
            maximumPoolSize = 3
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }
        return HikariDataSource(config)
    }

    private fun runFlyway(dataSource: DataSource) {
        val flyway = Flyway.configure()
            .dataSource(dataSource)
            .load()
        flyway.migrate()
    }
}