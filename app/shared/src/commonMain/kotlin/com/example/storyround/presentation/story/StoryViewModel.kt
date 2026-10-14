package com.example.storyround.presentation.story

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storyround.domain.idea.StoryIdeaProvider
import com.example.storyround.domain.model.GameState
import com.example.storyround.domain.repository.GameRepository
import com.example.storyround.domain.usecase.SubmitContributionUseCase
import com.example.storyround.domain.usecase.SubmitPromptUseCase
import com.example.storyround.presentation.WhileUiSubscribed
import com.example.storyround.presentation.mapper.RoomUiMapper
import com.example.storyround.presentation.model.RoomUi
import com.example.storyround.presentation.model.TIME_UP_TOAST_MILLIS
import com.example.storyround.protocol.GameStatus
import com.example.storyround.protocol.MAX_STORY_TEXT_LENGTH
import com.example.storyround.protocol.RoomState
import com.example.storyround.protocol.UserTextValidator
import com.example.storyround.rules.TurnRules
import com.example.storyround.time.TimeProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val COUNTDOWN_TICK_MILLIS = 100L

internal class StoryViewModel(
    private val repository: GameRepository,
    private val submitPromptUseCase: SubmitPromptUseCase,
    private val submitContributionUseCase: SubmitContributionUseCase,
    private val timeProvider: TimeProvider,
    private val roomUiMapper: RoomUiMapper,
    private val turnRules: TurnRules,
    private val storyIdeaProvider: StoryIdeaProvider,
    private val userTextValidator: UserTextValidator,
) : ViewModel() {
    private val openingPromptDraft = MutableStateFlow("")
    private val contributionDraft = MutableStateFlow("")
    private val now = MutableStateFlow(timeProvider.nowEpochMillis())
    private val timeUpHandOverPlayerId = MutableStateFlow<String?>(null)

    val uiState: StateFlow<StoryUiState> =
        combine(
            repository.state,
            openingPromptDraft,
            contributionDraft,
            now,
            timeUpHandOverPlayerId,
        ) { state, prompt, contribution, time, handOverPlayerId ->
            val room = state.room ?: return@combine StoryUiState.Loading
            val roomUi = roomUiMapper.map(room)
            when (room.status) {
                GameStatus.Prompt -> openingPrompt(state, room, roomUi, prompt)
                GameStatus.Playing -> playing(state, room, roomUi, time, contribution, handOverPlayerId)
                GameStatus.Lobby, GameStatus.Finished -> StoryUiState.Loading
            }
        }.stateIn(viewModelScope, WhileUiSubscribed, StoryUiState.Loading)

    init {
        viewModelScope.launch {
            repository.state
                .map { it.room?.turnDeadlineEpochMillis }
                .distinctUntilChanged()
                .collectLatest { deadline -> ticker(deadline).collect { now.value = it } }
        }
        viewModelScope.launch { announceExpiredTurns() }
    }

    fun onAction(action: StoryAction) {
        when (action) {
            is StoryAction.OpeningPromptChanged -> openingPromptDraft.value = action.value.take(maxDraftLength())
            StoryAction.SubmitOpeningPrompt -> submitOpeningPrompt()
            StoryAction.RandomOpeningPrompt -> openingPromptDraft.value = storyIdeaProvider.openingIdea(openingPromptDraft.value)
            is StoryAction.ContributionChanged -> contributionDraft.value = action.value.take(maxDraftLength())
            StoryAction.SubmitContribution -> submitContribution()
            StoryAction.RandomContribution ->
                contributionDraft.value = storyIdeaProvider.continuationIdea(latestPiece(), contributionDraft.value)
        }
    }

    private fun maxDraftLength(): Int =
        repository.state.value.room
            ?.settings
            ?.maxStoryTextLength ?: MAX_STORY_TEXT_LENGTH

    private fun latestPiece(): String {
        val room = repository.state.value.room ?: return ""
        return room.contributions.lastOrNull()?.text ?: room.prompt.orEmpty()
    }

    private fun openingPrompt(
        state: GameState,
        room: RoomState,
        roomUi: RoomUi,
        draft: String,
    ): StoryUiState.OpeningPrompt {
        val openingPlayerId = room.openingPlayerId ?: room.hostId
        return StoryUiState.OpeningPrompt(
            room = roomUi,
            openingPromptDraft = draft,
            openingPlayer = roomUi.players.firstOrNull { it.id == openingPlayerId },
            isOpeningWriter = openingPlayerId == state.playerId,
            isReconnecting = !state.isConnected,
            oneWordPerTurn = room.settings.oneWordPerTurn,
            maxDraftLength = room.settings.maxStoryTextLength,
            isDraftValid = userTextValidator.validStoryText(draft, room.settings) != null,
        )
    }

    private fun playing(
        state: GameState,
        room: RoomState,
        roomUi: RoomUi,
        time: Long,
        contribution: String,
        handOverPlayerId: String?,
    ): StoryUiState.Playing {
        val playerCount = roomUi.players.size.coerceAtLeast(1)
        val nextTurnNumber = room.turnNumber + 1
        val isFinalTurn = turnRules.isComplete(nextTurnNumber, playerCount, room.settings.rounds)
        return StoryUiState.Playing(
            room = roomUi,
            playerId = state.playerId,
            nowEpochMillis = time,
            contributionDraft = contribution,
            segments = roomUi.segments,
            round = turnRules.round(room.turnNumber, playerCount),
            turn = room.turnNumber + 1,
            totalTurns = turnRules.totalTurns(playerCount, room.settings.rounds),
            turnDurationMillis = turnRules.durationMillis(room.turnNumber),
            nextPlayer = if (isFinalTurn) null else roomUi.players.getOrNull(turnRules.playerIndex(nextTurnNumber, playerCount)),
            nextTurnDurationMillis = turnRules.durationMillis(nextTurnNumber),
            timeUpHandOver = roomUi.players.firstOrNull { it.id == handOverPlayerId },
            isReconnecting = !state.isConnected,
            oneWordPerTurn = room.settings.oneWordPerTurn,
            maxDraftLength = room.settings.maxStoryTextLength,
            isDraftValid = userTextValidator.validStoryText(contribution, room.settings) != null,
        )
    }

    private suspend fun announceExpiredTurns() {
        var previousTurnNumber: Int? = null
        repository.state
            .map { state -> state.room?.takeIf { it.status == GameStatus.Playing } }
            .filterNotNull()
            .distinctUntilChangedBy(RoomState::turnNumber)
            .collectLatest { room ->
                val expiredTurnNumber = room.turnNumber - 1
                val expired = previousTurnNumber == expiredTurnNumber && room.contributions.none { it.turnNumber == expiredTurnNumber }
                previousTurnNumber = room.turnNumber
                timeUpHandOverPlayerId.value = room.currentPlayerId?.takeIf { expired }
                if (expired) {
                    delay(TIME_UP_TOAST_MILLIS)
                    timeUpHandOverPlayerId.value = null
                }
            }
    }

    private fun submitOpeningPrompt() {
        viewModelScope.launch {
            submitPromptUseCase(openingPromptDraft.value)
            openingPromptDraft.value = ""
        }
    }

    private fun submitContribution() {
        viewModelScope.launch {
            submitContributionUseCase(contributionDraft.value)
            contributionDraft.value = ""
        }
    }

    private fun ticker(deadline: Long?): Flow<Long> =
        flow {
            while (deadline != null && timeProvider.nowEpochMillis() <= deadline) {
                emit(timeProvider.nowEpochMillis())
                delay(COUNTDOWN_TICK_MILLIS)
            }
            emit(timeProvider.nowEpochMillis())
        }
}
