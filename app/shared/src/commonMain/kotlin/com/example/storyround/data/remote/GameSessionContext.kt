package com.example.storyround.data.remote

import com.example.storyround.domain.model.GameState

internal data class GameSessionContext(
    val state: GameState,
    val sessionToken: String?,
    val pendingPlayerName: String?,
    val isConnected: Boolean,
)
