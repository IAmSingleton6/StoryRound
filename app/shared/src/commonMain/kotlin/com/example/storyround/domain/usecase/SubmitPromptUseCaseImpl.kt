package com.example.storyround.domain.usecase

import com.example.storyround.domain.repository.GameRepository

internal class SubmitPromptUseCaseImpl(
    private val repository: GameRepository,
) : SubmitPromptUseCase {
    override suspend fun invoke(text: String) = repository.submitPrompt(text)
}
