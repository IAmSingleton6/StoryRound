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
import com.example.storyround.protocol.Contribution
import com.example.storyround.protocol.GameStatus
import com.example.storyround.protocol.Player
import com.example.storyround.protocol.PlayerColour
import com.example.storyround.protocol.RoomState
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
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
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class StoryPlayingViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val alex = Player("alex", "Alex", PlayerColour.Blue)
    private val sam = Player("sam", "Sam", PlayerColour.Yellow)
    private val configuration = GameConfiguration(firstTurnMillis = 15_000L, turnReductionMillis = 1_000L, minimumTurnMillis = 5_000L)

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `given the third timed turn of two players and two rounds when observed then round, turn, durations and next writer are exposed`() =
        runTest(dispatcher) {
            val viewModel = viewModel(MutableStateFlow(RemoteGameState(room = playingRoom(turnNumber = 2), playerId = alex.id)))
            val collection = backgroundScope.launch { viewModel.uiState.collect() }
            advanceUntilIdle()

            val uiState = assertIs<StoryUiState.Playing>(viewModel.uiState.value)
            assertEquals(2, uiState.round)
            assertEquals(3, uiState.turn)
            assertEquals(4, uiState.totalTurns)
            assertEquals(13_000L, uiState.turnDurationMillis)
            assertEquals("Sam", uiState.nextPlayer?.name)
            assertEquals(12_000L, uiState.nextTurnDurationMillis)
            assertEquals(listOf("A door opened.", "A horse arrived.", "It wore a hat."), uiState.segments.map { it.text })
            collection.cancel()
        }

    @Test
    fun `given the final turn when observed then there is no next writer`() =
        runTest(dispatcher) {
            val viewModel = viewModel(MutableStateFlow(RemoteGameState(room = playingRoom(turnNumber = 3), playerId = alex.id)))
            val collection = backgroundScope.launch { viewModel.uiState.collect() }
            advanceUntilIdle()

            assertNull(assertIs<StoryUiState.Playing>(viewModel.uiState.value).nextPlayer)
            collection.cancel()
        }

    @Test
    fun `given a turn that passes without a contribution when play moves on then the hand-over is announced briefly`() =
        runTest(dispatcher) {
            val remoteState = MutableStateFlow(RemoteGameState(room = playingRoom(turnNumber = 2), playerId = alex.id))
            val viewModel = viewModel(remoteState)
            val collection = backgroundScope.launch { viewModel.uiState.collect() }
            runCurrent()

            remoteState.value = remoteState.value.copy(room = playingRoom(turnNumber = 3))
            runCurrent()

            assertEquals("Sam", assertIs<StoryUiState.Playing>(viewModel.uiState.value).timeUpHandOver?.name)
            advanceTimeBy(1_801)
            assertNull(assertIs<StoryUiState.Playing>(viewModel.uiState.value).timeUpHandOver)
            collection.cancel()
        }

    @Test
    fun `given a turn that passes with a contribution when play moves on then nothing is announced`() =
        runTest(dispatcher) {
            val remoteState = MutableStateFlow(RemoteGameState(room = playingRoom(turnNumber = 2), playerId = alex.id))
            val viewModel = viewModel(remoteState)
            val collection = backgroundScope.launch { viewModel.uiState.collect() }
            runCurrent()

            val nextTurn = playingRoom(turnNumber = 3)
            val contributions = nextTurn.contributions + Contribution(alex.id, "Then it spoke.", 2)
            remoteState.value = remoteState.value.copy(room = nextTurn.copy(contributions = contributions))
            runCurrent()

            assertNull(assertIs<StoryUiState.Playing>(viewModel.uiState.value).timeUpHandOver)
            collection.cancel()
        }

    private fun playingRoom(turnNumber: Int) =
        RoomState(
            roomCode = "AB12",
            hostId = alex.id,
            players = listOf(alex, sam),
            status = GameStatus.Playing,
            openingPlayerId = alex.id,
            prompt = "A door opened.",
            contributions = listOf(Contribution(alex.id, "A horse arrived.", 0), Contribution(sam.id, "It wore a hat.", 1)),
            currentPlayerId = listOf(alex, sam)[turnNumber % 2].id,
            turnNumber = turnNumber,
            turnDeadlineEpochMillis = null,
            maxPlayers = 10,
            maxRounds = 2,
        )

    private fun viewModel(state: MutableStateFlow<RemoteGameState>): StoryViewModel {
        val repository = mockk<GameRepository>(relaxed = true)
        every { repository.state } returns state
        return StoryViewModel(
            repository = repository,
            submitPromptUseCase = SubmitPromptUseCaseImpl(repository),
            submitContributionUseCase = SubmitContributionUseCaseImpl(repository),
            timeProvider = FixedTimeProvider,
            roomUiMapper = RoomUiMapperImpl(PlayerUiMapperImpl(), ContributionUiMapperImpl()),
            gameConfiguration = configuration,
        )
    }

    private object FixedTimeProvider : TimeProvider {
        override fun nowEpochMillis(): Long = 0
    }
}
