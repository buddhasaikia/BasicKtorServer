package com.bs.basicktorserver.model

import kotlinx.serialization.Serializable

@Serializable
data class NoteResponse(val id: Int, val title: String, val content: String)