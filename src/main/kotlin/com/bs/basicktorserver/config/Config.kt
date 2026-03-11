package com.bs.basicktorserver.config

import java.util.*

object Config {
    const val JWT_AUDIENCE = "my-api-audience"
    const val JWT_ISSUER = "my-api-issuer"
    val JWT_SECRET: String = System.getenv("JWT_SECRET") ?: run {
        val isProduction = System.getenv("ENVIRONMENT")?.equals("production", ignoreCase = true) ?: false
        if (isProduction) {
            throw IllegalStateException(
                "JWT_SECRET environment variable is required in production. " +
                "Set it to a secure random string before starting the server."
            )
        }
        // Generate ephemeral random secret for development/testing only
        UUID.randomUUID().toString()
    }
    const val JWT_NAME = "auth-jwt"
}