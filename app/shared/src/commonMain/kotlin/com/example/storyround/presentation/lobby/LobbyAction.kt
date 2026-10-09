package com.example.storyround.presentation.lobby

internal sealed interface LobbyAction {
    data object StartGame : LobbyAction

    data class OpeningPlayerSelected(
        val playerId: String?,
    ) : LobbyAction

    data class RoundsChanged(
        val value: Int,
    ) : LobbyAction

    data object Leave : LobbyAction
}
