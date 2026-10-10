package com.example.storyround.presentation.story

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storyround.configuration.GameConfiguration
import com.example.storyround.domain.model.RemoteGameState
import com.example.storyround.domain.repository.GameRepository
import com.example.storyround.domain.time.TimeProvider
import com.example.storyround.domain.usecase.SubmitContributionUseCase
import com.example.storyround.domain.usecase.SubmitPromptUseCase
import com.example.storyround.presentation.mapper.RoomUiMapper
import com.example.storyround.presentation.model.MAX_CONTRIBUTION_LENGTH
import com.example.storyround.presentation.model.RoomUi
import com.example.storyround.presentation.model.TIME_UP_TOAST_MILLIS
import com.example.storyround.presentation.model.storySegments
import com.example.storyround.protocol.GameStatus
import com.example.storyround.protocol.RoomState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
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

internal class StoryViewModel(
    private val repository: GameRepository,
    private val submitPromptUseCase: SubmitPromptUseCase,
    private val submitContributionUseCase: SubmitContributionUseCase,
    private val timeProvider: TimeProvider,
    private val roomUiMapper: RoomUiMapper,
    private val gameConfiguration: GameConfiguration,
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
                GameStatus.Prompt -> {
                    val openingPlayerId = room.openingPlayerId ?: room.hostId
                    StoryUiState.OpeningPrompt(
                        room = roomUi,
                        openingPromptDraft = prompt,
                        openingPlayer = roomUi.players.firstOrNull { it.id == openingPlayerId },
                        isOpeningWriter = openingPlayerId == state.playerId,
                        isReconnecting = !state.isConnected,
                    )
                }
                GameStatus.Playing -> playing(state, room, roomUi, time, contribution, handOverPlayerId)
                else -> StoryUiState.Loading
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StoryUiState.Loading)

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
            is StoryAction.OpeningPromptChanged -> openingPromptDraft.value = action.value.take(MAX_CONTRIBUTION_LENGTH)
            StoryAction.SubmitOpeningPrompt -> submitOpeningPrompt()
            is StoryAction.ContributionChanged -> contributionDraft.value = action.value.take(MAX_CONTRIBUTION_LENGTH)
            StoryAction.SubmitContribution -> submitContribution()
        }
    }

    private fun playing(
        state: RemoteGameState,
        room: RoomState,
        roomUi: RoomUi,
        time: Long,
        contribution: String,
        handOverPlayerId: String?,
    ): StoryUiState.Playing {
        val playerCount = roomUi.players.size.coerceAtLeast(1)
        val totalTurns = room.maxRounds * playerCount
        val nextTurnNumber = room.turnNumber + 1
        return StoryUiState.Playing(
            room = roomUi,
            playerId = state.playerId,
            nowEpochMillis = time,
            contributionDraft = contribution,
            segments = storySegments(roomUi.prompt, roomUi.promptAuthor, roomUi.contributions),
            round = room.turnNumber / playerCount + 1,
            turn = room.turnNumber + 1,
            totalTurns = totalTurns,
            turnDurationMillis = gameConfiguration.turnDurationMillis(room.turnNumber),
            nextPlayer = roomUi.players.takeIf { nextTurnNumber < totalTurns }?.getOrNull(nextTurnNumber % playerCount),
            nextTurnDurationMillis = gameConfiguration.turnDurationMillis(nextTurnNumber),
            timeUpHandOver = roomUi.players.firstOrNull { it.id == handOverPlayerId },
            isReconnecting = !state.isConnected,
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

    private fun ticker(deadline: Long?) =
        flow {
            while (deadline != null && timeProvider.nowEpochMillis() <= deadline) {
                emit(timeProvider.nowEpochMillis())
                delay(100)
            }
            emit(timeProvider.nowEpochMillis())
        }
}
