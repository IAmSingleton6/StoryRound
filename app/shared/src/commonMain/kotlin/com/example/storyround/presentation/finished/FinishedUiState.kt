package com.example.storyround.presentation.finished

import com.example.storyround.presentation.model.PlayerUi
import com.example.storyround.presentation.model.StorySegmentUi
import com.example.storyround.presentation.model.StoryUi
import com.example.storyround.presentation.model.StoryWriterUi

internal data class FinishedUiState(
    val story: StoryUi? = null,
    val segments: List<StorySegmentUi> = emptyList(),
    val writers: List<StoryWriterUi> = emptyList(),
    val host: PlayerUi? = null,
    val playerId: String? = null,
    val finishedStoryId: String? = null,
    val canViewStory: Boolean = false,
    val canPlayAgain: Boolean = false,
    val isReconnecting: Boolean = false,
)
