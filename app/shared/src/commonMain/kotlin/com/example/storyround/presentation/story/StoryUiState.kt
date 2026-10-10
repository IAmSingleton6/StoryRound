package com.example.storyround.presentation.story

import com.example.storyround.presentation.model.CONTRIBUTION_WARNING_LENGTH
import com.example.storyround.presentation.model.PlayerUi
import com.example.storyround.presentation.model.RoomUi
import com.example.storyround.presentation.model.StorySegmentUi
import com.example.storyround.presentation.model.URGENT_TURN_MILLIS

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
        val segments: List<StorySegmentUi> = emptyList(),
        val round: Int = 1,
        val turn: Int = 1,
        val totalTurns: Int = 1,
        val turnDurationMillis: Long = 0,
        val nextPlayer: PlayerUi? = null,
        val nextTurnDurationMillis: Long = 0,
        val timeUpHandOver: PlayerUi? = null,
        val isReconnecting: Boolean = false,
    ) : StoryUiState {
        val activePlayer: PlayerUi?
            get() = room.turn?.player

        val isYourTurn: Boolean
            get() = playerId != null && activePlayer?.id == playerId

        val timeLeftMillis: Long
            get() = ((room.turn?.deadlineEpochMillis ?: nowEpochMillis) - nowEpochMillis).coerceIn(0, turnDurationMillis)

        val timeLeftFraction: Float
            get() = if (turnDurationMillis > 0) timeLeftMillis.toFloat() / turnDurationMillis else 0f

        val isUrgent: Boolean
            get() = timeLeftMillis <= URGENT_TURN_MILLIS

        val canSubmitContribution: Boolean
            get() = isYourTurn && contributionDraft.isNotBlank()

        val isDraftNearLimit: Boolean
            get() = contributionDraft.length >= CONTRIBUTION_WARNING_LENGTH
    }
}
