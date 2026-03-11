package com.bs.basicktorserver.config

object Config {
    const val JWT_AUDIENCE = "my-api-audience"
    const val JWT_ISSUER = "my-api-issuer"
    val JWT_SECRET = System.getenv("JWT_SECRET") ?: "my-auth-secret-key"
    const val JWT_NAME = "auth-jwt"
}