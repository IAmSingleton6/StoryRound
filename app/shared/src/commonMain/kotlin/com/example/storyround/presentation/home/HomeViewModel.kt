package com.example.storyround.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storyround.domain.repository.GameRepository
import com.example.storyround.domain.repository.StoryHistoryRepository
import com.example.storyround.presentation.WhileUiSubscribed
import com.example.storyround.presentation.mapper.GameErrorUiMapper
import com.example.storyround.protocol.MAX_PLAYER_NAME_LENGTH
import com.example.storyround.protocol.UserTextValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class HomeViewModel(
    private val gameRepository: GameRepository,
    storyHistoryRepository: StoryHistoryRepository,
    private val userTextValidator: UserTextValidator,
    private val gameErrorUiMapper: GameErrorUiMapper,
) : ViewModel() {
    private val formState = MutableStateFlow(HomeUiState())

    val uiState: StateFlow<HomeUiState> =
        combine(formState, gameRepository.state, storyHistoryRepository.state) { form, state, history ->
            form.copy(
                isConnecting = state.isConnecting,
                historyCount = history.stories.size,
                error = state.error?.let(gameErrorUiMapper::map),
            )
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
        viewModelScope.launch { gameRepository.createRoom(current.playerName) }
    }

    private fun joinGame() {
        val current = formState.value
        viewModelScope.launch { gameRepository.joinRoom(current.roomCode, current.playerName) }
    }
}
