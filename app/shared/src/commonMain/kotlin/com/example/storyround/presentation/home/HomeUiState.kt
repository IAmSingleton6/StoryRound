package com.example.storyround.presentation.home

import com.example.storyround.presentation.model.UiText

internal data class HomeUiState(
    val playerName: String = "",
    val hasValidPlayerName: Boolean = false,
    val roomCode: String = "",
    val isJoining: Boolean = false,
    val isConnecting: Boolean = false,
    val historyCount: Int = 0,
    val error: UiText? = null,
) {
    val canCreateGame: Boolean
        get() = hasValidPlayerName && !isConnecting

    val canJoinGame: Boolean
        get() = canCreateGame && roomCode.isNotBlank()
}
