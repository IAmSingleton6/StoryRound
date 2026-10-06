package com.example.storyround.domain.usecase

import com.example.storyround.domain.repository.GameRepository

internal class CreateRoomUseCaseImpl(
    private val repository: GameRepository,
) : CreateRoomUseCase {
    override suspend fun invoke(playerName: String) = repository.createRoom(playerName)
}
