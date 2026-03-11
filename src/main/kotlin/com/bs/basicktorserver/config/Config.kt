package com.bs.basicktorserver.config

import io.ktor.server.application.*
import io.ktor.server.config.*
import java.util.*

object Config {
    private lateinit var appConfig: ApplicationConfig
    
    // JWT Configuration
    lateinit var JWT_AUDIENCE: String
    lateinit var JWT_ISSUER: String
    lateinit var JWT_SECRET: String
    const val JWT_NAME = "auth-jwt"
    
    // Server Configuration
    var SERVER_PORT: Int = 8080
    
    // Database Configuration
    lateinit var DATABASE_DRIVER: String
    lateinit var DATABASE_URL: String
    lateinit var DATABASE_USER: String
    lateinit var DATABASE_PASSWORD: String
    
    // Environment Configuration
    lateinit var ENVIRONMENT: String
    var SEED_DATA_ENABLED: Boolean = false
    
    fun init(config: ApplicationConfig) {
        appConfig = config
        
        val environment = System.getenv("ENVIRONMENT")?.takeIf { it.isNotBlank() }
            ?: config.tryGetString("environment") ?: "development"
        ENVIRONMENT = environment
        
        // Load JWT configuration
        JWT_AUDIENCE = System.getenv("JWT_AUDIENCE")?.takeIf { it.isNotBlank() }
            ?: config.tryGetString("jwt.audience") ?: "my-api-audience"
        
        JWT_ISSUER = System.getenv("JWT_ISSUER")?.takeIf { it.isNotBlank() }
            ?: config.tryGetString("jwt.issuer") ?: "my-api-issuer"
        
        JWT_SECRET = System.getenv("JWT_SECRET")?.takeIf { it.isNotBlank() } ?: run {
            val secretFromConfig = config.tryGetString("jwt.secret")
            if (secretFromConfig?.isNotBlank() == true) {
                secretFromConfig
            } else if (ENVIRONMENT == "production") {
                throw IllegalStateException(
                    "JWT_SECRET environment variable or jwt.secret in application.yaml is required in production. " +
                    "Set it to a secure random string (e.g., openssl rand -hex 32) before starting the server."
                )
            } else {
                // Generate ephemeral random secret for development/testing only
                UUID.randomUUID().toString()
            }
        }
        
        // Load server configuration
        SERVER_PORT = System.getenv("SERVER_PORT")?.toIntOrNull()
            ?: config.tryGetString("ktor.deployment.port")?.toIntOrNull() ?: 8080
        
        // Load database configuration
        DATABASE_DRIVER = System.getenv("DATABASE_DRIVER")?.takeIf { it.isNotBlank() }
            ?: config.tryGetString("database.driver") ?: "org.h2.Driver"
        
        DATABASE_URL = System.getenv("DATABASE_URL")?.takeIf { it.isNotBlank() }
            ?: config.tryGetString("database.url") ?: "jdbc:h2:./db/data;DB_CLOSE_DELAY=-1;"
        
        DATABASE_USER = System.getenv("DATABASE_USER")?.takeIf { it.isNotBlank() }
            ?: config.tryGetString("database.user") ?: "root"
        
        DATABASE_PASSWORD = System.getenv("DATABASE_PASSWORD")?.takeIf { it.isNotBlank() }
            ?: config.tryGetString("database.password") ?: ""
        
        // Load seed data configuration
        SEED_DATA_ENABLED = System.getenv("SEED_DATA")?.equals("true", ignoreCase = true) ?: false
    }
    
    // Helper function to safely get string from config
    private fun ApplicationConfig.tryGetString(path: String): String? = try {
        property(path).getString()
    } catch (e: Exception) {
        null
    }
}