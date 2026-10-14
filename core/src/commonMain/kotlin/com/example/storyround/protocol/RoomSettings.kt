package com.example.storyround.protocol

import kotlinx.serialization.Serializable

@Serializable
data class RoomSettings(
    val rounds: Int,
    val oneWordPerTurn: Boolean = false,
) {
    val maxStoryTextLength: Int
        get() = if (oneWordPerTurn) MAX_WORD_LENGTH else MAX_STORY_TEXT_LENGTH
}
