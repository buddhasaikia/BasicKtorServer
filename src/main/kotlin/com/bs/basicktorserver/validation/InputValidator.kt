package com.bs.basicktorserver.validation

object InputValidator {
    private const val MIN_USERNAME_LENGTH = 3
    private const val MAX_USERNAME_LENGTH = 50
    private const val MIN_PASSWORD_LENGTH = 8
    private const val MAX_PASSWORD_LENGTH = 128
    private const val MAX_EMAIL_LENGTH = 255
    private const val MAX_TITLE_LENGTH = 255
    private const val MAX_CONTENT_LENGTH = 10000
    
    private val USERNAME_PATTERN = Regex("^[a-zA-Z0-9_-]+$")
    private val EMAIL_PATTERN = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    data class ValidationResult(val isValid: Boolean, val errors: List<String> = emptyList())

    fun validateUsername(username: String?): ValidationResult {
        val errors = mutableListOf<String>()
        
        if (username.isNullOrBlank()) {
            errors.add("Username is required")
        } else {
            if (username.length < MIN_USERNAME_LENGTH) {
                errors.add("Username must be at least $MIN_USERNAME_LENGTH characters")
            }
            if (username.length > MAX_USERNAME_LENGTH) {
                errors.add("Username must not exceed $MAX_USERNAME_LENGTH characters")
            }
            if (!username.matches(USERNAME_PATTERN)) {
                errors.add("Username must contain only letters, numbers, underscores, and hyphens")
            }
        }
        
        return ValidationResult(errors.isEmpty(), errors)
    }

    fun validateEmail(email: String?): ValidationResult {
        val errors = mutableListOf<String>()
        
        if (email.isNullOrBlank()) {
            errors.add("Email is required")
        } else {
            if (email.length > MAX_EMAIL_LENGTH) {
                errors.add("Email must not exceed $MAX_EMAIL_LENGTH characters")
            }
            if (!email.matches(EMAIL_PATTERN)) {
                errors.add("Email format is invalid")
            }
        }
        
        return ValidationResult(errors.isEmpty(), errors)
    }

    fun validatePassword(password: String?): ValidationResult {
        val errors = mutableListOf<String>()
        
        if (password.isNullOrBlank()) {
            errors.add("Password is required")
        } else {
            if (password.length < MIN_PASSWORD_LENGTH) {
                errors.add("Password must be at least $MIN_PASSWORD_LENGTH characters")
            }
            if (password.length > MAX_PASSWORD_LENGTH) {
                errors.add("Password must not exceed $MAX_PASSWORD_LENGTH characters")
            }
        }
        
        return ValidationResult(errors.isEmpty(), errors)
    }

    fun validateTitle(title: String?): ValidationResult {
        val errors = mutableListOf<String>()
        
        if (title.isNullOrBlank()) {
            errors.add("Title is required")
        } else if (title.length > MAX_TITLE_LENGTH) {
            errors.add("Title must not exceed $MAX_TITLE_LENGTH characters")
        }
        
        return ValidationResult(errors.isEmpty(), errors)
    }

    fun validateContent(content: String?): ValidationResult {
        val errors = mutableListOf<String>()
        
        if (content.isNullOrBlank()) {
            errors.add("Content is required")
        } else if (content.length > MAX_CONTENT_LENGTH) {
            errors.add("Content must not exceed $MAX_CONTENT_LENGTH characters")
        }
        
        return ValidationResult(errors.isEmpty(), errors)
    }
}
