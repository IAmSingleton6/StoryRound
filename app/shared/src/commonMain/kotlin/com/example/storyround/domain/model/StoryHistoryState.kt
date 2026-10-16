package com.example.storyround.domain.model

import com.example.storyround.protocol.SavedStory

internal data class StoryHistoryState(
    val stories: List<SavedStory> = emptyList(),
    val selectedStory: SavedStory? = null,
)
