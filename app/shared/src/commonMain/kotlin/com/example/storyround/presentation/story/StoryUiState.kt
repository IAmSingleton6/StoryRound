package com.example.storyround.presentation.story

import com.example.storyround.presentation.model.CONTRIBUTION_WARNING_LENGTH
import com.example.storyround.presentation.model.PlayerUi
import com.example.storyround.presentation.model.RoomUi

internal sealed interface StoryUiState {
    data object Loading : StoryUiState

    data class OpeningPrompt(
        val room: RoomUi,
        val openingPromptDraft: String,
        val openingPlayer: PlayerUi?,
        val isOpeningWriter: Boolean,
        val isReconnecting: Boolean = false,
    ) : StoryUiState {
        val canSubmitOpeningPrompt: Boolean
            get() = isOpeningWriter && openingPromptDraft.isNotBlank()

        val isDraftNearLimit: Boolean
            get() = openingPromptDraft.length >= CONTRIBUTION_WARNING_LENGTH
    }

    data class Playing(
        val room: RoomUi,
        val playerId: String?,
        val nowEpochMillis: Long,
        val contributionDraft: String,
    ) : StoryUiState
}
