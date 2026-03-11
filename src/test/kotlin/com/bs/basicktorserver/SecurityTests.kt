package com.bs.basicktorserver

import com.bs.basicktorserver.model.ErrorResponse
import com.bs.basicktorserver.model.RegisterRequest
import com.bs.basicktorserver.model.TokenResponse
import com.bs.basicktorserver.model.UserCredentials
import com.bs.basicktorserver.validation.InputValidator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InputValidationTests {

    @Test
    fun testValidUsernameAccepted() {
        val result = InputValidator.validateUsername("validuser123")
        assertTrue(result.isValid)
        println("✅ Valid username accepted: PASSED")
    }

    @Test
    fun testShortUsernamRejected() {
        val result = InputValidator.validateUsername("ab")
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("at least 3 characters") })
        println("✅ Short username rejected: PASSED")
    }

    @Test
    fun testLongUsernameRejected() {
        val result = InputValidator.validateUsername("a".repeat(51))
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("not exceed 50 characters") })
        println("✅ Long username rejected: PASSED")
    }

    @Test
    fun testInvalidUsernameFormat() {
        val result = InputValidator.validateUsername("invalid@user!")
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("only letters, numbers, underscores, and hyphens") })
        println("✅ Invalid username format rejected: PASSED")
    }

    @Test
    fun testValidEmailAccepted() {
        val result = InputValidator.validateEmail("user@example.com")
        assertTrue(result.isValid)
        println("✅ Valid email accepted: PASSED")
    }

    @Test
    fun testInvalidEmailRejected() {
        val result = InputValidator.validateEmail("invalid-email")
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("Email format is invalid") })
        println("✅ Invalid email rejected: PASSED")
    }

    @Test
    fun testShortPasswordRejected() {
        val result = InputValidator.validatePassword("short")
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("at least 8 characters") })
        println("✅ Short password rejected: PASSED")
    }

    @Test
    fun testValidPasswordAccepted() {
        val result = InputValidator.validatePassword("validpassword123")
        assertTrue(result.isValid)
        println("✅ Valid password accepted: PASSED")
    }

    @Test
    fun testEmptyTitleRejected() {
        val result = InputValidator.validateTitle("")
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("required") })
        println("✅ Empty title rejected: PASSED")
    }

    @Test
    fun testValidTitleAccepted() {
        val result = InputValidator.validateTitle("My Note Title")
        assertTrue(result.isValid)
        println("✅ Valid title accepted: PASSED")
    }

    @Test
    fun testEmptyContentRejected() {
        val result = InputValidator.validateContent("")
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("required") })
        println("✅ Empty content rejected: PASSED")
    }

    @Test
    fun testValidContentAccepted() {
        val result = InputValidator.validateContent("This is my note content")
        assertTrue(result.isValid)
        println("✅ Valid content accepted: PASSED")
    }
}

