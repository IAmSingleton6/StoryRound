package com.example.storyround.presentation.lobby

internal sealed interface LobbyAction {
    data object StartGame : LobbyAction

    data class OpeningPlayerSelected(
        val playerId: String?,
    ) : LobbyAction

    data object DecreaseRounds : LobbyAction

    data object IncreaseRounds : LobbyAction

    data class OneWordPerTurnChanged(
        val enabled: Boolean,
    ) : LobbyAction

    data object Leave : LobbyAction
}
