package com.example.storyround.game

import com.example.storyround.protocol.ServerMessage
import io.ktor.server.websocket.DefaultWebSocketServerSession

internal interface RoomSocketHub {
    fun bind(
        session: DefaultWebSocketServerSession,
        roomCode: String,
        playerId: String,
    ): PlayerBinding?

    fun unbind(session: DefaultWebSocketServerSession): PlayerBinding?

    fun unbindPlayer(
        roomCode: String,
        playerId: String,
    )

    fun unbindRoom(roomCode: String)

    fun bindingOf(session: DefaultWebSocketServerSession): PlayerBinding?

    fun hasConnections(roomCode: String): Boolean

    suspend fun send(
        session: DefaultWebSocketServerSession,
        message: ServerMessage,
    )

    suspend fun broadcast(
        roomCode: String,
        message: ServerMessage,
    )
}

internal data class PlayerBinding(
    val roomCode: String,
    val playerId: String,
)
