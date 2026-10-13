package com.example.storyround.domain.usecase

import com.example.storyround.domain.repository.GameRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class JoinRoomUseCaseImplTest {
    private val repository = mockk<GameRepository>(relaxed = true)

    @Test
    fun `given the use case when invoked then the repository carries out the action`() =
        runTest {
            JoinRoomUseCaseImpl(repository)("AB12", "Sam")

            coVerify(exactly = 1) { repository.joinRoom("AB12", "Sam") }
        }
}
