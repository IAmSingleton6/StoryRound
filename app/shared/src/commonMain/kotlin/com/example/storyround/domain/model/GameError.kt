package com.example.storyround.domain.model

internal sealed interface GameError {
    data object ServerUnreachable : GameError

    data object Reconnecting : GameError

    data object UnreadableMessage : GameError

    data object NoRoom : GameError

    data class Server(
        val message: String,
    ) : GameError
}
