package com.example.storyround.game

internal interface RoomTimers {
    fun schedule(
        roomCode: String,
        timer: RoomTimer,
        delayMillis: Long,
        action: suspend () -> Unit,
    )

    fun cancel(
        roomCode: String,
        timer: RoomTimer,
    )

    fun cancelAll(roomCode: String)
}

internal enum class RoomTimer {
    TurnExpiry,
    IdleClose,
}
