package com.example.storyround.presentation.story

import com.example.storyround.configuration.GameConfiguration
import com.example.storyround.domain.model.RemoteGameState
import com.example.storyround.domain.repository.GameRepository
import com.example.storyround.domain.time.TimeProvider
import com.example.storyround.domain.usecase.SubmitContributionUseCaseImpl
import com.example.storyround.domain.usecase.SubmitPromptUseCaseImpl
import com.example.storyround.presentation.mapper.ContributionUiMapperImpl
import com.example.storyround.presentation.mapper.PlayerUiMapperImpl
import com.example.storyround.presentation.mapper.RoomUiMapperImpl
import com.example.storyround.protocol.GameStatus
import com.example.storyround.protocol.Player
import com.example.storyround.protocol.PlayerColour
import com.example.storyround.protocol.RoomState
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class StoryViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val roomUiMapper = RoomUiMapperImpl(PlayerUiMapperImpl(), ContributionUiMapperImpl())

    @BeforeTest
    fun setUp() {
        kotlinx.coroutines.Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        kotlinx.coroutines.Dispatchers.resetMain()
    }

    @Test
    fun `given an opening prompt when the host submits then the story delegates to the server`() =
        runTest(dispatcher) {
            val host = Player("host", "Alex", PlayerColour.Blue)
            val repositoryState =
                MutableStateFlow(
                    RemoteGameState(
                        room = RoomState("AB12", host.id, listOf(host), GameStatus.Prompt, maxPlayers = 10, maxRounds = 3),
                        playerId = host.id,
                    ),
                )
            val repository = mockk<GameRepository>(relaxed = true)
            every { repository.state } returns repositoryState
            val viewModel =
                StoryViewModel(
                    repository,
                    SubmitPromptUseCaseImpl(repository),
                    SubmitContributionUseCaseImpl(repository),
                    FakeTimeProvider(),
                    roomUiMapper,
                    GameConfiguration(),
                )
            val collection = backgroundScope.launch { viewModel.uiState.collect() }
            advanceUntilIdle()

            viewModel.onAction(StoryAction.OpeningPromptChanged("A door opened."))
            viewModel.onAction(StoryAction.SubmitOpeningPrompt)
            advanceUntilIdle()

            val uiState = assertIs<StoryUiState.OpeningPrompt>(viewModel.uiState.value)
            assertTrue(uiState.isOpeningWriter)
            assertEquals("Alex", uiState.openingPlayer?.name)
            coVerify(exactly = 1) { repository.submitPrompt("A door opened.") }
            collection.cancel()
        }

    @Test
    fun `given the server starts timed play when the opening prompt is accepted then the same state flow enters playing`() =
        runTest(dispatcher) {
            val host = Player("host", "Alex", PlayerColour.Blue)
            val repositoryState =
                MutableStateFlow(
                    RemoteGameState(
                        room = RoomState("AB12", host.id, listOf(host), GameStatus.Prompt, maxPlayers = 10, maxRounds = 3),
                        playerId = host.id,
                    ),
                )
            val repository = mockk<GameRepository>(relaxed = true)
            every { repository.state } returns repositoryState
            val viewModel =
                StoryViewModel(
                    repository,
                    SubmitPromptUseCaseImpl(repository),
                    SubmitContributionUseCaseImpl(repository),
                    FakeTimeProvider(),
                    roomUiMapper,
                    GameConfiguration(),
                )
            val collection = backgroundScope.launch { viewModel.uiState.collect() }
            advanceUntilIdle()

            repositoryState.value =
                RemoteGameState(
                    room =
                        RoomState(
                            roomCode = "AB12",
                            hostId = host.id,
                            players = listOf(host),
                            status = GameStatus.Playing,
                            prompt = "A door opened.",
                            currentPlayerId = host.id,
                            turnDeadlineEpochMillis = null,
                            maxPlayers = 10,
                            maxRounds = 3,
                        ),
                    playerId = host.id,
                )
            advanceUntilIdle()

            val uiState = assertIs<StoryUiState.Playing>(viewModel.uiState.value)
            assertEquals("A door opened.", uiState.room.prompt)
            assertEquals(0, uiState.nowEpochMillis)
            collection.cancel()
        }

    @Test
    fun `given a guest while another player opens and the socket drops when observed then the guest waits and sees reconnecting`() =
        runTest(dispatcher) {
            val host = Player("host", "Alex", PlayerColour.Blue)
            val guest = Player("guest", "Sam", PlayerColour.Yellow)
            val repositoryState =
                MutableStateFlow(
                    RemoteGameState(
                        room = RoomState("AB12", host.id, listOf(host, guest), GameStatus.Prompt, maxPlayers = 10, maxRounds = 3),
                        playerId = guest.id,
                        isConnected = false,
                    ),
                )
            val repository = mockk<GameRepository>(relaxed = true)
            every { repository.state } returns repositoryState
            val viewModel =
                StoryViewModel(
                    repository,
                    SubmitPromptUseCaseImpl(repository),
                    SubmitContributionUseCaseImpl(repository),
                    FakeTimeProvider(),
                    roomUiMapper,
                    GameConfiguration(),
                )
            val collection = backgroundScope.launch { viewModel.uiState.collect() }

            viewModel.onAction(StoryAction.OpeningPromptChanged("I should not be able to send this."))
            advanceUntilIdle()

            val uiState = assertIs<StoryUiState.OpeningPrompt>(viewModel.uiState.value)
            assertFalse(uiState.isOpeningWriter)
            assertFalse(uiState.canSubmitOpeningPrompt)
            assertEquals("Alex", uiState.openingPlayer?.name)
            assertTrue(uiState.isReconnecting)
            collection.cancel()
        }

    private class FakeTimeProvider(
        private val nowMillis: Long = 0,
    ) : TimeProvider {
        override fun nowEpochMillis(): Long = nowMillis
    }
}
