package com.example.storyround.domain.usecase

import com.example.storyround.domain.repository.GameRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class LoadStoryHistoryUseCaseImplTest {
    private val repository = mockk<GameRepository>(relaxed = true)

    @Test
    fun `given the use case when invoked then the repository carries out the action`() =
        runTest {
            LoadStoryHistoryUseCaseImpl(repository)()

            coVerify(exactly = 1) { repository.loadHistory() }
        }
}
