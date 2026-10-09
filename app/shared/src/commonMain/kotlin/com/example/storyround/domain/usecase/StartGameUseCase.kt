package com.example.storyround.domain.usecase

internal interface StartGameUseCase {
    suspend operator fun invoke(openingPlayerId: String?, maxRounds: Int)
}
