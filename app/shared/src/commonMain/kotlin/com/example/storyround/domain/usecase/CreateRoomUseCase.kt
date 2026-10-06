package com.example.storyround.domain.usecase

internal interface CreateRoomUseCase {
    suspend operator fun invoke(playerName: String)
}
