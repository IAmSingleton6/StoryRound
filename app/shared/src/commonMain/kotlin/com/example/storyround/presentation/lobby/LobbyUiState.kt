package com.example.storyround.presentation.lobby

import com.example.storyround.presentation.model.PlayerUi

internal data class LobbyUiState(
    val roomCode: String = "",
    val players: List<PlayerUi> = emptyList(),
    val maxPlayers: Int = 10,
    val isHost: Boolean = false,
)
