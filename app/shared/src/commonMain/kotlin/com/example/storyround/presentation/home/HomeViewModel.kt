package com.example.storyround.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storyround.domain.repository.GameRepository
import com.example.storyround.domain.usecase.CreateRoomUseCase
import com.example.storyround.domain.usecase.JoinRoomUseCase
import com.example.storyround.presentation.WhileUiSubscribed
import com.example.storyround.protocol.MAX_PLAYER_NAME_LENGTH
import com.example.storyround.protocol.UserTextValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class HomeViewModel(
    repository: GameRepository,
    private val createRoomUseCase: CreateRoomUseCase,
    private val joinRoomUseCase: JoinRoomUseCase,
    private val userTextValidator: UserTextValidator,
) : ViewModel() {
    private val formState = MutableStateFlow(HomeUiState())

    val uiState: StateFlow<HomeUiState> =
        combine(formState, repository.state) { form, state ->
            form.copy(isConnecting = state.isConnecting, historyCount = state.history.size, error = state.error)
        }.stateIn(viewModelScope, WhileUiSubscribed, HomeUiState())

    fun onAction(action: HomeAction) {
        when (action) {
            is HomeAction.PlayerNameChanged -> {
                val playerName = action.value.take(MAX_PLAYER_NAME_LENGTH)
                val isValid = userTextValidator.validPlayerName(playerName) != null
                formState.update { it.copy(playerName = playerName, hasValidPlayerName = isValid) }
            }
            is HomeAction.RoomCodeChanged -> formState.update { it.copy(roomCode = action.value.uppercase()) }
            HomeAction.CreateGame -> createGame()
            HomeAction.ShowJoinGame -> formState.update { it.copy(isJoining = true) }
            HomeAction.JoinGame -> joinGame()
            HomeAction.CancelJoin -> formState.update { it.copy(isJoining = false, roomCode = "") }
            HomeAction.OpenHistory -> Unit
        }
    }

    private fun createGame() {
        val current = formState.value
        viewModelScope.launch { createRoomUseCase(current.playerName) }
    }

    private fun joinGame() {
        val current = formState.value
        viewModelScope.launch { joinRoomUseCase(current.roomCode, current.playerName) }
    }
}
