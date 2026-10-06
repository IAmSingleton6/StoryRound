package com.example.storyround.domain.usecase

import com.example.storyround.domain.repository.GameRepository

internal class ConnectToGameUseCaseImpl(
    private val repository: GameRepository,
) : ConnectToGameUseCase {
    override suspend fun invoke() = repository.connect()
}
