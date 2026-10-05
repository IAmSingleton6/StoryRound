package com.example.storyround.data.remote

import com.example.storyround.protocol.ClientMessage
import kotlinx.coroutines.flow.Flow

internal interface GameSocketConnection {
    val incomingMessages: Flow<GameSocketIncomingMessage>

    suspend fun send(message: ClientMessage)

    suspend fun close()

    suspend fun closeReason(): String?
}
