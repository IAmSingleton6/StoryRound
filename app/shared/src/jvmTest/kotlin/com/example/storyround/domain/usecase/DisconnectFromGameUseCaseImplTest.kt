package com.example.storyround.domain.usecase

import com.example.storyround.domain.repository.GameRepository
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test

class DisconnectFromGameUseCaseImplTest {
    private val repository = mockk<GameRepository>(relaxed = true)

    @Test
    fun `given the use case when invoked then the repository carries out the action`() {
        DisconnectFromGameUseCaseImpl(repository)()

        verify(exactly = 1) { repository.disconnect() }
    }
}
