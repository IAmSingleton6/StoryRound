package com.example.storyround.domain.usecase

internal interface OpenSavedStoryUseCase {
    suspend operator fun invoke(id: String): Boolean
}
