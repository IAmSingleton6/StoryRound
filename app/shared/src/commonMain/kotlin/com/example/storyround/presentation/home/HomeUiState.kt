package com.example.storyround.presentation.home

internal data class HomeUiState(
    val playerName: String = "",
    val roomCode: String = "",
    val isJoining: Boolean = false,
    val isConnecting: Boolean = false,
    val error: String? = null,
)
