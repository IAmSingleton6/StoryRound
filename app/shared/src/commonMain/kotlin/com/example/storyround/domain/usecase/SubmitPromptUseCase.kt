package com.example.storyround.domain.usecase

internal interface SubmitPromptUseCase {
    suspend operator fun invoke(text: String)
}
