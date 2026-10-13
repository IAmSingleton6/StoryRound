package com.example.storyround.domain.usecase

import com.example.storyround.domain.repository.GameRepository

internal class DisconnectFromGameUseCaseImpl(
    private val repository: GameRepository,
) : DisconnectFromGameUseCase {
    override fun invoke() = repository.disconnect()
}
