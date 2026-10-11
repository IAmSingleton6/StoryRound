package com.example.storyround.presentation

import com.example.storyround.configuration.GameConfiguration
import com.example.storyround.domain.model.RemoteGameState
import com.example.storyround.domain.repository.GameRepository
import com.example.storyround.domain.time.TimeProvider
import com.example.storyround.domain.usecase.LoadStoryHistoryUseCaseImpl
import com.example.storyround.domain.usecase.OpenSavedStoryUseCaseImpl
import com.example.storyround.domain.usecase.PlayAgainUseCaseImpl
import com.example.storyround.domain.usecase.StartGameUseCaseImpl
import com.example.storyround.domain.usecase.SubmitContributionUseCaseImpl
import com.example.storyround.domain.usecase.SubmitPromptUseCaseImpl
import com.example.storyround.presentation.finished.FinishedAction
import com.example.storyround.presentation.finished.FinishedViewModel
import com.example.storyround.presentation.history.HistoryAction
import com.example.storyround.presentation.history.HistoryUiState
import com.example.storyround.presentation.history.HistoryViewModel
import com.example.storyround.presentation.lobby.LobbyAction
import com.example.storyround.presentation.lobby.LobbyViewModel
import com.example.storyround.presentation.mapper.ContributionUiMapperImpl
import com.example.storyround.presentation.mapper.PlayerUiMapperImpl
import com.example.storyround.presentation.mapper.RoomUiMapperImpl
import com.example.storyround.presentation.mapper.SavedStoryUiMapperImpl
import com.example.storyround.presentation.story.StoryAction
import com.example.storyround.presentation.story.StoryUiState
import com.example.storyround.presentation.story.StoryViewModel
import com.example.storyround.protocol.GameStatus
import com.example.storyround.protocol.Player
import com.example.storyround.protocol.PlayerColour
import com.example.storyround.protocol.RoomState
import com.example.storyround.protocol.SavedStory
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
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PresentationViewModelFlowTest {
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
    fun `given a host lobby state when start is requested then room data flows and start is delegated`() =
        runTest(dispatcher) {
            val state =
                MutableStateFlow(
                    RemoteGameState(room = room(GameStatus.Lobby), playerId = HOST.id),
                )
            val repository = repository(state)
            val viewModel =
                LobbyViewModel(
                    repository,
                    StartGameUseCaseImpl(repository),
                    roomUiMapper,
                    GameConfiguration(defaultRounds = 4),
                )
            val collection = backgroundScope.launch { viewModel.uiState.collect() }
            advanceUntilIdle()

            viewModel.onAction(LobbyAction.StartGame)
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.isHost)
            assertEquals("AB12", viewModel.uiState.value.roomCode)
            val playerNames =
                viewModel.uiState.value.players
                    .map { player -> player.name }
            assertEquals(listOf(HOST.name), playerNames)
            coVerify(exactly = 1) { repository.startGame(null, 4) }
            collection.cancel()
        }

    @Test
    fun `given a contribution draft when submitted then it is cleared and delegated`() =
        runTest(dispatcher) {
            val state = MutableStateFlow(RemoteGameState(room = room(GameStatus.Playing), playerId = HOST.id))
            val repository = repository(state)
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
            runCurrent()

            viewModel.onAction(StoryAction.ContributionChanged("A horse appeared."))
            viewModel.onAction(StoryAction.SubmitContribution)
            runCurrent()

            assertEquals("", (viewModel.uiState.value as StoryUiState.Playing).contributionDraft)
            coVerify(exactly = 1) { repository.submitContribution("A horse appeared.") }
            collection.cancel()
        }

    @Test
    fun `given a completed host room when actions are requested then finished state and commands flow`() =
        runTest(dispatcher) {
            val state =
                MutableStateFlow(
                    RemoteGameState(
                        room = room(GameStatus.Finished),
                        playerId = HOST.id,
                        finishedStoryId = "story-1",
                    ),
                )
            val repository = repository(state)
            val viewModel =
                FinishedViewModel(
                    repository,
                    OpenSavedStoryUseCaseImpl(repository),
                    PlayAgainUseCaseImpl(repository),
                    roomUiMapper,
                )
            val collection = backgroundScope.launch { viewModel.uiState.collect() }
            advanceUntilIdle()

            viewModel.onAction(FinishedAction.ViewStory)
            viewModel.onAction(FinishedAction.PlayAgain)
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.canViewStory)
            assertTrue(viewModel.uiState.value.canPlayAgain)
            coVerify(exactly = 1) { repository.openStory("story-1") }
            coVerify(exactly = 1) { repository.playAgain() }
            collection.cancel()
        }

    @Test
    fun `given history state when loading and opening a story then UI flows and repository is delegated`() =
        runTest(dispatcher) {
            val summary = SavedStory("story-1", "A door opened.", HOST.id, 1, listOf(HOST), emptyList())
            val state = MutableStateFlow(RemoteGameState(history = listOf(summary)))
            val repository = repository(state)
            val viewModel =
                HistoryViewModel(
                    repository,
                    LoadStoryHistoryUseCaseImpl(repository),
                    OpenSavedStoryUseCaseImpl(repository),
                    SavedStoryUiMapperImpl(PlayerUiMapperImpl(), ContributionUiMapperImpl()),
                )
            val collection = backgroundScope.launch { viewModel.uiState.collect() }
            advanceUntilIdle()

            viewModel.onAction(HistoryAction.OpenStory(summary.id))
            advanceUntilIdle()

            val content = assertIs<HistoryUiState.Content>(viewModel.uiState.value)
            assertEquals(listOf("story-1"), content.stories.map { it.id })
            val writers = content.stories.single().writers
            assertEquals(listOf(HOST.name to 1), writers.map { it.player.name to it.pieces })
            coVerify(exactly = 1) { repository.loadHistory() }
            coVerify(exactly = 1) { repository.openStory(summary.id) }
            collection.cancel()
        }

    private fun repository(state: MutableStateFlow<RemoteGameState>): GameRepository {
        val repository = mockk<GameRepository>(relaxed = true)
        every { repository.state } returns state
        return repository
    }

    private fun room(status: GameStatus): RoomState =
        RoomState(
            roomCode = "AB12",
            hostId = HOST.id,
            players = listOf(HOST),
            status = status,
            prompt = "A door opened.",
            currentPlayerId = HOST.id,
            turnDeadlineEpochMillis = null,
            maxPlayers = 10,
            maxRounds = 3,
        )

    private companion object {
        val HOST = Player("host", "Alex", PlayerColour.Blue)
    }

    private class FakeTimeProvider : TimeProvider {
        override fun nowEpochMillis(): Long = 0
    }
}
