package com.example.storyround.presentation.history

import com.example.storyround.presentation.model.StoryUi
import com.example.storyround.presentation.model.UiText

internal sealed interface HistoryUiState {
    data object Loading : HistoryUiState

    data class Content(
        val stories: List<StoryUi>,
        val error: UiText? = null,
    ) : HistoryUiState
}
