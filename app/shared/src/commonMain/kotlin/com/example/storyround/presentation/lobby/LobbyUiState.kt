package com.example.storyround.presentation.lobby

import com.example.storyround.presentation.model.PlayerUi

internal data class LobbyUiState(
    val roomCode: String = "",
    val players: List<PlayerUi> = emptyList(),
    val maxPlayers: Int = 0,
    val isHost: Boolean = false,
    val openingPlayerId: String? = null,
    val maxRounds: Int = 0,
)
