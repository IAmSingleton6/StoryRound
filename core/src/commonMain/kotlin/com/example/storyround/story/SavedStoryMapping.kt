package com.example.storyround.story

import com.example.storyround.protocol.SavedStory

sealed interface SavedStoryMapping {
    data class Mapped(
        val story: SavedStory,
    ) : SavedStoryMapping

    data class Incomplete(
        val reason: String,
    ) : SavedStoryMapping
}
