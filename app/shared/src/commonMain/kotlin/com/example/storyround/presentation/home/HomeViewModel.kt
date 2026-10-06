package com.example.storyround.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storyround.domain.repository.GameRepository
import com.example.storyround.domain.usecase.CreateRoomUseCase
import com.example.storyround.domain.usecase.JoinRoomUseCase
import com.example.storyround.protocol.MAX_PLAYER_NAME_LENGTH
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal class HomeViewModel(
    repository: GameRepository,
    private val createRoomUseCase: CreateRoomUseCase,
    private val joinRoomUseCase: JoinRoomUseCase,
) : ViewModel() {
    private val formState = MutableStateFlow(HomeUiState())

    val uiState: StateFlow<HomeUiState> =
        combine(formState, repository.state) { form, state ->
            form.copy(isConnecting = state.isConnecting, error = state.error)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun onAction(action: HomeAction) {
        when (action) {
            is HomeAction.PlayerNameChanged ->
                formState.value = formState.value.copy(playerName = action.value.take(MAX_PLAYER_NAME_LENGTH))
            is HomeAction.RoomCodeChanged ->
                formState.value = formState.value.copy(roomCode = action.value.uppercase())
            HomeAction.Submit -> submit()
            HomeAction.OpenHistory -> Unit
        }
    }

    private fun submit() {
        val current = formState.value

        viewModelScope.launch {
            if (current.roomCode.isNotBlank()) {
                joinRoomUseCase(current.roomCode, current.playerName)
            } else {
                createRoomUseCase(current.playerName)
            }
        }
    }
}
