package com.example.storyround.presentation.home

internal sealed interface HomeAction {
    data class PlayerNameChanged(
        val value: String,
    ) : HomeAction

    data class RoomCodeChanged(
        val value: String,
    ) : HomeAction

    data object Submit : HomeAction

    data object OpenHistory : HomeAction
}
