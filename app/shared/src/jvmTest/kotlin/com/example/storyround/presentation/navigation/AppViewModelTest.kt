package com.example.storyround.presentation.navigation

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.storyround.domain.model.GameState
import com.example.storyround.domain.model.StoryHistoryState
import com.example.storyround.domain.repository.GameRepository
import com.example.storyround.domain.repository.StoryHistoryRepository
import com.example.storyround.protocol.GameStatus
import com.example.storyround.protocol.RoomSettings
import com.example.storyround.protocol.RoomState
import com.example.storyround.protocol.SavedStory
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val gameState = MutableStateFlow(GameState())
    private val historyState = MutableStateFlow(StoryHistoryState())
    private val gameRepository = mockk<GameRepository>(relaxed = true) { every { state } returns gameState }
    private val storyHistoryRepository = mockk<StoryHistoryRepository>(relaxed = true) { every { state } returns historyState }
    private val story = SavedStory("story-1", "A door opened.", "host", 1, emptyList(), emptyList())

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `given the app starts when the view model is created then it connects to the game`() =
        runTest(dispatcher) {
            // When
            viewModel()
            advanceUntilIdle()

            // Then
            coVerify(exactly = 1) { gameRepository.connect() }
        }

    @Test
    fun `given each room status when observed then the matching screen is shown`() =
        runTest(dispatcher) {
            // Given
            val viewModel = viewModel()
            val expected =
                mapOf(
                    null to AppDestination.Home,
                    GameStatus.Lobby to AppDestination.Lobby,
                    GameStatus.Prompt to AppDestination.Story,
                    GameStatus.Playing to AppDestination.Story,
                    GameStatus.Finished to AppDestination.Finished,
                )

            // When
            val shown =
                expected.keys.associateWith { status ->
                    gameState.value = GameState(room = status?.let(::room))
                    advanceUntilIdle()
                    viewModel.destination.value
                }

            // Then
            assertEquals(expected, shown)
        }

    @Test
    fun `given a room when history is opened and closed then history shows and back returns to the room`() =
        runTest(dispatcher) {
            // Given
            gameState.value = GameState(room = room(GameStatus.Lobby))
            val viewModel = viewModel()

            // When
            viewModel.showHistory()
            advanceUntilIdle()
            val shownWhileOpen = viewModel.destination.value
            viewModel.closeHistory()
            advanceUntilIdle()

            // Then
            assertEquals(AppDestination.History, shownWhileOpen)
            assertEquals(AppDestination.Lobby, viewModel.destination.value)
        }

    @Test
    fun `given a selected saved story when it is closed then the story is released and history reopens`() =
        runTest(dispatcher) {
            // Given
            historyState.value = StoryHistoryState(selectedStory = story)
            val viewModel = viewModel()
            advanceUntilIdle()
            assertEquals(AppDestination.SavedStory, viewModel.destination.value)

            // When
            viewModel.closeSavedStory()
            historyState.value = StoryHistoryState()
            advanceUntilIdle()

            // Then
            verify(exactly = 1) { storyHistoryRepository.close() }
            assertEquals(AppDestination.History, viewModel.destination.value)
        }

    @Test
    fun `given a finished game when returning home then the game is disconnected without leaving`() =
        runTest(dispatcher) {
            // Given
            val viewModel = viewModel()

            // When
            viewModel.goHomeWithoutLeavingRoom()
            advanceUntilIdle()

            // Then
            verify(exactly = 1) { gameRepository.disconnect() }
            coVerify(exactly = 0) { gameRepository.leaveRoom() }
        }

    @Test
    fun `given the lobby when leaving then the room is left`() =
        runTest(dispatcher) {
            // Given
            val viewModel = viewModel()

            // When
            viewModel.leaveLobby()
            advanceUntilIdle()

            // Then
            coVerify(exactly = 1) { gameRepository.leaveRoom() }
        }

    @Test
    fun `given the app UI goes away when the view model is cleared then the game is disconnected but the shared repository stays usable`() =
        runTest(dispatcher) {
            // Given
            val store = ViewModelStore()
            ViewModelProvider.create(
                store,
                viewModelFactory {
                    initializer {
                        AppViewModel(gameRepository, storyHistoryRepository)
                    }
                },
            )[AppViewModel::class]
            advanceUntilIdle()

            // When
            store.clear()

            // Then
            verify(exactly = 1) { gameRepository.disconnect() }
            coVerify(exactly = 0) { gameRepository.leaveRoom() }
        }

    private fun TestScope.viewModel(): AppViewModel {
        val viewModel = AppViewModel(gameRepository, storyHistoryRepository)
        backgroundScope.launch { viewModel.destination.collect() }
        return viewModel
    }

    private fun room(status: GameStatus) =
        RoomState("AB12", "host", emptyList(), status, maxPlayers = 10, settings = RoomSettings(rounds = 3))
}
