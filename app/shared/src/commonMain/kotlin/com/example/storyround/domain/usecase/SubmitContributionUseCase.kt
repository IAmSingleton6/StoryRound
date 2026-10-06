package com.example.storyround.domain.usecase

internal interface SubmitContributionUseCase {
    suspend operator fun invoke(text: String)
}
