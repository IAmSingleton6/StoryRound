package com.example.storyround.room

import com.example.storyround.protocol.Player
import com.example.storyround.protocol.RoomState
import com.example.storyround.protocol.ServerMessage

internal sealed interface RegistryResult {
    data class Success(
        val room: RoomState,
        val affectedPlayer: Player?,
        val events: List<ServerMessage> = emptyList(),
        val sessionToken: String? = null,
        val isNewPlayer: Boolean = true,
    ) : RegistryResult

    data class Error(
        val code: String,
        val message: String,
    ) : RegistryResult
}
