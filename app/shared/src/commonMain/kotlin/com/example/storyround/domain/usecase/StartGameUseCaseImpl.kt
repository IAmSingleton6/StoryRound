package com.example.storyround.domain.usecase

import com.example.storyround.domain.repository.GameRepository
import com.example.storyround.protocol.RoomSettings

internal class StartGameUseCaseImpl(
    private val repository: GameRepository,
) : StartGameUseCase {
    override suspend fun invoke(
        openingPlayerId: String?,
        settings: RoomSettings,
    ) = repository.startGame(openingPlayerId, settings)
}
