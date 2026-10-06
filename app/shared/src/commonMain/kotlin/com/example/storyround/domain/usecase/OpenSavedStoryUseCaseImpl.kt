package com.example.storyround.domain.usecase

import com.example.storyround.domain.repository.GameRepository

internal class OpenSavedStoryUseCaseImpl(
    private val repository: GameRepository,
) : OpenSavedStoryUseCase {
    override suspend fun invoke(id: String): Boolean = repository.openStory(id)
}
