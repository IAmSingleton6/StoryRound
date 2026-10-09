package com.example.storyround.presentation.mapper

import com.example.storyround.presentation.model.StoryUi
import com.example.storyround.protocol.SavedStory

internal interface SavedStoryUiMapper {
    fun map(story: SavedStory): StoryUi
}
