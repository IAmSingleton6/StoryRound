package com.example.storyround.presentation.finished

import com.example.storyround.presentation.model.StoryUi

internal data class FinishedUiState(
    val story: StoryUi? = null,
    val finishedStoryId: String? = null,
    val canViewStory: Boolean = false,
    val canPlayAgain: Boolean = false,
)
