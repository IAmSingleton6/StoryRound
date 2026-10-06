package com.example.storyround.domain.usecase

import com.example.storyround.domain.repository.GameRepository

internal class LoadStoryHistoryUseCaseImpl(
    private val repository: GameRepository,
) : LoadStoryHistoryUseCase {
    override suspend fun invoke() = repository.loadHistory()
}
