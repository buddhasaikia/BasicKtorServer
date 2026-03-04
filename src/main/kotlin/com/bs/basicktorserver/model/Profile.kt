package com.bs.basicktorserver.model

import kotlinx.serialization.Serializable

@Serializable
data class Profile(val name: String, val age: Int)