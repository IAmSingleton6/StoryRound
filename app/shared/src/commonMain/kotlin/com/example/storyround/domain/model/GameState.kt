package com.example.storyround.domain.model

import com.example.storyround.protocol.RoomState

internal data class GameState(
    val room: RoomState? = null,
    val playerId: String? = null,
    val isConnecting: Boolean = false,
    val isConnected: Boolean = false,
    val error: GameError? = null,
)
