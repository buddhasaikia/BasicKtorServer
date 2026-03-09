package com.bs.basicktorserver.model

import kotlinx.serialization.Serializable

@Serializable
data class NoteRequest(val title: String, val content: String)