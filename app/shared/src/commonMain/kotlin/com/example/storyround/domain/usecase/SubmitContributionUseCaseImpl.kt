package com.example.storyround.domain.usecase

import com.example.storyround.domain.repository.GameRepository

internal class SubmitContributionUseCaseImpl(
    private val repository: GameRepository,
) : SubmitContributionUseCase {
    override suspend fun invoke(text: String) = repository.submitContribution(text)
}
