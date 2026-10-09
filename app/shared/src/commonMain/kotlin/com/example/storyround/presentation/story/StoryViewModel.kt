package com.example.storyround.presentation.story

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storyround.domain.repository.GameRepository
import com.example.storyround.domain.time.TimeProvider
import com.example.storyround.domain.usecase.SubmitContributionUseCase
import com.example.storyround.domain.usecase.SubmitPromptUseCase
import com.example.storyround.presentation.model.MAX_CONTRIBUTION_LENGTH
import com.example.storyround.presentation.mapper.RoomUiMapper
import com.example.storyround.protocol.GameStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
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
) : ViewModel() {
    private val openingPromptDraft = MutableStateFlow("")
    private val contributionDraft = MutableStateFlow("")
    private val now = MutableStateFlow(timeProvider.nowEpochMillis())

    val uiState: StateFlow<StoryUiState> =
        combine(repository.state, openingPromptDraft, contributionDraft, now) { state, prompt, contribution, time ->
            val room = state.room ?: return@combine StoryUiState.Loading
            val roomUi = roomUiMapper.map(room)
            when (room.status) {
                GameStatus.Prompt ->
                    StoryUiState.OpeningPrompt(
                        room = roomUi,
                        openingPromptDraft = prompt,
                        canSubmitOpeningPrompt = (room.openingPlayerId ?: room.hostId) == state.playerId,
                        hostName =
                            roomUi.players
                                .firstOrNull { it.id == (room.openingPlayerId ?: room.hostId) }
                                ?.name
                                .orEmpty(),
                    )
                GameStatus.Playing ->
                    StoryUiState.Playing(
                        room = roomUi,
                        playerId = state.playerId,
                        nowEpochMillis = time,
                        contributionDraft = contribution,
                    )
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
    }

    fun onAction(action: StoryAction) {
        when (action) {
            is StoryAction.OpeningPromptChanged -> openingPromptDraft.value = action.value.take(MAX_CONTRIBUTION_LENGTH)
            StoryAction.SubmitOpeningPrompt -> submitOpeningPrompt()
            is StoryAction.ContributionChanged -> contributionDraft.value = action.value.take(MAX_CONTRIBUTION_LENGTH)
            StoryAction.SubmitContribution -> submitContribution()
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
