package com.example.storyround.game

internal sealed interface StorySaveResult {
    data object NotSaved : StorySaveResult

    data class Saved(
        val storyId: String,
    ) : StorySaveResult

    data object Failed : StorySaveResult
}
