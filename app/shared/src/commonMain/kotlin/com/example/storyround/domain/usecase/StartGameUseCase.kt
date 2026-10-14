package com.example.storyround.domain.usecase

import com.example.storyround.protocol.RoomSettings

internal interface StartGameUseCase {
    suspend operator fun invoke(
        openingPlayerId: String?,
        settings: RoomSettings,
    )
}
