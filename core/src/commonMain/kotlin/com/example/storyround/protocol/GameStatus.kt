package com.example.storyround.protocol

import kotlinx.serialization.Serializable

@Serializable
enum class GameStatus {
    Lobby,
    Prompt,
    Playing,
    Finished,
}
