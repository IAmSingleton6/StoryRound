package com.example.storyround.presentation.story

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storyround.domain.idea.StoryIdeaProvider
import com.example.storyround.domain.repository.GameRepository
import com.example.storyround.presentation.WhileUiSubscribed
import com.example.storyround.presentation.mapper.StoryUiStateMapper
import com.example.storyround.protocol.GameStatus
import com.example.storyround.protocol.MAX_STORY_TEXT_LENGTH
import com.example.storyround.protocol.RoomState
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val COUNTDOWN_TICK_MILLIS = 100L
private const val TIME_UP_TOAST_MILLIS = 1_800L

internal class StoryViewModel(
    private val gameRepository: GameRepository,
    private val timeProvider: TimeProvider,
    private val storyUiStateMapper: StoryUiStateMapper,
    private val storyIdeaProvider: StoryIdeaProvider,
) : ViewModel() {
    private val local = MutableStateFlow(StoryLocalState(nowEpochMillis = timeProvider.nowEpochMillis()))

    val uiState: StateFlow<StoryUiState> =
        combine(gameRepository.state, local, storyUiStateMapper::map)
            .stateIn(viewModelScope, WhileUiSubscribed, StoryUiState.Loading)

    init {
        viewModelScope.launch { tickUntilTurnDeadlines() }
        viewModelScope.launch { announceExpiredTurns() }
    }

    fun onAction(action: StoryAction) {
        when (action) {
            is StoryAction.OpeningPromptChanged -> updateOpeningPromptDraft(action.value)
            StoryAction.SubmitOpeningPrompt -> submitOpeningPrompt()
            StoryAction.RandomOpeningPrompt -> showOpeningIdea()
            is StoryAction.ContributionChanged -> updateContributionDraft(action.value)
            StoryAction.SubmitContribution -> submitContribution()
            StoryAction.RandomContribution -> showContributionIdea()
        }
    }

    private fun updateOpeningPromptDraft(draft: String) {
        local.update { it.copy(openingPromptDraft = draft.take(maxDraftLength())) }
    }

    private fun updateContributionDraft(draft: String) {
        local.update { it.copy(contributionDraft = draft.take(maxDraftLength())) }
    }

    private fun maxDraftLength(): Int =
        gameRepository.state.value.room
            ?.settings
            ?.maxStoryTextLength ?: MAX_STORY_TEXT_LENGTH

    private fun submitOpeningPrompt() {
        viewModelScope.launch {
            gameRepository.submitPrompt(local.value.openingPromptDraft)
            updateOpeningPromptDraft("")
        }
    }

    private fun submitContribution() {
        viewModelScope.launch {
            gameRepository.submitContribution(local.value.contributionDraft)
            updateContributionDraft("")
        }
    }

    private fun showOpeningIdea() {
        viewModelScope.launch {
            updateOpeningPromptDraft(storyIdeaProvider.openingIdea(local.value.openingPromptDraft))
        }
    }

    private fun showContributionIdea() {
        viewModelScope.launch {
            updateContributionDraft(storyIdeaProvider.continuationIdea(latestPiece(), local.value.contributionDraft))
        }
    }

    private fun latestPiece(): String {
        val room = gameRepository.state.value.room ?: return ""
        return room.contributions.lastOrNull()?.text ?: room.prompt.orEmpty()
    }

    private suspend fun tickUntilTurnDeadlines() {
        gameRepository.state
            .map { it.room?.turnDeadlineEpochMillis }
            .distinctUntilChanged()
            .collectLatest { deadline ->
                ticker(deadline).collect { now -> local.update { it.copy(nowEpochMillis = now) } }
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

    private suspend fun announceExpiredTurns() {
        var previousTurnNumber: Int? = null
        gameRepository.state
            .map { state -> state.room?.takeIf { it.status == GameStatus.Playing } }
            .filterNotNull()
            .distinctUntilChangedBy(RoomState::turnNumber)
            .collectLatest { room ->
                val expiredTurnNumber = room.turnNumber - 1
                val expired = previousTurnNumber == expiredTurnNumber && room.contributions.none { it.turnNumber == expiredTurnNumber }
                previousTurnNumber = room.turnNumber
                showTimeUpHandOver(room.currentPlayerId?.takeIf { expired })
                if (expired) {
                    val hideAtEpochMillis = timeProvider.nowEpochMillis() + TIME_UP_TOAST_MILLIS
                    while (timeProvider.nowEpochMillis() < hideAtEpochMillis) delay(COUNTDOWN_TICK_MILLIS)
                    showTimeUpHandOver(null)
                }
            }
    }

    private fun showTimeUpHandOver(playerId: String?) {
        local.update { it.copy(timeUpHandOverPlayerId = playerId) }
    }
}
