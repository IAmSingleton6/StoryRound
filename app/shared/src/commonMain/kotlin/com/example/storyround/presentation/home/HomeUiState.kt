package com.example.storyround.presentation.home

internal data class HomeUiState(
    val playerName: String = "",
    val roomCode: String = "",
    val isJoining: Boolean = false,
    val isConnecting: Boolean = false,
    val shelfCount: Int = 0,
    val error: String? = null,
) {
    val canCreateGame: Boolean
        get() = playerName.isNotBlank() && !isConnecting

    val canJoinGame: Boolean
        get() = canCreateGame && roomCode.isNotBlank()
}
