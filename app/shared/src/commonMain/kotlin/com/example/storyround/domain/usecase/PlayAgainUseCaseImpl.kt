package com.example.storyround.domain.usecase

import com.example.storyround.domain.repository.GameRepository

internal class PlayAgainUseCaseImpl(
    private val repository: GameRepository,
) : PlayAgainUseCase {
    override suspend fun invoke() = repository.playAgain()
}
