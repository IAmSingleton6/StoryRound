package com.example.storyround.presentation.lobby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storyround.configuration.GameConfiguration
import com.example.storyround.domain.repository.GameRepository
import com.example.storyround.domain.usecase.StartGameUseCase
import com.example.storyround.presentation.WhileUiSubscribed
import com.example.storyround.presentation.mapper.RoomUiMapper
import com.example.storyround.protocol.RoomSettings
import com.example.storyround.protocol.RoomState
import com.example.storyround.rules.TurnRules
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class LobbyViewModel(
    private val repository: GameRepository,
    private val startGameUseCase: StartGameUseCase,
    private val roomUiMapper: RoomUiMapper,
    private val gameConfiguration: GameConfiguration,
    private val turnRules: TurnRules,
) : ViewModel() {
    private val settings = MutableStateFlow(LobbySettings(roomSettings = RoomSettings(rounds = gameConfiguration.defaultRounds)))

    val uiState: StateFlow<LobbyUiState> =
        combine(repository.state, settings) { state, selected ->
            val room = state.room ?: return@combine LobbyUiState()
            val roomUi = roomUiMapper.map(room)
            LobbyUiState(
                roomCode = roomUi.roomCode,
                players = roomUi.players,
                maxPlayers = roomUi.maxPlayers,
                hostId = roomUi.hostId,
                currentPlayerId = state.playerId,
                openingPlayerId = selected.openingPlayerIdIn(room),
                maxRounds = selected.roomSettings.rounds,
                canDecreaseRounds = selected.roomSettings.rounds > gameConfiguration.minRounds,
                canIncreaseRounds = selected.roomSettings.rounds < gameConfiguration.maxRounds,
                totalTurns = turnRules.totalTurns(roomUi.players.size, selected.roomSettings.rounds),
                oneWordPerTurn = selected.roomSettings.oneWordPerTurn,
                isReconnecting = !state.isConnected,
            )
        }.stateIn(viewModelScope, WhileUiSubscribed, LobbyUiState())

    fun onAction(action: LobbyAction) {
        when (action) {
            LobbyAction.StartGame -> startGame()
            is LobbyAction.OpeningPlayerSelected -> settings.update { it.copy(openingPlayerId = action.playerId) }
            is LobbyAction.RoundsChanged -> {
                val rounds = action.value.coerceIn(gameConfiguration.minRounds, gameConfiguration.maxRounds)
                settings.update { it.copy(roomSettings = it.roomSettings.copy(rounds = rounds)) }
            }
            is LobbyAction.OneWordPerTurnChanged ->
                settings.update { it.copy(roomSettings = it.roomSettings.copy(oneWordPerTurn = action.enabled)) }
            LobbyAction.Leave -> Unit
        }
    }

    private fun startGame() {
        val selected = settings.value
        val openingPlayerId =
            repository.state.value.room
                ?.let(selected::openingPlayerIdIn)
        viewModelScope.launch { startGameUseCase(openingPlayerId, selected.roomSettings) }
    }

    private data class LobbySettings(
        val openingPlayerId: String? = null,
        val roomSettings: RoomSettings,
    ) {
        fun openingPlayerIdIn(room: RoomState): String? = openingPlayerId?.takeIf { id -> room.players.any { it.id == id } }
    }
}
