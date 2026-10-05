package com.example.storyround.data.local

import kotlinx.serialization.Serializable

@Serializable
internal data class RemoteSession(
    val roomCode: String,
    val playerId: String,
    val sessionToken: String,
)
