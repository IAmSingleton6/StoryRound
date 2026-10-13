package com.example.storyround.domain.usecase

import com.example.storyround.domain.repository.GameRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class CreateRoomUseCaseImplTest {
    private val repository = mockk<GameRepository>(relaxed = true)

    @Test
    fun `given the use case when invoked then the repository carries out the action`() =
        runTest {
            CreateRoomUseCaseImpl(repository)("Alex")

            coVerify(exactly = 1) { repository.createRoom("Alex") }
        }
}
