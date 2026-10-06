package com.example.storyround.presentation.history

import com.example.storyround.protocol.StorySummary

internal sealed interface HistoryUiState {
    data object Loading : HistoryUiState

    data class Content(
        val stories: List<StorySummary>,
    ) : HistoryUiState

    data class Error(
        val message: String,
    ) : HistoryUiState
}
