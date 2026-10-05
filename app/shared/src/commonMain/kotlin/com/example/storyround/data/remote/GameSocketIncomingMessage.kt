package com.example.storyround.data.remote

import com.example.storyround.protocol.ServerMessage

internal sealed interface GameSocketIncomingMessage {
    data class Received(
        val message: ServerMessage,
    ) : GameSocketIncomingMessage

    class Unreadable : GameSocketIncomingMessage
}
