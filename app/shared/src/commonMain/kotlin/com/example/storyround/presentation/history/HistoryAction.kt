package com.example.storyround.presentation.history

internal sealed interface HistoryAction {
    data class OpenStory(
        val id: String,
    ) : HistoryAction

    data object Back : HistoryAction
}
