package com.example.storyround.domain.usecase

import com.example.storyround.domain.repository.GameRepository

internal class CloseSavedStoryUseCaseImpl(
    private val repository: GameRepository,
) : CloseSavedStoryUseCase {
    override fun invoke() = repository.closeStory()
}
