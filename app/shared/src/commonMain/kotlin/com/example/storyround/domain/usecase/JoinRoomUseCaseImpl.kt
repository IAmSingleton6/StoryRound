package com.example.storyround.domain.usecase

import com.example.storyround.domain.repository.GameRepository

internal class JoinRoomUseCaseImpl(
    private val repository: GameRepository,
) : JoinRoomUseCase {
    override suspend fun invoke(
        roomCode: String,
        playerName: String,
    ) = repository.joinRoom(roomCode, playerName)
}
