package com.example.storyround.presentation.finished

import com.example.storyround.domain.model.GameState
import com.example.storyround.domain.repository.GameRepository
import com.example.storyround.domain.usecase.PlayAgainUseCase
import com.example.storyround.presentation.mapper.ContributionUiMapperImpl
import com.example.storyround.presentation.mapper.PlayerUiMapperImpl
import com.example.storyround.presentation.mapper.RoomUiMapperImpl
import com.example.storyround.presentation.mapper.StorySegmentMapperImpl
import com.example.storyround.protocol.Contribution
import com.example.storyround.protocol.GameStatus
import com.example.storyround.protocol.Player
import com.example.storyround.protocol.PlayerColour
import com.example.storyround.protocol.RoomState
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class FinishedViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val alex = Player("alex", "Alex", PlayerColour.Blue)
    private val sam = Player("sam", "Sam", PlayerColour.Yellow)
    private val finishedRoom =
        RoomState(
            roomCode = "AB12",
            hostId = alex.id,
            players = listOf(alex, sam),
            status = GameStatus.Finished,
            openingPlayerId = alex.id,
            prompt = "A door opened.",
            contributions = listOf(Contribution(sam.id, "A horse arrived.", 0), Contribution(alex.id, "It wore a hat.", 1)),
            maxPlayers = 10,
            maxRounds = 1,
        )
    private val gameState = MutableStateFlow(GameState(room = finishedRoom, playerId = alex.id, isConnected = true))
    private val repository = mockk<GameRepository> { every { state } returns gameState }
    private val playAgainUseCase = mockk<PlayAgainUseCase>(relaxed = true)

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `given a finished room seen by a guest when observed then the story, writer counts and host are exposed`() =
        runTest(dispatcher) {
            gameState.value = gameState.value.copy(playerId = sam.id)
            val viewModel = viewModel()
            advanceUntilIdle()

            val uiState = viewModel.uiState.value
            assertEquals(listOf("A door opened.", "A horse arrived.", "It wore a hat."), uiState.segments.map { it.text })
            assertEquals(listOf("Alex" to 2, "Sam" to 1), uiState.writers.map { it.player.name to it.pieces })
            assertEquals(2, uiState.contributionCount)
            assertEquals("Alex", uiState.host?.name)
            assertFalse(uiState.canPlayAgain)
            assertFalse(uiState.isReconnecting)
        }

    @Test
    fun `given the host when play again is chosen then another game is requested`() =
        runTest(dispatcher) {
            val viewModel = viewModel()

            viewModel.onAction(FinishedAction.PlayAgain)
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.canPlayAgain)
            coVerify(exactly = 1) { playAgainUseCase() }
        }

    @Test
    fun `given the socket drops on the finished screen when observed then reconnecting is exposed`() =
        runTest(dispatcher) {
            gameState.value = gameState.value.copy(isConnected = false)
            val viewModel = viewModel()
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.isReconnecting)
        }

    private fun TestScope.viewModel(): FinishedViewModel {
        val viewModel =
            FinishedViewModel(
                repository = repository,
                playAgainUseCase = playAgainUseCase,
                roomUiMapper = RoomUiMapperImpl(PlayerUiMapperImpl(), ContributionUiMapperImpl(), StorySegmentMapperImpl()),
            )
        backgroundScope.launch { viewModel.uiState.collect() }
        return viewModel
    }
}
