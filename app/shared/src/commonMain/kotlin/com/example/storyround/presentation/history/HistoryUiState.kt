package com.example.storyround.presentation.history

import com.example.storyround.presentation.model.StoryUi

internal sealed interface HistoryUiState {
    data object Loading : HistoryUiState

    data class Content(
        val stories: List<StoryUi>,
        val error: String? = null,
    ) : HistoryUiState
}
