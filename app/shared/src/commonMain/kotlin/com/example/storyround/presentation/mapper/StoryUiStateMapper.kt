package com.example.storyround.presentation.mapper

import com.example.storyround.domain.model.GameState
import com.example.storyround.presentation.story.StoryLocalState
import com.example.storyround.presentation.story.StoryUiState

internal interface StoryUiStateMapper {
    fun map(
        state: GameState,
        local: StoryLocalState,
    ): StoryUiState
}
