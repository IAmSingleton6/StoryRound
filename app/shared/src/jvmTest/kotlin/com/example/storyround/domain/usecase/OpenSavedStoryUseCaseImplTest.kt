package com.example.storyround.domain.usecase

import com.example.storyround.domain.repository.GameRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OpenSavedStoryUseCaseImplTest {
    private val repository = mockk<GameRepository>()

    @Test
    fun `given a story id when opened then whether the story exists on this device is returned`() =
        runTest {
            coEvery { repository.openStory("story-1") } returns true
            coEvery { repository.openStory("missing") } returns false
            val useCase = OpenSavedStoryUseCaseImpl(repository)

            assertTrue(useCase("story-1"))
            assertFalse(useCase("missing"))
        }
}
