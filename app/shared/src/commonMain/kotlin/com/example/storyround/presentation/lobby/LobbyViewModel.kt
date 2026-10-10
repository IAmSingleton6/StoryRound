package com.example.storyround.presentation.lobby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storyround.configuration.GameConfiguration
import com.example.storyround.domain.repository.GameRepository
import com.example.storyround.domain.usecase.StartGameUseCase
import com.example.storyround.presentation.mapper.RoomUiMapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal class LobbyViewModel(
    repository: GameRepository,
    private val startGame: StartGameUseCase,
    private val roomUiMapper: RoomUiMapper,
    private val gameConfiguration: GameConfiguration,
) : ViewModel() {
    private val settings = MutableStateFlow(LobbySettings(maxRounds = gameConfiguration.defaultRounds))

    val uiState: StateFlow<LobbyUiState> =
        combine(repository.state, settings) { state, selectedSettings ->
            state.room?.let(roomUiMapper::map)?.let {
                LobbyUiState(
                    roomCode = it.roomCode,
                    players = it.players,
                    maxPlayers = state.room.maxPlayers,
                    hostId = it.hostId,
                    currentPlayerId = state.playerId,
                    openingPlayerId = selectedSettings.openingPlayerId?.takeIf { id -> it.players.any { player -> player.id == id } },
                    maxRounds = selectedSettings.maxRounds,
                    canDecreaseRounds = selectedSettings.maxRounds > gameConfiguration.minRounds,
                    canIncreaseRounds = selectedSettings.maxRounds < gameConfiguration.maxRounds,
                    isReconnecting = !state.isConnected,
                )
            } ?: LobbyUiState()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LobbyUiState())

    fun onAction(action: LobbyAction) {
        when (action) {
            LobbyAction.StartGame -> viewModelScope.launch { startGame(settings.value.openingPlayerId, settings.value.maxRounds) }
            is LobbyAction.OpeningPlayerSelected -> settings.value = settings.value.copy(openingPlayerId = action.playerId)
            is LobbyAction.RoundsChanged -> {
                val rounds = action.value.coerceIn(gameConfiguration.minRounds, gameConfiguration.maxRounds)
                settings.value = settings.value.copy(maxRounds = rounds)
            }
            LobbyAction.Leave -> Unit
        }
    }

    private data class LobbySettings(
        val openingPlayerId: String? = null,
        val maxRounds: Int,
    )
}
