package com.example.storyround.presentation.finished

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storyround.domain.repository.GameRepository
import com.example.storyround.domain.usecase.OpenSavedStoryUseCase
import com.example.storyround.domain.usecase.PlayAgainUseCase
import com.example.storyround.presentation.model.StoryUi
import com.example.storyround.presentation.model.toRoomUi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal class FinishedViewModel(
    repository: GameRepository,
    private val openSavedStoryUseCase: OpenSavedStoryUseCase,
    private val playAgainUseCase: PlayAgainUseCase,
) : ViewModel() {
    val uiState: StateFlow<FinishedUiState> =
        repository.state
            .map { state ->
                val room = state.room?.toRoomUi()
                val story = room?.let { StoryUi(it.roomCode, it.prompt, it.players, it.contributions) }
                FinishedUiState(
                    story = story,
                    finishedStoryId = state.finishedStoryId,
                    canViewStory = state.finishedStoryId != null,
                    canPlayAgain = room?.hostId == state.playerId,
                )
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FinishedUiState())

    fun onAction(action: FinishedAction) {
        when (action) {
            FinishedAction.ViewStory ->
                uiState.value.finishedStoryId?.let { id ->
                    viewModelScope.launch { openSavedStoryUseCase(id) }
                }
            FinishedAction.PlayAgain -> viewModelScope.launch { playAgainUseCase() }
            FinishedAction.OpenHistory, FinishedAction.Home -> Unit
        }
    }
}
