package com.bs.basicktorserver.model

import kotlinx.serialization.Serializable

@Serializable
data class RegistrationForm(val username: String, val email: String)