package com.example.storyround.presentation.lobby

import com.example.storyround.configuration.GameConfiguration
import com.example.storyround.domain.model.GameState
import com.example.storyround.domain.repository.GameRepository
import com.example.storyround.domain.usecase.StartGameUseCase
import com.example.storyround.presentation.mapper.ContributionUiMapperImpl
import com.example.storyround.presentation.mapper.PlayerUiMapperImpl
import com.example.storyround.presentation.mapper.RoomUiMapperImpl
import com.example.storyround.presentation.mapper.StorySegmentMapperImpl
import com.example.storyround.protocol.GameStatus
import com.example.storyround.protocol.Player
import com.example.storyround.protocol.PlayerColour
import com.example.storyround.protocol.RoomState
import com.example.storyround.rules.TurnRulesImpl
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LobbyViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val host = Player("host", "Alex", PlayerColour.Blue)
    private val guest = Player("guest", "Sam", PlayerColour.Yellow)
    private val room = RoomState("AB12", host.id, listOf(host, guest), GameStatus.Lobby, maxPlayers = 10, maxRounds = 3)
    private val configuration = GameConfiguration(minRounds = 1, maxRounds = 10, defaultRounds = 3)
    private val gameState = MutableStateFlow(GameState(room = room, playerId = host.id, isConnected = true))
    private val repository = mockk<GameRepository> { every { state } returns gameState }
    private val startGameUseCase = mockk<StartGameUseCase>(relaxed = true)

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
            gameState.value = gameState.value.copy(playerId = guest.id)
            val viewModel = viewModel()
            advanceUntilIdle()

            val uiState = viewModel.uiState.value
            assertEquals("AB12", uiState.roomCode)
            assertFalse(uiState.isHost)
            assertEquals(guest.id, uiState.currentPlayerId)
            assertEquals("Alex", uiState.host?.name)
            assertEquals(8, uiState.openSeats)
            assertFalse(uiState.isReconnecting)
        }

    @Test
    fun `given the socket drops while in the lobby when observed then reconnecting is exposed`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            advanceUntilIdle()

            gameState.value = gameState.value.copy(isConnected = false)
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.isReconnecting)
        }

    @Test
    fun `given the minimum rounds when rounds are lowered then the value is clamped and only increasing is allowed`() =
        runTest(dispatcher) {
            val viewModel = viewModel()

            viewModel.onAction(LobbyAction.RoundsChanged(0))
            advanceUntilIdle()

            val uiState = viewModel.uiState.value
            assertEquals(1, uiState.maxRounds)
            assertFalse(uiState.canDecreaseRounds)
            assertTrue(uiState.canIncreaseRounds)
            assertEquals(2, uiState.totalTurns)
        }

    @Test
    fun `given the host changes nothing when starting then a random opener and the configured default rounds are sent`() =
        runTest(dispatcher) {
            val viewModel = viewModel()

            viewModel.onAction(LobbyAction.StartGame)
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.isHost)
            coVerify(exactly = 1) { startGameUseCase(null, 3) }
        }

    @Test
    fun `given a chosen opener and rounds when the host starts then the settings are sent`() =
        runTest(dispatcher) {
            val viewModel = viewModel()

            viewModel.onAction(LobbyAction.OpeningPlayerSelected(guest.id))
            viewModel.onAction(LobbyAction.RoundsChanged(10))
            viewModel.onAction(LobbyAction.StartGame)
            advanceUntilIdle()

            assertEquals(guest.id, viewModel.uiState.value.openingPlayerId)
            assertFalse(viewModel.uiState.value.canIncreaseRounds)
            coVerify(exactly = 1) { startGameUseCase(guest.id, 10) }
        }

    @Test
    fun `given the chosen opener leaves the lobby when the host starts then a random opener is sent as shown`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            viewModel.onAction(LobbyAction.OpeningPlayerSelected(guest.id))

            gameState.value = gameState.value.copy(room = room.copy(players = listOf(host)))
            viewModel.onAction(LobbyAction.StartGame)
            advanceUntilIdle()

            assertNull(viewModel.uiState.value.openingPlayerId)
            coVerify(exactly = 1) { startGameUseCase(null, 3) }
        }

    private fun TestScope.viewModel(): LobbyViewModel {
        val viewModel =
            LobbyViewModel(
                repository = repository,
                startGameUseCase = startGameUseCase,
                roomUiMapper = RoomUiMapperImpl(PlayerUiMapperImpl(), ContributionUiMapperImpl(), StorySegmentMapperImpl()),
                gameConfiguration = configuration,
                turnRules = TurnRulesImpl(configuration),
            )
        backgroundScope.launch { viewModel.uiState.collect() }
        return viewModel
    }
}
