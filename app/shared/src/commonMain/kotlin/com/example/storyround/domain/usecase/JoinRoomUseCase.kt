package com.example.storyround.domain.usecase

internal interface JoinRoomUseCase {
    suspend operator fun invoke(
        roomCode: String,
        playerName: String,
    )
}
