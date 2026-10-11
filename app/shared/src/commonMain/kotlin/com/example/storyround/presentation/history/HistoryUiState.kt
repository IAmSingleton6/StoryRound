package com.example.storyround.presentation.history

import com.example.storyround.presentation.model.HistoryStoryUi

internal sealed interface HistoryUiState {
    data object Loading : HistoryUiState

    data class Content(
        val stories: List<HistoryStoryUi>,
    ) : HistoryUiState

    data class Error(
        val message: String,
    ) : HistoryUiState
}
