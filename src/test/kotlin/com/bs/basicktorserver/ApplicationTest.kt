package com.bs.basicktorserver

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.*

class ApplicationTest {

    @Test
    fun testRoot() = testApplication {
        application {
            module()
        }
        val response = client.get("/")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Hello, Ktor!", response.bodyAsText())
    }

    @Test
    fun testInvalidLogin() = testApplication {
        application { module() }
        val response = client.post("/v1/login") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody("""{"username": "invalid", "password": "wrongpassword"}""")
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun testUnauthorizedProfile() = testApplication {
        application { module() }
        val response = client.get("/profile")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
}
