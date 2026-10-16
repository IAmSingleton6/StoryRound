package com.example.storyround.presentation.story

import com.example.storyround.presentation.model.PlayerUi
import com.example.storyround.presentation.model.RoomUi
import com.example.storyround.presentation.model.StorySegmentUi
import com.example.storyround.protocol.MAX_STORY_TEXT_LENGTH

private const val URGENT_TURN_MILLIS = 5_000L
private const val MILLIS_PER_SECOND = 1_000L
private const val MILLIS_PER_TENTH = 100L
private const val TENTHS_PER_SECOND = 10L

internal sealed interface StoryUiState {
    data object Loading : StoryUiState

    data class OpeningPrompt(
        val room: RoomUi,
        val openingPromptDraft: String,
        val openingPlayer: PlayerUi?,
        val isOpeningWriter: Boolean,
        val isReconnecting: Boolean = false,
        val oneWordPerTurn: Boolean = false,
        val maxDraftLength: Int = MAX_STORY_TEXT_LENGTH,
        val isDraftValid: Boolean = false,
    ) : StoryUiState {
        val canSubmitOpeningPrompt: Boolean
            get() = isOpeningWriter && isDraftValid

        val breaksOneWordRule: Boolean
            get() = breaksOneWordRule(openingPromptDraft, oneWordPerTurn, isDraftValid)
    }

    data class Playing(
        val room: RoomUi,
        val activePlayer: PlayerUi,
        val playerId: String?,
        val timeLeftMillis: Long,
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
        val oneWordPerTurn: Boolean = false,
        val maxDraftLength: Int = MAX_STORY_TEXT_LENGTH,
        val isDraftValid: Boolean = false,
    ) : StoryUiState {
        val isYourTurn: Boolean
            get() = playerId != null && activePlayer.id == playerId

        val timeLeftFraction: Float
            get() = if (turnDurationMillis > 0) timeLeftMillis.toFloat() / turnDurationMillis else 0f

        val isUrgent: Boolean
            get() = timeLeftMillis <= URGENT_TURN_MILLIS

        val canSubmitContribution: Boolean
            get() = isYourTurn && isDraftValid

        val breaksOneWordRule: Boolean
            get() = breaksOneWordRule(contributionDraft, oneWordPerTurn, isDraftValid)

        val timeLeftText: String
            get() {
                val tenths = (timeLeftMillis + MILLIS_PER_TENTH - 1) / MILLIS_PER_TENTH
                return "${tenths / TENTHS_PER_SECOND}.${tenths % TENTHS_PER_SECOND}"
            }

        val turnSeconds: Long
            get() = turnDurationMillis / MILLIS_PER_SECOND

        val nextTurnSeconds: Long
            get() = nextTurnDurationMillis / MILLIS_PER_SECOND

        val newestAuthor: PlayerUi?
            get() = segments.lastOrNull()?.author
    }
}

private fun breaksOneWordRule(
    draft: String,
    oneWordPerTurn: Boolean,
    isDraftValid: Boolean,
): Boolean = oneWordPerTurn && draft.isNotBlank() && !isDraftValid
