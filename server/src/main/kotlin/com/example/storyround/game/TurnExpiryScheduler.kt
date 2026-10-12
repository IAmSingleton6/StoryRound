package com.example.storyround.game

import com.example.storyround.protocol.RoomState
import com.example.storyround.room.RegistryResult

internal interface TurnExpiryScheduler {
    fun schedule(
        room: RoomState,
        onExpired: suspend (RegistryResult.Success) -> Unit,
    )
}
