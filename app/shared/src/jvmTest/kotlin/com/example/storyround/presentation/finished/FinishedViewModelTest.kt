package com.example.storyround.presentation.finished

import com.example.storyround.domain.model.RemoteGameState
import com.example.storyround.domain.repository.GameRepository
import com.example.storyround.domain.usecase.PlayAgainUseCaseImpl
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
            val viewModel = viewModel(MutableStateFlow(RemoteGameState(room = finishedRoom, playerId = sam.id, isConnected = true)))
            val collection = backgroundScope.launch { viewModel.uiState.collect() }
            advanceUntilIdle()

            val uiState = viewModel.uiState.value
            assertEquals(listOf("A door opened.", "A horse arrived.", "It wore a hat."), uiState.segments.map { it.text })
            assertEquals(listOf("Alex" to 2, "Sam" to 1), uiState.writers.map { it.player.name to it.pieces })
            assertEquals("Alex", uiState.host?.name)
            assertFalse(uiState.canPlayAgain)
            assertFalse(uiState.isReconnecting)
            collection.cancel()
        }

    @Test
    fun `given the socket drops on the finished screen when observed then reconnecting is exposed`() =
        runTest(dispatcher) {
            val viewModel = viewModel(MutableStateFlow(RemoteGameState(room = finishedRoom, playerId = alex.id, isConnected = false)))
            val collection = backgroundScope.launch { viewModel.uiState.collect() }
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.isReconnecting)
            collection.cancel()
        }

    private fun viewModel(state: MutableStateFlow<RemoteGameState>): FinishedViewModel {
        val repository = mockk<GameRepository>(relaxed = true)
        every { repository.state } returns state
        return FinishedViewModel(
            repository,
            PlayAgainUseCaseImpl(repository),
            RoomUiMapperImpl(PlayerUiMapperImpl(), ContributionUiMapperImpl()),
        )
    }
}
