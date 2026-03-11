package com.bs.basicktorserver.client

import com.bs.basicktorserver.model.TokenResponse
import com.bs.basicktorserver.model.UserCredentials
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) = runBlocking {
    val apiClient = ApiClient()
    apiClient.main()
}

class ApiClient {
    suspend fun main() {
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
        }

        val loginResponse = client.post("http://localhost:8080/login") {
            contentType(ContentType.Application.Json)
            setBody(
                UserCredentials(
                    username = "buddha",
                    password = "password123"
                )
            )
        }
        val tokenResponse = loginResponse.body<TokenResponse>()
        val myToken = tokenResponse.token
        println("Successfully logged in! Received token: $myToken")

        val userResponse = client.get("http://localhost:8080/users") {
            header("Authorization", "Bearer $myToken")
        }
        val userListJson: String = userResponse.body()
        println("Received user list: $userListJson")
    }
}
