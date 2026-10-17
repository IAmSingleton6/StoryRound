package com.example.storyround.game

import com.example.storyround.room.RegistryResult
import io.ktor.server.websocket.DefaultWebSocketServerSession

internal interface RoomCoordinator {
    suspend fun runForNewRoom(
        sender: DefaultWebSocketServerSession,
        action: () -> RegistryResult,
    )

    suspend fun runInRoom(
        roomCode: String,
        sender: DefaultWebSocketServerSession,
        action: () -> RegistryResult,
    )

    suspend fun connectionClosed(session: DefaultWebSocketServerSession)
}
