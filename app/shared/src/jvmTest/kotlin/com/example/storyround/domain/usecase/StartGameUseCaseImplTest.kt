package com.example.storyround.domain.usecase

import com.example.storyround.domain.repository.GameRepository
import com.example.storyround.protocol.RoomSettings
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class StartGameUseCaseImplTest {
    private val repository = mockk<GameRepository>(relaxed = true)

    @Test
    fun `given the use case when invoked then the repository carries out the action`() =
        runTest {
            StartGameUseCaseImpl(repository)("sam", RoomSettings(rounds = 3))

            coVerify(exactly = 1) { repository.startGame("sam", RoomSettings(rounds = 3)) }
        }
}
