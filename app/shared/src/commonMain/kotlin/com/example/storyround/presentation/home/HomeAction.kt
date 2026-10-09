package com.example.storyround.presentation.home

internal sealed interface HomeAction {
    data class PlayerNameChanged(
        val value: String,
    ) : HomeAction

    data class RoomCodeChanged(
        val value: String,
    ) : HomeAction

    data object CreateGame : HomeAction

    data object ShowJoinGame : HomeAction

    data object JoinGame : HomeAction

    data object CancelJoin : HomeAction

    data object OpenHistory : HomeAction
}
