package com.example.storyround.presentation.home

import com.example.storyround.domain.model.GameError
import com.example.storyround.domain.model.GameState
import com.example.storyround.domain.model.StoryHistoryState
import com.example.storyround.domain.repository.GameRepository
import com.example.storyround.domain.repository.StoryHistoryRepository
import com.example.storyround.presentation.mapper.GameErrorUiMapperImpl
import com.example.storyround.presentation.model.UiText
import com.example.storyround.protocol.SavedStory
import com.example.storyround.protocol.UserTextValidator
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
class HomeViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val gameState = MutableStateFlow(GameState())
    private val historyState = MutableStateFlow(StoryHistoryState())
    private val gameRepository = mockk<GameRepository>(relaxed = true) { every { state } returns gameState }
    private val storyHistoryRepository = mockk<StoryHistoryRepository> { every { state } returns historyState }
    private val userTextValidator = mockk<UserTextValidator>()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `given a valid name when creating a room then the name is sent to create the room`() =
        runTest(dispatcher) {
            // Given
            every { userTextValidator.validPlayerName("Alex") } returns "Alex"
            val viewModel = viewModel()
            viewModel.onAction(HomeAction.PlayerNameChanged("Alex"))

            // When
            viewModel.onAction(HomeAction.CreateGame)
            advanceUntilIdle()

            // Then
            assertEquals("Alex", viewModel.uiState.value.playerName)
            assertTrue(viewModel.uiState.value.canCreateGame)
            coVerify(exactly = 1) { gameRepository.createRoom("Alex") }
        }

    @Test
    fun `given a name the validator rejects when entered then create is disabled`() =
        runTest(dispatcher) {
            // Given
            every { userTextValidator.validPlayerName(" A ") } returns null
            val viewModel = viewModel()

            // When
            viewModel.onAction(HomeAction.PlayerNameChanged(" A "))
            advanceUntilIdle()

            // Then
            assertFalse(viewModel.uiState.value.canCreateGame)
        }

    @Test
    fun `given a name longer than the limit when entered then it is cut to the maximum length`() =
        runTest(dispatcher) {
            // Given
            every { userTextValidator.validPlayerName(any()) } returns null
            val viewModel = viewModel()

            // When
            viewModel.onAction(HomeAction.PlayerNameChanged("A".repeat(30)))
            advanceUntilIdle()

            // Then
            assertEquals(20, viewModel.uiState.value.playerName.length)
        }

    @Test
    fun `given join mode with a lower-case code when joining then the upper-case code and name are sent`() =
        runTest(dispatcher) {
            // Given
            every { userTextValidator.validPlayerName("Sam") } returns "Sam"
            val viewModel = viewModel()
            viewModel.onAction(HomeAction.ShowJoinGame)
            viewModel.onAction(HomeAction.PlayerNameChanged("Sam"))
            viewModel.onAction(HomeAction.RoomCodeChanged("ab12"))

            // When
            viewModel.onAction(HomeAction.JoinGame)
            advanceUntilIdle()

            // Then
            assertTrue(viewModel.uiState.value.isJoining)
            assertEquals("AB12", viewModel.uiState.value.roomCode)
            coVerify(exactly = 1) { gameRepository.joinRoom("AB12", "Sam") }
        }

    @Test
    fun `given join mode with a code when cancelled then join mode closes and the code is cleared`() =
        runTest(dispatcher) {
            // Given
            val viewModel = viewModel()
            viewModel.onAction(HomeAction.ShowJoinGame)
            viewModel.onAction(HomeAction.RoomCodeChanged("AB12"))

            // When
            viewModel.onAction(HomeAction.CancelJoin)
            advanceUntilIdle()

            // Then
            assertFalse(viewModel.uiState.value.isJoining)
            assertEquals("", viewModel.uiState.value.roomCode)
        }

    @Test
    fun `given the game state reports connecting, an error and saved stories when observed then home shows them`() =
        runTest(dispatcher) {
            // Given
            val viewModel = viewModel()

            // When
            gameState.value =
                GameState(
                    isConnecting = true,
                    error = GameError.Server("That room does not exist."),
                )
            historyState.value = StoryHistoryState(stories = listOf(savedStory("story-1"), savedStory("story-2")))
            advanceUntilIdle()

            // Then
            val uiState = viewModel.uiState.value
            assertTrue(uiState.isConnecting)
            assertEquals(UiText.Plain("That room does not exist."), uiState.error)
            assertEquals(2, uiState.historyCount)
        }

    private fun TestScope.viewModel(): HomeViewModel {
        val viewModel = HomeViewModel(gameRepository, storyHistoryRepository, userTextValidator, GameErrorUiMapperImpl())
        backgroundScope.launch { viewModel.uiState.collect() }
        return viewModel
    }

    private fun savedStory(id: String) = SavedStory(id, "A door opened.", "player-1", 1, emptyList(), emptyList())
}
