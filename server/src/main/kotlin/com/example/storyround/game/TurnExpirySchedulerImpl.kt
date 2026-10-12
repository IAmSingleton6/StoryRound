package com.example.storyround.game

import com.example.storyround.protocol.RoomState
import com.example.storyround.room.RegistryResult
import com.example.storyround.room.RoomRegistry
import com.example.storyround.time.TimeProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal class TurnExpirySchedulerImpl(
    private val registry: RoomRegistry,
    private val scope: CoroutineScope,
    private val timeProvider: TimeProvider,
) : TurnExpiryScheduler {
    override fun schedule(
        room: RoomState,
        onExpired: suspend (RegistryResult.Success) -> Unit,
    ) {
        val deadline = room.turnDeadlineEpochMillis ?: return
        scope.launch {
            delay((deadline - timeProvider.nowEpochMillis()).coerceAtLeast(0))
            registry.expireTurn(room.roomCode, deadline)?.let { expiry -> onExpired(expiry) }
        }
    }
}
