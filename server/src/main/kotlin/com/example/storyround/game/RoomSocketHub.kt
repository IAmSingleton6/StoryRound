package com.example.storyround.game

import com.example.storyround.protocol.ServerMessage
import io.ktor.server.websocket.DefaultWebSocketServerSession

internal interface RoomSocketHub {
    fun subscribe(
        roomCode: String,
        session: DefaultWebSocketServerSession,
    )

    fun unsubscribe(session: DefaultWebSocketServerSession)

    suspend fun send(
        session: DefaultWebSocketServerSession,
        message: ServerMessage,
    )

    suspend fun broadcast(
        roomCode: String,
        message: ServerMessage,
    )
}
