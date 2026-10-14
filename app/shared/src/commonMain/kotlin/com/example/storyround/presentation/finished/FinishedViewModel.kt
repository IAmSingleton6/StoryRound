package com.example.storyround.presentation.finished

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storyround.domain.repository.GameRepository
import com.example.storyround.domain.usecase.PlayAgainUseCase
import com.example.storyround.presentation.WhileUiSubscribed
import com.example.storyround.presentation.mapper.RoomUiMapper
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal class FinishedViewModel(
    repository: GameRepository,
    private val playAgainUseCase: PlayAgainUseCase,
    private val roomUiMapper: RoomUiMapper,
) : ViewModel() {
    val uiState: StateFlow<FinishedUiState> =
        repository.state
            .map { state ->
                val room = state.room?.let(roomUiMapper::map) ?: return@map FinishedUiState()
                FinishedUiState(
                    segments = room.segments,
                    writers = room.writers,
                    contributionCount = room.contributions.size,
                    host = room.host,
                    playerId = state.playerId,
                    canPlayAgain = room.hostId == state.playerId,
                    isReconnecting = !state.isConnected,
                )
            }.stateIn(viewModelScope, WhileUiSubscribed, FinishedUiState())

    fun onAction(action: FinishedAction) {
        when (action) {
            FinishedAction.PlayAgain -> viewModelScope.launch { playAgainUseCase() }
            FinishedAction.Home -> Unit
        }
    }
}
