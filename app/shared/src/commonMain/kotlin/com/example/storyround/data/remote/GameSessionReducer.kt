package com.example.storyround.data.remote

import com.example.storyround.protocol.ServerMessage

internal interface GameSessionReducer {
    fun reduce(
        context: GameSessionContext,
        message: ServerMessage,
    ): GameSessionReduction
}
