package com.bs.basicktorserver.model

import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(val id: Int, val username: String, val email: String)
