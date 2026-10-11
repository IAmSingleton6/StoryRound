package com.example.storyround.domain.usecase

import com.example.storyround.domain.repository.GameRepository

internal class StartGameUseCaseImpl(
    private val repository: GameRepository,
) : StartGameUseCase {
    override suspend fun invoke(
        openingPlayerId: String?,
        maxRounds: Int,
    ) = repository.startGame(openingPlayerId, maxRounds)
}
