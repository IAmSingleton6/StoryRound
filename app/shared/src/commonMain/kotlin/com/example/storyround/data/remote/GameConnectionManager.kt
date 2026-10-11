package com.example.storyround.data.remote

import com.example.storyround.protocol.ClientMessage

internal interface GameConnectionManager {
    val isConnected: Boolean

    suspend fun connect(
        serverUrl: String,
        onConnected: () -> Unit,
        onIncomingMessage: suspend (GameSocketIncomingMessage) -> Unit,
        onDisconnected: (String?) -> Unit,
    ): GameConnectionResult

    suspend fun send(message: ClientMessage): Boolean

    fun disconnect()

    fun close()
}
