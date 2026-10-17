package com.example.storyround.game

import com.example.storyround.protocol.ClientMessage
import io.ktor.server.websocket.DefaultWebSocketServerSession

internal interface ClientMessageHandler {
    suspend fun handle(
        session: DefaultWebSocketServerSession,
        message: ClientMessage,
    )
}
