package com.example.storyround.presentation.mapper

import com.example.storyround.domain.model.GameState
import com.example.storyround.presentation.model.RoomUi
import com.example.storyround.presentation.story.StoryLocalState
import com.example.storyround.presentation.story.StoryUiState
import com.example.storyround.protocol.GameStatus
import com.example.storyround.protocol.RoomState
import com.example.storyround.protocol.UserTextValidator
import com.example.storyround.rules.TurnRules

internal class StoryUiStateMapperImpl(
    private val roomUiMapper: RoomUiMapper,
    private val turnRules: TurnRules,
    private val userTextValidator: UserTextValidator,
) : StoryUiStateMapper {
    override fun map(
        state: GameState,
        local: StoryLocalState,
    ): StoryUiState {
        val room = state.room ?: return StoryUiState.Loading
        val roomUi = roomUiMapper.map(room)
        return when (room.status) {
            GameStatus.Prompt -> openingPrompt(state, room, roomUi, local)
            GameStatus.Playing -> playing(state, room, roomUi, local)
            GameStatus.Lobby, GameStatus.Finished -> StoryUiState.Loading
        }
    }

    private fun openingPrompt(
        state: GameState,
        room: RoomState,
        roomUi: RoomUi,
        local: StoryLocalState,
    ): StoryUiState.OpeningPrompt {
        val openingPlayerId = room.openingPlayerId ?: room.hostId
        return StoryUiState.OpeningPrompt(
            room = roomUi,
            openingPromptDraft = local.openingPromptDraft,
            openingPlayer = roomUi.players.firstOrNull { it.id == openingPlayerId },
            isOpeningWriter = openingPlayerId == state.playerId,
            isReconnecting = !state.isConnected,
            oneWordPerTurn = room.settings.oneWordPerTurn,
            maxDraftLength = room.settings.maxStoryTextLength,
            isDraftValid = userTextValidator.validStoryText(local.openingPromptDraft, room.settings) != null,
        )
    }

    private fun playing(
        state: GameState,
        room: RoomState,
        roomUi: RoomUi,
        local: StoryLocalState,
    ): StoryUiState {
        val activePlayer = roomUi.players.firstOrNull { it.id == room.currentPlayerId } ?: return StoryUiState.Loading
        val playerCount = roomUi.players.size.coerceAtLeast(1)
        val turnNumber = room.turnNumber
        val nextTurnNumber = turnNumber + 1
        val isFinalTurn = turnRules.isComplete(nextTurnNumber, playerCount, room.settings.rounds)
        val turnDurationMillis = turnRules.durationMillis(turnNumber)
        val deadline = roomUi.turn?.deadlineEpochMillis ?: local.nowEpochMillis
        return StoryUiState.Playing(
            room = roomUi,
            activePlayer = activePlayer,
            playerId = state.playerId,
            timeLeftMillis = (deadline - local.nowEpochMillis).coerceIn(0, turnDurationMillis),
            contributionDraft = local.contributionDraft,
            segments = roomUi.segments,
            round = turnRules.round(turnNumber, playerCount),
            turn = turnNumber + 1,
            totalTurns = turnRules.totalTurns(playerCount, room.settings.rounds),
            turnDurationMillis = turnDurationMillis,
            nextPlayer = if (isFinalTurn) null else roomUi.players.getOrNull(turnRules.playerIndex(nextTurnNumber, playerCount)),
            nextTurnDurationMillis = turnRules.durationMillis(nextTurnNumber),
            timeUpHandOver = roomUi.players.firstOrNull { it.id == local.timeUpHandOverPlayerId },
            isReconnecting = !state.isConnected,
            oneWordPerTurn = room.settings.oneWordPerTurn,
            maxDraftLength = room.settings.maxStoryTextLength,
            isDraftValid = userTextValidator.validStoryText(local.contributionDraft, room.settings) != null,
        )
    }
}
