package com.example.storyround.data.remote

internal sealed interface GameConnectionResult {
    data object Connected : GameConnectionResult

    data class Failed(
        val message: String,
    ) : GameConnectionResult
}
