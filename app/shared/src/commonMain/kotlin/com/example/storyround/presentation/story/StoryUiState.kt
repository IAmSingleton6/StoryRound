package com.example.storyround.presentation.story

import com.example.storyround.presentation.model.RoomUi

internal sealed interface StoryUiState {
    data object Loading : StoryUiState

    data class OpeningPrompt(
        val room: RoomUi,
        val openingPromptDraft: String,
        val canSubmitOpeningPrompt: Boolean,
        val hostName: String,
    ) : StoryUiState

    data class Playing(
        val room: RoomUi,
        val playerId: String?,
        val nowEpochMillis: Long,
        val contributionDraft: String,
    ) : StoryUiState
}
