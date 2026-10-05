package com.example.storyround.data.remote

import com.example.storyround.domain.model.RemoteGameState

internal data class GameSessionContext(
    val state: RemoteGameState,
    val sessionToken: String?,
    val pendingPlayerName: String?,
    val isConnected: Boolean,
)
