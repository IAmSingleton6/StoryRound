package com.example.storyround.domain.usecase

import com.example.storyround.domain.repository.GameRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlin.test.Test

class GameUseCasesTest {
    private val repository = mockk<GameRepository>(relaxed = true)

    @Test
    fun `given game actions when use cases run then each delegates its domain command`() =
        runBlocking {
            coEvery { repository.openStory("story-1") } returns true

            ConnectToGameUseCaseImpl(repository)()
            CreateRoomUseCaseImpl(repository)("Alex")
            JoinRoomUseCaseImpl(repository)("AB12", "Sam")
            StartGameUseCaseImpl(repository)(null, 3)
            SubmitPromptUseCaseImpl(repository)("A door opened.")
            SubmitContributionUseCaseImpl(repository)("A horse arrived.")
            PlayAgainUseCaseImpl(repository)()
            LoadStoryHistoryUseCaseImpl(repository)()
            OpenSavedStoryUseCaseImpl(repository)("story-1")

            coVerify {
                repository.connect()
                repository.createRoom("Alex")
                repository.joinRoom("AB12", "Sam")
                repository.startGame(null, 3)
                repository.submitPrompt("A door opened.")
                repository.submitContribution("A horse arrived.")
                repository.playAgain()
                repository.loadHistory()
                repository.openStory("story-1")
            }
        }
}
