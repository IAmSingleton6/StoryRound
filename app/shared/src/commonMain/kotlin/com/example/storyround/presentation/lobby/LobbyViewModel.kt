package com.example.storyround.presentation.lobby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storyround.domain.repository.GameRepository
import com.example.storyround.domain.usecase.StartGameUseCase
import com.example.storyround.presentation.model.toRoomUi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal class LobbyViewModel(
    repository: GameRepository,
    private val startGame: StartGameUseCase,
) : ViewModel() {
    val uiState: StateFlow<LobbyUiState> =
        repository.state
            .map { state ->
                state.room?.toRoomUi()?.let {
                    LobbyUiState(it.roomCode, it.players, state.room.maxPlayers, it.hostId == state.playerId)
                } ?: LobbyUiState()
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LobbyUiState())

    fun onAction(action: LobbyAction) {
        if (action == LobbyAction.StartGame) viewModelScope.launch { startGame() }
    }
}
