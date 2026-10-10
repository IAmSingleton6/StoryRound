package com.example.storyround.presentation.lobby

import com.example.storyround.configuration.GameConfiguration
import com.example.storyround.domain.model.RemoteGameState
import com.example.storyround.domain.repository.GameRepository
import com.example.storyround.domain.usecase.StartGameUseCaseImpl
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
class LobbyViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val host = Player("host", "Alex", PlayerColour.Blue)
    private val guest = Player("guest", "Sam", PlayerColour.Yellow)
    private val room = RoomState("AB12", host.id, listOf(host, guest), GameStatus.Lobby, maxPlayers = 10, maxRounds = 3)
    private val configuration = GameConfiguration(minRounds = 1, maxRounds = 10, defaultRounds = 3)

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `given a guest in a connected lobby when observed then host, current player and seats are exposed`() =
        runTest(dispatcher) {
            val viewModel = viewModel(MutableStateFlow(RemoteGameState(room = room, playerId = guest.id, isConnected = true)))
            val collection = backgroundScope.launch { viewModel.uiState.collect() }
            advanceUntilIdle()

            val uiState = viewModel.uiState.value
            assertFalse(uiState.isHost)
            assertEquals(guest.id, uiState.currentPlayerId)
            assertEquals("Alex", uiState.host?.name)
            assertEquals(8, uiState.openSeats)
            assertFalse(uiState.isReconnecting)
            collection.cancel()
        }

    @Test
    fun `given the socket drops while in the lobby when observed then reconnecting is exposed`() =
        runTest(dispatcher) {
            val remoteState = MutableStateFlow(RemoteGameState(room = room, playerId = host.id, isConnected = true))
            val viewModel = viewModel(remoteState)
            val collection = backgroundScope.launch { viewModel.uiState.collect() }
            advanceUntilIdle()

            remoteState.value = remoteState.value.copy(isConnected = false, error = "Reconnecting...")
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.isReconnecting)
            collection.cancel()
        }

    @Test
    fun `given the minimum rounds when rounds are lowered then the value is clamped and only increasing is allowed`() =
        runTest(dispatcher) {
            val viewModel = viewModel(MutableStateFlow(RemoteGameState(room = room, playerId = host.id, isConnected = true)))
            val collection = backgroundScope.launch { viewModel.uiState.collect() }

            viewModel.onAction(LobbyAction.RoundsChanged(0))
            advanceUntilIdle()

            val uiState = viewModel.uiState.value
            assertEquals(1, uiState.maxRounds)
            assertFalse(uiState.canDecreaseRounds)
            assertTrue(uiState.canIncreaseRounds)
            assertEquals(2, uiState.totalTurns)
            collection.cancel()
        }

    @Test
    fun `given a chosen opener and rounds when the host starts then the settings are sent`() =
        runTest(dispatcher) {
            val repository = repository(MutableStateFlow(RemoteGameState(room = room, playerId = host.id, isConnected = true)))
            val viewModel = viewModel(repository)
            val collection = backgroundScope.launch { viewModel.uiState.collect() }

            viewModel.onAction(LobbyAction.OpeningPlayerSelected(guest.id))
            viewModel.onAction(LobbyAction.RoundsChanged(10))
            viewModel.onAction(LobbyAction.StartGame)
            advanceUntilIdle()

            assertEquals(guest.id, viewModel.uiState.value.openingPlayerId)
            assertFalse(viewModel.uiState.value.canIncreaseRounds)
            coVerify(exactly = 1) { repository.startGame(guest.id, 10) }
            collection.cancel()
        }

    private fun viewModel(state: MutableStateFlow<RemoteGameState>) = viewModel(repository(state))

    private fun viewModel(repository: GameRepository) =
        LobbyViewModel(
            repository = repository,
            startGame = StartGameUseCaseImpl(repository),
            roomUiMapper = RoomUiMapperImpl(PlayerUiMapperImpl(), ContributionUiMapperImpl()),
            gameConfiguration = configuration,
        )

    private fun repository(state: MutableStateFlow<RemoteGameState>): GameRepository {
        val repository = mockk<GameRepository>(relaxed = true)
        every { repository.state } returns state
        return repository
    }
}
