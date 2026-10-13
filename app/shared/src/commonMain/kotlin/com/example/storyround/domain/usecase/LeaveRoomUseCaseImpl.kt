package com.example.storyround.domain.usecase

import com.example.storyround.domain.repository.GameRepository

internal class LeaveRoomUseCaseImpl(
    private val repository: GameRepository,
) : LeaveRoomUseCase {
    override suspend fun invoke() = repository.leaveRoom()
}
