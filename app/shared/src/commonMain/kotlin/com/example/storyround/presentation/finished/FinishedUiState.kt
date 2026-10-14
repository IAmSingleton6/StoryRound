package com.example.storyround.presentation.finished

import com.example.storyround.presentation.model.PlayerUi
import com.example.storyround.presentation.model.StorySegmentUi
import com.example.storyround.presentation.model.StoryWriterUi

internal data class FinishedUiState(
    val segments: List<StorySegmentUi> = emptyList(),
    val writers: List<StoryWriterUi> = emptyList(),
    val contributionCount: Int = 0,
    val host: PlayerUi? = null,
    val playerId: String? = null,
    val canPlayAgain: Boolean = false,
    val isReconnecting: Boolean = false,
)
