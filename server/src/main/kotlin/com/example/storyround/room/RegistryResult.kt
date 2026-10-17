package com.example.storyround.room

import com.example.storyround.protocol.RoomState

internal sealed interface RegistryResult {
    data class Success(
        val room: RoomState,
        val event: RoomEvent,
    ) : RegistryResult

    data class Error(
        val code: String,
        val message: String,
    ) : RegistryResult
}
